package com.insta.hms.billing.payment;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.PaymentRule.PaymentRuleDAO;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PaymentEngine.
 */
/*
 * PaymentEngine calculates and updates the payment amounts for doctor,
 * prescribing and referrals. The payments are all bill_charge based, and the
 * payments are stored in bill_charge or bill_activity_charge tables. The
 * following are the general rules applicable to the payment calculation:
 *
 * 1. Storage: Prescribing and referral amounts/doctor_ids are stored in
 * bill_charge. For conducting doctor amounts, we store it in bill_charge when
 * it is not a package item, otherwise we store it in bill_activity_charge.
 *
 * 2. Charge Head Eligibility: For normal items, charge_head eligibility
 * determines if any processing is done for the charge at all or not. For
 * Package items, there is on presc/ref payment and multiple cond payments. For
 * presc/ref payments, the PKGPKG charge_head is checked for eligibility. For
 * cond payments, both PKGPKG and the activity's eligibility should be Y. In
 * other words, if PKGPKG eligibility is set to N, then, NO payment will be
 * paid, even for activities like test withing the package, for which payment is
 * eligible.
 *
 * It is assumed that charge head eligibility will not change, so we don't
 * handle the case where an amount was updated to no-zero, and then the
 * eligibility was changed to N. This is to optimize performance by skipping
 * processing when not eligible.
 *
 * Same is true for doctor eligibility.
 *
 * 3. Activity Conduction: cond amount is computed only when the activity is
 * conducted (or conduction is not required) in case of tests and services. For
 * others (doctors, surgeon), cond amount is computed and available for payment
 * without this check.
 *
 * Since conduction status cannot go backwards, there is no need to calculate
 * amounts when in unconducted status (otherwise, we may have to set a non-zero
 * amount back to 0 when the conduction status changes from Y to N).
 *
 * 4. All checks are done in the Payment engine except for bill status (this is
 * because bills can be reopened). If a bill is finalized, there is no other
 * check needed to determine if an amount is OK to be paid.
 *
 */
public class PaymentEngine {

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(PaymentEngine.class);

  /** The zero. */
  private static BigDecimal zero = BigDecimal.ZERO;

  /**
   * Update all payout amounts.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  /*
   * Entry point: calculate and update the payout amounts of a given charge ID.
   * The payout amounts will not be updated if the payment has already been posted
   * into payments_details.
   */
  public static boolean updateAllPayoutAmounts(Connection con, String chargeId) throws Exception {
    return updatePayoutAmounts(con, chargeId, true, true, true);
  }

  /**
   * Update payout amounts.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @param updateDoctorAmount
   *          the update doctor amount
   * @param updatePresAmount
   *          the update pres amount
   * @param updateRefAmount
   *          the update ref amount
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean updatePayoutAmounts(Connection con, String chargeId,
      boolean updateDoctorAmount, boolean updatePresAmount, boolean updateRefAmount)
      throws Exception {

    BasicDynaBean charge = ChargeDAO.getChargePaymentDetails(con, chargeId);

    String mainChargeHead = (String) charge.get("charge_head");
    boolean discountOk = true;

    boolean headEligible = ChargeHeadsDAO.isEligibleForPayment(mainChargeHead);
    log.debug("updatePayoutAmounts main: chargeId= " + charge.get("charge_id") + "; head="
        + mainChargeHead + " headEligible=" + headEligible);

    if (!headEligible) {
      return true;
    }

    /*
     * Get the main charge's matching rule
     */
    BasicDynaBean mainRule = getPaymentRule(con, mainChargeHead,
        (String) charge.get("act_description_id"), (String) charge.get("org_id"),
        (String) charge.get("center_id"), (String) charge.get("con_doc_category"),
        (String) charge.get("pres_doc_category"), (String) charge.get("ref_doc_category"));

    // bail out if there is no rule that matched.
    if (mainRule == null) {
      return true;
    }




    String docPaymentId = (String) charge.get("doc_payment_id");
    if (docPaymentId != null && !"".equals(docPaymentId)) {
      updateDoctorAmount = false;
    }

    String refPaymentId = (String) charge.get("ref_payment_id");
    if (refPaymentId != null && !"".equals(refPaymentId)) {
      updateRefAmount = false;
    }

