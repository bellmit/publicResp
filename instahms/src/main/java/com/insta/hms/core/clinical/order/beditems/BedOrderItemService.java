package com.insta.hms.core.clinical.order.beditems;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.DiscountService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.mdm.bedtypes.BedTypeRepository;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Need to check prefix and prefix_id passed. Currently not in used. Once Start using in order post
 * request, then it is required
 * 
 * @author ritolia
 *
 */
@Service
@Order(key = "Bed", value = { "Bed", "ICU" }, prefix = "beds")
public class BedOrderItemService extends OrderItemService {

  @LazyAutowired
  private DiscountService discountService;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  @Autowired
  private BedOrderItemRepository bedOrderItemRepository;

  @LazyAutowired
  private BedTypeRepository bedTypeRepository;

  @LazyAutowired
  private IpPreferencesService ipPreferencesService;

  @LazyAutowired
  private DiscountPlanService discountPlanService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private SessionService sessionService;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  private static final String ALLOW_RATE_INCREASE = "allow_rate_increase";
  private static final String ALLOW_RATE_DECREASE = "allow_rate_decrease";
  private static final String FROM_DATE = "from_date";
  private static final String TO_DATE = "to_date";
  private static final String ACT_UNIT = "act_unit";
  private static final String BILL_NO = "bill_no";
  private static final String ACT_RATE_PLAN_ITEM_CODE = "act_rate_plan_item_code";
  private static final String CODE_TYPE = "code_type";

  public BedOrderItemService(BedOrderItemRepository repository) {
    super(repository, "", "");
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    paramMap.put("is_bystander", false);
    paramMap.put("only_main_charges", false);
    paramMap.put("day_care_status", "N");
    paramMap.put("is_first_bed", true);
    paramMap.put("bed_state", "O");
    paramMap.put("ret_bed_days_hours", null);
    paramMap.put(BILL_NO, null);
    paramMap.put("disc_cat_id", 0);

    BasicDynaBean masterCharge;
    String type = (String) paramMap.get("type");
    if ("Bed".equals(type)) {
      masterCharge = getNormalBedChargesBean((String) paramMap.get("id"), ratePlanId);
    } else {
      masterCharge = getIcuBedChargesBean((String) paramMap.get("id"), bedType, ratePlanId);
    }
    
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    return getChargesList(masterCharge, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
        paramMap);
  }

  @Override
  public BasicDynaBean getMasterChargesBean(Object bedType, String billingBedType,
      String ratePlanId, Integer centerId) {
    return this.isIcuBedType((String) bedType)
      ? getIcuBedChargesBean((String) bedType, billingBedType, ratePlanId)
      : getNormalBedChargesBean((String) bedType, ratePlanId);
  }

  public BasicDynaBean getNormalBedChargesBean(String bedType, String ratePlanId) {
    return bedOrderItemRepository.getNormalBedChargesBean(bedType, ratePlanId);
  }

  private BasicDynaBean getIcuBedChargesBean(String bedType, String previousBedType, String orgId) {

    if (isIcuBedType(previousBedType)) {
      return bedOrderItemRepository.getIcuBedChargesBean("GENERAL", orgId, bedType);
    } else {
      return bedOrderItemRepository.getIcuBedChargesBean(previousBedType, orgId, bedType);
    }
  }

  public List<BasicDynaBean> getAllIcuBedChargesBean(String bedType, String orgId) {
    return bedOrderItemRepository.getAllIcuBedChargesBean(bedType, orgId);
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object billingBedType,
      String ratePlanId) {
    return getAllNormalBedChargesBean(ratePlanId);
  }

  public List<BasicDynaBean> getAllNormalBedChargesBean(String ratePlanId) {
    return bedOrderItemRepository.getAllNormalBedChargesBean(ratePlanId);
  }

