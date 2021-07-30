package com.insta.hms.extension.payments;

import com.insta.hms.billing.payment.ChargeBreakup;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.doctors.DoctorService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PaymentProcessor.
 */
@Service
public class PaymentProcessor {

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(PaymentEngine.class);

  /** The zero. */
  private static BigDecimal zero = BigDecimal.ZERO;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The charge heads service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The bill activity charge service. */
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  /** The payment rules repo. */
  @LazyAutowired
  private PaymentRulesRepository paymentRulesRepo;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /**
   * Update all payout amounts.
   *
   * @param chargeId the charge id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  /*
   * Entry point: calculate and update the payout amounts of a given charge ID. The payout amounts
   * will not be updated if the payment has already been posted into payments_details.
   */
  public boolean updateAllPayoutAmounts(String chargeId) throws IOException, TemplateException {
    return updatePayoutAmounts(chargeId, true, true, true);
  }

  /**
   * Update payout amounts.
   *
   * @param chargeId the charge id
   * @param updateDoctorAmount the update doctor amount
   * @param updatePresAmount the update pres amount
   * @param updateRefAmount the update ref amount
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public boolean updatePayoutAmounts(String chargeId, boolean updateDoctorAmount,
      boolean updatePresAmount, boolean updateRefAmount) throws IOException, TemplateException {

    BasicDynaBean charge = billChargeService.getChargePaymentDetails(chargeId);

    String mainChargeHead = (String) charge.get("charge_head");
    boolean discountOk = true;

    boolean headEligible = chargeHeadsService.isEligibleForPayment(mainChargeHead);
    log.debug("updatePayoutAmounts main: chargeId= " + charge.get("charge_id") + "; head="
        + mainChargeHead + " headEligible=" + headEligible);

    if (!headEligible) {
      return true;
    }

    /*
     * Get the main charge's matching rule
     */
    BasicDynaBean mainRule = getPaymentRule(mainChargeHead,
        (String) charge.get("act_description_id"), (String) charge.get("org_id"),
        (String) charge.get("center_id"), (String) charge.get("con_doc_category"),
        (String) charge.get("pres_doc_category"), (String) charge.get("ref_doc_category"));

    // bail out if there is no rule that matched.
    if (mainRule == null) {
      return true;
    }

    BigDecimal doctorAmount = zero;
    BigDecimal referralAmount = zero;
    BigDecimal prescribingAmount = zero;

    /*
     * Disable updation of amounts if the payment has already been posted.
     */
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

    BigDecimal totalDoctorAmount = zero;
    /*
     * Update the main charge's amounts: all three are updated
     */
    boolean isSplitDiscounts = isSplitDiscounts(charge);
    ChargeBreakup breakup = calculatePayments(mainRule, charge, null, isSplitDiscounts,
        "1".equals(mainRule.get("use_discounted_amount")));
    totalDoctorAmount = totalDoctorAmount.add(breakup.getDoctorComponent());

    log.debug("Main charge payouts for " + chargeId + "; doc: " + updateDoctorAmount + ", "
        + breakup.getDoctorComponent() + "; pres: " + updatePresAmount + ","
        + breakup.getPrescribedComponent() + "; ref: " + updateRefAmount + ","
        + breakup.getReferralComponent());

    /*
     * Update bill_charge with the amounts
     */
    updatePayout(chargeId, updateDoctorAmount, breakup.getDoctorComponent(), updatePresAmount,
        breakup.getPrescribedComponent(), updateRefAmount, breakup.getReferralComponent());

    if ((breakup.getDoctorComponent().compareTo(BigDecimal.ZERO) < 0)
        || (breakup.getPrescribedComponent().compareTo(BigDecimal.ZERO) < 0)
        || (breakup.getReferralComponent().compareTo(BigDecimal.ZERO) < 0)) {
      discountOk = false;
    }

    /*
     * Now, update the payouts for the subsidary activities: only doctor amount in bac is updated
     * here.
     */

    boolean isPackage = mainChargeHead.equals("PKGPKG");
    List<BasicDynaBean> activities = isPackage ? billActivityChargeService
        .getChargeActivities(chargeId) : new ArrayList();

