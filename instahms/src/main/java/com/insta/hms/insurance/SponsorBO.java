package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillingHelper;
import com.insta.hms.billing.BillingTaxCalculator;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class SponsorBO.
 */
public class SponsorBO {

  /** The Constant NEW_CHARGE_ID_PREFIX. */
  private static final String NEW_CHARGE_ID_PREFIX = "";
  // TODO : we may not need any prefix,
  // since _ comes
  // after all uppercase letters. We will change this if required.
  // Otherwise, we will have to deal with unpacking it when
  // sending the results back

  /** The bill tax calculator. */
  BillingTaxCalculator billTaxCalculator = new BillingTaxCalculator();

  /** The pip dao. */
  PatientInsurancePlanDAO pipDao = new PatientInsurancePlanDAO();

  /** The tpa dao. */
  TpaMasterDAO tpaDao = new TpaMasterDAO();

  /** The bill charge claim tax dao. */
  GenericDAO billChargeClaimTaxDao = new GenericDAO("bill_charge_claim_tax");

  /** The sales claim tax dao. */
  GenericDAO salesClaimTaxDao = new GenericDAO("sales_claim_tax_details");

  /** The bill charge tax dao. */
  BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();

  /** The billing helper. */
  BillingHelper billingHelper = new BillingHelper();

  /** The pat reg dao. */
  GenericDAO patRegDao = new GenericDAO("patient_registration");
  
  /** The medicine sales DAO. */
  static MedicineSalesDAO medicineSalesDAO = new MedicineSalesDAO(null);
  
  private static final GenericDAO insurancePayableBillChargesViewDAO =
      new GenericDAO("insurance_payable_bill_charges_view");