  private boolean isIcuBedType(String bedType) {
    BasicDynaBean icuBedBean = bedOrderItemRepository.findByKey("bed_type_name", bedType);
    return "Y".equals(icuBedBean.get("is_icu"));
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean bedCharge, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    Timestamp from = (Timestamp) otherParams.get(FROM_DATE);
    Timestamp to = (Timestamp) otherParams.get(TO_DATE);
    String type = ((String) otherParams.get("type")).toUpperCase();
    String daycareStatus = (String) otherParams.get("day_care_status");
    Boolean isFirstBed = (Boolean) otherParams.get("is_first_bed");
    BigDecimal[] retBedDaysHours = (BigDecimal[]) otherParams.get("ret_bed_days_hours");
    Integer discCatId = (Integer) otherParams.get("disc_cat_id");
    discCatId = discCatId != null ? (Integer) discCatId : 0;
    List<BasicDynaBean> discountPlanDetails = discountPlanService.listAllDiscountPlanDetails(null,
        "discount_plan_id", discCatId, "priority");

    String bedType = (String) (type.equals("ICU") ? bedCharge.get("intensive_bed_type")
        : bedCharge.get("bed_type"));
    BasicDynaBean bedTypeBean = bedTypeRepository.findByKey("bed_type_name", bedType);
    BasicDynaBean ipPrefs = ipPreferencesService.getAllPreferences();

    BigDecimal[] bedDaysHours = getBedDaysHours(from, to, quantity, daycareStatus, isFirstBed,
        (String) bedTypeBean.get("is_icu"), ipPrefs);

    BigDecimal bedDays = bedDaysHours[0];
    BigDecimal bedHours = bedDaysHours[1];

    if (retBedDaysHours != null) {
      retBedDaysHours[0] = bedDays;
      retBedDaysHours[1] = bedHours;
    }

    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    BigDecimal bedAmount = BigDecimal.ZERO;

    Map<String, Object> chargeHeadkeyMap = new HashMap<String, Object>();

    Integer insuranceCategoryId = 0;
    if (bedTypeBean.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) bedTypeBean.get("insurance_category_id");
    }

    /*
     * Daily bed charge: One daily charge OR one hourly charge is added
     */
    
    String billNo = (String) otherParams.get(BILL_NO);
    Boolean isBystander = (Boolean) otherParams.get("is_bystander");
    String visitType = (String) otherParams.get("visit_type");

    Map<String, Object> returnValues = getBedCharges(bedCharge, type, bedType, isBystander, bedDays,
        daycareStatus, insuranceCategoryId, isInsurance, discountPlanDetails, billNo, bedHours,
        visitType, discCatId, from, to, ipPrefs);
    chargesList = (List<BasicDynaBean>) returnValues.get("chargesList");
    bedAmount = (BigDecimal) returnValues.get("bedAmount");
    BigDecimal totalAmount = BigDecimal.ZERO;
    totalAmount = (BigDecimal) returnValues.get("totalAmount");

    if (chargesList.size() == 0) {
      return chargesList;
    }
    /*
     * Associated charges for non-bystander, non-daycare beds provided !onlyMainCharges: nurse, duty
     * doctor, professional charge: only daily charges added if the corresponding rate is non-zero.
     */
    Boolean onlyMainCharges = (Boolean) otherParams.get("only_main_charges");
    returnValues = getBedAssociatedCharges(bedCharge, type, bedType, bedDays, daycareStatus,
        insuranceCategoryId, isInsurance, discountPlanDetails, billNo, visitType, discCatId, from,
        to, onlyMainCharges);

    totalAmount = totalAmount.add((BigDecimal) returnValues.get("totalAmount"));
    chargesList.addAll((List<BasicDynaBean>) returnValues.get("chargesList"));

    /*
     * Luxury tax
     */
    BigDecimal taxPer = (BigDecimal) bedCharge.get("luxary_tax");