    String prescPaymentId = (String) charge.get("prescribing_dr_payment_id");
    if (prescPaymentId != null && !"".equals(prescPaymentId)) {
      updatePresAmount = false;
    }
    BigDecimal doctorAmount = zero;
    BigDecimal totalDoctorAmount = zero;
    boolean isSplitDiscounts = isSplitDiscounts(charge);
    ChargeBreakup breakup = calculatePayments(con, mainRule, charge, null, isSplitDiscounts,
        "1".equals(mainRule.get("use_discounted_amount")));
    totalDoctorAmount = totalDoctorAmount.add(breakup.getDoctorComponent());

    log.debug("Main charge payouts for " + chargeId + "; doc: " + updateDoctorAmount + ", "
        + breakup.getDoctorComponent() + "; pres: " + updatePresAmount + ","
        + breakup.getPrescribedComponent() + "; ref: " + updateRefAmount + ","
        + breakup.getReferralComponent());

    /*
     * Update bill_charge with the amounts
     */
    updatePayout(con, chargeId, updateDoctorAmount, breakup.getDoctorComponent(), updatePresAmount,
        breakup.getPrescribedComponent(), updateRefAmount, breakup.getReferralComponent());

    if ((breakup.getDoctorComponent().compareTo(BigDecimal.ZERO) < 0)
        || (breakup.getPrescribedComponent().compareTo(BigDecimal.ZERO) < 0)
        || (breakup.getReferralComponent().compareTo(BigDecimal.ZERO) < 0)) {
      discountOk = false;
    }
    boolean isPackage = mainChargeHead.equals("PKGPKG");
    List<BasicDynaBean> activities = isPackage
        ? BillActivityChargeDAO.getChargeActivities(con, chargeId)
        : new ArrayList();
    /*
     * Now, update the payouts for the subsidary activities: only doctor amount in
     * bac is updated here.
     */
    for (BasicDynaBean act : activities) {

      String actDocPaymentId = (String) act.get("doctor_payment_id");
      if (actDocPaymentId != null && !"".equals(actDocPaymentId)) {
        // don't update this: it is already a confirmed payment, and there is no other
        // payment to update.
        continue;
      }

      String head = (String) act.get("payment_charge_head");
      boolean actHeadEligible = ChargeHeadsDAO.isEligibleForPayment(head);
      log.debug("updatePkgPayouts: chargeHead=" + head + "; actHeadEligible=" + actHeadEligible);

      if (!actHeadEligible) {
        continue;
      }

      BasicDynaBean rule = getPaymentRule(con, head, (String) act.get("act_description_id"),
          (String) charge.get("org_id"), (String) charge.get("center_id"),
          (String) act.get("con_doc_category"), (String) charge.get("pres_doc_category"),
          (String) charge.get("ref_doc_category"));

      // bail out if there is no rule that matched.
      if (rule == null) {
        continue;
      }

      breakup = calculatePayments(con, rule, charge, act, isSplitDiscounts, true);
      doctorAmount = breakup.getDoctorComponent();
      log.debug("Pkg cond pmt: " + doctorAmount);

      String activityCode = (String) act.get("activity_code");
      String activityId = (String) act.get("activity_id");
      BillActivityChargeDAO.updateActivityPayout(con, activityCode, activityId, doctorAmount);

      if (doctorAmount.compareTo(BigDecimal.ZERO) < 0) {
        discountOk = false;
      }
    }