  /**
   * Recalculate sponsor amount.
   *
   * @param visitId the visit id
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void recalculateSponsorAmount(String visitId) throws SQLException, IOException {

    List<BasicDynaBean> planList = new SponsorDAO().getPlanDetails(visitId);
    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    Boolean includeFollowUpVisits = false;

    for (BasicDynaBean planBean : planList) {
      String limitsIncludeFollowUp = (String) planBean.get("limits_include_followup");
      includeFollowUpVisits = includeFollowUpVisits
          || (null != limitsIncludeFollowUp && limitsIncludeFollowUp.equals("Y"));
    }

    String mainVisitID = new SponsorDAO().getMainVisitId(visitId);

    String followUpVisitIds = "'".concat(visitId).concat("'");
    Boolean insuranceDetailsFromMainVisit = false;

    if (includeFollowUpVisits) {
      Boolean isMainVisit = visitId.equals(mainVisitID);
      if (isMainVisit) {
        followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
      } else {
        if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, visitId)) {
          followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
          insuranceDetailsFromMainVisit = true;
        }
      }
    }

    Map<String, BasicDynaBean> additionalDetails = getAdditionalDetails(visitId);

    List<BasicDynaBean> visitInsuranceList = new SponsorDAO()
        .getVisitInsDetails(insuranceDetailsFromMainVisit ? mainVisitID : visitId);

    List<String> chargeTaxToBeAdjusted = new ArrayList<String>();

    List<BasicDynaBean> billCharges = new BillDAO().getVisitBillCharges(visitId,
        includeFollowUpVisits, followUpVisitIds);

    initializeBillChargeAdjustments(billCharges);

    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = 
        new HashMap<Integer, List<BasicDynaBean>>();

    visitInsurancePlanMap = ConversionUtils.listBeanToMapListBean(visitInsuranceList, "plan_id");

    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<Integer, Map<Integer, Integer>>();

    for (int key : visitInsurancePlanMap.keySet()) {

      List<BasicDynaBean> billChargeClaims = new BillDAO().getVisitBillChargeClaims(visitId, key,
          includeFollowUpVisits, followUpVisitIds);

      Map<String, List<BasicDynaBean>> billChargeClaimMap = 
          new HashMap<String, List<BasicDynaBean>>();

      billChargeClaimMap = ConversionUtils.listBeanToMapListBean(billChargeClaims, "charge_id");

      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);

      if (null != billCharges) {
        Map<Integer, Integer> planAdjMap = new HashMap<Integer, Integer>();
        planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaimMap,
            visitInsurancePlanMap.get(key), billChargeClaims);

        adjMap.put(key, planAdjMap);

        Map<String, Object> chargeAndSponsorTax = new HashMap<String, Object>();
        chargeAndSponsorTax = calculateSponsorAmountAndTaxSplit(billCharges, billChargeClaimMap,
            isClaimAmtIncludesTax, isLimitIncludesTax, null, additionalDetails, visitId);
        updateBillChargeClaimTaxEntries(billCharges, billChargeClaimMap, chargeAndSponsorTax,
            chargeTaxToBeAdjusted);

        new BillChargeClaimDAO().updateBillChargeClaims(billCharges, billChargeClaimMap);
      }

      updateBillChargeTaxAmountsForExempts(chargeTaxToBeAdjusted);
      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, isClaimAmtIncludesTax,
          isLimitIncludesTax);
    }

    // new SponsorDAO().updateExclusionsInBillCharge(billCharges);
    new SponsorDAO().insertBillAdjustmentAlerts(adjMap, visitId);
  }

  /**
   * Gets the follow up visit ids.
   *
   * @param followUpVisitIds the follow up visit ids
   * @param mainVisitID      the main visit ID
   * @return the follow up visit ids
   * @throws SQLException the SQL exception
   */
  private String getFollowUpVisitIds(String followUpVisitIds, String mainVisitID)
      throws SQLException {
    // TODO Auto-generated method stub
    String visitIds = followUpVisitIds;
    List<BasicDynaBean> followUpVisits = patRegDao.findAllByKey("main_visit_id", mainVisitID);
    for (BasicDynaBean followUpVisitBean : followUpVisits) {
      String followUpVisitId = (String) followUpVisitBean.get("patient_id");
      if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, followUpVisitId)) {
        visitIds = visitIds == null ? "'".concat(followUpVisitId).concat("'")
            : visitIds.concat(",").concat("'".concat(followUpVisitId).concat("'"));
      }
    }
    return visitIds;
  }

  /**
   * Checks if is main visit plan same as followup plan.
   *
   * @param mainVisitID the main visit ID
   * @param visitId     the visit id
   * @return the boolean
   * @throws SQLException the SQL exception
   */
  private Boolean isMainVisitPlanSameAsFollowupPlan(String mainVisitID, String visitId)
      throws SQLException {
    // TODO Auto-generated method stub
    Boolean isPlansSame = false;
    List<BasicDynaBean> mainVisitPlanBeans = pipDao.findAllByKey("patient_id", mainVisitID);
    List<BasicDynaBean> followupVisitPlanBeans = pipDao.findAllByKey("patient_id", visitId);
    int mainVisitPriPlanId = null != mainVisitPlanBeans && !mainVisitPlanBeans.isEmpty()
        && null != mainVisitPlanBeans.get(0) ? (Integer) mainVisitPlanBeans.get(0).get("plan_id")
            : 0;
    int mainVisitSecPlanId = (null != mainVisitPlanBeans && mainVisitPlanBeans.size() > 1
        && null != mainVisitPlanBeans.get(1)) ? (Integer) mainVisitPlanBeans.get(1).get("plan_id")
            : 0;

    int visitPriPlanId = null != followupVisitPlanBeans && !followupVisitPlanBeans.isEmpty()
        && null != followupVisitPlanBeans.get(0)
            ? (Integer) followupVisitPlanBeans.get(0).get("plan_id")
            : 0;
    int visitSecPlanId = (null != followupVisitPlanBeans && followupVisitPlanBeans.size() > 1
        && null != followupVisitPlanBeans.get(1))
            ? (Integer) followupVisitPlanBeans.get(1).get("plan_id")
            : 0;
    if (mainVisitPriPlanId == visitPriPlanId && mainVisitSecPlanId == visitSecPlanId) {
      isPlansSame = true;
    }
    return isPlansSame;
  }

  /**
   * Update bill charge tax amounts for exempts.
   *
   * @param chargeTaxToBeAdjusted the charge tax to be adjusted
   * @throws SQLException the SQL exception
   */
  private void updateBillChargeTaxAmountsForExempts(List<String> chargeTaxToBeAdjusted)
      throws SQLException {
    new BillChargeTaxDAO().updateBillChargeTaxAmountsForExempts(chargeTaxToBeAdjusted);
  }

  /**
   * Sets the bill charge insurance claim amt.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param isClaimAmtIncludesTax the is claim amt includes tax
   * @param isLimitIncludesTax    the is limit includes tax
   */
  private void setBillChargeInsuranceClaimAmt(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, String isClaimAmtIncludesTax,
      String isLimitIncludesTax) {
    for (BasicDynaBean charge : billCharges) {
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      String chargeId = (String) charge.get("charge_id");
      BasicDynaBean billChgClaimBean = billChargeClaimMap.get(chargeId).get(0);
      if (!isClaimLocked) {
        BigDecimal claimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
        if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
          claimAmt = claimAmt.add((BigDecimal) billChgClaimBean.get("tax_amt"));
        }

        BigDecimal billChgInsClaimAmt = ((BigDecimal) charge.get("insurance_claim_amount"))
            .add(claimAmt);
        charge.set("insurance_claim_amount", billChgInsClaimAmt);
      } else {
        BigDecimal amount = (BigDecimal) charge.get("amount");
        BigDecimal claimAmt = (BigDecimal) billChgClaimBean.get("insurance_claim_amt");
        if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
          claimAmt = claimAmt.add((BigDecimal) billChgClaimBean.get("tax_amt"));
        }
        charge.set("amount", amount.subtract(claimAmt));
      }
    }
  }

  /**
   * Initialize bill charge claim adjustments.
   *
   * @param billChargeClaimMap the bill charge claim map
   * @param billCharges        the bill charges
   * @throws SQLException the SQL exception
   */
  private void initializeBillChargeClaimAdjustments(
      Map<String, List<BasicDynaBean>> billChargeClaimMap, List<BasicDynaBean> billCharges)
      throws SQLException {

    Map<String, BasicDynaBean> billChgMap = new HashMap<String, BasicDynaBean>();

    billChgMap = ConversionUtils.listBeanToMapBean(billCharges, "charge_id");
    for (String key : billChargeClaimMap.keySet()) {
      Boolean isClaimLocked = (Boolean) (billChgMap.get(key).get("is_claim_locked"));

      if (!isClaimLocked) {
        List<BasicDynaBean> billChgClaimList = billChargeClaimMap.get(key);
        // RC Anupama : get(0) is
        // incorrect
        BasicDynaBean billChgClaimBean = billChgClaimList.get(0);
        billChgClaimBean.set("copay_ded_adj", BigDecimal.ZERO);
        billChgClaimBean.set("max_copay_adj", BigDecimal.ZERO);
        billChgClaimBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        billChgClaimBean.set("copay_perc_adj", BigDecimal.ZERO);
      }
    }
  }

  /**
   * Initialize bill charge adjustments.
   *
   * @param billCharges the bill charges
   */
  private void initializeBillChargeAdjustments(List<BasicDynaBean> billCharges) {
    for (BasicDynaBean billCharge : billCharges) {
      Boolean isClaimLocked = (Boolean) billCharge.get("is_claim_locked");

      if (!isClaimLocked) {
        billCharge.set("insurance_claim_amount", BigDecimal.ZERO);
        billCharge.set("copay_ded_adj", BigDecimal.ZERO);
        billCharge.set("max_copay_adj", BigDecimal.ZERO);
        billCharge.set("sponsor_limit_adj", BigDecimal.ZERO);
        billCharge.set("copay_perc_adj", BigDecimal.ZERO);
      }
    }
  }

  /**
   * Gets the hospital sponosor amount.
   *
   * @param newCharges     the new charges
   * @param editedCharges  the edited charges
   * @param visitId        the visit id
   * @param adjMap         the adj map
   * @param sponsorTaxMap  the sponsor tax map
   * @param subGrpCodesMap the sub grp codes map
   * @return the hospital sponosor amount
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public Map<Integer, List<BasicDynaBean>> getHospitalSponosorAmount(
      List<Map<String, Object>> newCharges, List<Map<String, Object>> editedCharges, String visitId,
      Map<Integer, Map<Integer, Integer>> adjMap, Map<Integer, Object> sponsorTaxMap,
      Map<String, List<BasicDynaBean>> subGrpCodesMap) throws SQLException, IOException {
    List<BasicDynaBean> newBeanList = mapToChargeBean(newCharges);
    List<BasicDynaBean> editedBeanList = mapToChargeBean(editedCharges);
    return calculateSponosorAmount(newBeanList, editedBeanList, visitId, adjMap, sponsorTaxMap,
        subGrpCodesMap);
  }

  /**
   * Gets the pharmacy sponosor amount.
   *
   * @param newCharges     the new charges
   * @param visitId        the visit id
   * @param adjMap         the adj map
   * @param sponsorTaxMap  the sponsor tax map
   * @param subGrpCodesMap the sub grp codes map
   * @return the pharmacy sponosor amount
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public Map<Integer, List<BasicDynaBean>> getPharmacySponosorAmount(
      List<Map<String, Object>> newCharges, String visitId,
      Map<Integer, Map<Integer, Integer>> adjMap, Map<Integer, Object> sponsorTaxMap,
      Map<String, List<BasicDynaBean>> subGrpCodesMap) throws SQLException, IOException {
    List<BasicDynaBean> newBeanList = mapToSaleBean(newCharges);
    return calculateSponosorAmount(newBeanList, null, visitId, adjMap, sponsorTaxMap,
        subGrpCodesMap);
  }

  /**
   * Gets the reg screen order items sponosor amount.
   *
   * @param newCharges      the new charges
   * @param visitInsDetails the visit ins details
   * @param visitId         the visit id
   * @param adjMap          the adj map
   * @return the reg screen order items sponosor amount
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public Map<Integer, List<BasicDynaBean>> getRegScreenOrderItemsSponosorAmount(
      List<Map<String, Object>> newCharges, List<BasicDynaBean> visitInsDetails, String visitId,
      Map<Integer, Map<Integer, Integer>> adjMap) throws SQLException, IOException {
    List<BasicDynaBean> newBeanList = mapToChargeBean(newCharges);
    return calculateRegScreenOrderItemsSponosorAmount(newBeanList, visitInsDetails, visitId,
        adjMap);
  }

  /**
   * Map to sale bean.
   *
   * @param newCharges the new charges
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> mapToSaleBean(List<Map<String, Object>> newCharges)
      throws SQLException {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    // Kludge !! - the view is created only so that we will be able to create a bean with these
    // fields.
    // We will have to get rid of this, with the next revision of this code.
    for (Map<String, Object> newCharge : newCharges) {
      BasicDynaBean chargeBean = insurancePayableBillChargesViewDAO.getBean();
      String chargeId = (String) newCharge.get("charge_id");
      String chargeHead = (String) newCharge.get("charge_head_id");
      if (null != chargeId) {
        if (chargeId.startsWith("_")) {
          chargeBean.set("charge_id", NEW_CHARGE_ID_PREFIX + chargeId);
        } else {
          chargeBean.set("charge_id", chargeId);
        }
        chargeBean.set("is_claim_locked", newCharge.get("is_claim_locked"));
        chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
        chargeBean.set("pri_insurance_claim_amount",
            new BigDecimal((String) newCharge.get("primclaimAmt")));

        chargeBean.set("pri_include_in_claim_calc",
            newCharge.get("pri_include_in_claim") != null
                && (newCharge.get("pri_include_in_claim").equals("Y")
                    || newCharge.get("pri_include_in_claim").equals("")));

        if (null != newCharge.get("secclaimAmt")) {
          chargeBean.set("sec_insurance_claim_amount",
              new BigDecimal((String) newCharge.get("secclaimAmt")));
        }

        if (null != newCharge.get("sec_include_in_claim")) {
          chargeBean.set("sec_include_in_claim_calc",
              newCharge.get("sec_include_in_claim") != null
                  && (newCharge.get("sec_include_in_claim").equals("Y")
                      || newCharge.get("sec_include_in_claim").equals("")));
        }

        chargeBean.set("include_in_claim_calc", Boolean.TRUE);
        chargeBean.set("copay_ded_adj", BigDecimal.ZERO);
        chargeBean.set("max_copay_adj", BigDecimal.ZERO);
        chargeBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        chargeBean.set("copay_perc_adj", BigDecimal.ZERO);
        chargeBean.set("amount", new BigDecimal((String) newCharge.get("amount")));
        chargeBean.set("discount", new BigDecimal((String) newCharge.get("discount")));
        chargeBean.set("insurance_category_id",
            new Integer((String) newCharge.get("insurance_category_id")));
        chargeBean.set("is_charge_head_payable", newCharge.get("is_insurance_payable"));
        chargeBean.set("charge_head", chargeHead);
        chargeBean.set("charge_group", newCharge.get("charge_group_id"));
        chargeBean.set("store_item_category_payable", newCharge.get("store_item_category_payable"));
        chargeBean.set("act_description_id", newCharge.get("descriptionId"));
        chargeBean.set("consultation_type_id", 0);
        chargeBean.set("claim_amount_includes_tax",
            newCharge.get("claim_amount_includes_tax") != null
                ? (String) newCharge.get("claim_amount_includes_tax")
                : "N");
        if (StringUtils.isNotBlank(newCharge.get("item_excluded_from_doctor").toString())) {
          if ("MI".equals(newCharge.get(
              "item_excluded_from_doctor").toString()) || "N".equals(newCharge.get(
              "item_excluded_from_doctor").toString()) || (Boolean.FALSE.equals(newCharge.get(
              "item_excluded_from_doctor")))) {
            chargeBean.set("item_excluded_from_doctor", false);
            chargeBean.set("item_excluded_from_doctor_remarks", newCharge.get(
                "item_excluded_from_doctor_remarks"));
          } else if ("ME".equals(newCharge.get(
              "item_excluded_from_doctor").toString()) || "Y".equals(newCharge.get(
              "item_excluded_from_doctor").toString()) || "NA".equals(newCharge.get(
              "item_excluded_from_doctor").toString()) || (Boolean.TRUE.equals(newCharge.get(
              "item_excluded_from_doctor")))) {

            chargeBean.set("item_excluded_from_doctor", true);
            chargeBean.set("item_excluded_from_doctor_remarks", newCharge.get(
                "item_excluded_from_doctor_remarks"));
          }
        }
        setSponsorTaxAmount(chargeBean, newCharge);
        list.add(chargeBean);
      }
    }
    return list;
  }

  /**
   * Map to charge bean.
   *
   * @param newCharges the new charges
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> mapToChargeBean(List<Map<String, Object>> newCharges)
      throws SQLException {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    // Kludge !! - the view is created only so that we will be able to create a bean with these
    // fields.
    // We will have to get rid of this, with the next revision of this code.
    for (Map<String, Object> newCharge : newCharges) {
      BasicDynaBean chargeBean = insurancePayableBillChargesViewDAO.getBean();
      String chargeId = (String) newCharge.get("charge_id");
      if (null != chargeId) {
        if (chargeId.startsWith("_")) {
          chargeBean.set("charge_id", NEW_CHARGE_ID_PREFIX + chargeId);
        } else {
          chargeBean.set("charge_id", chargeId);
        }
        Boolean isClaimLocked = (Boolean) newCharge.get("is_claim_locked");
        chargeBean.set("is_claim_locked", isClaimLocked);
        chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
        chargeBean.set("pri_insurance_claim_amount",
            new BigDecimal((String) newCharge.get("primclaimAmt")));

        chargeBean.set("pri_include_in_claim_calc",
            newCharge.get("pri_include_in_claim") != null
                && (newCharge.get("pri_include_in_claim").equals("Y")
                    || newCharge.get("pri_include_in_claim").equals("")));

        if (null != newCharge.get("secclaimAmt")) {
          chargeBean.set("sec_insurance_claim_amount",
              new BigDecimal((String) newCharge.get("secclaimAmt")));
        }

        if (null != newCharge.get("sec_include_in_claim")) {
          chargeBean.set("sec_include_in_claim_calc",
              newCharge.get("sec_include_in_claim") != null
                  && (newCharge.get("sec_include_in_claim").equals("Y")
                      || newCharge.get("sec_include_in_claim").equals("")));
        }

        chargeBean.set("include_in_claim_calc", Boolean.TRUE);
        chargeBean.set("copay_ded_adj", BigDecimal.ZERO);
        chargeBean.set("max_copay_adj", BigDecimal.ZERO);
        chargeBean.set("sponsor_limit_adj", BigDecimal.ZERO);
        chargeBean.set("copay_perc_adj", BigDecimal.ZERO);
        chargeBean.set("amount", newCharge.get("amount"));
        chargeBean.set("discount", newCharge.get("discount"));
        chargeBean.set("insurance_category_id", newCharge.get("insurance_category_id"));
        chargeBean.set("is_charge_head_payable", newCharge.get("is_insurance_payable"));
        chargeBean.set("charge_head", newCharge.get("charge_head_id"));
        chargeBean.set("charge_group", newCharge.get("charge_group_id"));
        chargeBean.set("store_item_category_payable", true);
        chargeBean.set("act_description_id", newCharge.get("descriptionId"));
        chargeBean.set("consultation_type_id",
            newCharge.get("consultationTypeId") != null
                && !((String) newCharge.get("consultationTypeId")).isEmpty()
                    ? Integer.parseInt(((String) newCharge.get("consultationTypeId")))
                    : 0);
        chargeBean.set("claim_amount_includes_tax",
            newCharge.get("claim_amount_includes_tax") != null
                ? (String) newCharge.get("claim_amount_includes_tax")
                : "N");
        chargeBean.set("limit_includes_tax",
            newCharge.get("limit_includes_tax") != null
                ? (String) newCharge.get("limit_includes_tax")
                : "N");
        chargeBean.set("op_id", newCharge.get("op_id"));
        chargeBean.set("package_id", newCharge.get("package_id"));
        setSponsorTaxAmount(chargeBean, newCharge);

        list.add(chargeBean);
      }
    }
    return list;
  }

  /**
   * Calculate sponosor amount.
   *
   * @param newCharges     the new charges
   * @param editedCharges  the edited charges
   * @param visitId        the visit id
   * @param adjMap         the adj map
   * @param sponsorTaxMap  the sponsor tax map
   * @param subGrpCodesMap the sub grp codes map
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private Map<Integer, List<BasicDynaBean>> calculateSponosorAmount(List<BasicDynaBean> newCharges,
      List<BasicDynaBean> editedCharges, String visitId, Map<Integer, Map<Integer, Integer>> adjMap,
      Map<Integer, Object> sponsorTaxMap, Map<String, List<BasicDynaBean>> subGrpCodesMap)
      throws SQLException, IOException {

    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    Map<Integer, List<BasicDynaBean>> retMap = new HashMap<Integer, List<BasicDynaBean>>();
    Boolean includeFollowUpVisits = isFollowupVisitsIncluded(visitId);
    String refVisitId = getReferenceVisitId(visitId, includeFollowUpVisits);

    String mainVisitID = refVisitId;

    String followUpVisitIds = "'".concat(visitId).concat("'");
    Boolean insuranceDetailsFromMainVisit = false;

    if (includeFollowUpVisits) {
      Boolean isMainVisit = visitId.equals(mainVisitID);
      if (isMainVisit) {
        followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
      } else {
        if (isMainVisitPlanSameAsFollowupPlan(mainVisitID, visitId)) {
          followUpVisitIds = getFollowUpVisitIds(followUpVisitIds, mainVisitID);
          insuranceDetailsFromMainVisit = true;
        }
      }
    }

    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = getVisitInsuranceDetails(
        insuranceDetailsFromMainVisit ? refVisitId : visitId);

    BasicDynaBean billBean = new GenericDAO("bill").getBean();
    billBean.set("is_tpa", visitInsurancePlanMap.keySet().size() > 0);
    Map<String, BasicDynaBean> details = getAdditionalDetails(visitId);
    details.put("bill", billBean);

    List<BasicDynaBean> billCharges = getBillChargesList(visitId, includeFollowUpVisits,
        followUpVisitIds);

    if (null != editedCharges && editedCharges.size() > 0) {
      billCharges = setEditedBillCharges(editedCharges, billCharges);
    }

    if (null != newCharges) {
      billCharges.addAll(newCharges);
    }
    initializeBillChargeAdjustments(billCharges);

    setCatIdBasedOnPlanIds(newCharges, visitInsurancePlanMap.keySet(), details);

    for (int planId : visitInsurancePlanMap.keySet()) {

      List<BasicDynaBean> visitInsBeanList = visitInsurancePlanMap.get(planId);
      BasicDynaBean visitInsBean = visitInsBeanList.get(0);
      int priority = (Integer) visitInsBean.get("priority");

      List<BasicDynaBean> billChargeClaims = getBillChargeClaimList(visitId, planId,
          includeFollowUpVisits, followUpVisitIds);

      if (null != editedCharges && editedCharges.size() > 0) {
        billChargeClaims = setEditedBillChargeClaims(editedCharges, billChargeClaims, priority);
      }

      List<BasicDynaBean> chargeClaimList = getNewChargeClaimList(newCharges, planId, priority,
          visitInsBeanList, details);

      if (chargeClaimList != null) {
        billChargeClaims.addAll(chargeClaimList);
      }

      Map<String, List<BasicDynaBean>> billChargeClaimMap = 
          new HashMap<String, List<BasicDynaBean>>();
      billChargeClaimMap = ConversionUtils.listBeanToMapListBean(billChargeClaims, "charge_id");
      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);

      Map<Integer, Integer> planAdjMap = new HashMap<Integer, Integer>();
      Map<String, Object> chargeAndSponsorTax = new HashMap<String, Object>();

      if (null != billCharges) {
        planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaimMap,
            visitInsurancePlanMap.get(planId), billChargeClaims);

        chargeAndSponsorTax = calculateSponsorAmountAndTaxSplit(billCharges, billChargeClaimMap,
            isClaimAmtIncludesTax, isLimitIncludesTax, subGrpCodesMap, details, visitId);
      }

      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, "N", "N");
      // billChargeClaimMap already has claim amount and tax amount. so we no need to add tax
      // amount to billCharges. so to avoid that hard coding the tpa flags.
      List<BasicDynaBean> resultList = new ArrayList<BasicDynaBean>();
      for (String chargeId : billChargeClaimMap.keySet()) {
        resultList.addAll(billChargeClaimMap.get(chargeId));
      }
      retMap.put(planId, resultList);
      adjMap.put(planId, planAdjMap);
      if (sponsorTaxMap != null) {
        sponsorTaxMap.put(planId, chargeAndSponsorTax);
      }
    }

    return retMap;
  }

  /**
   * Sets the cat id based on plan ids.
   *
   * @param newCharges the new charges
   * @param keySet     the key set
   * @param details    the details
   * @throws SQLException the SQL exception
   */
  // Related to PHIL-HEALTH supported MODEL changes
  private void setCatIdBasedOnPlanIds(List<BasicDynaBean> newCharges, Set<Integer> keySet,
      Map<String, BasicDynaBean> details) throws SQLException {

    if (keySet.toArray().length == 0) {
      return;
    }

    Map<String, Object> itemMap = billingHelper.getItemMapForInsuranceCategories(newCharges);

    billingHelper.getCatIdBasedOnPlanIds(newCharges, itemMap, keySet, details);

  }

  /**
   * Gets the additional details.
   *
   * @param visitId the visit id
   * @return the additional details
   * @throws SQLException the SQL exception
   */
  private Map<String, BasicDynaBean> getAdditionalDetails(String visitId) throws SQLException {
    Map<String, BasicDynaBean> details = new HashMap<String, BasicDynaBean>();
    BasicDynaBean visiBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
    if (null != visiBean && !visiBean.getMap().isEmpty()) {
      BasicDynaBean patientBean = new PatientDetailsDAO().findByKey("mr_no", visiBean.get("mr_no"));
      BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id",
          visiBean.get("center_id"));
      details.put("patient", patientBean);
      details.put("center", centerBean);
      details.put("visit", visiBean);
    }
    return details;
  }

  /**
   * Sets the edited bill charges.
   *
   * @param editedCharges the edited charges
   * @param billCharges   the bill charges
   * @return the list
   */
  private List<BasicDynaBean> setEditedBillCharges(List<BasicDynaBean> editedCharges,
      List<BasicDynaBean> billCharges) {

    Map<String, BasicDynaBean> billChgMap = new LinkedHashMap<String, BasicDynaBean>();

    billChgMap = ConversionUtils.listBeanToMapBean(billCharges, "charge_id");

    for (BasicDynaBean bean : editedCharges) {
      String chargeId = (String) bean.get("charge_id");
      BigDecimal amount = (BigDecimal) bean.get("amount");
      BigDecimal discount = (BigDecimal) bean.get("discount");
      Boolean isClaimLocked = (Boolean) bean.get("is_claim_locked");

      billChgMap.get(chargeId).set("amount", amount);
      billChgMap.get(chargeId).set("discount", discount);
      billChgMap.get(chargeId).set("is_claim_locked", isClaimLocked);
    }

    List<BasicDynaBean> retList = new ArrayList<BasicDynaBean>();

    for (Map.Entry<String, BasicDynaBean> entry : billChgMap.entrySet()) {
      retList.add(entry.getValue());
    }

    return retList;
  }

  /**
   * Sets the edited bill charge claims.
   *
   * @param editedCharges    the edited charges
   * @param billChargeClaims the bill charge claims
   * @param priroity         the priroity
   * @return the list
   */
  private List<BasicDynaBean> setEditedBillChargeClaims(List<BasicDynaBean> editedCharges,
      List<BasicDynaBean> billChargeClaims, int priroity) {
    Map<String, BasicDynaBean> billChgClaimMap = new LinkedHashMap<String, BasicDynaBean>();
    billChgClaimMap = ConversionUtils.listBeanToMapBean(billChargeClaims, "charge_id");
    for (BasicDynaBean bean : editedCharges) {
      String chargeId = (String) bean.get("charge_id");
      BigDecimal claimAmt = priroity == 1 ? (BigDecimal) bean.get("pri_insurance_claim_amount")
          : (BigDecimal) bean.get("sec_insurance_claim_amount");
      billChgClaimMap.get(chargeId).set("insurance_claim_amt", claimAmt);

      BigDecimal claimTaxAmt = priroity == 1 ? (BigDecimal) bean.get("pri_claim_tax_amt")
          : (BigDecimal) bean.get("sec_claim_tax_amt");
      billChgClaimMap.get(chargeId).set("tax_amt", claimTaxAmt);

      Boolean includeInClaimCalc = priroity == 1 ? (Boolean) bean.get("pri_include_in_claim_calc")
          : (Boolean) bean.get("sec_include_in_claim_calc");
      billChgClaimMap.get(chargeId).set("include_in_claim_calc", includeInClaimCalc);
    }

    List<BasicDynaBean> retList = new ArrayList<BasicDynaBean>();

    for (Map.Entry<String, BasicDynaBean> entry : billChgClaimMap.entrySet()) {
      retList.add(entry.getValue());
    }

    return retList;
  }

  /**
   * Gets the new charge claim list.
   *
   * @param newCharges       the new charges
   * @param planId           the plan id
   * @param priority         the priority
   * @param visitInsBeanList the visit ins bean list
   * @param details          the details
   * @return the new charge claim list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getNewChargeClaimList(List<BasicDynaBean> newCharges, int planId,
      int priority, List<BasicDynaBean> visitInsBeanList, Map<String, BasicDynaBean> details)
      throws SQLException {
    GenericDAO chargeClaimDao = new GenericDAO("bill_charge_claim");
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    for (BasicDynaBean charge : newCharges) {
      BasicDynaBean chargeClaimBean = chargeClaimDao.getBean();
      chargeClaimBean.set("charge_id", charge.get("charge_id"));

      chargeClaimBean.set("include_in_claim_calc", true);
      chargeClaimBean.set("tax_amt",
          (priority == 1) ? charge.get("pri_claim_tax_amt") : charge.get("sec_claim_tax_amt"));

      chargeClaimBean.set("insurance_category_id", (Integer) charge.get("insurance_category_id"));
      Boolean isClaimLocked = (Boolean) charge.get("is_claim_locked");
      if (isClaimLocked) {
        if (priority == 1) {
          chargeClaimBean.set("insurance_claim_amt", charge.get("pri_insurance_claim_amount"));
          chargeClaimBean.set("tax_amt", charge.get("pri_claim_tax_amt"));
          chargeClaimBean.set("include_in_claim_calc", charge.get("pri_include_in_claim_calc"));
        } else {
          chargeClaimBean.set("insurance_claim_amt", charge.get("sec_insurance_claim_amount"));
          chargeClaimBean.set("tax_amt", charge.get("sec_claim_tax_amt"));
          chargeClaimBean.set("include_in_claim_calc", charge.get("sec_include_in_claim_calc"));
        }
      }

      String chargeGroup = (String) charge.get("charge_group");
      // set values for insurance_category_id
      if (chargeGroup.equals("REG")) {
        setRegistrationChargeCategoryId(charge, chargeClaimBean, planId, visitInsBeanList);
      } else {
        if (details != null && !details.isEmpty()) {
          BasicDynaBean visitBean = details.get("visit");
          billingHelper.getInsCatIdFromTemplate(charge, planId, chargeClaimBean,
              (String) visitBean.get("visit_type"));
        }
      }

      // TODO : we need to set the claim id here
      list.add(chargeClaimBean);
    }
    return list;
  }

  /**
   * Sets the registration charge category id.
   *
   * @param charge           the charge
   * @param chargeClaimBean  the charge claim bean
   * @param planId           the plan id
   * @param visitInsBeanList the visit ins bean list
   */
  private void setRegistrationChargeCategoryId(BasicDynaBean charge, BasicDynaBean chargeClaimBean,
      int planId, List<BasicDynaBean> visitInsBeanList) {
    // TODO Auto-generated method stub
    Map<Integer, BasicDynaBean> insuranceCategoryMap = ConversionUtils
        .listBeanToMapBean(visitInsBeanList, "insurance_category_id");
    if (insuranceCategoryMap.containsKey(-1)) {
      chargeClaimBean.set("insurance_category_id", -1);
    }
  }

  /**
   * Gets the bill charge claim list.
   *
   * @param visitId               the visit id
   * @param planId                the plan id
   * @param includeFollowUpVisits the include follow up visits
   * @param followUpVisitIds      the follow up visit ids
   * @return the bill charge claim list
   * @throws SQLException the SQL exception
   */
  protected List<BasicDynaBean> getBillChargeClaimList(String visitId, Integer planId,
      Boolean includeFollowUpVisits, String followUpVisitIds) throws SQLException {
    List<BasicDynaBean> billChargeClaims = new BillDAO().getVisitBillChargeClaims(visitId, planId,
        includeFollowUpVisits, followUpVisitIds);
    return billChargeClaims;
  }

  /**
   * Gets the bill charges list.
   *
   * @param visitId               the visit id
   * @param includeFollowUpVisits the include follow up visits
   * @param followUpVisitIds      the follow up visit ids
   * @return the bill charges list
   * @throws SQLException the SQL exception
   */
  protected List<BasicDynaBean> getBillChargesList(String visitId, Boolean includeFollowUpVisits,
      String followUpVisitIds) throws SQLException {
    List<BasicDynaBean> billCharges = new BillDAO().getVisitBillCharges(visitId,
        includeFollowUpVisits, followUpVisitIds);
    return billCharges;
  }

  /**
   * Gets the reference visit id.
   *
   * @param visitId               the visit id
   * @param includeFollowUpVisits the include follow up visits
   * @return the reference visit id
   * @throws SQLException the SQL exception
   */
  private String getReferenceVisitId(String visitId, Boolean includeFollowUpVisits)
      throws SQLException {
    String mainVisitID = new SponsorDAO().getMainVisitId(visitId);
    String refVisitId = (null != mainVisitID && includeFollowUpVisits) ? mainVisitID : visitId;
    return refVisitId;
  }

  /**
   * Checks if is followup visits included.
   *
   * @param visitId the visit id
   * @return the boolean
   * @throws SQLException the SQL exception
   */
  private Boolean isFollowupVisitsIncluded(String visitId) throws SQLException {
    Boolean includeFollowUpVisits = false;
    List<BasicDynaBean> planList = new SponsorDAO().getPlanDetails(visitId);

    for (BasicDynaBean planBean : planList) {
      String limitsIncludeFollowUp = (String) planBean.get("limits_include_followup");
      includeFollowUpVisits = includeFollowUpVisits
          || (null != limitsIncludeFollowUp && limitsIncludeFollowUp.equals("Y"));
    }
    return includeFollowUpVisits;
  }

  /**
   * Gets the visit insurance details.
   *
   * @param visitId the visit id
   * @return the visit insurance details
   * @throws SQLException the SQL exception
   */
  private Map<Integer, List<BasicDynaBean>> getVisitInsuranceDetails(String visitId)
      throws SQLException {
    List<BasicDynaBean> visitInsuranceList = new SponsorDAO().getVisitInsDetails(visitId);
    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = 
        new HashMap<Integer, List<BasicDynaBean>>();
    visitInsurancePlanMap = ConversionUtils.listBeanToMapListBean(visitInsuranceList, "plan_id");
    return visitInsurancePlanMap;
  }

  /**
   * Calculate reg screen order items sponosor amount.
   *
   * @param newCharges      the new charges
   * @param visitInsDetails the visit ins details
   * @param visitId         the visit id
   * @param adjMap          the adj map
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<Integer, List<BasicDynaBean>> calculateRegScreenOrderItemsSponosorAmount(
      List<BasicDynaBean> newCharges, List<BasicDynaBean> visitInsDetails, String visitId,
      Map<Integer, Map<Integer, Integer>> adjMap) throws SQLException {
    Map<Integer, List<BasicDynaBean>> retMap = new HashMap<Integer, List<BasicDynaBean>>();

    Map<Integer, List<BasicDynaBean>> visitInsurancePlanMap = ConversionUtils
        .listBeanToMapListBean(visitInsDetails, "plan_id");
    List<BasicDynaBean> billCharges = new ArrayList<BasicDynaBean>();

    if (null != newCharges) {
      billCharges.addAll(newCharges);
    }
    initializeBillChargeAdjustments(billCharges);

    for (int planId : visitInsurancePlanMap.keySet()) {

      List<BasicDynaBean> visitInsBeanList = visitInsurancePlanMap.get(planId);
      BasicDynaBean visitInsBean = visitInsBeanList.get(0);
      int priority = (Integer) visitInsBean.get("priority");

      List<BasicDynaBean> billChargeClaims = new ArrayList<BasicDynaBean>();

      List<BasicDynaBean> chargeClaimList = getNewChargeClaimList(newCharges, planId, priority,
          visitInsBeanList, null);

      if (chargeClaimList != null) {
        billChargeClaims.addAll(chargeClaimList);
      }

      Map<String, List<BasicDynaBean>> billChargeClaimMap = 
          new HashMap<String, List<BasicDynaBean>>();
      billChargeClaimMap = ConversionUtils.listBeanToMapListBean(billChargeClaims, "charge_id");
      initializeBillChargeClaimAdjustments(billChargeClaimMap, billCharges);

      Map<Integer, Integer> planAdjMap = new HashMap<Integer, Integer>();

      if (null != billCharges) {
        planAdjMap = new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaimMap,
            visitInsurancePlanMap.get(planId), billChargeClaims);
      }

      setBillChargeInsuranceClaimAmt(billCharges, billChargeClaimMap, "N", "N");
      List<BasicDynaBean> resultList = new ArrayList<BasicDynaBean>();
      for (String chargeId : billChargeClaimMap.keySet()) {
        resultList.addAll(billChargeClaimMap.get(chargeId));
      }
      retMap.put(planId, resultList);
      adjMap.put(planId, planAdjMap);
    }

    return retMap;
  }

  /**
   * Calculate sponsor amount and tax split.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param isClaimAmtIncludesTax the is claim amt includes tax
   * @param isLimitIncludesTax    the is limit includes tax
   * @param subGrpCodesMap        the sub grp codes map
   * @param details               the details
   * @param visitId               the visit id
   * @return the map
   * @throws SQLException the SQL exception
   */
  private Map<String, Object> calculateSponsorAmountAndTaxSplit(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, String isClaimAmtIncludesTax,
      String isLimitIncludesTax, Map<String, List<BasicDynaBean>> subGrpCodesMap,
      Map<String, BasicDynaBean> details, String visitId) throws SQLException {

    String itemId = null;
    String chargeGroup = null;
    String chargeHead = null;
    int consId = 0;
    String opId = null;
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal insClaimAmt = BigDecimal.ZERO;
    BigDecimal refInsClaimAmt = BigDecimal.ZERO;

    Map<String, Object> chargeAndSponsorTax = new HashMap<String, Object>(); 
    // It will hold charge and its sponsor tax details
    
    Map<Integer, List<BasicDynaBean>> chargeTaxBeansMap = 
        new HashMap<Integer, List<BasicDynaBean>>();
    // KSA Changes
    // BasicDynaBean patientBean = new VisitDetailsDAO().findByKey("patient_id",
    // sponsorDetailsBean.get("patient_id"));

    Map<Integer, List<BasicDynaBean>> saleItemsTaxBeansMap = 
        new HashMap<Integer, List<BasicDynaBean>>();

    if (null != visitId && !visitId.equals("")) {
      chargeTaxBeansMap = ConversionUtils.listBeanToMapListBean(
          billChargeTaxDao.getAllHospitalItemsContainingTotalTax(visitId), "charge_id");

      saleItemsTaxBeansMap = ConversionUtils.listBeanToMapListBean(
          medicineSalesDAO.getSalesTaxDetailsForvisit(visitId), "sale_item_id");
    }

    for (BasicDynaBean chargeBean : billCharges) {
      Boolean isClaimLocked = (Boolean) chargeBean.get("is_claim_locked");
      final String chargeId = (String) chargeBean.get("charge_id");
      // if(!isClaimLocked){
      Map<String, Object> itemSponsorTaxMap = new HashMap<String, Object>();
      itemId = (String) chargeBean.get("act_description_id");
      if ("PKG".equals(chargeBean.get("charge_group"))
          && !"PKGPKG".equals(chargeBean.get("charge_head"))) {
        itemId = String.valueOf((int) chargeBean.get("package_id"));
      }
      chargeGroup = (String) chargeBean.get("charge_group");
      chargeHead = (String) chargeBean.get("charge_head");
      if (null == chargeGroup) {
        BasicDynaBean chargeHeadBean = ChargeHeadsDAO.getChargeHeadBean(chargeHead);
        if (null != chargeHeadBean) {
          chargeGroup = (String) chargeHeadBean.get("chargegroup_id");
        }
      }
      amount = (BigDecimal) chargeBean.get("amount");
      consId = (Integer) chargeBean.get("consultation_type_id");
      opId = (String) chargeBean.get("op_id");

      insClaimAmt = (BigDecimal) ((billChargeClaimMap.get(chargeId)).get(0))
          .get("insurance_claim_amt");

      boolean isTotalTaxExist = false;

      if ((chargeId).startsWith("_")) {
        isTotalTaxExist = true;
      } else if (chargeGroup.equals("MED")) {
        isTotalTaxExist = true;
      } else if (null != chargeTaxBeansMap && !chargeTaxBeansMap.isEmpty()) {
        isTotalTaxExist = chargeTaxBeansMap.containsKey((String) chargeBean.get("charge_id"));
      }

      ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
      itemTaxDetails.setAmount(insClaimAmt);
      // itemTaxDetails.setInsClaimAmt(insClaimAmt);

      TaxContext taxContext = new TaxContext();
      taxContext.setItemBean(chargeBean);
      if (!details.isEmpty()) {
        taxContext.setPatientBean(details.get("patient"));
        taxContext.setCenterBean(details.get("center"));
        taxContext.setBillBean(details.get("bill"));
        taxContext.setVisitBean(details.get("visit"));
        
      }

      if (null != itemId && null != chargeGroup && isTotalTaxExist) {
        // Used to get the Item sub group codes.
        // List<BasicDynaBean> subGroupCodes = billingHelper.getItemSubgroupCodes(itemId,
        // chargeGroup, chargeHead, consId);

        List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();

        if (chargeGroup.equals("MED")) {
          if (null != subGrpCodesMap && null != subGrpCodesMap.get(chargeId)) {
            subGroupCodes = subGrpCodesMap.get(chargeId);
          } else {
            if (!chargeId.startsWith("_")) {
              if (chargeId.contains("-")) {
                int saleItemId = Integer.parseInt(chargeId.split("\\-")[1]);
                subGroupCodes = saleItemsTaxBeansMap.get(saleItemId);
              }
              if (subGroupCodes == null || subGroupCodes.isEmpty()) {
                subGroupCodes = MedicineSalesDAO.getItemSubgroupCodes(chargeId);
              }
            }
            if ((subGroupCodes == null || subGroupCodes.isEmpty()) 
                && StringUtils.isNumeric(itemId)) {
              subGroupCodes = MedicineSalesDAO.getMedicineSubgroups(Integer.parseInt(itemId));
            }
          }
        } else {
          if (null != subGrpCodesMap && null != subGrpCodesMap.get(chargeId)) {
            subGroupCodes = subGrpCodesMap.get(chargeId);
          } else {
            if (!chargeId.startsWith("_")) {
              subGroupCodes = billingHelper.getItemSubgroupCodes(chargeId);
            }

            if (subGroupCodes.isEmpty()) {
              subGroupCodes = billingHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead,
                  consId, opId);
            } else {
              taxContext.setTransactionId(chargeId);
            }
          }
        }

        if (subGroupCodes != null && subGroupCodes.size() > 0) {
          if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")
              && !isClaimLocked) {
            taxContext.setSubgroups(subGroupCodes);
            itemTaxDetails.setTaxBasis("A");
            itemSponsorTaxMap = billingHelper.calculateSponsorTaxes(itemTaxDetails, taxContext,
                subGroupCodes);
          } else if (isClaimAmtIncludesTax.equals("Y")) {
            itemSponsorTaxMap = billingHelper.calculateSponsorTaxes(itemTaxDetails, taxContext,
                subGroupCodes);
          }

          // getTaxDetails(taxMap, subGroupCodes, itemSponsorTaxMap);
          chargeAndSponsorTax.put((String) chargeBean.get("charge_id"), itemSponsorTaxMap);
        }
      }
      // }
    }
    return chargeAndSponsorTax;
  }

  /**
   * Update bill charge claim tax entries.
   *
   * @param billCharges           the bill charges
   * @param billChargeClaimMap    the bill charge claim map
   * @param chargeAndSponsorTax   the charge and sponsor tax
   * @param chargeTaxToBeAdjusted the charge tax to be adjusted
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private void updateBillChargeClaimTaxEntries(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimMap, Map<String, Object> chargeAndSponsorTax,
      List<String> chargeTaxToBeAdjusted) throws SQLException, IOException {
    Connection con = null;
    boolean success = true;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (BasicDynaBean chargeBean : billCharges) {

        String isClaimAmtIncludesTax = "N";
        // String isLimitIncludesTax = "N";

        String chargeId = (String) chargeBean.get("charge_id");
        String chargeType = (String) chargeBean.get("charge_type");

        Map<String, Object> sponsorTaxDetailsMap = (Map<String, Object>) chargeAndSponsorTax
            .get((String) chargeBean.get("charge_id"));

        if (sponsorTaxDetailsMap == null || sponsorTaxDetailsMap.size() <= 0) {
          continue;
        }

        String claimId = (String) ((billChargeClaimMap.get((String) chargeBean.get("charge_id")))
            .get(0)).get("claim_id");
        String sponsorId = (String) ((billChargeClaimMap.get((String) chargeBean.get("charge_id")))
            .get(0)).get("sponsor_id");
        BasicDynaBean tpaBean = tpaDao.getTpaDetails(sponsorId);

        if (tpaBean != null) {
          isClaimAmtIncludesTax = (String) tpaBean.get("claim_amount_includes_tax");
          // isLimitIncludesTax = (String)tpaBean.get("limit_includes_tax");
        }
        // insClaimAmt =
        // (BigDecimal)((billChargeClaimMap.get
        // ((String)chargeBean.get("charge_id"))).get(0)).get("insurance_claim_amt");
        BigDecimal totSponsorTax = BigDecimal.ZERO; // total tax amount per claim
        BigDecimal sponsorAmt = (BigDecimal) sponsorTaxDetailsMap.get("sponsorAmount");
        if (sponsorAmt != null) {
          ((billChargeClaimMap.get((String) chargeBean.get("charge_id"))).get(0))
              .set("insurance_claim_amt", sponsorAmt);
        }

        Map<Integer, Object> subGrpSponTaxDetailsMap = (Map<Integer, Object>) sponsorTaxDetailsMap
            .get("subGrpSponTaxDetailsMap");
        // key:
        // subgroupid,
        // value:
        // map<str,map<str,str>>
        if (subGrpSponTaxDetailsMap != null && isClaimAmtIncludesTax.equals("Y")) {
          for (Map.Entry<Integer, Object> subgroupTaxDetMap : subGrpSponTaxDetailsMap.entrySet()) {

            Integer taxSubGroupId = subgroupTaxDetMap.getKey();
            Map<String, String> subGroupTaxSplit = (Map<String, String>) subgroupTaxDetMap
                .getValue();

            if (chargeType.equals("pharmacy")) {
              int saleItemId = Integer.parseInt(chargeId.split("-")[1]);
              Map<String, Object> saleClaimTaxKeyMap = new HashMap<String, Object>();
              saleClaimTaxKeyMap.put("sale_item_id", saleItemId);
              saleClaimTaxKeyMap.put("claim_id", claimId);
              saleClaimTaxKeyMap.put("item_subgroup_id", taxSubGroupId);
              BasicDynaBean salesClaimTaxBean = salesClaimTaxDao.findByKey(con, saleClaimTaxKeyMap);

              if (salesClaimTaxBean != null) {
                salesClaimTaxBean.set("tax_rate",
                    new BigDecimal((String) subGroupTaxSplit.get("rate")));
                salesClaimTaxBean.set("tax_amt",
                    new BigDecimal((String) subGroupTaxSplit.get("amount")));
                if (null != subGroupTaxSplit.get("adjTaxAmt")
                    && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                  salesClaimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
                }
                salesClaimTaxDao.update(con, salesClaimTaxBean.getMap(), saleClaimTaxKeyMap);
              } else {
                salesClaimTaxBean = salesClaimTaxDao.getBean();
                salesClaimTaxBean.set("sale_item_id", saleItemId);
                salesClaimTaxBean.set("claim_id", claimId);
                salesClaimTaxBean.set("item_subgroup_id", taxSubGroupId);
                salesClaimTaxBean.set("tax_rate",
                    new BigDecimal((String) subGroupTaxSplit.get("rate")));
                salesClaimTaxBean.set("tax_amt",
                    new BigDecimal((String) subGroupTaxSplit.get("amount")));
                if (null != subGroupTaxSplit.get("adjTaxAmt")
                    && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                  salesClaimTaxBean.set("adj_amt", (String) subGroupTaxSplit.get("adjTaxAmt"));
                }
                salesClaimTaxDao.insert(con, salesClaimTaxBean);
              }
              totSponsorTax = totSponsorTax.add((BigDecimal) salesClaimTaxBean.get("tax_amt"));

            } else {

              Map<String, Object> chargeTaxMap = new HashMap<String, Object>();
              chargeTaxMap.put("charge_id", chargeId);
              chargeTaxMap.put("tax_sub_group_id", taxSubGroupId);
              BasicDynaBean chargeTaxBean = billChargeTaxDao.findByKey(chargeTaxMap);

              int chargeTaxId = 0;
              if (null != chargeTaxBean && null != chargeTaxBean.get("charge_tax_id")) {
                chargeTaxId = (Integer) chargeTaxBean.get("charge_tax_id");
              }
              Map<String, Object> claimTaxKeyMap = new HashMap<String, Object>();
              claimTaxKeyMap.put("charge_id", chargeId);
              claimTaxKeyMap.put("claim_id", claimId);
              claimTaxKeyMap.put("charge_tax_id", chargeTaxId);

              BasicDynaBean claimTaxBean = billChargeClaimTaxDao.findByKey(con, claimTaxKeyMap);

              if (claimTaxBean != null) {
                claimTaxBean.set("tax_rate", new BigDecimal((String) subGroupTaxSplit.get("rate")));
                claimTaxBean.set("sponsor_tax_amount",
                    new BigDecimal((String) subGroupTaxSplit.get("amount")));
                claimTaxBean.set("tax_sub_group_id", taxSubGroupId);
                claimTaxBean.set("charge_tax_id", chargeTaxId);
                String adjAmt = "N";
                if (null != subGroupTaxSplit.get("adjTaxAmt")
                    && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                  adjAmt = (String) subGroupTaxSplit.get("adjTaxAmt");
                  chargeTaxToBeAdjusted.add(chargeId);
                }
                claimTaxBean.set("adj_amt", adjAmt);
                billChargeClaimTaxDao.update(con, claimTaxBean.getMap(), claimTaxKeyMap);
              } else {
                claimTaxBean = billChargeClaimTaxDao.getBean();
                claimTaxBean.set("charge_id", chargeId);
                claimTaxBean.set("claim_id", claimId);
                claimTaxBean.set("sponsor_id", sponsorId);
                claimTaxBean.set("tax_sub_group_id", taxSubGroupId);
                claimTaxBean.set("tax_rate", new BigDecimal((String) subGroupTaxSplit.get("rate")));
                claimTaxBean.set("sponsor_tax_amount",
                    new BigDecimal((String) subGroupTaxSplit.get("amount")));
                String adjAmt = "N";
                if (null != subGroupTaxSplit.get("adjTaxAmt")
                    && !subGroupTaxSplit.get("adjTaxAmt").equals("")) {
                  adjAmt = (String) subGroupTaxSplit.get("adjTaxAmt");
                  chargeTaxToBeAdjusted.add(chargeId);
                }
                claimTaxBean.set("adj_amt", adjAmt);
                claimTaxBean.set("charge_tax_id", chargeTaxId);
                billChargeClaimTaxDao.insert(con, claimTaxBean);
              }
              totSponsorTax = totSponsorTax
                  .add((BigDecimal) claimTaxBean.get("sponsor_tax_amount"));
            }
          }
        }
        ((billChargeClaimMap.get((String) chargeBean.get("charge_id"))).get(0)).set("tax_amt",
            totSponsorTax);
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Sets the sponsor tax amount.
   *
   * @param chargeBean the charge bean
   * @param newCharge  the new charge
   * @throws SQLException the SQL exception
   */
  private void setSponsorTaxAmount(BasicDynaBean chargeBean, Map<String, Object> newCharge)
      throws SQLException {
    Boolean isClaimLocked = (Boolean) newCharge.get("is_claim_locked");
    if (!isClaimLocked) {
      chargeBean.set("pri_claim_tax_amt",
          newCharge.get("priInsClaimTaxAmt") != null
              ? (BigDecimal) newCharge.get("priInsClaimTaxAmt")
              : BigDecimal.ZERO);
      chargeBean.set("sec_claim_tax_amt",
          newCharge.get("secInsClaimTaxAmt") != null
              ? (BigDecimal) newCharge.get("secInsClaimTaxAmt")
              : BigDecimal.ZERO);
    } else {
      // get sponsor tax amount by inputting edited claim amount
      BigDecimal priClaimTaxAmt = BigDecimal.ZERO;
      BigDecimal secClaimTaxAmt = BigDecimal.ZERO;
      String isClaimAmtIncludesTax = newCharge.get("claim_amount_includes_tax") != null
          ? (String) newCharge.get("claim_amount_includes_tax")
          : "N";
      if (isClaimAmtIncludesTax.equals("Y")) {
        BigDecimal priClaimAmt = (BigDecimal) chargeBean.get("pri_insurance_claim_amount");
        Map<String, Object> chargeDetMap = new HashMap<String, Object>();
        chargeDetMap = chargeBean.getMap();
        chargeDetMap.put("amount", priClaimAmt);
        priClaimTaxAmt = getTaxAmountForCharge(chargeDetMap);
        BigDecimal secClaimAmt = (BigDecimal) chargeBean.get("sec_insurance_claim_amount");
        chargeDetMap.put("amount", secClaimAmt);
        secClaimTaxAmt = getTaxAmountForCharge(chargeDetMap);
      }
      chargeBean.set("pri_claim_tax_amt", priClaimTaxAmt);
      chargeBean.set("sec_claim_tax_amt", secClaimTaxAmt);
    }
  }

  /**
   * Gets the tax amount for charge.
   *
   * @param chargeMap the charge map
   * @return the tax amount for charge
   * @throws SQLException the SQL exception
   */
  public BigDecimal getTaxAmountForCharge(Map<String, Object> chargeMap) throws SQLException {

    Map<Integer, Object> itemSubGrpTaxMap = new HashMap<Integer, Object>();
    BigDecimal taxAmt = BigDecimal.ZERO;
    String chargeId = (String) chargeMap.get("charge_id");
    String itemId = (String) chargeMap.get("act_description_id");
    if ("PKG".equals(chargeMap.get("charge_group"))
        && !"PKGPKG".equals(chargeMap.get("charge_head"))) {
      itemId = String.valueOf((int) chargeMap.get("package_id"));
    }
    String chargeGroup = (String) chargeMap.get("charge_group");
    String chargeHead = (String) chargeMap.get("charge_head");
    Integer consId = (Integer) chargeMap.get("consultation_type_id");
    String opId = (String) chargeMap.get("op_id");

    boolean isTotalTaxExist = billChargeTaxDao.exist("charge_id",
        (String) chargeMap.get("charge_id"), false) || (chargeId).startsWith("_");

    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setAmount((BigDecimal) chargeMap.get("amount"));

    TaxContext taxContext = new TaxContext();

    if (null != itemId && null != chargeGroup && isTotalTaxExist) {
      List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();
      if (!chargeId.startsWith("_")) {
        subGroupCodes = billingHelper.getItemSubgroupCodes(chargeId);
      }

      if (subGroupCodes.isEmpty()) {
        subGroupCodes = billingHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consId,
            opId);
      } else {
        taxContext.setTransactionId(chargeId);
      }

      if (subGroupCodes != null && subGroupCodes.size() > 0) {
        itemSubGrpTaxMap = billingHelper.getTaxChargesMap(itemTaxDetails, taxContext,
            subGroupCodes);
      }
    }

    for (Map.Entry<Integer, Object> subGrpMap : itemSubGrpTaxMap.entrySet()) {
      Map<String, String> taxMap = (Map<String, String>) subGrpMap.getValue();
      taxAmt = taxAmt.add(new BigDecimal(taxMap.get("amount")));
    }
    return taxAmt;
  }

  // public List<BasicDynaBean> getItemSubgroupCodesList(String itemId,
  // String chargeGroup) throws SQLException {
  // if(chargeGroup.equals("SNP")) {
  // return new ServicesDAO().getServiceItemSubGroupTaxDetails(itemId);
  // }
  // return null;
  // }
}