    if (taxPer.compareTo(BigDecimal.ZERO) > 0) {
      chargeHeadkeyMap.put("chargehead_id", "PC" + type);
      BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(chargeHeadkeyMap);
      BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
      BigDecimal taxAmount = ((String) gprefs.get("luxary_tax_applicable_on")).equals("B")
          ? bedAmount : totalAmount;
      taxAmount = taxAmount.multiply(taxPer).divide(new BigDecimal(100), 2);

      BasicDynaBean ch = billChargeService.setBillChargeBean("TAX", "LTAX", taxAmount,
          BigDecimal.ONE, BigDecimal.ZERO, bedType, "On Bed Charges (" + bedType + ")", null,
          (Integer) chargeHeadBean.get("service_sub_group_id"), insuranceCategoryId, isInsurance);
      ch.set(ALLOW_RATE_INCREASE, true);
      ch.set(ALLOW_RATE_DECREASE, true);
      ch.set(FROM_DATE, from);
      ch.set(TO_DATE, to);
      if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
        ch.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
      }
      chargesList.add(ch);
    }

    return chargesList;
  }

  /**
   * Returns the list of bed item details by passing there id.
   * @param entityIdList the entityIdList
   * @param paramMap the paramMap
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return bedOrderItemRepository.getItemDetails(entityIdList);

  }

  /**
   * insert Order Charges.
   * @param chargesList the chargesList
   * @param activityCode the activityCode
   * @param prescId the prescId
   * @param remarks the remarks
   * @param presDrId the presDrId
   * @param postedDate the postedDate
   * @param activityConducted the activityConducted
   * @param conductedOn the conductedOn
   * @param commonOrderId the commonOrderId
   * @param preAuthIds the preAuthIds
   * @param preAuthModeIds the preAuthModeIds
   * @param bill the bill
   * @param planIds the planIds
   */
  public void insertOrderCharges(List<BasicDynaBean> chargesList, String activityCode, int prescId,
      String remarks, String presDrId, Timestamp postedDate, String activityConducted,
      Timestamp conductedOn, Integer commonOrderId, String[] preAuthIds, Integer[] preAuthModeIds,
      BasicDynaBean bill, int[] planIds) {

    String billNo = (String) bill.get("bill_no");
    String username = (String) bill.get("username");

    Integer billDiscountPlanId = 0;
    if (bill.get("discount_category_id") != null) {
      billDiscountPlanId = (Integer) bill.get("discount_category_id");
    }

    for (BasicDynaBean charge : chargesList) {
      if (postedDate.getTime() < ((java.sql.Timestamp) bill.get("open_date")).getTime()) {
        postedDate = (java.sql.Timestamp) bill.get("open_date");
      }
      setOrderAttributes(charge, billChargeService.getNextPrefixedId(), billNo, username, remarks,
          presDrId, postedDate);
      if (((BigDecimal) charge.get("discount")).compareTo(BigDecimal.ZERO) > 0) {
        charge.set("overall_discount_amt", (BigDecimal) charge.get("discount"));
        charge.set("overall_discount_auth", -1);
      }
      if ((Boolean) charge.get("allow_discount")) {
        discountService.applyDiscountRule(charge, billDiscountPlanId,
            (String) bill.get("visit_type"));
      }
      charge.set("bill_no", billNo);
      charge.set("order_number", commonOrderId);
      charge.set("conducted_datetime", conductedOn);
      if (preAuthIds != null) {
        charge.set("prior_auth_id", preAuthIds[0]);
      }
      if (preAuthModeIds != null) {
        charge.set("prior_auth_mode_id", preAuthModeIds[0]);
      }
      charge.set("activity_conducted", activityConducted);
    }

    for (BasicDynaBean charge : chargesList.subList(1, chargesList.size())) {
      charge.set("charge_ref", chargesList.get(0).get("charge_id"));
      charge.set("hasactivity", true);
      if (remarks != null && !charge.get("charge_head").equals("ANATOPE")) {
        charge.set("act_remarks", chargesList.get(0).get("act_remarks"));
      }
    }

    BasicDynaBean mainCharge = chargesList.get(0);
    mainCharge.set("hasactivity", prescId != 0);

    List<BasicDynaBean> activityChargeList = billActivityChargeService
        .getActivityChargeList(chargesList, prescId, activityCode, activityConducted);

    billChargeService.batchInsert(chargesList);
    billActivityChargeService.batchInsert(activityChargeList);
    if ((Boolean) bill.get("is_tpa")) {
      String patientId = (String) bill.get("visit_id");
      billChargeClaimService.insertBillChargeClaims(chargesList, planIds, patientId, bill,
          preAuthIds, preAuthModeIds);
    }
  }

  @Override
  public void setOrderAttributes(BasicDynaBean billChargeBean, String chargeId, String billNo,
      String userName, String remarks, String presDrId, Timestamp postedDate) {
    billChargeBean.set("charge_id", chargeId);
    billChargeBean.set("bill_no", billNo);
    billChargeBean.set("username", userName);
    billChargeBean.set("user_remarks", remarks);
    billChargeBean.set("prescribing_dr_id", presDrId);
    billChargeBean.set("posted_date", postedDate);
    billChargeBean.set("mod_time", DateUtil.getCurrentTimestamp());
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * get Thresholds.
   * @param prefs the prefs
   * @param daysHours the daysHours
   * @param isFirstBed the isFirstBed
   * @param isICU the isICU
   * @return String
   */
  public static String[] getThresholds(BasicDynaBean prefs, int[] daysHours, boolean isFirstBed,
      String isICU) {
    String hrlyThreshHold = null;
    String halfDayThreshold = null;
    String dailyThreshold = null;
    if (isICU.equals("Y")) {
      // use days, hours and get number of days and hours to charge for
      // the bed.
      String prefix = isFirstBed ? "icu_" : "icu_bedshift_";
      hrlyThreshHold = (String) prefs.get(prefix + "hrly_charge_threshold");
      halfDayThreshold = (String) prefs.get(prefix + "halfday_charge_threshold");
      dailyThreshold = (String) prefs.get(prefix + "fullday_charge_threshold");
    } else {
      // use days, hours and get number of days and hours to charge for
      // the bed.
      String prefix = isFirstBed ? "" : "bedshift_";
      hrlyThreshHold = (String) prefs.get(prefix + "hrly_charge_threshold");
      halfDayThreshold = (String) prefs.get(prefix + "halfday_charge_threshold");
      dailyThreshold = (String) prefs.get(prefix + "fullday_charge_threshold");
    }
    return new String[] { hrlyThreshHold, halfDayThreshold, dailyThreshold };
  }

  /**
   * get Bed Days Hours.
   * @param from the from
   * @param to the to
   * @param quantity the quantity
   * @param daycareStatus the daycareStatus
   * @param isFirstBed the isFirstBed
   * @param icuStatus the icuStatus
   * @param prefs the prefs
   * @return BigDecimal
   */
  public static BigDecimal[] getBedDaysHours(Timestamp from, Timestamp to, BigDecimal quantity,
      String daycareStatus, boolean isFirstBed, String icuStatus, BasicDynaBean prefs) {

    int[] daysHours;

    if (from == null) {
      if (daycareStatus.equals("Y")) {
        daysHours = new int[] { 0, quantity.intValue() };
      } else {
        daysHours = DateUtil.getDaysHours(quantity);
      }
    } else if (daycareStatus.equals("Y")) {
      if (prefs.get("merge_beds").equals("Y")) {
        // returns days, hours for the given range with out round off
        daysHours = new int[] { 0, DateUtil.getHours(from, to, false) };
      } else {
        // returns days, hours for the given range
        daysHours = new int[] { 0, DateUtil.getHours(from, to) };
      }
    } else {
      if (prefs.get("merge_beds").equals("Y")) {
        daysHours = DateUtil.getDaysHours(from, to, false);
      } else {
        daysHours = DateUtil.getDaysHours(from, to);
      }
    }

    if (daycareStatus.equals("Y")) {
      return new BigDecimal[] { BigDecimal.ZERO, new BigDecimal(daysHours[1]) };
    }
    String[] thresholds = getThresholds(prefs, daysHours, isFirstBed, icuStatus);

    String hourlyThreshold = thresholds[0];
    String halfDayThreshold = thresholds[1];
    String fullDayThreshold = thresholds[2];

    int fullDay = 24;
    if (!fullDayThreshold.equals("-")) {
      fullDay = Integer.parseInt(fullDayThreshold);
    }

    int halfDay = fullDay;
    if (!halfDayThreshold.equals("-")) {
      halfDay = Integer.parseInt(halfDayThreshold);
    }

    int hourly = halfDay;
    if (!hourlyThreshold.equals("-")) {
      hourly = Integer.parseInt(hourlyThreshold);
    }

    int days = daysHours[0];
    int hours = daysHours[1];
    BigDecimal bedDays = new BigDecimal(days).setScale(1);
    BigDecimal bedHours = BigDecimal.ZERO;

    if (hours > fullDay) {
      bedDays = bedDays.add(BigDecimal.ONE);
    } else if (hours > halfDay) {
      bedDays = bedDays.add(new BigDecimal("0.5"));
    } else if (hours > hourly) {
      bedHours = new BigDecimal(hours);
    }

    return new BigDecimal[] { bedDays, bedHours };
  }

  private Map<String, Object> getBedCharges(BasicDynaBean bedCharge, String type, String bedType,
      Boolean isBystander, BigDecimal bedDays, String daycareStatus, Integer insuranceCategoryId,
      Boolean isInsurance, List<BasicDynaBean> discountPlanDetails, String billNo,
      BigDecimal bedHours, String visitType, Integer discCatId, Timestamp from, Timestamp to,
      BasicDynaBean ipPrefs) {
    BasicDynaBean mainCharge = null;
    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    BigDecimal bedAmount = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;

    Map<String, Object> chargeHeadkeyMap = new HashMap<String, Object>();
    chargeHeadkeyMap.put("chargehead_id", isBystander ? "BYBDE" : "B" + type);

    BasicDynaBean mainChargeHeadBean = chargeHeadsService.findByPk(chargeHeadkeyMap);
    int mainServiceSubGroup = (Integer) mainChargeHeadBean.get("service_sub_group_id");

    if (isBystander && bedDays.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal bystanderBedCharges = BigDecimal.ZERO;
      BigDecimal bystanderBedDiscount = BigDecimal.ZERO;
      if (ipPrefs.get("bystander_bed_charges_applicable_on").equals("W")) {
        bystanderBedCharges = ((BigDecimal) bedCharge.get("bed_charge"))
            .add((BigDecimal) bedCharge.get("nursing_charge"))
            .add((BigDecimal) bedCharge.get("duty_charge"))
            .add((BigDecimal) bedCharge.get("maintainance_charge"));

        bystanderBedDiscount = ((BigDecimal) bedCharge.get("bed_charge_discount"))
            .add((BigDecimal) bedCharge.get("nursing_charge_discount"))
            .add((BigDecimal) bedCharge.get("duty_charge_discount"))
            .add((BigDecimal) bedCharge.get("maintainance_charge_discount"));
      } else {
        bystanderBedCharges = (BigDecimal) bedCharge.get("bed_charge");
        bystanderBedDiscount = (BigDecimal) bedCharge.get("bed_charge_discount");
      }

      mainCharge = billChargeService.setBillChargeBean(type, "BYBED", bystanderBedCharges, bedDays,
          bystanderBedDiscount.multiply(bedDays), bedType, bedType, null, mainServiceSubGroup,
          insuranceCategoryId, isInsurance);

      setChargeAttributes(mainCharge, "Days", billNo, true, true, from, to,
          (String) bedCharge.get("item_code"), (String) bedCharge.get("code_type"));

      discountPlanService.applyDiscountRule(mainCharge, discCatId, discountPlanDetails, visitType);

    } else if (daycareStatus.equals("Y")) {
      // day care: special handling based on slabs and incr rate
      int duration = bedHours.intValue();
      int minDuration = (Integer) ipPrefs.get("daycare_min_duration");
      int slab1Duration = (Integer) ipPrefs.get("daycare_slab_1_threshold");
      int slab2Duration = (Integer) ipPrefs.get("daycare_slab_2_threshold");
      int incrDuration = 1;

      if (minDuration == 0 && slab1Duration == 0 && slab2Duration == 0) {
        // only hourly charges: now we can have rate*hours = amount, qty
        // is Hours.
        BigDecimal rate = (BigDecimal) bedCharge.get("hourly_charge");
        BigDecimal discount = (BigDecimal) bedCharge.get("hourly_charge_discount");
        mainCharge = billChargeService.setBillChargeBean(type, "B" + type, rate, bedHours,
            discount.multiply(bedHours), bedType, bedType, null, mainServiceSubGroup,
            insuranceCategoryId, isInsurance);

        setChargeAttributes(mainCharge, "Hrs", billNo, true, true, from, to,
            (String) bedCharge.get("item_code"), (String) bedCharge.get("code_type"));

      } else {
        // rate is slab determined, quantity has to be 1
        BigDecimal minRate = (BigDecimal) bedCharge.get("daycare_slab_1_charge");
        BigDecimal minDiscount = (BigDecimal) bedCharge.get("daycare_slab_1_charge_discount");
        BigDecimal slab1Rate = (BigDecimal) bedCharge.get("daycare_slab_2_charge");
        BigDecimal slab1Discount = (BigDecimal) bedCharge.get("daycare_slab_2_charge_discount");
        BigDecimal slab2Rate = (BigDecimal) bedCharge.get("daycare_slab_3_charge");
        BigDecimal slab2Discount = (BigDecimal) bedCharge.get("daycare_slab_3_charge_discount");
        BigDecimal incrRate = (BigDecimal) bedCharge.get("hourly_charge");
        BigDecimal incrDiscount = (BigDecimal) bedCharge.get("hourly_charge_discount");

        BigDecimal charge = getDurationCharge(duration, minDuration, slab1Duration, slab2Duration,
            incrDuration, minRate, slab1Rate, slab2Rate, incrRate, false);
        BigDecimal discount = getDurationCharge(duration, minDuration, slab1Duration, slab2Duration,
            incrDuration, minDiscount, slab1Discount, slab2Discount, incrDiscount, false);

        mainCharge = billChargeService.setBillChargeBean(type, "B" + type, charge, BigDecimal.ONE,
            discount, bedType, bedType, null, mainServiceSubGroup, insuranceCategoryId,
            isInsurance);

        setChargeAttributes(mainCharge, "", billNo, true, true, from, to,
            (String) bedCharge.get("item_code"), (String) bedCharge.get("code_type"));
      }

      discountPlanService.applyDiscountRule(mainCharge, discCatId, discountPlanDetails, visitType);
    } else {
      // normal bed (or ICU): add a "Day" charge if num days is not 0
      if (bedDays.compareTo(BigDecimal.ZERO) > 0) {
        mainCharge = billChargeService.setBillChargeBean(type, "B" + type,
            (BigDecimal) bedCharge.get("bed_charge"), bedDays,
            ((BigDecimal) bedCharge.get("bed_charge_discount")).multiply(bedDays), bedType, bedType,
            null, mainServiceSubGroup, insuranceCategoryId, isInsurance);

        setChargeAttributes(mainCharge, "Days", billNo, true, true, from, to,
            (String) bedCharge.get("item_code"), (String) bedCharge.get("code_type"));

        discountPlanService.applyDiscountRule(mainCharge, discCatId, discountPlanDetails,
            visitType);
      }
    }

    if (mainCharge != null) {
      if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
        mainCharge.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
      }
      chargesList.add(mainCharge);
      bedAmount = (BigDecimal) mainCharge.get("amount");
      totalAmount = (BigDecimal) mainCharge.get("amount");
    }

    if (daycareStatus.equals("N") && bedHours.compareTo(BigDecimal.ZERO) > 0) {
      BasicDynaBean ch = billChargeService.setBillChargeBean(type,
          isBystander ? "BYBED" : "B" + type, (BigDecimal) bedCharge.get("hourly_charge"), bedHours,
          ((BigDecimal) bedCharge.get("hourly_charge_discount")).multiply(bedHours), bedType,
          bedType, null, mainServiceSubGroup, insuranceCategoryId, isInsurance);

      setChargeAttributes(ch, "Hrs", null, true, true, from, to, null, null);

      discountPlanService.applyDiscountRule(ch, discCatId, discountPlanDetails, visitType);

      if (mainCharge == null) {
        mainCharge = ch;
      }
      if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
        ch.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
      }
      chargesList.add(ch);
      bedAmount = bedAmount.add((BigDecimal) ch.get("amount"));
      totalAmount = totalAmount.add((BigDecimal) ch.get("amount"));
    }
    Map<String, Object> returnMap = new HashMap<String, Object>();
    returnMap.put("bedAmount", bedAmount);
    returnMap.put("totalAmount", totalAmount);
    returnMap.put("chargesList", chargesList);
    return returnMap;
  }

  private Map<String, Object> getBedAssociatedCharges(BasicDynaBean bedCharge, String type,
      String bedType, BigDecimal bedDays, String daycareStatus, Integer insuranceCategoryId,
      Boolean isInsurance, List<BasicDynaBean> discountPlanDetails, String billNo, String visitType,
      Integer discCatId, Timestamp from, Timestamp to, Boolean onlyMainCharges) {
    Map<String, Object> returnMap = new HashMap<String, Object>();
    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    Map<String, Object> chargeHeadkeyMap = new HashMap<String, Object>();

    if (daycareStatus.equals("N") && !onlyMainCharges && bedDays.compareTo(BigDecimal.ZERO) > 0) {

      if (((BigDecimal) bedCharge.get("nursing_charge")).compareTo(BigDecimal.ZERO) > 0) {

        chargeHeadkeyMap.put("chargehead_id", "NC" + type);
        BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(chargeHeadkeyMap);
        BasicDynaBean ch = billChargeService.setBillChargeBean(type, "NC" + type,
            (BigDecimal) bedCharge.get("nursing_charge"), bedDays,
            ((BigDecimal) bedCharge.get("nursing_charge_discount")).multiply(bedDays), bedType,
            bedType, null, (Integer) chargeHeadBean.get("service_sub_group_id"),
            insuranceCategoryId, isInsurance);

        setChargeAttributes(ch, "Days", billNo, true, true, from, to, null, null);

        discountPlanService.applyDiscountRule(ch, discCatId, discountPlanDetails, visitType);
        if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
          ch.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
        }
        chargesList.add(ch);
        totalAmount = totalAmount.add((BigDecimal) ch.get("amount"));
      }
      if (((BigDecimal) bedCharge.get("duty_charge")).compareTo(BigDecimal.ZERO) > 0) {
        chargeHeadkeyMap.put("chargehead_id", "DD" + type);
        BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(chargeHeadkeyMap);
        BasicDynaBean ch = billChargeService.setBillChargeBean(type, "DD" + type,
            (BigDecimal) bedCharge.get("duty_charge"), bedDays,
            ((BigDecimal) bedCharge.get("duty_charge_discount")).multiply(bedDays), bedType,
            bedType, null, (Integer) chargeHeadBean.get("service_sub_group_id"),
            insuranceCategoryId, isInsurance);

        setChargeAttributes(ch, "Days", billNo, true, true, from, to, null, null);

        discountPlanService.applyDiscountRule(ch, discCatId, discountPlanDetails, visitType);
        if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
          ch.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
        }
        chargesList.add(ch);
        totalAmount = totalAmount.add((BigDecimal) ch.get("amount"));
      }
      if (((BigDecimal) bedCharge.get("maintainance_charge")).compareTo(BigDecimal.ZERO) > 0) {
        chargeHeadkeyMap.put("chargehead_id", "PC" + type);
        BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(chargeHeadkeyMap);
        BasicDynaBean ch = billChargeService.setBillChargeBean(type, "PC" + type,
            (BigDecimal) bedCharge.get("maintainance_charge"), bedDays,
            ((BigDecimal) bedCharge.get("maintainance_charge_discount")).multiply(bedDays), bedType,
            bedType, null, (Integer) chargeHeadBean.get("service_sub_group_id"),
            insuranceCategoryId, isInsurance);

        setChargeAttributes(ch, "Days", billNo, true, true, from, to, null, null);
        ch.set("charge_head", "maintainance_charge");

        discountPlanService.applyDiscountRule(ch, discCatId, discountPlanDetails, visitType);
        if (bedCharge != null || bedCharge.get("billing_group_id") != null) {
          ch.set("billing_group_id", (Integer) bedCharge.get("billing_group_id"));
        }
        chargesList.add(ch);
        totalAmount = totalAmount.add((BigDecimal) ch.get("amount"));
      }
    }
    returnMap.put("totalAmount", totalAmount);
    returnMap.put("chargesList", chargesList);
    return returnMap;
  }

  private void setChargeAttributes(BasicDynaBean charge, String actUnit, String billNo,
      Boolean allowRateIncrease, Boolean allowRateDecrease, Timestamp from, Timestamp to,
      String actRatePlanItemCode, String codeType) {
    charge.set(ACT_UNIT, actUnit);
    charge.set(BILL_NO, billNo);
    charge.set(ALLOW_RATE_INCREASE, allowRateIncrease);
    charge.set(ALLOW_RATE_DECREASE, allowRateDecrease);
    charge.set(FROM_DATE, from);
    charge.set(TO_DATE, to);
    charge.set(ACT_RATE_PLAN_ITEM_CODE, actRatePlanItemCode);
    charge.set(CODE_TYPE, codeType);
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    // TODO Auto-generated method stub
    BasicDynaBean bean = getBean();
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    // TODO Auto-generated method stub
    BasicDynaBean bean = getBean();
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    return null;
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getOrderItemActivityCode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPrescriptionDocKey() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException {
    if (itemsMapsList.isEmpty()) {
      return Collections.emptyList();
    }
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    String username = (String) billInfoMap.get("user_name");
    String[] preAuthIds = (String[]) billInfoMap.get("pre_auth_ids");
    Integer[] preAuthModeIds = (Integer[]) billInfoMap.get("pre_auth_mode_ids");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    String bedType = (String) headerInformation.get("bed_type");
    String orgId = (String) headerInformation.get("bill_rate_plan_id");
    List<Object> newPreAuths = billItemAuthMap.get("newPreAuths");

    List<BasicDynaBean> chargesList = new ArrayList<>();
    for (int index = 0; index < itemsMapsList.size(); index++) {
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      BasicDynaBean bedBean = null;

      Map<String, Object> bedItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      if (null != bill) {
        chargesList = insertOrderItemCharges(chargeable, headerInformation,
            bedBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, "", "BED",
            centerId, true, bedItemDetails);
      }
    }

    return chargesList;
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    BigDecimal quantity = BigDecimal.valueOf((int) orderItemDetails.get("quantity"));
    List<BasicDynaBean> chargesList = new ArrayList<>();
    String actDescription = (String) orderItemDetails.get("act_description");
    String actDescriptionId = (String) orderItemDetails.get("act_description_id");


    if (chargeable) {
      String type = (String) orderItemDetails.get("type");
      String isIcu = String.valueOf('N');
      if (!"Bed".equals(type)) {
        isIcu = String.valueOf('Y');
      }
      DateUtil dateUtil = new DateUtil();
      Timestamp from = dateUtil.parseTheTimestamp((String) orderItemDetails.get("from_date"));
      Timestamp to = dateUtil.parseTheTimestamp((String) orderItemDetails.get("to_date"));
      BasicDynaBean ipPrefs = ipPreferencesService.getAllPreferences();
      BigDecimal[] bedDaysHours = getBedDaysHours(from, to, quantity, String.valueOf('N'), true,
          (String) isIcu, ipPrefs);

      // For MVP Package pick the quantity from Order POP Up.
      BigDecimal bedDays = BigDecimal.ZERO;
      if (isMultivisitPackage) {
        bedDays = new BigDecimal((int) orderItemDetails.get("quantity"));
        for (BasicDynaBean charge : chargesList) {
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
              (Integer) charge.get("package_id"));
          charge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          charge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
      } else {
        bedDays = bedDaysHours[0];
      }

      BasicDynaBean charge = billChargeService.setBillChargeBean("BED",
          (String) orderItemDetails.get("charge_head"),
          new BigDecimal(orderItemDetails.get("act_rate").toString()),
          bedDays,
          new BigDecimal((String) orderItemDetails.get("discount")).multiply(bedDays),
          actDescriptionId,
          actDescription,
          null, (int) orderItemDetails.get("service_sub_group_id"),
          Integer.valueOf((String) orderItemDetails.get("insurance_category_id")), isInsurance);

      setChargeAttributes(charge, "Days", (String) bill.get("bill_no"), true, true, from, to,
          (String) orderItemDetails.get("item_code"), (String) orderItemDetails.get("code_type"));
      Integer discCatId = (Integer) orderItemDetails.get("disc_cat_id");
      discCatId = discCatId != null ? (Integer) discCatId : 0;
      List<BasicDynaBean> discountPlanDetails = discountPlanService.listAllDiscountPlanDetails(null,
          "discount_plan_id", discCatId, "priority");
      String visitType = (String) orderItemDetails.get("visit_type");
      discountPlanService.applyDiscountRule(charge, discCatId, discountPlanDetails, visitType);
      if (orderItemDetails.get("package_id") != null) {
        charge.set("package_id", orderItemDetails.get("package_id"));
      }
      chargesList.add(charge);
      if (chargesList.size() > 0) {
        (chargesList.get(0)).set("order_number",
            headerInformation.get("commonorderid"));
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, null,
            planIds, 0, null, new Timestamp(postedDate.getTime()));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, null,
            planIds, 0,
            null, null);
      }
      if (isMultivisitPackage) {
        Object chargeId = chargesList.get(0).get("charge_id");
        Map<String, Object> mvpItem = (Map<String, Object>) orderItemDetails.get("mvp_item");
        boolean isOldMvp = mvpItem.get("is_old_mvp") != null 
            ? (boolean) mvpItem.get("is_old_mvp") : false;
        //MVP which is partially consumed in 12.3 and upgraded to 12.4
        //Not inserting the data into patient package consumed table
        if (!isOldMvp) {
          multiVisitPackageService.insertPatientPackageConsumed(
              orderItemDetails.get("patient_package_content_id"),
              orderItemDetails.get("pack_ob_id"), orderItemDetails.get("pat_package_id"),
              orderItemDetails.get("quantity"), chargeId, null,
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionId) {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }
}
