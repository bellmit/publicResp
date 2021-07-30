package com.insta.hms.core.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.core.clinical.order.master.OrderService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class OrderRatesService.
 */
@Service
public class OrderRatesService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OrderRatesService.class);

  /** The Constant ITEM_RATE. */
  private static final String ITEM_RATE = "item_rate";

  /** The Constant CHARGE. */
  private static final String CHARGE = "charge";

  /** The Constant DISCOUNT. */
  private static final String DISCOUNT = "discount";

  /** The generic preferences service. */
  @LazyAutowired
  GenericPreferencesService genericPreferencesService;

  /** The ip preferences service. */
  @LazyAutowired
  IpPreferencesService ipPreferencesService;

  /** The order service. */
  @LazyAutowired
  OrderService orderService;

  /**
   * Gets the registration charges.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @param chargeHead
   *          the charge head
   * @param isRenewal
   *          the is renewal
   * @param visitType
   *          the visit type
   * @return the registration charges
   */
  public Map<String, BigDecimal> getRegistrationCharges(String bedType, String orgId,
      String chargeHead, boolean isRenewal, String visitType) {

    Map<String, BigDecimal> map = new HashMap<>();
    Map<String, BigDecimal> ratesMap = orderService.getRegChargeandDiscount(chargeHead, isRenewal,
        orgId, bedType, visitType);

    map.put(ITEM_RATE, ratesMap.get(CHARGE));
    map.put(DISCOUNT, ratesMap.get(DISCOUNT));
    return map;
  }

  /*
   * OT Doctor charges depend on the doctor's OT charges plus the operation's charge depending on
   * the ot doctor role.
   */

  /**
   * OT Doctor charges depend on the doctor's OT charges plus the operation's charge depending on
   * the ot doctor role.
   * 
   * @param doctor
   *          the doctor
   * @param otDocRole
   *          the ot doc role
   * @param operationRates
   *          the operation rates
   * @param quantity
   *          the quantity
   * @return the ot doctor charges
   */
  public Map<String, BigDecimal> getOtDoctorCharges(BasicDynaBean doctor, String otDocRole,
      BasicDynaBean operationRates, BigDecimal quantity) {

    String desc = (String) doctor.get("doctor_name");
    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    // base charge is based on the operation
    if (operationRates == null) {
      logger.warn("Surgeon/anaesthetist charge without operation " + desc);

    } else {
      if (otDocRole.equals("SUOPE") || otDocRole.equals("ASUOPE") || otDocRole.equals("COSOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("surgeon_charge");
        discount = (BigDecimal) operationRates.get("surg_discount");
      } else if (otDocRole.equals("ANAOPE") || otDocRole.equals("AANOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("anesthetist_charge");
        discount = (BigDecimal) operationRates.get("anest_discount");
      } else {
        logger.error("Invalid OT Doc role supplied: " + otDocRole);
      }
    }

    // the doctor premium is based on the ot doc role, hardcoded to doctor fields
    if (otDocRole.equals("COSOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));
    } else if (otDocRole.equals("ASUOPE") || otDocRole.equals("AANOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
    } else if (otDocRole.equals("IPDOC")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("doctor_ip_charge"));
      discount = discount.add((BigDecimal) doctor.get("doctor_ip_charge_discount"));
    } else {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("ot_charge"));
      discount = discount.add((BigDecimal) doctor.get("ot_charge_discount"));
    }

    rateMap.put(ITEM_RATE, doctorCharge);
    rateMap.put(DISCOUNT, discount.multiply(quantity));

    return rateMap;
  }

  /**
   * Gets the doctor cons charges.
   *
   * @param doctor
   *          the doctor
   * @param consTypeBean
   *          the cons type bean
   * @param orgDetails
   *          the org details
   * @param quantity
   *          the quantity
   * @param bedType
   *          the bed type
   * @return the doctor cons charges
   */
  /*
   * Doctor charges depend on the consultation type charges, plus a premium for each doctor. The
   * doctor premium field is based on the consultation's doctor_charge_type. Eg, if
   * doctor_charge_type is "doctor_ip_charge", then we look at the doctor_ip_charge field in the
   * doctor charges master.
   *
   */
  public Map<String, BigDecimal> getDoctorConsCharges(BasicDynaBean doctor,
      BasicDynaBean consTypeBean, BasicDynaBean orgDetails, BigDecimal quantity, String bedType) {

    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    Map<String, BigDecimal> rateMap = new HashMap<>();

    int consTypeId = (Integer) consTypeBean.get("consultation_type_id");
    BasicDynaBean consultationTypeCharge = orderService.getConsultationCharge(consTypeId, bedType,
        (String) orgDetails.get("org_id"));

    String docChargeType = (String) consTypeBean.get("doctor_charge_type");

    doctorCharge = (BigDecimal) doctor.get(docChargeType);
    discount = (BigDecimal) doctor.get(docChargeType + "_discount");

    doctorCharge = doctorCharge.add((BigDecimal) consultationTypeCharge.get(CHARGE));
    discount = (consultationTypeCharge.get(DISCOUNT) != null
        ? discount.add((BigDecimal) consultationTypeCharge.get(DISCOUNT)) : discount);

    rateMap.put(ITEM_RATE, doctorCharge);
    rateMap.put(DISCOUNT, discount.multiply(quantity));
    return rateMap;
  }

  /**
   * Gets the equipment charges.
   *
   * @param equip
   *          the equip
   * @param from
   *          the from
   * @param to
   *          the to
   * @param units
   *          the units
   * @param isOperation
   *          the is operation
   * @param quantity
   *          the quantity
   * @param isInsurance
   *          the is insurance
   * @param planIds
   *          the plan ids
   * @param visitType
   *          the visit type
   * @param patientId
   *          the patient id
   * @param firstOfCategory
   *          the first of category
   * @return the equipment charges
   */
  /*
   * Returns a list of charges as a charge DTO applicable to an equipment being ordered. For
   * equipment, there can be only one charge, either daily charge or hourly.
   */
  public Map<String, BigDecimal> getEquipmentCharges(BasicDynaBean equip, Timestamp from,
      Timestamp to, String units, boolean isOperation, BigDecimal quantity, Boolean isInsurance,
      int[] planIds, String visitType, String patientId, Boolean firstOfCategory) {

    BigDecimal rate = null;
    BigDecimal discount = null;
    int qty = 1;
    int duration = 0;

    if (units == null || units.equals(""))
      units = "H";
    Map<String, BigDecimal> rateMap = new HashMap<>();
    if ((units.equals("D") || units.equals("Days"))) {
      /*
       * For Daily Charge, we put num days as qty, so that rate*qty-discount = amt is maintained.
       * This cannot be done for hourly charge.
       */
      rate = (BigDecimal) equip.get(CHARGE);
      discount = (BigDecimal) equip.get("daily_charge_discount");
      if (from == null) {
        qty = quantity.intValue(); // equipment supports only integer quantities
      } else {
        qty = getDuration(from, to, "D");
      }

    } else {
      /*
       * rate*qty-discount = amt must be maintained. So, we calculate the total charge as per
       * min/incr and put the rate as the total charge, set the qty=1. Note that we should not
       * display units as Hrs because it is not 1 hrs. The trade off is between maintaining
       * rate*qty-discount=amt vs. showing the correct amount of Hrs in the display. We choose the
       * former.
       */
      qty = 1;
      BigDecimal minRate = (BigDecimal) equip.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) equip.get("min_charge_discount");
      BigDecimal slab1Rate = (BigDecimal) equip.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) equip.get("slab_1_charge_discount");
      BigDecimal incrRate = (BigDecimal) equip.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) equip.get("incr_charge_discount");

      int minDuration = ((BigDecimal) equip.get("min_duration")).intValue();
      int slab1Duration = ((BigDecimal) equip.get("slab_1_threshold")).intValue();
      int incrDuration = ((BigDecimal) equip.get("incr_duration")).intValue();

      if (from == null) {
        duration = quantity.intValue(); // equipment supports only integer quantities
      } else {
        duration = getDuration(from, to, "H", (Integer) equip.get("duration_unit_minutes"));
      }

      rate = getDurationCharge(duration, minDuration, slab1Duration, incrDuration, minRate,
          slab1Rate, incrRate, false);
      discount = getDurationCharge(duration, minDuration, slab1Duration, incrDuration, minDiscount,
          slab1Discount, incrDiscount, false);
    }

    rateMap.put(ITEM_RATE, rate);
    rateMap.put(DISCOUNT, discount.multiply(new BigDecimal(qty)));
    return rateMap;
  }

  /**
   * Gets the operation charges for theater TCOPE.
   *
   * @param theatre
   *          the theatre
   * @param from
   *          the from
   * @param to
   *          the to
   * @param units
   *          the units
   * @param itemQty
   *          the item qty
   * @return the operation charges for theater TCOPE
   */
  public Map<String, BigDecimal> getOperationChargesForTheaterTCOPE(BasicDynaBean theatre,
      Timestamp from, Timestamp to, String units, BigDecimal itemQty) {

    /*
     * Theater charge: this is like equipment charge: we get only a single charge amount. Depending
     * on Daily or Hourly, we get number of Days or number of Hours the equipment is used. For Days,
     * it is a straightforward calculation: rate*numDays. For hourly, depending on the min duration
     * etc, we get an amount that is not directly proportional to the number of hours, thus qty=1
     * and rate is variable.
     */

    if (theatre == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();

    BasicDynaBean gprefs = genericPreferencesService.getAllPreferences();
    BasicDynaBean ipprefs = ipPreferencesService.getPreferences();
    String splitTheatreCharges = ipprefs.get("split_theatre_charges") != null
        ? (String) ipprefs.get("split_theatre_charges") : "N";

    if (gprefs.get("fixed_ot_charges").equals("Y")) {// from and to times are required for
                                                     // Anesthecia type
      from = DataBaseUtil.getDateandTime();
      to = DataBaseUtil.getDateandTime();
    }

    if (units.equals("D")) {
      BigDecimal rate = (BigDecimal) theatre.get("daily_charge");
      BigDecimal discount = (BigDecimal) theatre.get("daily_charge_discount");
      rateMap.put(ITEM_RATE, rate);
      rateMap.put(DISCOUNT, discount.multiply(itemQty));
    } else if (splitTheatreCharges.equals("N")) {
      /*
       * Do the hourly charge calculations
       */
      BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
      BigDecimal incrDuration = (BigDecimal) theatre.get("incr_duration");
      int duration = getDuration(from, to, "H", (Integer) theatre.get("duration_unit_minutes"));
      BigDecimal rate = getDurationCharge(duration, minDuration.intValue(),
          slab1Duration.intValue(), incrDuration.intValue(), minRate, slab1Rate, incrRate, false);
      BigDecimal discount = getDurationCharge(duration, minDuration.intValue(),
          slab1Duration.intValue(), incrDuration.intValue(), minDiscount, slab1Discount,
          incrDiscount, false);
      rateMap.put(ITEM_RATE, rate);
      rateMap.put(DISCOUNT, discount);
    } else {

      BigDecimal minRate = (BigDecimal) theatre.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) theatre.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) theatre.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) theatre.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) theatre.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) theatre.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) theatre.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) theatre.get("incr_charge_discount");
      int unitSize = (Integer) theatre.get("duration_unit_minutes");
      int duration = getDuration(from, to, "H", unitSize);
      int hrlyDuration = 0;
      if (duration <= minDuration.intValue()) {
        hrlyDuration = minDuration.intValue();

      } else {
        hrlyDuration = slab1Duration.intValue();
      }

      BigDecimal rate = getDurationCharge(hrlyDuration, minDuration.intValue(),
          slab1Duration.intValue(), 0, minRate, slab1Rate, incrRate, false);
      BigDecimal discount = getDurationCharge(hrlyDuration, minDuration.intValue(),
          slab1Duration.intValue(), 0, minDiscount, slab1Discount, incrDiscount, false);

      rateMap.put(ITEM_RATE, rate);
      rateMap.put(DISCOUNT, discount);
    }
    return rateMap;
  }

  /**
   * Gets the surgeon charges SUOPE.
   *
   * @param operationBean
   *          the operation bean
   * @param surgeonDocBean
   *          the surgeon doc bean
   * @return the surgeon charges SUOPE
   */
  public Map<String, BigDecimal> getSurgeonChargesSUOPE(BasicDynaBean operationBean,
      BasicDynaBean surgeonDocBean) {

    if (operationBean == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal rate = BigDecimal.ZERO;
    if (surgeonDocBean != null) {
      rate = (BigDecimal) operationBean.get("surgeon_charge");
      rate = rate.add((BigDecimal) surgeonDocBean.get(CHARGE));

      BigDecimal discount = (BigDecimal) operationBean.get("surg_discount");
      discount = discount.add((BigDecimal) surgeonDocBean.get(DISCOUNT));
      rateMap.put(ITEM_RATE, rate);
      rateMap.put(DISCOUNT, discount);
    }
    return rateMap;
  }

  /**
   * Gets the anestiatist charges ANAOPE.
   *
   * @param operationBean
   *          the operation bean
   * @param anaDocBean
   *          the ana doc bean
   * @return the anestiatist charges ANAOPE
   */
  public Map<String, BigDecimal> getAnestiatistChargesANAOPE(BasicDynaBean operationBean,
      BasicDynaBean anaDocBean) {

    if (operationBean == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal rate = BigDecimal.ZERO;
    if (anaDocBean != null) {
      rate = (BigDecimal) operationBean.get("anesthetist_charge");
      rate = rate.add((BigDecimal) anaDocBean.get(CHARGE));
      BigDecimal discount = (BigDecimal) operationBean.get("anest_discount");
      discount = discount.add((BigDecimal) anaDocBean.get(DISCOUNT));
      rateMap.put(ITEM_RATE, rate);
      rateMap.put(DISCOUNT, discount);
    }
    return rateMap;
  }

  /**
   * Gets the anaesthesia type charges ANATOPE.
   *
   * @param anasthesiaTypeBean
   *          the anasthesia type bean
   * @param from
   *          the from
   * @param to
   *          the to
   * @return the anaesthesia type charges ANATOPE
   */
  public Map<String, BigDecimal> getAnaesthesiaTypeChargesANATOPE(BasicDynaBean anasthesiaTypeBean,
      Timestamp from, Timestamp to) {

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    if (anasthesiaTypeBean != null) {
      BigDecimal minRate = (BigDecimal) anasthesiaTypeBean.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) anasthesiaTypeBean.get("min_charge_discount");
      BigDecimal minDuration = (BigDecimal) anasthesiaTypeBean.get("min_duration");
      BigDecimal slab1Rate = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) anasthesiaTypeBean.get("slab_1_charge_discount");
      BigDecimal slab1Duration = (BigDecimal) anasthesiaTypeBean.get("slab_1_threshold");
      BigDecimal incrRate = (BigDecimal) anasthesiaTypeBean.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) anasthesiaTypeBean.get("incr_charge_discount");
      BigDecimal incrDuration = (BigDecimal) anasthesiaTypeBean.get("incr_duration");
      Integer baseUnit = (Integer) anasthesiaTypeBean.get("base_unit");
      Integer totalUnit = 0;

      int duration = getDuration(from, to, "H",
          (Integer) anasthesiaTypeBean.get("duration_unit_minutes"));
      if (baseUnit != null) {
        totalUnit = baseUnit + duration;
        rate = incrRate;
        discount = getBaseCharge(totalUnit, incrDiscount);
        rateMap.put(ITEM_RATE, rate);
        rateMap.put(DISCOUNT, discount);
      } else {
        rate = getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
            incrDuration.intValue(), minRate, slab1Rate, incrRate, false);
        discount = getDurationCharge(duration, minDuration.intValue(), slab1Duration.intValue(),
            incrDuration.intValue(), minDiscount, slab1Discount, incrDiscount, false);
        rateMap.put(ITEM_RATE, rate);
        rateMap.put(DISCOUNT, discount);
      }
    }
    return rateMap;
  }

  /**
   * Gets the surgical assistance charge SACOPE.
   *
   * @param operationBean
   *          the operation bean
   * @return the surgical assistance charge SACOPE
   */
  public Map<String, BigDecimal> getSurgicalAssistanceChargeSACOPE(BasicDynaBean operationBean) {

    if (operationBean == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;
    rate = (BigDecimal) operationBean.get("surg_asstance_charge");
    discount = (BigDecimal) operationBean.get("surg_asst_discount");

    rateMap.put(ITEM_RATE, rate);
    rateMap.put(DISCOUNT, discount);

    return rateMap;
  }

  /**
   * Gets the co operate surgeon fee COSOPE.
   *
   * @param operationBean
   *          the operation bean
   * @param doctor
   *          the doctor
   * @return the co operate surgeon fee COSOPE
   */
  public Map<String, BigDecimal> getCoOperateSurgeonFeeCOSOPE(BasicDynaBean operationBean,
      BasicDynaBean doctor) {

    if (operationBean == null || doctor == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    doctorCharge = (BigDecimal) operationBean.get("surgeon_charge");
    discount = (BigDecimal) operationBean.get("surg_discount");

    doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
    discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));

    rateMap.put(ITEM_RATE, doctorCharge);
    rateMap.put(DISCOUNT, discount);

    return rateMap;
  }

  /**
   * Gets the asst surgeon fee ASUOPE.
   *
   * @param operationBean
   *          the operation bean
   * @param doctor
   *          the doctor
   * @return the asst surgeon fee ASUOPE
   */
  public Map<String, BigDecimal> getAsstSurgeonFeeASUOPE(BasicDynaBean operationBean,
      BasicDynaBean doctor) {

    if (operationBean == null || doctor == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    doctorCharge = (BigDecimal) operationBean.get("surgeon_charge");
    discount = (BigDecimal) operationBean.get("surg_discount");

    doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
    discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));

    rateMap.put(ITEM_RATE, doctorCharge);
    rateMap.put(DISCOUNT, discount);

    return rateMap;
  }

  /**
   * Gets the asst anaesthetist fee AANOPE.
   *
   * @param operationBean
   *          the operation bean
   * @param doctor
   *          the doctor
   * @return the asst anaesthetist fee AANOPE
   */
  public Map<String, BigDecimal> getAsstAnaesthetistFeeAANOPE(BasicDynaBean operationBean,
      BasicDynaBean doctor) {

    if (operationBean == null || doctor == null)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();
    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    doctorCharge = (BigDecimal) operationBean.get("anesthetist_charge");
    discount = (BigDecimal) operationBean.get("anest_discount");

    doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
    discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));

    rateMap.put(ITEM_RATE, doctorCharge);
    rateMap.put(DISCOUNT, discount);

    return rateMap;
  }

  /**
   * Gets the duration.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param units
   *          the units
   * @return the duration
   */
  public int getDuration(Timestamp from, Timestamp to, String units) {
    return getDuration(from, to, units, 60);
  }

  /**
   * Gets the duration.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param type
   *          the type
   * @param unitSize
   *          the unit size
   * @return the duration
   */
  public int getDuration(Timestamp from, Timestamp to, String type, int unitSize) {
    long timeDiff = to.getTime() - from.getTime(); // milliseconds
    int minutes = (int) (timeDiff / 60 / 1000);

    int duration;
    if (type.equals("D")) {
      // any part of an hour is considered a full hour, eg, 60 minutes = 1hr, but 61 minutes = 2 hrs
      int hours = minutes / 60 + ((minutes % 60 > 0) ? 1 : 0);
      duration = hours / 24 + ((hours % 24 > 0) ? 1 : 0);
    } else {
      /*
       * We use ceil (any part thereof): if unitSize is 15, then the following are the conversions:
       * 0-15: 1, 16-30: 2, 31-45: 3, 46-60: 4, 61-75: 5 ...
       */
      duration = minutes / unitSize + ((minutes % unitSize > 0) ? 1 : 0);
    }
    return duration;
  }

  /**
   * Gets the duration charge.
   *
   * @param duration
   *          the duration
   * @param minDuration
   *          the min duration
   * @param slab1Duration
   *          the slab 1 duration
   * @param incrDuration
   *          the incr duration
   * @param minCharge
   *          the min charge
   * @param slab1Rate
   *          the slab 1 rate
   * @param incrRate
   *          the incr rate
   * @param splitCharge
   *          the split charge
   * @return the duration charge
   */
  /*
   * 2-slab duration charge calculation
   */
  public BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate, BigDecimal incrRate,
      boolean splitCharge) {

    return getDurationCharge(duration, minDuration, slab1Duration, slab1Duration, incrDuration,
        minCharge, slab1Rate, slab1Rate, incrRate, splitCharge);
  }

  /**
   * Gets the duration charge.
   *
   * @param duration
   *          the duration
   * @param minDuration
   *          the min duration
   * @param slab1Duration
   *          the slab 1 duration
   * @param slab2Duration
   *          the slab 2 duration
   * @param incrDuration
   *          the incr duration
   * @param minCharge
   *          the min charge
   * @param slab1Rate
   *          the slab 1 rate
   * @param slab2Rate
   *          the slab 2 rate
   * @param incrRate
   *          the incr rate
   * @param splitCharge
   *          the split charge
   * @return the duration charge
   */
  /*
   * 3-slab duration charge calculation
   */
  public BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int slab2Duration, int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate,
      BigDecimal slab2Rate, BigDecimal incrRate, boolean splitCharge) {

    logger.debug("Getting duration charge for " + duration + ": " + minDuration + " "
        + slab1Duration + " " + slab2Duration + " " + incrDuration + " " + minCharge + " "
        + slab1Rate + " " + slab2Rate + " " + incrRate);

    if (duration <= minDuration) {
      return minCharge;

    } else if (duration <= slab1Duration) {
      return slab1Rate;

    } else if (duration <= slab2Duration) {
      return slab2Rate;

    } else {
      if (incrDuration != 0) {
        int addnlUnits = duration - slab2Duration; // eg, 5 - 4 = 1
        // again we apply ceil, ie, any part thereof will get into the next slot
        // eg, 1-2 => 1, 3-4 => 2 etc.
        int incrUnits = addnlUnits / incrDuration + (addnlUnits % incrDuration > 0 ? 1 : 0);
        if (splitCharge)
          return incrRate.multiply(new BigDecimal(incrUnits));
        else
          return slab2Rate.add(incrRate.multiply(new BigDecimal(incrUnits)));

      } else {
        if (minDuration == 0) // assume plain hourly charge
          return minCharge.add(incrRate.multiply(new BigDecimal(duration)));
        else // they don't want the incr charge, only the min charge.
          return minCharge;
      }
    }
  }

  /**
   * Gets the base charge.
   *
   * @param totalUnit
   *          the total unit
   * @param baseCharge
   *          the base charge
   * @return the base charge
   */
  public BigDecimal getBaseCharge(Integer totalUnit, BigDecimal baseCharge) {
    return baseCharge.multiply(new BigDecimal(totalUnit));
  }

}
