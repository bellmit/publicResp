package com.insta.hms.extension.dynapackage;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.adt.BedNamesService;
import com.insta.hms.core.inventory.sales.SalesClaimDetailsService;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.dynapackage.DynaPackageService;
import com.insta.hms.mdm.dynapackagerules.DynaPackageRulesService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DynaPackageProcessorService.
 */
@Component
public class DynaPackageProcessorService {

  /** The log. */
  private Logger log = LoggerFactory.getLogger(DynaPackageProcessorService.class);

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The charge head service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadService;

  /** The dyna package service. */
  @LazyAutowired
  private DynaPackageService dynaPackageService;

  /** The dyna package rules service. */
  @LazyAutowired
  private DynaPackageRulesService dynaPackageRulesService;

  /** The pat ins plan service. */
  @LazyAutowired
  private PatientInsurancePlansService patInsPlanService;

  /** The bed names service. */
  @LazyAutowired
  private BedNamesService bedNamesService;

  /** The service sub group service. */
  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  /** The sales service. */
  @LazyAutowired
  private SalesService salesService;

  /** The item service. */
  @LazyAutowired
  private StoreItemDetailsService itemService;

  /** The bill activity service. */
  @LazyAutowired
  private BillActivityChargeService billActivityService;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The insurance service. */
  @LazyAutowired
  private InsurancePlanService insuranceService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The sales claim details service. */
  @LazyAutowired
  private SalesClaimDetailsService salesClaimDetailsService;

  /** The pkg limit map. */
  private static Map<Integer, BigDecimal> pkgLimitMap = new HashMap<Integer, BigDecimal>();

  /** The pkg quantity map. */
  private static Map<Integer, BigDecimal> pkgQuantityMap = new HashMap<Integer, BigDecimal>();

  /** The included charges. */
  private static List<String> includedCharges = Arrays.asList(BillChargeService.CH_BYBED,
      BillChargeService.CH_LUXURY_TAX, BillChargeService.CH_SERVICE_TAX);

  /**
   * Process.
   *
   * @param billNo
   *          the bill no
   * @return the map
   * @throws ParseException
   *           the parse exception
   */
  public Map<String, String> process(String billNo) throws ParseException {
    Map<String, String> errors = new HashMap<String, String>();
    BasicDynaBean bill = billService.findByKey(billNo);
    int dynaPkgId = (Integer) bill.get("dyna_package_id");

    if (dynaPkgId == 0) {
      return errors;
    }
    
    String visitId = (String) bill.get("visit_id");
    BasicDynaBean visitbean = regService.findByKey(visitId);
    Integer visitCenterId = (Integer) visitbean.get("center_id");

    Map<String, Object> session = sessionService.getSessionAttributes();
    String userid = session == null ? "auto_update" : (String) session.get("userId");
    Integer center = (Integer) (session == null ? visitCenterId
        : (Integer) session.get("centerId"));
    userid = userid != null ? userid.toString() : "auto_update";
    String centerid = center != null ? center.toString() : "*";

    List<BasicDynaBean> plansList = patInsPlanService.getPlanDetails(visitId);
    int[] planIds = new int[plansList.size()];
    String[] preAuthIds = new String[plansList.size()];
    Integer[] preAuthModeIds = new Integer[plansList.size()];
    int planIdIndex = 0;

    for (BasicDynaBean planBean : plansList) {
      planIds[planIdIndex] = (Integer) planBean.get("plan_id");
      preAuthIds[planIdIndex] = (String) planBean.get("prior_auth_id");
      preAuthModeIds[planIdIndex] = (Integer) planBean.get("prior_auth_mode_id");
      planIdIndex++;
    }

    bedChargeCalculation(bill, visitbean, planIds, preAuthIds, preAuthModeIds);

    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("bill_no", billNo);
    filterMap.put("status", "A");
    
    String ratePlan = (String) bill.get("bill_rate_plan_id");
    boolean isTpa = (Boolean) bill.get("is_tpa");

    Map<String, String> filterKey = new HashMap<String, String>();
    filterKey.put("chargehead_id", BillChargeService.CH_DYNA_PACKAGE_MARGIN);
    BasicDynaBean chargeHeadBean = chargeHeadService.findByPk(filterKey);
    String claimable = (String) chargeHeadBean.get("insurance_payable");
    boolean marginClaimable = isTpa && claimable.equals("Y");

    BasicDynaBean dynaPkgDetails = dynaPackageService.getDynaPackageDetails(dynaPkgId, ratePlan);
    String bedType = (String) visitbean.get("bed_type");
    // Bill dyna package (active/inactive) i.e existing or new package details.
    List<BasicDynaBean> dynaPkgCharges = dynaPackageService.getDynaPackageCharges(ratePlan, bedType,
        dynaPkgId);

    List<BasicDynaBean> rules = dynaPackageRulesService.listAll("priority");

    if (dynaPkgCharges == null) {
      errors.put("error", "Invalid dyna package id: " + dynaPkgId + " for bill no: " + billNo);
      return errors;
    }

    // Initialize the category limits
    createDynaPkgLimitsMap(dynaPkgCharges);

    // Initialize the category quantities
    createDynaPkgQuantityMap(dynaPkgCharges);

    /**
     * All hospitals have Day care beds as separate bed type with separate kind of charges. So, if
     * DAY CARE bed type needs to be excluded or included with separate limit then we need to define
     * separate category with limit.
     * Otherwise, day care beds are processed as normal beds.
     * All bed charges viz. BBED,NCBED,DDBED,PCBED,BICU,NCICU,DDICU,PCICU,BYBED are consider for
     * dyna package processing.
     * LTAX -- First, we will check if this charge head has any category. If category exists then
     * the rule is applied. If category does not exists then LTAX is processed along with normal bed
     * charges.
     */
    List<BasicDynaBean> charges = billChargeService.list(filterMap, "posted_date");
    for (BasicDynaBean charge : charges) {
      String status = (String) charge.get("status");
      String chargeGroup = (String) charge.get("charge_group");
      String chargeHead = (String) charge.get("charge_head");
      Map<String, Object> primaryKey = new HashMap<String, Object>();
      primaryKey.put("chargehead_id", chargeHead);
      BasicDynaBean chrgbean = chargeHeadService.findByPk(primaryKey);
      boolean isInsurancePayable = chrgbean.get("insurance_payable") != null
          && ((String) chrgbean.get("insurance_payable")).equals("Y");

      // Exclude cancelled charges, discounts & claim service tax from package
      // i.e amount and qty included is zero.
      boolean exclude = checkForProcessingExclusions(charge, status, chargeGroup, chargeHead);
      if (exclude) {
        continue;
      }

      BasicDynaBean processbean = getProcessingChargeBean();
      processbean.set("margin_claimable", marginClaimable);
      setProcessingAttributes(charge, processbean);
      setActivityBedType(processbean);

      String chargeRef = charge.get("charge_ref") == null ? null
          : (String) charge.get("charge_ref");

      /**
       * bystander bed, Luxury tax and Service tax are associated charges of Bed Charge and Service
       * charges. So, we will check if these have any separate categories first. Do not process if
       * main charge category and associated charge categories are same and has limit as Quantity.
       * i.e these will be included along with main charges while processing.
       */
      if (includedCharges.contains(chargeHead)) {
        boolean included = includeAssociateWithMainCharge(charges, processbean, dynaPkgCharges,
            rules, centerid);

        if (included) {
          continue;
        } 
      }

      boolean hasActivity = (Boolean) charge.get("hasactivity");
      if (hasActivity && (chargeHead.equals(BillChargeService.CH_PHARMACY_CREDIT_MEDICINE)
          || chargeHead.equals(BillChargeService.CH_PHARMACY_CREDIT_RETURNS))) {
        // Process pharmacy items (amounts with returns) according to category and rules.
        processPharmacyItems(processbean, dynaPkgCharges, rules, centerid);

        // Copy the amount,qty included to charge
        charge.set("amount_included", (BigDecimal) processbean.get("amount_included"));
        charge.set("qty_included", (BigDecimal) processbean.get("qty_included"));

      } else if (hasActivity && (chargeHead.equals(BillChargeService.CH_INVENTORY_ITEM))) {
        // Process inventory items (amounts with returns) according to category and rules.
        BigDecimal amt = (BigDecimal) processbean.get("amount");
        BigDecimal retAmt = (BigDecimal) processbean.get("return_amt");
        BigDecimal qty = (BigDecimal) processbean.get("act_quantity");
        BigDecimal retQty = (BigDecimal) processbean.get("return_qty");

        amt = amt.add(retAmt);
        qty = qty.add(retQty);

        processbean.set("amount", amt);
        processbean.set("act_quantity", qty);

        processInventoryItems(processbean, dynaPkgCharges, rules, centerid);

        // Copy the amount,qty included to charge
        charge.set("amount_included", (BigDecimal) processbean.get("amount_included"));
        charge.set("qty_included", (BigDecimal) processbean.get("qty_included"));

      } else {

        processOtherItems(processbean, dynaPkgCharges, rules, centerid, charge, planIds,
            isInsurancePayable, bill, chargeHead, chargeRef, dynaPkgDetails, charges,
            marginClaimable);
      }
    }
    BigDecimal packageAmount = (BigDecimal) bill.get("dyna_package_charge");
    // Update charge included amount, qty and user details.
    updateMarginAndBill(charges, userid, marginClaimable, packageAmount, bill);
    return errors;
  }