    for (BasicDynaBean act : activities) {

      String actDocPaymentId = (String) act.get("doctor_payment_id");
      if (actDocPaymentId != null && !"".equals(actDocPaymentId)) {
        // don't update this: it is already a confirmed payment, and there is no other
        // payment to update.
        continue;
      }

      String head = (String) act.get("payment_charge_head");
      boolean actHeadEligible = chargeHeadsService.isEligibleForPayment(head);
      log.debug("updatePkgPayouts: chargeHead=" + head + "; actHeadEligible=" + actHeadEligible);

      if (!actHeadEligible) {
        continue; // or, should we force it to zero?
      }

      BasicDynaBean rule = getPaymentRule(head, (String) act.get("act_description_id"),
          (String) charge.get("org_id"), (String) charge.get("center_id"),
          (String) act.get("con_doc_category"), (String) charge.get("pres_doc_category"),
          (String) charge.get("ref_doc_category"));

      // bail out if there is no rule that matched.
      if (rule == null) {
        continue;
      }

      breakup = calculatePayments(rule, charge, act, isSplitDiscounts, true);
      doctorAmount = breakup.getDoctorComponent();
      log.debug("Pkg cond pmt: " + doctorAmount);

      BasicDynaBean activitybean = billActivityChargeService.getBean();
      Map keys = new HashMap();
      keys.put("activity_code", (String) act.get("activity_code"));
      keys.put("activity_id", (String) act.get("activity_id"));
      activitybean.set("doctor_amount", doctorAmount);
      billActivityChargeService.update(activitybean, keys);

      if (doctorAmount.compareTo(BigDecimal.ZERO) < 0) {
        discountOk = false;
      }
    }