    return discountOk;
  }

  /**
   * Calculate payments.
   *
   * @param con
   *          the con
   * @param rule
   *          the rule
   * @param chargeBean
   *          the charge bean
   * @param actBean
   *          the act bean
   * @param isSplitDiscount
   *          the is split discount
   * @param useDiscountedAmount
   *          the use discounted amount
   * @return the charge breakup
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws TemplateException
   *           the template exception
   * @throws Exception
   *           the exception
   */
  public static ChargeBreakup calculatePayments(Connection con, BasicDynaBean rule,
      DynaBean chargeBean, DynaBean actBean, boolean isSplitDiscount, boolean useDiscountedAmount)
      throws SQLException, ParseException, TemplateException, Exception {

    String billNo = (String) chargeBean.get("bill_no");
    BigDecimal docAmount = zero;
    BigDecimal refAmount = zero;
    BigDecimal presAmount = zero;

    BigDecimal chargeAmount = (BigDecimal) chargeBean.get("amount"); // this is netAmt
    if (isSplitDiscount) {
      // using origAmt (same as net_amt + totalDiscount).
      // Every payee's share of discount is explicitly provided. So we don't consider
      // the preference setting, and force it to use origAmt to start with. Later, the
      // payee's share of discount will be subtracted from his payment amount.
      log.debug("Split discount: using origAmt");
      chargeAmount = chargeAmount.add((BigDecimal) chargeBean.get("discount"));
    } else if (!useDiscountedAmount) {
      // using origAmt (same as net_amt + totalDiscount).
      log.debug("Preference setting: Using pre-discount amount");
      chargeAmount = chargeAmount.add((BigDecimal) chargeBean.get("discount"));
    } else {
      log.debug("Using discounted amount");
    }

    String docEligibleStr = (String) (actBean != null ? actBean.get("con_doc_eligible")
        : chargeBean.get("con_doc_eligible"));

    if (docEligibleStr != null && docEligibleStr.equals("Y")) {

      boolean activityConducted = true;

      /*
       * For services and tests, payment is eligible only after activity is conducted.
       * For others, we don't bother. If there is no activity (as in directly adding
       * to bill) there is no question of conduction for tests and services.
       */
      String head = (String) rule.get("charge_head");
      if (head.equals("LTDIA") || head.equals("RTDIA") || head.equals("SERSNP")) {

        String activityConductedStr = (actBean != null) ? (String) actBean.get("activity_conducted")
            : (String) chargeBean.get("activity_conducted");

        if (activityConductedStr != null && activityConductedStr.equals("N")) {
          activityConducted = false;
        }

        log.debug("Conduction status for conductible activity: " + activityConducted
            + " using actBean=" + actBean);
      } else {
        log.debug("Assuming conducted=" + activityConducted + " for charge head " + head);
      }

      if (activityConducted) {
        docAmount = getPayeeAmount(con, "dr_payment", rule, chargeAmount, (actBean != null),
            chargeBean, billNo);
        // split discount is not applicable for pkg payments. todo: block this in
        // billing screen
        if (isSplitDiscount && (actBean == null)) {
          docAmount = docAmount.subtract((BigDecimal) chargeBean.get("dr_discount_amt"));
        }
        if (docAmount.compareTo(zero) < 0) {
          docAmount = zero;
        }
      }
    } else {
      log.debug("Conducting Doctor not eligible for payment: "
          + ((actBean != null) ? actBean.get("doctor_id") : chargeBean.get("payee_doctor_id")));
    }

    // ref/pres are useful only if we are not getting the activity conducting
    // amount.
    if (actBean == null) {
      String refEligibleStr = (String) chargeBean.get("ref_doc_eligible");
      if (refEligibleStr != null && "Y".equals(refEligibleStr)) {
        refAmount = getPayeeAmount(con, "ref_payment", rule, chargeAmount, false, chargeBean,
            billNo);
        if (isSplitDiscount) {
          refAmount = refAmount.subtract((BigDecimal) chargeBean.get("ref_discount_amt"));
        }
        if (refAmount.compareTo(zero) < 0) {
          refAmount = zero;
        }
      }

      String presEligibleStr = (String) chargeBean.get("pres_doc_eligible");
      if (presEligibleStr != null && "Y".equals(presEligibleStr)) {
        presAmount = getPayeeAmount(con, "presc_payment", rule, chargeAmount, false, chargeBean,
            billNo);
        if (isSplitDiscount) {
          presAmount = presAmount.subtract((BigDecimal) chargeBean.get("pres_dr_discount_amt"));
        }
        if (presAmount.compareTo(zero) < 0) {
          presAmount = zero;
        }
      }
    }

    return new ChargeBreakup(docAmount, refAmount, presAmount, null);
  }

  /**
   * Gets the payee amount.
   *
   * @param con
   *          the con
   * @param payee
   *          the payee
   * @param rule
   *          the rule
   * @param amount
   *          the amount
   * @param isPkgAmt
   *          the is pkg amt
   * @param charge
   *          the charge
   * @param billNo
   *          the bill no
   * @return the payee amount
   * @throws TemplateException
   *           the template exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  public static BigDecimal getPayeeAmount(Connection con, String payee, DynaBean rule,
      BigDecimal amount, boolean isPkgAmt, DynaBean charge, String billNo)
      throws TemplateException, ParseException, Exception {

    if (isPkgAmt) {
      return (BigDecimal) rule.get("dr_pkg_amt");
    }

    BigDecimal payeePart = BigDecimal.ZERO;
    String option = (String) rule.get(payee + "_option");
    BigDecimal value = (BigDecimal) rule.get(payee + "_value");

    log.debug("getPayeeAmount: " + payee + ": opt=" + option + ", value=" + value);
    if ("5".equals(option)) {
      String expr = (String) rule.get(payee + "_expr");
      if (!(expr.equals(""))) {
        payeePart = getPayeePartForExpr(con, expr, charge, billNo);
      }
    } else if ("4".equals(option)) { // less bill amount
      payeePart = amount.subtract(value);
    } else if ("3".equals(option)) { // absolute amount
      payeePart = value;
      // 2 (percentage of balance) is no longer supported
    } else if ("1".equals(option)) { // percentage of amount
      payeePart = amount.multiply(value).divide(new BigDecimal(100));
    }

    return payeePart;
  }

  /**
   * Checks if is split discounts.
   *
   * @param chargeBean
   *          the charge bean
   * @return true, if is split discounts
   */
  public static boolean isSplitDiscounts(DynaBean chargeBean) {

    BigDecimal amount = BigDecimal.ZERO;
    int discountAuthDr = (Integer) chargeBean.get("discount_auth_dr");
    int discountAuthPresDr = (Integer) chargeBean.get("discount_auth_pres_dr");
    int discountAuthRef = (Integer) chargeBean.get("discount_auth_ref");

    if (discountAuthDr != 0) {
      return true;
    }
    if (discountAuthPresDr != 0) {
      return true;
    }
    if (discountAuthRef != 0) {
      return true;
    }
    int discountAuthHosp = (Integer) chargeBean.get("discount_auth_hosp");
    if (discountAuthHosp != 0) {
      return true;
    }
    return false;
  }

  /**
   * Gets the payment rule.
   *
   * @param con
   *          the con
   * @param chargeHead
   *          the charge head
   * @param activityId
   *          the activity id
   * @param ratePlan
   *          the rate plan
   * @param centerId
   *          the center id
   * @param doctorCategory
   *          the doctor category
   * @param prescribedCategory
   *          the prescribed category
   * @param referralCategory
   *          the referral category
   * @return the payment rule
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * Get the payment rule relevant to the given charge attributes: head,
   * activity_id, org_id, doc_category, ref_category, pres_category
   */
  protected static BasicDynaBean getPaymentRule(Connection con, String chargeHead,
      String activityId, String ratePlan, String centerId, String doctorCategory,
      String prescribedCategory, String referralCategory) throws SQLException {

    log.debug("Find rule: doc category: " + doctorCategory + "; pres category: "
        + prescribedCategory + "; ref category: " + referralCategory + "; rate plan: " + ratePlan
        + "; head: " + chargeHead + "; activity: " + activityId);

    BasicDynaBean bean = null;
    PaymentRuleDAO dao = new PaymentRuleDAO();
    List rules = dao.getRules(chargeHead, activityId);

    for (Object obj : rules) {
      BasicDynaBean rule = (BasicDynaBean) obj;

      if (!isDoctorCategoryMatch(rule, doctorCategory)) {
        continue;
      }
      if (!isReferralCategoryMatch(rule, referralCategory)) {
        continue;
      }
      if (!isPrescribedCategoryMatch(rule, prescribedCategory)) {
        continue;
      }
      if (!isRatePlanMatch(rule, ratePlan)) {
        continue;
      }
      if (!isCenterMatch(rule, centerId)) {
        continue;
      }
      bean = rule;
      break;
    }

    if (bean != null) {
      log.debug("Found: " + bean.get("precedance"));
    } else {
      log.warn("No rule found for: " + " doc category: " + doctorCategory + "; pres category: "
          + prescribedCategory + "; ref category: " + referralCategory + "; rate plan: " + ratePlan
          + "center: " + centerId + "; head: " + chargeHead + "; activity: " + activityId);
    }

    return bean;
  }

  /**
   * Matches this rule if rule.doctor_category='*' or
   * rule.doctor_category=doctorCategory
   *
   * @param rule
   *          the rule
   * @param doctorCategory
   *          the doctor category
   * @return true, if is doctor category match
   */
  private static boolean isDoctorCategoryMatch(BasicDynaBean rule, String doctorCategory) {
    String category = (String) rule.get("doctor_category");
    if ("*".equals(category) || category.equals(doctorCategory)) {
      return true;
    }
    return false;
  }

  /**
   * Matches the rule if rule.referrer_category = null and referralCategory = null
   * Matches the rule if rule.referrer_category = '*' or rule.referrer_category =
   * referralCategory
   *
   * @param rule
   *          the rule
   * @param referralCategory
   *          the referral category
   * @return true, if is referral category match
   */
  private static boolean isReferralCategoryMatch(BasicDynaBean rule, String referralCategory) {
    String category = (String) rule.get("referrer_category");
    if (category != null && !"".equals(category)) {
      if ("*".equals(category) || category.equals(referralCategory)) {
        return true;
      }
    } else if (referralCategory == null) {
      return true;
    }
    return false;
  }

  /**
   * Matches the rule if rule.prescribed_category = null and prescribedCategory =
   * null Matches the rule if rule.prescribed_category = '*' or
   * rule.prescribed_category = prescribedCategory
   *
   * @param rule
   *          the rule
   * @param prescribedCategory
   *          the prescribed category
   * @return true, if is prescribed category match
   */
  private static boolean isPrescribedCategoryMatch(BasicDynaBean rule, String prescribedCategory) {
    String category = (String) rule.get("prescribed_category");
    if (category != null && !"".equals(category)) {
      if ("*".equals(category) || category.equals(prescribedCategory)) {
        return true;
      }
    } else if (prescribedCategory == null) {
      return true;
    }
    return false;
  }

  /**
   * Matches this rule if rule.rate_plan='*' or rule.rate_plan=ratePlan
   *
   * @param rule
   *          the rule
   * @param ratePlan
   *          the rate plan
   * @return true, if is rate plan match
   */
  private static boolean isRatePlanMatch(BasicDynaBean rule, String ratePlan) {
    String plan = (String) rule.get("rate_plan");
    if ("*".equals(plan) || plan.equals(ratePlan)) {
      return true;
    }
    return false;
  }

  /**
   * Matches this rule if rule.center_id='*' or rule.center_id=centerId
   *
   * @param rule
   *          the rule
   * @param centerId
   *          the center id
   * @return true, if is center match
   */

  private static boolean isCenterMatch(BasicDynaBean rule, String centerId) {
    String center = (String) rule.get("center_id");
    if ("*".equals(center) || center.equals(centerId)) {
      return true;
    }
    return false;
  }

  /**
   * Update payout.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @param updateDoctorAmount
   *          the update doctor amount
   * @param doctorAmount
   *          the doctor amount
   * @param updatePresAmount
   *          the update pres amount
   * @param presAmount
   *          the pres amount
   * @param updateRefAmount
   *          the update ref amount
   * @param refAmount
   *          the ref amount
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updatePayout(Connection con, String chargeId, boolean updateDoctorAmount,
      BigDecimal doctorAmount, boolean updatePresAmount, BigDecimal presAmount,
      boolean updateRefAmount, BigDecimal refAmount) throws SQLException {

    if (updateDoctorAmount && updatePresAmount && updateRefAmount) {
      // do it together: optimization!!
      ChargeDAO.updatePayout(con, chargeId, doctorAmount, presAmount, refAmount);

    } else {
      if (updateDoctorAmount) {
        ChargeDAO.updateDrChargeAmount(con, chargeId, doctorAmount);
      }
      if (updatePresAmount) {
        ChargeDAO.updatePresChargeAmount(con, chargeId, presAmount);
      }
      if (updateRefAmount) {
        ChargeDAO.updateRefChargeAmount(con, chargeId, refAmount);
      }
    }
    return true;
  }

  /**
   * Gets the payee part for expr.
   *
   * @param con
   *          the con
   * @param expr
   *          the expr
   * @param chargeBean
   *          the charge bean
   * @param billNo
   *          the bill no
   * @return the payee part for expr
   * @throws TemplateException
   *           the template exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  public static BigDecimal getPayeePartForExpr(Connection con, String expr, DynaBean chargeBean,
      String billNo) throws TemplateException, ParseException, Exception {

    expr = "<#setting number_format=\"#.###\">\n" + expr;

    Template temp = new Template("name", new StringReader(expr), new Configuration());
    HashMap<String, Object> params = new HashMap<String, Object>();

    BasicDynaBean bill = BillDAO.getBillBean(con, billNo);
    BasicDynaBean patient = VisitDetailsDAO.getPatientVisitDetailsBean(con,
        (String) bill.get("visit_id"));
    BasicDynaBean conductingDr = DoctorMasterDAO
        .getDoctorPaymentBean((String) chargeBean.get("payee_doctor_id"));
    BasicDynaBean prescDr = DoctorMasterDAO
        .getDoctorPaymentBean((String) chargeBean.get("prescribing_dr_id"));
    BasicDynaBean refDr = DoctorMasterDAO
        .getDoctorPaymentBean((String) chargeBean.get("reference_docto_id"));

    putPaymentExprParams(params, patient, bill, conductingDr, prescDr, refDr, chargeBean);

    StringWriter writer = new StringWriter();
    temp.process(params, writer);
    String valueStr = writer.toString().trim();
    log.debug("Expression returned: '" + valueStr + "'");
    BigDecimal payeePart = new BigDecimal(valueStr);
    return payeePart;
  }

  /**
   * Put payment expr params.
   *
   * @param params
   *          the params
   * @param patient
   *          the patient
   * @param bill
   *          the bill
   * @param conductingDr
   *          the conducting dr
   * @param prescDr
   *          the presc dr
   * @param refDr
   *          the ref dr
   * @param charge
   *          the charge
   */
  public static void putPaymentExprParams(Map params, DynaBean patient, DynaBean bill,
      DynaBean conductingDr, DynaBean prescDr, DynaBean refDr, DynaBean charge) {

    if (patient == null) {
      log.warn("Patient bean is null");
    }

    putParamString(params, conductingDr, "conducting_dr_id", "doctor_id");
    putParamString(params, conductingDr, "conducting_dr_name", "doctor_name");
    putParamString(params, conductingDr, "conducting_dr_specialization", "specialization");
    putParamString(params, conductingDr, "conducting_dr_type", "doctor_type");
    putParamString(params, conductingDr, "conducting_dr_dept_id", "dept_id");
    putParamString(params, conductingDr, "conducting_dr_ot_doctor_flag", "ot_doctor_flag");
    putParamString(params, conductingDr, "conducting_dr_consul_doctor_flag",
        "consulting_doctor_flag");
    putParamString(params, conductingDr, "conducting_dr_consul_qualification", "qualification");
    putParamString(params, conductingDr, "conducting_dr_payment_category", "payment_category");
    putParamString(params, conductingDr, "conducting_dr_custom_field1", "custom_field1_value");
    putParamString(params, conductingDr, "conducting_dr_custom_field2", "custom_field2_value");
    putParamString(params, conductingDr, "conducting_dr_custom_field3", "custom_field3_value");
    putParamString(params, conductingDr, "conducting_dr_custom_field4", "custom_field4_value");
    putParamString(params, conductingDr, "conducting_dr_custom_field5", "custom_field5_value");
    putParamString(params, conductingDr, "conducting_dr_payment_eligible", "payment_eligible");

    putParamString(params, prescDr, "prescribing_dr_id", "doctor_id");
    putParamString(params, prescDr, "prescribing_dr_name", "doctor_name");
    putParamString(params, prescDr, "prescribing_dr_specialization", "specialization");
    putParamString(params, prescDr, "prescribing_dr_type", "doctor_type");
    putParamString(params, prescDr, "prescribing_dr_dept_id", "dept_id");
    putParamString(params, prescDr, "prescribing_dr_ot_doctor_flag", "ot_doctor_flag");
    putParamString(params, prescDr, "prescribing_dr_consul_doctor_flag", "consulting_doctor_flag");
    putParamString(params, prescDr, "prescribing_dr_consul_qualification", "qualification");
    putParamString(params, prescDr, "prescribing_dr_payment_category", "payment_category");
    putParamString(params, prescDr, "prescribing_dr_custom_field1", "custom_field1_value");
    putParamString(params, prescDr, "prescribing_dr_custom_field2", "custom_field2_value");
    putParamString(params, prescDr, "prescribing_dr_custom_field3", "custom_field3_value");
    putParamString(params, prescDr, "prescribing_dr_custom_field4", "custom_field4_value");
    putParamString(params, prescDr, "prescribing_dr_custom_field5", "custom_field5_value");
    putParamString(params, prescDr, "prescribing_dr_payment_eligible", "payment_eligible");

    putParamString(params, refDr, "referral_id", "doctor_id");
    putParamString(params, refDr, "referral_name", "doctor_name");
    putParamString(params, refDr, "referral_specialization", "specialization");
    putParamString(params, refDr, "referral_type", "doctor_type");
    putParamString(params, refDr, "referral_dept_id", "dept_id");
    putParamString(params, refDr, "referral_ot_doctor_flag", "ot_doctor_flag");
    putParamString(params, refDr, "referral_consul_doctor_flag", "consulting_doctor_flag");
    putParamString(params, refDr, "referral_consul_qualification", "qualification");
    putParamString(params, refDr, "referral_payment_category", "payment_category");
    putParamString(params, refDr, "referral_payment_eligible", "payment_eligible");
    putParamString(params, refDr, "referral_custom_field1", "custom_field1_value");
    putParamString(params, refDr, "referral_custom_field2", "custom_field2_value");
    putParamString(params, refDr, "referral_custom_field3", "custom_field3_value");
    putParamString(params, refDr, "referral_custom_field4", "custom_field4_value");
    putParamString(params, refDr, "referral_custom_field5", "custom_field5_value");

    putParamString(params, patient, "patient_gender");
    putParamString(params, patient, "cityname");
    putParamString(params, patient, "statename");
    putParamString(params, patient, "country_name");
    putParamString(params, patient, "custom_field1");
    putParamString(params, patient, "custom_field2");
    putParamString(params, patient, "custom_field3");
    putParamString(params, patient, "custom_field4");
    putParamString(params, patient, "custom_field5");
    putParamString(params, patient, "custom_field6");
    putParamString(params, patient, "custom_field7");
    putParamString(params, patient, "custom_field8");
    putParamString(params, patient, "custom_field9");
    putParamString(params, patient, "custom_field10");
    putParamString(params, patient, "custom_field11");
    putParamString(params, patient, "custom_field12");
    putParamString(params, patient, "custom_field13");
    putParamString(params, patient, "custom_field14");
    putParamString(params, patient, "custom_field15");
    putParamString(params, patient, "custom_field16");
    putParamString(params, patient, "custom_field17");
    putParamString(params, patient, "custom_field18");
    putParamString(params, patient, "custom_field19");
    putParamString(params, patient, "custom_list1_value");
    putParamString(params, patient, "custom_list2_value");
    putParamString(params, patient, "custom_list3_value");
    putParamString(params, patient, "custom_list4_value");
    putParamString(params, patient, "custom_list5_value");
    putParamString(params, patient, "custom_list6_value");
    putParamString(params, patient, "custom_list7_value");
    putParamString(params, patient, "custom_list8_value");
    putParamString(params, patient, "custom_list9_value");
    putParamString(params, patient, "patient_category_id");
    putParamString(params, patient, "visit_custom_field1");
    putParamString(params, patient, "visit_custom_field2");
    putParamString(params, patient, "visit_custom_field3");
    putParamString(params, patient, "visit_custom_field4");
    putParamString(params, patient, "visit_custom_field5");
    putParamString(params, patient, "visit_custom_field6");
    putParamString(params, patient, "visit_custom_field7");
    putParamString(params, patient, "visit_custom_field8");
    putParamString(params, patient, "visit_custom_field9");
    putParamString(params, patient, "visit_custom_list1");
    putParamString(params, patient, "visit_custom_list2");
    putParamString(params, patient, "mlc_status");
    putParamString(params, patient, "vip_status");
    putParamString(params, patient, "plan_name");
    putParamString(params, patient, "reference_docto_id");
    putParamString(params, patient, "refdoctorname");
    putParamString(params, patient, "op_type");

    putParamString(params, bill, "bill_type");
    putParamString(params, bill, "bill_status", "status");
    putParamString(params, bill, "visit_type");
    putParamString(params, bill, "claim_status", "primary_claim_status");
    putParamString(params, bill, "primary_claim_status");
    putParamString(params, bill, "secondary_claim_status");
    putParamString(params, bill, "payment_status");
    putParamString(params, bill, "tpa_id", "primary_sponsor_id");
    putParamString(params, bill, "primary_sponsor_id");
    putParamString(params, bill, "secondary_sponsor_id");
    putParamString(params, bill, "category_id");
    putParamString(params, bill, "bill_account_group", "account_group");
    putParamString(params, bill, "dyna_package_id");
    putParamString(params, bill, "bed_type");
    putParamBigDecimal(params, bill, "claim_recd_amount");
    putParamBigDecimal(params, bill, "deposit_set_off");
    putParamBigDecimal(params, bill, "approval_amount");
    putParamBigDecimal(params, bill, "total_amount");
    putParamBigDecimal(params, bill, "total_discount");
    putParamBigDecimal(params, bill, "total_claim");
    putParamBigDecimal(params, bill, "total_receipts");
    putParamBigDecimal(params, bill, "total_sponsor_receipts", "primary_total_sponsor_receipts");
    putParamBigDecimal(params, bill, "primary_total_sponsor_receipts");
    putParamBigDecimal(params, bill, "secondary_total_sponsor_receipts");
    putParamBigDecimal(params, bill, "insurance_deduction");
    putParamBigDecimal(params, bill, "dyna_package_charge");

    putParamString(params, charge, "charge_group");
    putParamString(params, charge, "charge_head");
    putParamString(params, charge, "act_department_id");
    putParamString(params, charge, "act_description");
    putParamString(params, charge, "posted_date");
    putParamString(params, charge, "charge_status", "status");
    putParamString(params, charge, "act_description_id");
    putParamString(params, charge, "hasactivity");
    putParamString(params, charge, "activity_conducted");
    putParamString(params, charge, "account_group");
    putParamString(params, charge, "conducted_datetime");
    putParamString(params, charge, "service_sub_group_id");
    putParamString(params, charge, "charge_excluded");
    putParamString(params, charge, "payee_doctor_id");
    putParamString(params, charge, "prescribing_dr_id");
    putParamString(params, charge, "overall_discount_auth");
    putParamString(params, charge, "discount_auth_dr");
    putParamString(params, charge, "discount_auth_pres_dr");
    putParamString(params, charge, "discount_auth_ref");
    putParamString(params, charge, "discount_auth_hosp");
    putParamString(params, charge, "insurance_category_id");
    putParamBigDecimal(params, charge, "act_rate");
    putParamBigDecimal(params, charge, "act_quantity");
    putParamBigDecimal(params, charge, "discount");
    putParamBigDecimal(params, charge, "amount");
    putParamBigDecimal(params, charge, "insurance_claim_amount");
    putParamBigDecimal(params, charge, "overall_discount_amt");
    putParamBigDecimal(params, charge, "pres_dr_discount_amt");
    putParamBigDecimal(params, charge, "ref_discount_amt");
    putParamBigDecimal(params, charge, "dr_discount_amt");
  }

  /**
   * Put param string.
   *
   * @param params
   *          the params
   * @param bean
   *          the bean
   * @param paramName
   *          the param name
   * @param attr
   *          the attr
   */
  public static void putParamString(Map params, DynaBean bean, String paramName, String attr) {
    if (bean == null) {
      params.put(paramName, "");
    } else {
      Object value = bean.get(attr);
      if (value == null) {
        params.put(paramName, "");
      } else {
        params.put(paramName, value);
      }
    }
  }

  /**
   * Put param string.
   *
   * @param params
   *          the params
   * @param bean
   *          the bean
   * @param attr
   *          the attr
   */
  public static void putParamString(Map params, DynaBean bean, String attr) {
    putParamString(params, bean, attr, attr);
  }

  /**
   * Put param big decimal.
   *
   * @param params
   *          the params
   * @param bean
   *          the bean
   * @param paramName
   *          the param name
   * @param attr
   *          the attr
   */
  public static void putParamBigDecimal(Map params, DynaBean bean, String paramName, String attr) {
    if (bean == null) {
      params.put(paramName, BigDecimal.ZERO);
    } else {
      Object value = bean.get(attr);
      if (value == null) {
        params.put(paramName, BigDecimal.ZERO);
      } else {
        params.put(paramName, value);
      }
    }
  }

  /**
   * Put param big decimal.
   *
   * @param params
   *          the params
   * @param bean
   *          the bean
   * @param attr
   *          the attr
   */
  public static void putParamBigDecimal(Map params, DynaBean bean, String attr) {
    putParamBigDecimal(params, bean, attr, attr);
  }

}