  /**
   * Update margin and bill.
   *
   * @param charges
   *          the charges
   * @param userId
   *          the user id
   * @param marginClaimable
   *          the margin claimable
   * @param packageAmount
   *          the package amount
   * @param bill
   *          the bill
   */
  private void updateMarginAndBill(List<BasicDynaBean> charges, String userId,
      boolean marginClaimable, BigDecimal packageAmount, BasicDynaBean bill) {
    Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
    for (BasicDynaBean charge : charges) {
      // All charges including inventory/pharmacy
      charge.set("mod_time", currentTimestamp);
      charge.set("username", userId);
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("charge_id", (String) charge.get("charge_id"));
      billChargeService.update(charge, keys);
    }

    // Calculate package margin and update.
    updatePackageMarginAmount(charges, packageAmount, marginClaimable, userId);

    billChargeService.updateChargeExcluded((String) bill.get("bill_no"));

    bill.set("dyna_pkg_processed", "Y");
    bill.set("mod_time", currentTimestamp);
    bill.set("username", userId);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("bill_no", bill.get("bill_no"));
    billService.update(bill, keys);

    log.info("Dyna package processing success for bill no: " + bill.get("bill_no"));

  }

  /**
   * Update package margin amount.
   *
   * @param charges
   *          the charges
   * @param packageAmount
   *          the package amount
   * @param marginClaimable
   *          the margin claimable
   * @param userId
   *          the user id
   */
  private void updatePackageMarginAmount(List<BasicDynaBean> charges, BigDecimal packageAmount,
      boolean marginClaimable, String userId) {

    BigDecimal pkgMargin = BigDecimal.ZERO;
    BigDecimal pkgIncluded = BigDecimal.ZERO;
    BasicDynaBean pkgMarginChargeBean = null;

    for (BasicDynaBean charge : charges) {
      if (((String) charge.get("charge_head")).equals("MARPKG")) {
        pkgMarginChargeBean = charge;
      } else {
        pkgIncluded = pkgIncluded.add((BigDecimal) charge.get("amount_included"));
      } 
    }

    pkgMargin = packageAmount.subtract(pkgIncluded);

    pkgMarginChargeBean.set("status", "A");
    pkgMarginChargeBean.set("act_quantity", BigDecimal.ONE);
    pkgMarginChargeBean.set("act_rate", pkgMargin);
    pkgMarginChargeBean.set("amount", pkgMargin);
    pkgMarginChargeBean.set("amount_included", pkgMargin);

    if (marginClaimable) {
      pkgMarginChargeBean.set("insurance_claim_amount", pkgMargin);
    } else {
      pkgMarginChargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
    }
    
    pkgMarginChargeBean.set("mod_time", DateUtil.getCurrentTimestamp());
    pkgMarginChargeBean.set("username", userId);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("charge_id", (String) pkgMarginChargeBean.get("charge_id"));
    billChargeService.update(pkgMarginChargeBean, keys);
    if (isBillChargeClaimExists(pkgMarginChargeBean)) {
      billChargeClaimService.updatepackageMarginInBillChgClaim(pkgMarginChargeBean);
    }
  }

  /**
   * Checks if is bill charge claim exists.
   *
   * @param pkgMarginChargeBean
   *          the pkg margin charge bean
   * @return true, if is bill charge claim exists
   */
  private boolean isBillChargeClaimExists(BasicDynaBean pkgMarginChargeBean) {
    boolean isBillChgClaimExists = false;
    String chargeId = (String) pkgMarginChargeBean.get("charge_id");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("charge_id", chargeId);
    BasicDynaBean billChgClaimBean = billChargeClaimService.findByKey(keys);
    if (billChgClaimBean != null) {
      isBillChgClaimExists = true;
    }
    return isBillChgClaimExists;
  }

  /**
   * Sets the secondary claim amount.
   *
   * @param bill
   *          the bill
   * @param planIds
   *          the plan ids
   * @param charge
   *          the charge
   * @param isInsurancePayable
   *          the is insurance payable
   * @param processbean
   *          the processbean
   * @param claimAmount
   *          the claim amount
   * @param secClaimAmt
   *          the sec claim amt
   * @param categoryId
   *          the category id
   * @return the big decimal
   */
  private BigDecimal setSecondaryClaimAmount(BasicDynaBean bill, int[] planIds,
      BasicDynaBean charge, boolean isInsurancePayable, BasicDynaBean processbean,
      BigDecimal claimAmount, BigDecimal secClaimAmt, int categoryId) {
    BasicDynaBean planDetails;
    if (null != planIds && planIds.length > 1) {
      planDetails = insuranceService.getChargeAmtForPlan(planIds[0], categoryId,
          (String) bill.get("visit_type"));
      BigDecimal remainingAmt = ((BigDecimal) charge.get("amount")).subtract(claimAmount);
      secClaimAmt = new AdvanceInsuranceCalculator().calculateClaim(remainingAmt, BigDecimal.ZERO,
          planIds[1], (Boolean) charge.get("first_of_category"),
          (Integer) charge.get("insurance_category_id"), isInsurancePayable, planDetails);

      processbean.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
    }
    return secClaimAmt;
  }