    return discountOk;

  }

  /**
   * Gets the payment rule.
   *
   * @param chargeHead the charge head
   * @param activityId the activity id
   * @param ratePlan the rate plan
   * @param centerId the center id
   * @param doctorCategory the doctor category
   * @param prescribedCategory the prescribed category
   * @param referralCategory the referral category
   * @return the payment rule
   */
  /*
   * Get the payment rule relevant to the given charge attributes: head, activity_id, org_id,
   * doc_category, ref_category, pres_category
   */
  protected BasicDynaBean getPaymentRule(String chargeHead, String activityId, String ratePlan,
      String centerId, String doctorCategory, String prescribedCategory, String referralCategory) {

    log.debug("Find rule: doc category: " + doctorCategory + "; pres category: "
        + prescribedCategory + "; ref category: " + referralCategory + "; rate plan: " + ratePlan
        + "; head: " + chargeHead + "; activity: " + activityId);

    BasicDynaBean bean = null;
    List<BasicDynaBean> rules = paymentRulesRepo.getRules(chargeHead, activityId);

    for (BasicDynaBean rule : rules) {

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
   * Matches this rule if rule.doctor_category='*' or rule.doctor_category=doctorCategory
   *
   * @param rule the rule
   * @param doctorCategory the doctor category
   * @return true, if is doctor category match
   */
  private static boolean isDoctorCategoryMatch(BasicDynaBean rule, String doctorCategory) {
    String category = (String) rule.get("doctor_category");
    return ("*".equals(category) || category.equals(doctorCategory)) ? true : false;
  }

  /**
   * Matches the rule if rule.referrer_category = null and referralCategory = null Matches the rule
   * if rule.referrer_category = '*' or rule.referrer_category = referralCategory
   *
   * @param rule the rule
   * @param referralCategory the referral category
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
   * Matches the rule if rule.prescribed_category = null and prescribedCategory = null Matches the
   * rule if rule.prescribed_category = '*' or rule.prescribed_category = prescribedCategory
   *
   * @param rule the rule
   * @param prescribedCategory the prescribed category
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
   * @param rule the rule
   * @param ratePlan the rate plan
   * @return true, if is rate plan match
   */
  private static boolean isRatePlanMatch(BasicDynaBean rule, String ratePlan) {
    String plan = (String) rule.get("rate_plan");
    return ("*".equals(plan) || plan.equals(ratePlan)) ? true : false;
  }

  /**
   * Matches this rule if rule.center_id='*' or rule.center_id=centerId
   *
   * @param rule the rule
   * @param centerId the center id
   * @return true, if is center match
   */

  private static boolean isCenterMatch(BasicDynaBean rule, String centerId) {
    String center = (String) rule.get("center_id");
    return ("*".equals(center) || center.equals(centerId)) ? true : false;
  }

  /*
   * Check for if the given charge involves a split discount
   */
  /**
   * Checks if is split discounts.
   *
   * @param chargeBean the charge bean
   * @return true, if is split discounts
   */
  public static boolean isSplitDiscounts(DynaBean chargeBean) {

    int discountAuthDr = (Integer) chargeBean.get("discount_auth_dr");
    int discountAuthPresDr = (Integer) chargeBean.get("discount_auth_pres_dr");
    int discountAuthRef = (Integer) chargeBean.get("discount_auth_ref");
    int discountAuthHosp = (Integer) chargeBean.get("discount_auth_hosp");
    return (discountAuthHosp != 0 
        || discountAuthRef != 0 
        || discountAuthPresDr != 0 
        || discountAuthDr != 0) ? true : false;
  }

  /*
   * Calculate the payment for a given charge and optionally an activity. An activity supplied means
   * we're really interested only in the doctor payment, so we don't bother calculating the other
   * components.
   */
  /**
   * Calculate payments.
   *
   * @param rule the rule
   * @param chargeBean the charge bean
   * @param actBean the act bean
   * @param isSplitDiscount the is split discount
   * @param useDiscountedAmount the use discounted amount
   * @return the charge breakup
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public ChargeBreakup calculatePayments(BasicDynaBean rule, DynaBean chargeBean, DynaBean actBean,
      boolean isSplitDiscount, boolean useDiscountedAmount) throws IOException, TemplateException {

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
       * For services and tests, payment is eligible only after activity is conducted. For others,
       * we don't bother. If there is no activity (as in directly adding to bill) there is no
       * question of conduction for tests and services.
       */
      String head = (String) rule.get("charge_head");
      if (head.equals("LTDIA") || head.equals("RTDIA") || head.equals("SERSNP")) {

        String activityConductedStr = (actBean != null) ? (String) actBean
            .get("activity_conducted") : (String) chargeBean.get("activity_conducted");

        if (activityConductedStr != null && activityConductedStr.equals("N")) {
          activityConducted = false;
        }

        log.debug("Conduction status for conductible activity: " + activityConducted
            + " using actBean=" + actBean);
      } else {
        log.debug("Assuming conducted=" + activityConducted + " for charge head " + head);
      }

      if (activityConducted) {
        docAmount = getPayeeAmount("dr_payment", rule, chargeAmount, (actBean != null), chargeBean,
            billNo);
        // split discount is not applicable for pkg payments. todo: block this in billing screen
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

    // ref/pres are useful only if we are not getting the activity conducting amount.
    if (actBean == null) {
      String refEligibleStr = (String) chargeBean.get("ref_doc_eligible");
      if (refEligibleStr != null && "Y".equals(refEligibleStr)) {
        refAmount = getPayeeAmount("ref_payment", rule, chargeAmount, false, chargeBean, billNo);
        if (isSplitDiscount) {
          refAmount = refAmount.subtract((BigDecimal) chargeBean.get("ref_discount_amt"));
        }
        if (refAmount.compareTo(zero) < 0) {
          refAmount = zero;
        }
      }

      String presEligibleStr = (String) chargeBean.get("pres_doc_eligible");
      if (presEligibleStr != null && "Y".equals(presEligibleStr)) {
        presAmount = getPayeeAmount("presc_payment", rule, chargeAmount, false, chargeBean, billNo);
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
  
  /*
   * Get a single payee's amount based on the payee settings. This takes advantage of the fact that
   * the payee name is suffixed with _option and _value for the settings, eg: dr_payment,
   * dr_payment_option, dr_payment_value
   */
  /**
   * Gets the payee amount.
   *
   * @param payee the payee
   * @param rule the rule
   * @param amount the amount
   * @param isPkgAmt the is pkg amt
   * @param charge the charge
   * @param billNo the bill no
   * @return the payee amount
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public BigDecimal getPayeeAmount(String payee, DynaBean rule, BigDecimal amount,
      boolean isPkgAmt, DynaBean charge, String billNo) throws IOException, TemplateException {

    if (isPkgAmt) {
      return (BigDecimal) rule.get("dr_pkg_amt");
    }

    BigDecimal payeePart = BigDecimal.ZERO;
    String option = (String) rule.get(payee + "_option");
    BigDecimal value = (BigDecimal) rule.get(payee + "_value");

    log.debug("getPayeeAmount: " + payee + ": opt=" + option + ", value=" + value);
    if ("5".equals(option)) {
      /* use expression */
      String expr = (String) rule.get(payee + "_expr");
      if (!(expr.equals(""))) {
        payeePart = getPayeePartForExpr(expr, charge, billNo);
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
   * Gets the payee part for expr.
   *
   * @param expr the expr
   * @param chargeBean the charge bean
   * @param billNo the bill no
   * @return the payee part for expr
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public BigDecimal getPayeePartForExpr(String expr, DynaBean chargeBean, String billNo)
      throws IOException, TemplateException {

    expr = "<#setting number_format=\"#.###\">\n" + expr;

    Template temp = new Template("name", new StringReader(expr), new Configuration());
    HashMap<String, Object> params = new HashMap<String, Object>();

    BasicDynaBean bill = billService.findByKey(billNo);
    BasicDynaBean patient = regService.getPatientVisitDetailsBean((String) bill.get("visit_id"));
    BasicDynaBean conductingDr = doctorService.getDoctorPaymentBean((String) chargeBean
        .get("payee_doctor_id"));
    BasicDynaBean prescDr = doctorService.getDoctorPaymentBean((String) chargeBean
        .get("prescribing_dr_id"));
    BasicDynaBean refDr = doctorService.getDoctorPaymentBean((String) chargeBean
        .get("reference_docto_id"));

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
   * @param params the params
   * @param patient the patient
   * @param bill the bill
   * @param conductingDr the conducting dr
   * @param prescDr the presc dr
   * @param refDr the ref dr
   * @param charge the charge
   */
  public void putPaymentExprParams(Map params, DynaBean patient, DynaBean bill,
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
    putParamString(params, patient, "tpa_id", "primary_sponsor_id");
    putParamString(params, patient, "primary_sponsor_id");
    putParamString(params, patient, "secondary_sponsor_id");
    putParamString(params, patient, "category_id", "insurance_category");
    putParamString(params, bill, "bill_account_group", "account_group");
    putParamString(params, bill, "dyna_package_id");
    putParamString(params, patient, "bed_type", "bill_bed_type");
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
   * @param params the params
   * @param bean the bean
   * @param paramName the param name
   * @param attr the attr
   */
  public void putParamString(Map params, DynaBean bean, String paramName, String attr) {
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
   * @param params the params
   * @param bean the bean
   * @param attr the attr
   */
  public void putParamString(Map params, DynaBean bean, String attr) {
    putParamString(params, bean, attr, attr);
  }

  /**
   * Put param big decimal.
   *
   * @param params the params
   * @param bean the bean
   * @param paramName the param name
   * @param attr the attr
   */
  public void putParamBigDecimal(Map params, DynaBean bean, String paramName, String attr) {
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
   * @param params the params
   * @param bean the bean
   * @param attr the attr
   */
  public void putParamBigDecimal(Map params, DynaBean bean, String attr) {
    putParamBigDecimal(params, bean, attr, attr);
  }

  /**
   * Update payout.
   *
   * @param chargeId the charge id
   * @param updateDoctorAmount the update doctor amount
   * @param doctorAmount the doctor amount
   * @param updatePresAmount the update pres amount
   * @param presAmount the pres amount
   * @param updateRefAmount the update ref amount
   * @param refAmount the ref amount
   * @return true, if successful
   */
  public boolean updatePayout(String chargeId, boolean updateDoctorAmount, BigDecimal doctorAmount,
      boolean updatePresAmount, BigDecimal presAmount,
      boolean updateRefAmount, BigDecimal refAmount) {

    Map<String, Object> keys = new HashMap<>();
    keys.put("charge_id", (String) chargeId);
    if (updateDoctorAmount && updatePresAmount && updateRefAmount) {
      BasicDynaBean chargeBean = billChargeService.getBean();
      chargeBean.set("doctor_amount", doctorAmount);
      chargeBean.set("prescribing_dr_amount", presAmount);
      chargeBean.set("referal_amount", refAmount);
      billChargeService.update(chargeBean, keys);
    } else {
      BasicDynaBean chargeBean = billChargeService.getBean();
      if (updateDoctorAmount) {
        chargeBean.set("doctor_amount", doctorAmount);
        billChargeService.update(chargeBean, keys);
      }
      if (updatePresAmount) {
        chargeBean.set("prescribing_dr_amount", presAmount);
        billChargeService.update(chargeBean, keys);
      }
      if (updateRefAmount) {
        chargeBean.set("referal_amount", refAmount);
        billChargeService.update(chargeBean, keys);
      }
    }
    return true;
  }

}