  /**
   * Sets the primary claim amount.
   *
   * @param bill
   *          the bill
   * @param planIds
   *          the plan ids
   * @param charge
   *          the charge
   * @param isInsurancePayable
   *          the is insurance payable
   * @param processbean
   *          the processbean
   * @param categoryId
   *          the category id
   * @return the big decimal
   */
  private BigDecimal setPrimaryClaimAmount(BasicDynaBean bill, int[] planIds, BasicDynaBean charge,
      boolean isInsurancePayable, BasicDynaBean processbean, int categoryId) {
    BigDecimal claimAmount;
    BasicDynaBean planDetails;

    if (planIds != null && planIds.length > 0) {
      planDetails = insuranceService.getChargeAmtForPlan(planIds[0], categoryId,
          (String) bill.get("visit_type"));
      claimAmount = new AdvanceInsuranceCalculator().calculateClaim(
          (BigDecimal) charge.get("amount"), (BigDecimal) charge.get("discount"), planIds[0],
          (Boolean) charge.get("first_of_category"), (Integer) charge.get("insurance_category_id"),
          isInsurancePayable, planDetails);
    } else {
      claimAmount = (BigDecimal) charge.get("amount");
    }

    // When we process dyna pkg again, already calulated claim amt for partial inclusion will be
    // taken into consideration,
    // to avoid this we are again setting the claimamount before any re-processing.
    processbean.set("insurance_claim_amount", claimAmount);
    return claimAmount;
  }

  /**
   * Process other items.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgCharges
   *          the dyna pkg charges
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @param charge
   *          the charge
   * @param planIds
   *          the plan ids
   * @param isInsurancePayable
   *          the is insurance payable
   * @param bill
   *          the bill
   * @param chargeHead
   *          the charge head
   * @param chargeRef
   *          the charge ref
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param charges
   *          the charges
   * @param marginClaimable
   *          the margin claimable
   */
  private void processOtherItems(BasicDynaBean processbean, List<BasicDynaBean> dynaPkgCharges,
      List<BasicDynaBean> rules, String centerid, BasicDynaBean charge, int[] planIds,
      boolean isInsurancePayable, BasicDynaBean bill, String chargeHead, String chargeRef,
      BasicDynaBean dynaPkgDetails, List<BasicDynaBean> charges, boolean marginClaimable) {
    // Get the category to which the charge belongs and process according to
    // the limit type (Amount/Quantity/Unlimited)
    BasicDynaBean pkgCatBean = getPackageCategoryBeanOld(processbean, dynaPkgCharges, rules,
        centerid);
    if (pkgCatBean != null) {
      BigDecimal secClaimAmt = BigDecimal.ZERO;
      BigDecimal claimAmount = BigDecimal.ZERO;
      boolean isTpa = (boolean) bill.get("is_tpa");
      String billNo = (String) bill.get("bill_no");
      String visitId = (String) bill.get("visit_id");

      int categoryId = (Integer) pkgCatBean.get("dyna_pkg_cat_id");
      processbean.set("package_category", categoryId);

      claimAmount = setPrimaryClaimAmount(bill, planIds, charge, isInsurancePayable, processbean,
          categoryId);

      secClaimAmt = setSecondaryClaimAmount(bill, planIds, charge, isInsurancePayable, processbean,
          claimAmount, secClaimAmt, categoryId);

      if (((String) pkgCatBean.get("limit_type")).equals("Q")) {
        processQuantityLimits(processbean, dynaPkgCharges, rules, centerid, charge, planIds,
            chargeHead, chargeRef, dynaPkgDetails, charges, marginClaimable, secClaimAmt,
            claimAmount, isTpa, billNo, visitId);
      } else if (((String) pkgCatBean.get("limit_type")).equals("A")) {
        processAmountLimits(processbean, charge, planIds, dynaPkgDetails, secClaimAmt, claimAmount,
            isTpa, billNo, visitId);
      } else if (((String) pkgCatBean.get("limit_type")).equals("U")) {
        setProcessChargeUnlimitedIncludedQuantity(processbean, dynaPkgCharges, categoryId);
        // Copy the amount,qty included to charge
        charge.set("amount_included", (BigDecimal) processbean.get("amount_included"));
        charge.set("qty_included", (BigDecimal) processbean.get("qty_included"));
      }

    } else {
      charge.set("amount_included", new BigDecimal(0));
      charge.set("qty_included", new BigDecimal(0));
    }

  }

  /**
   * Process amount limits.
   *
   * @param processbean
   *          the processbean
   * @param charge
   *          the charge
   * @param planIds
   *          the plan ids
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param secClaimAmt
   *          the sec claim amt
   * @param claimAmount
   *          the claim amount
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param visitId
   *          the visit id
   */
  private void processAmountLimits(BasicDynaBean processbean, BasicDynaBean charge, int[] planIds,
      BasicDynaBean dynaPkgDetails, BigDecimal secClaimAmt, BigDecimal claimAmount, boolean isTpa,
      String billNo, String visitId) {
    // When we process dyna pkg again, already calulated claim amt for partial inclusion will be
    // taken into consideration,
    // to avoid this we are again setting the claimamount before any re-processing.
    processbean.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
    // Set the included amount if charge is eligible to be included.
    setProcessChargeIncludedAmount(processbean);

    // Copy the amount,qty included to charge
    charge.set("amount_included", (BigDecimal) processbean.get("amount_included"));
    charge.set("qty_included", (BigDecimal) processbean.get("qty_included"));
    // Check for patially Included Item
    if (dynaPkgDetails.get("excluded_amt_claimable").equals("N") && isTpa
        && (((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO) != 0
            && charge.get("amount_included") != processbean.get("amount"))) {

      BigDecimal priClaimAmt = (BigDecimal) processbean.get("amount_included");

      BigDecimal amtIncluded = (BigDecimal) processbean.get("amount_included");
      BigDecimal totalClaimAmt = claimAmount.add(secClaimAmt);

      if (null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) != 0) {
        if (!(secClaimAmt.compareTo(BigDecimal.ZERO) == 0)) {
          BigDecimal excludedAmt = totalClaimAmt.subtract(amtIncluded);
          BigDecimal amtDeducted = BigDecimal.ZERO;
          if (secClaimAmt.compareTo(excludedAmt) > 0) {
            secClaimAmt = secClaimAmt.subtract(excludedAmt);
            priClaimAmt = claimAmount;
          } else {
            amtDeducted = secClaimAmt;
            secClaimAmt = BigDecimal.ZERO;
            priClaimAmt = claimAmount.subtract(excludedAmt.subtract(amtDeducted));
          }
        }
      }

      if (null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) == 0) {
        priClaimAmt = claimAmount;
      }
      updateInsAmtForItems(charge, billNo, priClaimAmt, secClaimAmt, planIds, visitId);
      // For TPA Only,Corporate and National Sponsers
      charge.set("insurance_claim_amount", (BigDecimal) processbean.get("amount_included"));
    } else if (isTpa) {
      updateInsAmtForItems(charge, billNo, claimAmount, secClaimAmt, planIds, visitId);
      // For TPA Only,Corporate and National Sponsers
      charge.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
    }

    setDynaPkgLimits(processbean);
  }

  /**
   * Process quantity limits.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgCharges
   *          the dyna pkg charges
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @param charge
   *          the charge
   * @param planIds
   *          the plan ids
   * @param chargeHead
   *          the charge head
   * @param chargeRef
   *          the charge ref
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param charges
   *          the charges
   * @param marginClaimable
   *          the margin claimable
   * @param secClaimAmt
   *          the sec claim amt
   * @param claimAmount
   *          the claim amount
   * @param isTpa
   *          the is tpa
   * @param billNo
   *          the bill no
   * @param visitId
   *          the visit id
   */
  private void processQuantityLimits(BasicDynaBean processbean, List<BasicDynaBean> dynaPkgCharges,
      List<BasicDynaBean> rules, String centerid, BasicDynaBean charge, int[] planIds,
      String chargeHead, String chargeRef, BasicDynaBean dynaPkgDetails,
      List<BasicDynaBean> charges, boolean marginClaimable, BigDecimal secClaimAmt,
      BigDecimal claimAmount, boolean isTpa, String billNo, String visitId) {
    if (!includedCharges.contains(chargeHead) && chargeRef != null
        && !chargeRef.trim().equals("")) {
      // Limit type: quantity, do not process charges if charge has charge-ref
      // do-nothing
    } else {

      // Set the included qty & amount if charge is eligible to be included.
      setProcessChargeIncludedQuantity(processbean);

      // Copy the amount,qty included to charge
      charge.set("amount_included", (BigDecimal) processbean.get("amount_included"));
      charge.set("qty_included", (BigDecimal) processbean.get("qty_included"));
      // Check for patially Included Item
      if (dynaPkgDetails.get("excluded_amt_claimable").equals("N") && isTpa
          && (((BigDecimal) charge.get("amount_included")).compareTo(BigDecimal.ZERO) != 0
              && charge.get("amount_included") != processbean.get("amount"))) {

        BigDecimal priClaimAmt = (BigDecimal) processbean.get("amount_included");

        BigDecimal amtIncluded = (BigDecimal) processbean.get("amount_included");
        BigDecimal totalClaimAmt = claimAmount.add(secClaimAmt);

        if (null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) != 0) {
          if (!(secClaimAmt.compareTo(BigDecimal.ZERO) == 0)) {

            BigDecimal excludedAmt = totalClaimAmt.subtract(amtIncluded);
            BigDecimal amtDeducted = BigDecimal.ZERO;
            if (secClaimAmt.compareTo(excludedAmt) > 0) {
              secClaimAmt = secClaimAmt.subtract(excludedAmt);
              priClaimAmt = claimAmount;
            } else {
              amtDeducted = secClaimAmt;
              secClaimAmt = BigDecimal.ZERO;
              priClaimAmt = claimAmount.subtract(excludedAmt.subtract(amtDeducted));
            }
          }
        }

        if (null != planIds && planIds.length > 1 && totalClaimAmt.compareTo(amtIncluded) == 0) {
          priClaimAmt = claimAmount;
        } 
        updateInsAmtForItems(charge, billNo, priClaimAmt, secClaimAmt, planIds, visitId);
        // For TPA Only,Corporate and National Sponsers
        charge.set("insurance_claim_amount", (BigDecimal) processbean.get("amount_included"));
      } else if (isTpa) {
        updateInsAmtForItems(charge, billNo, claimAmount, secClaimAmt, planIds, visitId);
        // For TPA Only,Corporate and National Sponsers
        charge.set("insurance_claim_amount", claimAmount.add(secClaimAmt));
      }
      copyChargeRefsIncudedQuantity(charge, charges, marginClaimable, processbean, dynaPkgCharges,
          rules, centerid);
      setDynaPkgQuantity(processbean);
    }
  }

  /**
   * Check for processing exclusions.
   *
   * @param charge
   *          the charge
   * @param status
   *          the status
   * @param chargeGroup
   *          the charge group
   * @param chargeHead
   *          the charge head
   * @return true, if successful
   */
  private boolean checkForProcessingExclusions(BasicDynaBean charge, String status,
      String chargeGroup, String chargeHead) {

    if (((String) charge.get("package_finalized")).equals("Y")) {
      return true;
    } else if (status.equals("X") || chargeGroup.equals(BillChargeService.CG_DISCOUNTS)
        || chargeHead.equals(BillChargeService.CH_CLAIM_SERVICE_TAX)) {
      charge.set("amount_included", new BigDecimal(0));
      charge.set("qty_included", new BigDecimal(0));
      return true;
    } else if (chargeHead.equals(BillChargeService.CH_INVENTORY_RETURNS)) {
      return true;
    }
    return false;
  }

  /**
   * Bed charge calculation.
   *
   * @param bill
   *          the bill
   * @param visitbean
   *          the visitbean
   * @param planIds
   *          the plan ids
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @throws ParseException
   *           the parse exception
   */
  private void bedChargeCalculation(BasicDynaBean bill, BasicDynaBean visitbean, int[] planIds,
      String[] preAuthIds, Integer[] preAuthModeIds) throws ParseException {
    // Check if bill has and excluded bed charges (split bed charges) using old way of dynamic
    // package process.
    List<BasicDynaBean> excludedBedCharges = billChargeService
        .getExcludedBedCharges((String) bill.get("bill_no"));

    if (null != excludedBedCharges && !excludedBedCharges.isEmpty()) {
      // Delete excluded bed charges.
      billChargeService.deleteExcludedBedCharges(excludedBedCharges);

      // Recalculate bed charges for old bills if reopened.l
      billService.recalculateBedCharges(visitbean, bill, planIds, preAuthIds, preAuthModeIds);
    }
  }

  /**
   * Creates the dyna pkg limits map.
   *
   * @param dynaPkgDetails
   *          the dyna pkg details
   */
  // Map of each category and amount, considered if included.
  private void createDynaPkgLimitsMap(List<BasicDynaBean> dynaPkgDetails) {
    for (BasicDynaBean bean : dynaPkgDetails) {
      if (((String) bean.get("limit_type")).equals("A")
          && ((String) bean.get("pkg_included")).equals("Y")) {
        pkgLimitMap.put((Integer) bean.get("dyna_pkg_cat_id"),
            (BigDecimal) bean.get("amount_limit"));
      } 
    }
  }

  /**
   * Creates the dyna pkg quantity map.
   *
   * @param dynaPkgDetails
   *          the dyna pkg details
   */
  // Map of each category and qty, considered if included.
  private void createDynaPkgQuantityMap(List<BasicDynaBean> dynaPkgDetails) {
    for (BasicDynaBean bean : dynaPkgDetails) {
      if (((String) bean.get("limit_type")).equals("Q")
          && ((String) bean.get("pkg_included")).equals("Y")) {
        pkgQuantityMap.put((Integer) bean.get("dyna_pkg_cat_id"),
            (BigDecimal) bean.get("qty_limit"));
      }
    }
  }

  /**
   * Gets the processing charge bean.
   *
   * @return the processing charge bean
   */
  private BasicDynaBean getProcessingChargeBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("charge_id");
    builder.add("charge_group");
    builder.add("charge_head");
    builder.add("act_description_id");
    builder.add("act_description");
    builder.add("service_sub_group_id", Integer.class);
    ;
    builder.add("amount", BigDecimal.class);
    builder.add("act_quantity", BigDecimal.class);
    builder.add("insurance_claim_amount", BigDecimal.class);
    builder.add("return_amt", BigDecimal.class);
    builder.add("return_qty", BigDecimal.class);
    builder.add("return_insurance_claim_amt", BigDecimal.class);

    builder.add("margin_claimable", Boolean.class);
    builder.add("package_category", Integer.class);

    builder.add("amount_included", BigDecimal.class);
    builder.add("qty_included", BigDecimal.class);

    return builder.build();
  }

  /**
   * Sets the processing attributes.
   *
   * @param charge
   *          the charge
   * @param processbean
   *          the processbean
   */
  private void setProcessingAttributes(BasicDynaBean charge, BasicDynaBean processbean) {
    processbean.set("charge_id", (String) charge.get("charge_id"));
    processbean.set("charge_group", (String) charge.get("charge_group"));
    processbean.set("charge_head", (String) charge.get("charge_head"));
    processbean.set("act_description_id", charge.get("act_description_id"));
    processbean.set("act_description", charge.get("act_description"));
    processbean.set("service_sub_group_id", (Integer) charge.get("service_sub_group_id"));
    processbean.set("amount", (BigDecimal) charge.get("amount"));
    processbean.set("act_quantity", (BigDecimal) charge.get("act_quantity"));
    processbean.set("insurance_claim_amount", (BigDecimal) charge.get("insurance_claim_amount"));
    processbean.set("return_amt", (BigDecimal) charge.get("return_amt"));
    processbean.set("return_qty", (BigDecimal) charge.get("return_qty"));
    processbean.set("return_insurance_claim_amt",
        (BigDecimal) charge.get("return_insurance_claim_amt"));
    processbean.set("amount_included", BigDecimal.ZERO);
    processbean.set("qty_included", BigDecimal.ZERO);
  }

  /**
   * Sets the activity bed type.
   *
   * @param processbean
   *          the new activity bed type
   */
  private void setActivityBedType(BasicDynaBean processbean) {
    String chargeGroup = (String) processbean.get("charge_group");
    String chargeHead = (String) processbean.get("charge_head");
    if (chargeGroup.equals(BillChargeService.CG_BED) 
        || chargeGroup.equals(BillChargeService.CG_ICU)
        || chargeHead.equals(BillChargeService.CH_LUXURY_TAX)) {

      String actDescId = processbean.get("act_description_id") != null
          ? (String) processbean.get("act_description_id") : null;
      if (actDescId != null && QueryBuilder.isInteger(actDescId)) {
        BasicDynaBean bedBean = bedNamesService.findByKey(new Integer(actDescId));
        if (bedBean != null) {
          processbean.set("act_description_id", (String) bedBean.get("bed_type"));
        }
      }
    }
  }

  /**
   * Include associate with main charge.
   *
   * @param charges
   *          the charges
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @return true, if successful
   */
  private boolean includeAssociateWithMainCharge(List<BasicDynaBean> charges,
      BasicDynaBean processbean, List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules,
      String centerid) {
    int associateCategory = 0;
    int mainChargeCategory = 0;
    int associateAsOtherCategory = 0;

    String chargeId = (String) processbean.get("charge_id");
    BasicDynaBean chargeBean = getChargeBean(charges, chargeId);
    String chargeRef = chargeBean.get("charge_ref") == null ? null
        : (String) chargeBean.get("charge_ref");

    BasicDynaBean mainChargeBean = null;

    if (chargeRef != null && !chargeRef.trim().equals("")) {
      mainChargeBean = getChargeBean(charges, chargeRef);

      setProcessingAttributes(mainChargeBean, processbean); // Main charge
      setActivityBedType(processbean);

      BasicDynaBean mainChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules,
          centerid);
      if (mainChargeCatBean != null) {
        mainChargeCategory = (Integer) mainChargeCatBean.get("dyna_pkg_cat_id");
      } 
    }

    setProcessingAttributes(chargeBean, processbean); // Associated charge
    setActivityBedType(processbean);

    BasicDynaBean assocChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules,
        centerid);
    if (assocChargeCatBean != null) {
      associateAsOtherCategory = (Integer) assocChargeCatBean.get("dyna_pkg_cat_id");
    } 
    if (mainChargeBean != null) {
      processbean.set("charge_group", (String) mainChargeBean.get("charge_group"));
    } 
    assocChargeCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
    if (assocChargeCatBean != null) {
      associateCategory = (Integer) assocChargeCatBean.get("dyna_pkg_cat_id");
    } 
    boolean include = false;
    include = (associateAsOtherCategory == 0
        && (associateCategory != 0 && associateCategory == mainChargeCategory
            && ((String) assocChargeCatBean.get("limit_type")).equals("Q")));

    processbean.set("charge_group", (String) chargeBean.get("charge_group"));
    return include;
  }

  /**
   * Gets the charge bean.
   *
   * @param charges
   *          the charges
   * @param chargeId
   *          the charge id
   * @return the charge bean
   */
  private BasicDynaBean getChargeBean(List<BasicDynaBean> charges, String chargeId) {
    for (BasicDynaBean charge : charges) {
      String chargeid = (String) charge.get("charge_id");
      if (chargeid.equals(chargeId)) {
        return charge;
      } 
    }
    return null;
  }

  /**
   * Gets the package category bean.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @return the package category bean
   */
  public BasicDynaBean getPackageCategoryBean(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) {
    int servSubGrp = (Integer) processbean.get("service_sub_group_id");
    String chargeGroup = (String) processbean.get("charge_group");
    String chargeHead = (String) processbean.get("charge_head");
    String chargeId = (String) processbean.get("charge_id");
    String actDescId = processbean.get("act_description_id") != null
        ? (String) processbean.get("act_description_id") : null;
    String actDesc = processbean.get("act_description") != null
        ? (String) processbean.get("act_description") : null;
    String serviceSubGrp = "*";
    String serviceGrp = "*";

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("service_sub_group_id", servSubGrp);
    BasicDynaBean subgrpbean = serviceSubGroupService.findByPk(key);

    if (subgrpbean != null) {
      serviceSubGrp = ((Integer) subgrpbean.get("service_sub_group_id")).toString();
      serviceGrp = ((Integer) subgrpbean.get("service_group_id")).toString();
    }

    log.debug("Find charge rule: charge group: " + chargeGroup + "; charge head: " + chargeHead
        + "; service group: " + serviceGrp + "; service sub group: " + serviceSubGrp
        + "; activity id: " + actDescId + "; activity: " + actDesc);

    BasicDynaBean chargeRule = null;
    BasicDynaBean pkgCategory = null;

    Map<String, String> evalMap = createRuleMap(processbean, centerid, serviceSubGrp, serviceGrp,
        actDescId, actDesc);

    for (BasicDynaBean rule : rules) {
      if (!ruleMatched(rule, evalMap)) {
        continue;
      }
      chargeRule = rule;
      break;
    }

    if (chargeRule != null) {

      log.debug("Found rule with priority: " + chargeRule.get("priority") + " for charge id: "
          + (String) processbean.get("charge_id"));

      int chargeCategory = (Integer) chargeRule.get("dyna_pkg_cat_id");
      for (BasicDynaBean pkgCatBean : dynaPkgDetails) {
        if ((Integer) pkgCatBean.get("dyna_pkg_cat_id") == chargeCategory) {
          pkgCategory = pkgCatBean;
          break;
        }
      }
    } else {
      log.warn("No rule found for: charge id: " + chargeId + "; charge group: " + chargeGroup
          + "; charge head: " + chargeHead + "; service group: " + serviceGrp
          + "; service sub group: " + serviceSubGrp 
          + "; activity id: " + actDescId + "; activity: "
          + actDesc);
    }
    return pkgCategory;
  }

  /**
   * Creates the rule map.
   *
   * @param processbean
   *          the processbean
   * @param centerid
   *          the centerid
   * @param serviceSubGrp
   *          the service sub grp
   * @param serviceGrp
   *          the service grp
   * @param actDescId
   *          the act desc id
   * @param actDesc
   *          the act desc
   * @return the map
   */
  private Map<String, String> createRuleMap(BasicDynaBean processbean, String centerid,
      String serviceSubGrp, String serviceGrp, String actDescId, String actDesc) {
    Map<String, String> evalMap = new HashMap<String, String>();
    evalMap.put("chargegroup_id", (String) processbean.get("charge_group"));
    evalMap.put("chargehead_id", (String) processbean.get("charge_head"));
    evalMap.put("service_group_id", serviceGrp);
    evalMap.put("service_sub_group_id", serviceSubGrp);
    evalMap.put("activity_type", actDesc);
    evalMap.put("activity_id", actDescId);
    evalMap.put("center_id", centerid);
    return evalMap;
  }

  /**
   * Rule matched.
   *
   * @param rule
   *          the rule
   * @param map
   *          the map
   * @return true, if successful
   */
  private boolean ruleMatched(BasicDynaBean rule, Map<String, String> map) {
    for (Map.Entry<String, String> entryMap : map.entrySet()) {
      String key = entryMap.getKey();
      String value = entryMap.getValue();
      String field = (String) rule.get(key);
      if (key.equals("activity_type")) {
        field = field.startsWith("_ALL_") ? "*" : field;
      }
      if ("*".equals(field) || field.equals(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Process inventory items.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @return true, if successful
   */
  private boolean processInventoryItems(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) {
    String chargeId = (String) processbean.get("charge_id");
    BasicDynaBean bac = billActivityService.getActivity(chargeId);
    if (bac == null || !bac.get("activity_code").equals("PHI")) {
      return true;
    } 
    int medicineId = new Integer((String) processbean.get("act_description_id"));
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("medicine_id", medicineId);
    BasicDynaBean storeitembean = itemService.findByPk(filterMap);
    int medCategoryId = (Integer) storeitembean.get("med_category_id");
    String medCategory = new Integer(medCategoryId).toString();
    processbean.set("act_description", medCategory);
    // Set package included qty & amount
    setItemIncludedAmountAndQty(processbean, dynaPkgDetails, rules, centerid);

    return true;
  }

  /**
   * Process pharmacy items.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @return true, if successful
   */
  private boolean processPharmacyItems(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) {
    String chargeId = (String) processbean.get("charge_id");

    HashMap<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("charge_id", chargeId);
    BasicDynaBean sale = salesService.findByKey(filterMap);
    if (sale == null) {
      return true;
    } 
    String saleId = (String) sale.get("sale_id");
    BigDecimal amountIncluded = BigDecimal.ZERO;

    String saletype = (String) sale.get("type");
    List<BasicDynaBean> saleItems = salesService.listAllDetails("sale_id", saleId, "sale_item_id");

    if (saletype.equals("S")) {
      for (BasicDynaBean saleitem : saleItems) {
        filterMap = null;
        if (((String) saleitem.get("package_finalized")).equals("Y")) {
          amountIncluded = amountIncluded.add((BigDecimal) saleitem.get("amount_included"));
          continue;
        }
        int medicineId = (Integer) saleitem.get("medicine_id");
        filterMap = new HashMap<String, Object>();
        filterMap.put("medicine_id", medicineId);
        BasicDynaBean storeitembean = itemService.findByPk(filterMap);
        int medCategoryId = (Integer) storeitembean.get("med_category_id");
        int serviceSubGroupId = (Integer) storeitembean.get("service_sub_group_id");
        String medCategory = new Integer(medCategoryId).toString();

        String medId = new Integer(medicineId).toString();

        // TAXATION changes
        BigDecimal taxAmt = salesService.getTaxAmt((Integer) saleitem.get("sale_item_id"));

        processbean.set("act_description_id", medId);
        processbean.set("act_description", medCategory);
        processbean.set("service_sub_group_id", serviceSubGroupId);
        processbean.set("amount",
            ((BigDecimal) saleitem.get("amount")).add((BigDecimal) saleitem.get("return_amt")));
        processbean.set("act_quantity",
            ((BigDecimal) saleitem.get("quantity")).add((BigDecimal) saleitem.get("return_qty")));
        // TAXATION
        // processbean.set("insurance_claim_amount",
        // ((BigDecimal)saleitem.get("insurance_claim_amt")).
        // add((BigDecimal)saleitem.get("return_insurance_claim_amt")));
        BasicDynaBean saleClaimDetailsBean = (BasicDynaBean) salesClaimDetailsService
            .getTaxAmtInsuranceAmt((Integer) saleitem.get("sale_item_id"));
        if (null != saleClaimDetailsBean && null != saleClaimDetailsBean.get("insurance_claim_amt")
            && null != saleClaimDetailsBean.get("tax_amt")) {
          processbean.set("insurance_claim_amount",
              ((BigDecimal) saleClaimDetailsBean.get("insurance_claim_amt"))
                  .add((BigDecimal) saleClaimDetailsBean.get("tax_amt")));
        } else {
          processbean.set("insurance_claim_amount", BigDecimal.ZERO);
        }

        processbean.set("amount_included", BigDecimal.ZERO);
        processbean.set("qty_included", BigDecimal.ZERO);

        // Set package included qty & amount
        setItemIncludedAmountAndQty(processbean, dynaPkgDetails, rules, centerid);

        // Copy the amount included to sale item
        saleitem.set("amount_included", (BigDecimal) processbean.get("amount_included"));
        saleitem.set("qty_included", (BigDecimal) processbean.get("qty_included"));

        amountIncluded = amountIncluded.add((BigDecimal) processbean.get("amount_included"));
        salesService.updateDetails(saleitem);

      }

    } else {
      // Pharmacy returns amount & qty included is zero.
      for (BasicDynaBean saleitem : saleItems) {
        saleitem.set("amount_included", BigDecimal.ZERO);
        saleitem.set("qty_included", BigDecimal.ZERO);
        salesService.updateDetails(saleitem);
      }
    }

    processbean.set("amount_included", amountIncluded);
    processbean.set("qty_included", BigDecimal.ZERO);
    return true;
  }

  /**
   * Sets the item included amount and qty.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   */
  private void setItemIncludedAmountAndQty(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) {
    // Get the category to which the item belongs and process according to
    // the limit type (Amount/Quantity/Unlimited)
    BasicDynaBean pkgCatBean = getPackageCategoryBean(processbean, dynaPkgDetails, rules, centerid);
    if (pkgCatBean == null) {
      processbean.set("amount_included", new BigDecimal(0));
      processbean.set("qty_included", new BigDecimal(0));
      return;
    }
    int categoryId = (Integer) pkgCatBean.get("dyna_pkg_cat_id");

    processbean.set("package_category", categoryId);

    if (((String) pkgCatBean.get("limit_type")).equals("Q")) {

      // Set the included qty & amount if item is eligible to be included.
      setProcessChargeIncludedQuantity(processbean);
      setDynaPkgQuantity(processbean);

    } else if (((String) pkgCatBean.get("limit_type")).equals("A")) {
      // Set the included amount if item is eligible to be included.
      setProcessChargeIncludedAmount(processbean);
      setDynaPkgLimits(processbean);

    } else if (((String) pkgCatBean.get("limit_type")).equals("U")) {

      setProcessChargeUnlimitedIncludedQuantity(processbean, dynaPkgDetails, categoryId);
    }
  }

  /**
   * Sets the process charge included quantity.
   *
   * @param processbean
   *          the new process charge included quantity
   */
  public void setProcessChargeIncludedQuantity(BasicDynaBean processbean) {

    // Consider amount included as zero, then set included amount based on eligibility.
    processbean.set("amount_included", new BigDecimal(0));

    // Consider qty included as zero, then set included qty based on eligibility.
    processbean.set("qty_included", new BigDecimal(0));

    BigDecimal packageQty = pkgQuantityMap.get((Integer) processbean.get("package_category"));

    BigDecimal includedQty = getProcessChargeIncludedQuantity(packageQty, processbean);

    BigDecimal actQty = (BigDecimal) processbean.get("act_quantity");
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal amountIncluded = BigDecimal.ZERO;

    if ((Boolean) processbean.get("margin_claimable")) {
      amount = (BigDecimal) processbean.get("insurance_claim_amount");
    } else {
      amount = (BigDecimal) processbean.get("amount");
    } 
    if (includedQty.compareTo(BigDecimal.ZERO) != 0) {
      amountIncluded = amount.subtract(((actQty.subtract(includedQty)).multiply(amount))
          .divide(actQty, BigDecimal.ROUND_HALF_UP));
      processbean.set("qty_included", includedQty);
      processbean.set("amount_included", amountIncluded);
    }
  }

  /**
   * Gets the process charge included quantity.
   *
   * @param packageQty
   *          the package qty
   * @param charge
   *          the charge
   * @return the process charge included quantity
   */
  public BigDecimal getProcessChargeIncludedQuantity(BigDecimal packageQty, BasicDynaBean charge) {
    BigDecimal includedQty = BigDecimal.ZERO;

    if (packageQty == null) {
      return includedQty;
    }
    BigDecimal qty = (BigDecimal) charge.get("act_quantity");
    includedQty = packageQty.min(qty); // Mimimum of package quantity and charge quantity
    return includedQty;
  }

  /**
   * Sets the dyna pkg quantity.
   *
   * @param processbean
   *          the new dyna pkg quantity
   */
  public void setDynaPkgQuantity(BasicDynaBean processbean) {

    BigDecimal qty = processbean.get("qty_included") == null ? BigDecimal.ZERO
        : (BigDecimal) processbean.get("qty_included");

    BigDecimal packageQty = pkgQuantityMap.get((Integer) processbean.get("package_category"));
    if (packageQty != null) {
      pkgQuantityMap.put((Integer) processbean.get("package_category"), packageQty.subtract(qty));
    } 
  }

  /**
   * Sets the process charge included amount.
   *
   * @param processbean
   *          the new process charge included amount
   */
  public void setProcessChargeIncludedAmount(BasicDynaBean processbean) {

    // Consider amount included as zero, then set included amount based on eligibility.
    processbean.set("amount_included", new BigDecimal(0));

    // Consider qty included as zero, then set included qty based on eligibility.
    processbean.set("qty_included", new BigDecimal(0));

    BigDecimal catLimit = pkgLimitMap.get((Integer) processbean.get("package_category"));

    BigDecimal includedAmount = getProcessChargeIncludedAmount(catLimit, processbean);
    processbean.set("amount_included", includedAmount);
  }

  /**
   * Gets the process charge included amount.
   *
   * @param limit
   *          the limit
   * @param processbean
   *          the processbean
   * @return the process charge included amount
   */
  public BigDecimal getProcessChargeIncludedAmount(BigDecimal limit, BasicDynaBean processbean) {
    BigDecimal includedAmt = BigDecimal.ZERO;

    if (limit == null) {
      return includedAmt;
    }
    BigDecimal amount = BigDecimal.ZERO;
    if ((Boolean) processbean.get("margin_claimable")) {
      amount = (BigDecimal) processbean.get("insurance_claim_amount");
    } else {
      amount = (BigDecimal) processbean.get("amount");
    }  
    includedAmt = limit.min(amount); // Mimimum of package limit and charge amount

    return includedAmt;
  }

  /**
   * Sets the dyna pkg limits.
   *
   * @param processbean
   *          the new dyna pkg limits
   */
  public void setDynaPkgLimits(BasicDynaBean processbean) {

    BigDecimal amount = (BigDecimal) processbean.get("amount_included");

    BigDecimal limit = pkgLimitMap.get(processbean.get("package_category"));
    if (limit != null) {
      pkgLimitMap.put((Integer) processbean.get("package_category"), limit.subtract(amount));
    } 
  }

  /**
   * Checks if is unlimited category included.
   *
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param categoryId
   *          the category id
   * @return true, if is unlimited category included
   */
  private boolean isUnlimitedCategoryIncluded(List<BasicDynaBean> dynaPkgDetails, int categoryId) {

    for (BasicDynaBean category : dynaPkgDetails) {
      int categoryid = (Integer) category.get("dyna_pkg_cat_id");
      if (categoryid == categoryId) {
        return (((String) category.get("pkg_included")).equals("Y"));
      } 
    }
    return false;
  }

  /**
   * Sets the process charge unlimited included quantity.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param categoryId
   *          the category id
   */
  private void setProcessChargeUnlimitedIncludedQuantity(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, int categoryId) {
    BigDecimal qtyIncluded = BigDecimal.ZERO;
    BigDecimal amountIncluded = BigDecimal.ZERO;

    boolean included = isUnlimitedCategoryIncluded(dynaPkgDetails, categoryId);

    if (included) {
      qtyIncluded = (BigDecimal) processbean.get("act_quantity");

      if ((Boolean) processbean.get("margin_claimable")) {
        amountIncluded = (BigDecimal) processbean.get("insurance_claim_amount");
      } else {
        amountIncluded = (BigDecimal) processbean.get("amount");
      } 
    }

    processbean.set("qty_included", qtyIncluded);
    processbean.set("amount_included", amountIncluded);
  }

  /**
   * Update ins amt for items.
   *
   * @param charge
   *          the charge
   * @param billNo
   *          the bill no
   * @param priClaimAmt
   *          the pri claim amt
   * @param secClaimAmt
   *          the sec claim amt
   * @param planIds
   *          the plan ids
   * @param visitId
   *          the visit id
   */
  private void updateInsAmtForItems(BasicDynaBean charge, String billNo, BigDecimal priClaimAmt,
      BigDecimal secClaimAmt, int[] planIds, String visitId) {

    String chargeHead = (String) charge.get("charge_head");
    if (chargeHead.equals("INVRET")) {
      return;
    }
    charge.set("insurance_claim_amount", priClaimAmt.add(secClaimAmt));

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("charge_id", charge.get("charge_id"));
    keys.put("bill_no", billNo);
    billChargeService.update(charge, keys);

    if (null == planIds) {
      return;
    }
    for (int i = 0; i < planIds.length; i++) {
      BasicDynaBean bean = billChargeClaimService.getBean();
      String claimId = billChargeClaimService.getClaimId(planIds[i], billNo, visitId);
      keys.put("claim_id", claimId);
      bean.set("insurance_claim_amt", i == 0 ? priClaimAmt : secClaimAmt);
      billChargeClaimService.update(bean, keys);
    }
  }

  /**
   * Copy charge refs incuded quantity.
   *
   * @param maincharge
   *          the maincharge
   * @param charges
   *          the charges
   * @param marginClaimable
   *          the margin claimable
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   */
  // Copy the included quantity from main charge to all charge refs.
  private void copyChargeRefsIncudedQuantity(BasicDynaBean maincharge, List<BasicDynaBean> charges,
      boolean marginClaimable, BasicDynaBean processbean, List<BasicDynaBean> dynaPkgDetails,
      List<BasicDynaBean> rules, String centerid) {
    for (BasicDynaBean charge : charges) {
      String chargeHead = (String) charge.get("charge_head");
      String mainChargeId = (String) maincharge.get("charge_id");
      String chargeRef = (String) charge.get("charge_ref");
      BigDecimal includedQty = (BigDecimal) maincharge.get("qty_included");
      BigDecimal mainChargeQty = (BigDecimal) maincharge.get("act_quantity");

      if (!mainChargeId.equals(chargeRef)) {
        continue;
      }

      if (includedQty.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }

      /*
       * Qty is copied to all charge refs if they are included with main charge. Even though BYBED,
       * LTAX and STAX have charge refs, these are considered as spl. cases. User can edit
       * qmount/qty in bill screen.
       */
      if (includedCharges.contains(chargeHead)) {
        BasicDynaBean processingbean = getProcessingChargeBean();
        processingbean.set("margin_claimable", marginClaimable);
        setProcessingAttributes(charge, processingbean);
        setActivityBedType(processingbean);
        boolean include = includeAssociateWithMainCharge(charges, processingbean, dynaPkgDetails,
            rules, centerid);

        if (!include) {
          continue;
        }
      }

      BigDecimal actQty = (BigDecimal) charge.get("act_quantity");
      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal amountIncluded = BigDecimal.ZERO;

      if (marginClaimable) {
        amount = (BigDecimal) charge.get("insurance_claim_amount");
      } else {
        amount = (BigDecimal) charge.get("amount");
      } 
      // Included luxury tax or service tax, included amount is calculated based on main charge
      // included qty.
      if (chargeHead.equals(BillChargeService.CH_LUXURY_TAX)
          || chargeHead.equals(BillChargeService.CH_SERVICE_TAX)) {

        amountIncluded = ConversionUtils.divide(amount.multiply(includedQty), mainChargeQty);
        if (amount.compareTo(amountIncluded) == 0) {
          charge.set("qty_included", actQty);
        } else {
          charge.set("qty_included", BigDecimal.ZERO); 
        }  
        charge.set("amount_included", amountIncluded);
      } else {
        amountIncluded = ConversionUtils.divide(amount.multiply(includedQty), actQty);
        charge.set("qty_included", includedQty);
        charge.set("amount_included", amountIncluded);
      }
    }
  }

  /**
   * Gets the package category bean old.
   *
   * @param processbean
   *          the processbean
   * @param dynaPkgDetails
   *          the dyna pkg details
   * @param rules
   *          the rules
   * @param centerid
   *          the centerid
   * @return the package category bean old
   */
  public BasicDynaBean getPackageCategoryBeanOld(BasicDynaBean processbean,
      List<BasicDynaBean> dynaPkgDetails, List<BasicDynaBean> rules, String centerid) {
    int servSubGrp = (Integer) processbean.get("service_sub_group_id");
    Map<String, Object> key = new HashMap<String, Object>();
    key.put("service_sub_group_id", servSubGrp);
    BasicDynaBean subgrpbean = serviceSubGroupService.findByPk(key);
    String serviceSubGrp = "*";
    String serviceGrp = "*";
    if (subgrpbean != null) {
      serviceSubGrp = ((Integer) subgrpbean.get("service_sub_group_id")).toString();
      serviceGrp = ((Integer) subgrpbean.get("service_group_id")).toString();
    }

    String chargeId = (String) processbean.get("charge_id");
    String chargeGroup = (String) processbean.get("charge_group");
    String chargeHead = (String) processbean.get("charge_head");
    String actDescId = processbean.get("act_description_id") != null
        ? (String) processbean.get("act_description_id") : null;
    String actDesc = processbean.get("act_description") != null
        ? (String) processbean.get("act_description") : null;

    log.debug("Find charge rule: charge group: " + chargeGroup + "; charge head: " + chargeHead
        + "; service group: " + serviceGrp + "; service sub group: " + serviceSubGrp
        + "; activity id: " + actDescId + "; activity: " + actDesc);

    BasicDynaBean chargeRule = null;
    BasicDynaBean pkgCategory = null;

    for (BasicDynaBean rule : rules) {

      if (!isChargeGroupMatch(rule, chargeGroup)) {
        continue;
      }
      if (!isChargeHeadMatch(rule, chargeHead)) {
        continue;
      }
      if (!isChargeServiceGroupMatch(rule, serviceGrp)) {
        continue;
      }
      if (!isChargeServiceSubGroupMatch(rule, serviceSubGrp)) {
        continue;
      }
      if (!isChargeActivityTypeMatch(rule, actDesc)) {
        continue;
      }
      if (!isChargeActivityIdMatch(rule, actDescId)) {
        continue;
      }
      if (!isCenterMatch(rule, centerid)) {
        continue;
      }
      chargeRule = rule;
      break;
    }

    if (chargeRule != null) {

      log.debug("Found rule with priority: " + chargeRule.get("priority") + " for charge id: "
          + (String) processbean.get("charge_id"));

      int chargeCategory = (Integer) chargeRule.get("dyna_pkg_cat_id");

      for (BasicDynaBean pkgCatBean : dynaPkgDetails) {
        if ((Integer) pkgCatBean.get("dyna_pkg_cat_id") == chargeCategory) {
          pkgCategory = pkgCatBean;
          break;
        }
      }
    } else {
      log.warn("No rule found for: charge id: " + chargeId + "; charge group: " + chargeGroup
          + "; charge head: " + chargeHead + "; service group: " + serviceGrp
          + "; service sub group: " + serviceSubGrp + "; activity id: " + actDescId + "; activity: "
          + actDesc);
    }
    return pkgCategory;
  }

  /**
   * Checks if is charge group match.
   *
   * @param rule
   *          the rule
   * @param chargeGroup
   *          the charge group
   * @return true, if is charge group match
   */
  private boolean isChargeGroupMatch(BasicDynaBean rule, String chargeGroup) {
    String categoryGrp = (String) rule.get("chargegroup_id");
    if ("*".equals(categoryGrp) || categoryGrp.equals(chargeGroup)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is charge head match.
   *
   * @param rule
   *          the rule
   * @param chargeHead
   *          the charge head
   * @return true, if is charge head match
   */
  private boolean isChargeHeadMatch(BasicDynaBean rule, String chargeHead) {
    String categoryHead = (String) rule.get("chargehead_id");
    if ("*".equals(categoryHead) || categoryHead.equals(chargeHead)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is charge service group match.
   *
   * @param rule
   *          the rule
   * @param serviceGroup
   *          the service group
   * @return true, if is charge service group match
   */
  private boolean isChargeServiceGroupMatch(BasicDynaBean rule, String serviceGroup) {
    String categoryServGrp = (String) rule.get("service_group_id");
    if ("*".equals(categoryServGrp) || categoryServGrp.equals(serviceGroup)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is charge service sub group match.
   *
   * @param rule
   *          the rule
   * @param serviceSubGroup
   *          the service sub group
   * @return true, if is charge service sub group match
   */
  private boolean isChargeServiceSubGroupMatch(BasicDynaBean rule, String serviceSubGroup) {
    String categoryServSubGrp = (String) rule.get("service_sub_group_id");
    if ("*".equals(categoryServSubGrp) || categoryServSubGrp.equals(serviceSubGroup)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is charge activity type match.
   *
   * @param rule
   *          the rule
   * @param activityType
   *          the activity type
   * @return true, if is charge activity type match
   */
  private boolean isChargeActivityTypeMatch(BasicDynaBean rule, String activityType) {
    String categoryActType = (String) rule.get("activity_type");
    categoryActType = categoryActType.startsWith("_ALL_") ? "*" : categoryActType;
    if ("*".equals(categoryActType) || categoryActType.equals(activityType)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is charge activity id match.
   *
   * @param rule
   *          the rule
   * @param activityId
   *          the activity id
   * @return true, if is charge activity id match
   */
  private boolean isChargeActivityIdMatch(BasicDynaBean rule, String activityId) {
    String categoryActId = (String) rule.get("activity_id");
    if ("*".equals(categoryActId) || categoryActId.equals(activityId)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if is center match.
   *
   * @param rule
   *          the rule
   * @param centerId
   *          the center id
   * @return true, if is center match
   */
  private boolean isCenterMatch(BasicDynaBean rule, String centerId) {
    String categoryCenter = (String) rule.get("center_id");
    if ("*".equals(categoryCenter) || categoryCenter.equals(centerId)) {
      return true;
    }
    return false;
  }

}
