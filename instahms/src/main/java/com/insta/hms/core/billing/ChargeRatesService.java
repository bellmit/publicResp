
package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.core.clinical.order.equipmentitems.EquipmentOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.anesthesiatypecharges.AnesthesiaTypeChargesService;
import com.insta.hms.mdm.beddetails.BedDetailsService;
import com.insta.hms.mdm.commoncharges.CommonChargesService;
import com.insta.hms.mdm.dietary.DietaryService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.equipment.EquipmentService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.services.ServicesService;
import com.insta.hms.mdm.tests.TestsService;
import com.insta.hms.mdm.theatrecharges.TheatreChargesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class ChargeRatesService.
 */
@Service
public class ChargeRatesService {

  /** The anesthesia type charges service. */
  @LazyAutowired
  AnesthesiaTypeChargesService anesthesiaTypeChargesService;

  /** The bed details service. */
  @LazyAutowired
  BedDetailsService bedDetailsService;

  /** The bed operation schedule service. */
  @LazyAutowired
  OperationOrderItemService bedOperationScheduleService;

  /** The bill activity charge service. */
  @LazyAutowired
  BillActivityChargeService billActivityChargeService;

  /** The common charges service. */
  @LazyAutowired
  CommonChargesService commonChargesService;

  /** The dietary service. */
  @LazyAutowired
  DietaryService dietaryService;

  /** The doctor consultation service. */
  @LazyAutowired
  DoctorConsultationService doctorConsultationService;

  /** The doctor service. */
  @LazyAutowired
  DoctorService doctorService;

  /** The equipment order item service. */
  @LazyAutowired
  EquipmentOrderItemService equipmentOrderItemService;

  /** The equipment service. */
  @LazyAutowired
  EquipmentService equipmentService;

  /** The ip preferences service. */
  @LazyAutowired
  IpPreferencesService ipPreferencesService;

  /** The operations service. */
  @LazyAutowired
  OperationsService operationsService;

  /** The order BO. */
  @LazyAutowired
  OrderService orderService;

  /** The order rates service. */
  @LazyAutowired
  OrderRatesService orderRatesService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The packages service. */
  @LazyAutowired
  PackagesService packagesService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The services service. */
  @LazyAutowired
  ServicesService servicesService;

  /** The tests service. */
  @LazyAutowired
  TestsService testsService;

  /** The theatre charges service. */
  @LazyAutowired
  TheatreChargesService theatreChargesService;

  /** The visit type. */
  private String visitType = "";

  /** The visit id. */
  private String visitId = "";

  /** The plan ids. */
  private int[] planIds = null;

  /** The is insurance. */
  private Boolean isInsurance = false;

  /** The patient details map. */
  private Map patientDetailsMap = new HashMap();

  private static final String PRESCRIBED_ID = "prescribed_id";

  /**
   * Instantiates a new charge rates.
   *
   * @param visitId
   *          the visit id
   * @param visitType
   *          the visit type
   * @param planIds
   *          the plan ids
   * @param isInsurance
   *          the is insurance
   */
  public void setChargeRates(String visitId, String visitType, int[] planIds, boolean isInsurance) {
    this.visitId = visitId;
    this.visitType = visitType;
    this.planIds = planIds;
    this.isInsurance = isInsurance;
  }

  /**
   * Sets the patient details map.
   *
   * @param visitId
   *          the new patient details map
   */
  public void setPatientDetailsMap(String visitId) {
    this.patientDetailsMap = registrationService.getPatientVisitDetailsMap(visitId);
  }

  /**
   * Gets the registration charge rate.
   *
   * @param chargeHead
   *          the charge head
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @return the registration charge rate
   */
  public Map<String, BigDecimal> getRegistrationChargeRate(String chargeHead, String orgId,
      String bedType, String itemId) {
    Map<String, BigDecimal> rateMap = new HashMap<>();

    Boolean isRegCharge = checkIfValidRegistrationCharge(itemId);
    if (!isRegCharge)
      return rateMap;

    Boolean isRenewal = patientDetailsMap.get("reg_charge_accepted") != null
        && patientDetailsMap.get("revisit").equals('Y');
    rateMap = orderRatesService.getRegistrationCharges(bedType, orgId, chargeHead, isRenewal,
        visitType);
    return rateMap;
  }

  /**
   * Gets the doctor charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param chargeId
   *          the charge id
   * @param itemQty
   *          the item qty
   * @param chargeType
   *          the charge type
   * @param hasactivity
   *          the hasactivity
   * @return the doctor charge
   */
  public Map<String, BigDecimal> getDoctorCharge(String orgId, String bedType, String itemId,
      String chargeId, BigDecimal itemQty, String chargeType, boolean hasactivity) {

    Boolean isDoctorCharge = checkIfValidDoctorCharge(itemId);
    if (!isDoctorCharge)
      return null;
    BasicDynaBean consTypeBean = null;
    Map<String, BigDecimal> rateMap = new HashMap<>();
    // Doctor added from order
    if (hasactivity) {
      DynaBean dcBean = null;
      dcBean = doctorConsultationService.getDoctorConsultationCharge(chargeId);
      BasicDynaBean opMasterBean = null;
      if (null != dcBean.get("operation_ref")) {
        BasicDynaBean opBean = bedOperationScheduleService.findByKey(PRESCRIBED_ID,
            dcBean.get("operation_ref"));
        String operId = (String) opBean.get("operation_name");
        opMasterBean = operationsService.getOperationCharge(operId, bedType, orgId);
      }

      String otDocRole = (String) dcBean.get("ot_doc_role");
      String consType = (String) dcBean.get("head");

      if (opMasterBean == null) {
        // get the consultation type bean
        consTypeBean = orderService.getConsultationTypeBean(Integer.parseInt(consType));
      }

      BasicDynaBean doctor = doctorService.getDoctorCharges((String) dcBean.get("doctor_name"),
          orgId, bedType);
      if (opMasterBean != null) {
        // This is an operation related doctor order...
        rateMap = orderRatesService.getOtDoctorCharges(doctor, otDocRole, opMasterBean, itemQty);

      } else {
        BasicDynaBean orgDetails = organizationService.getOrgdetailsDynaBean(orgId);
        rateMap = orderRatesService.getDoctorConsCharges(doctor, consTypeBean, orgDetails, itemQty,
            bedType);
      }

    } else {

      consTypeBean = orderService.getConsultationTypeBean(Integer.parseInt(chargeType));
      BasicDynaBean doctor = doctorService.getDoctorCharges((String) itemId, orgId,
          (String) bedType);
      BasicDynaBean orgDetails = organizationService.getOrgdetailsDynaBean(orgId);
      rateMap = orderRatesService.getDoctorConsCharges(doctor, consTypeBean, orgDetails, itemQty,
          bedType);
    }
    return rateMap;
  }

  /**
   * Gets the diag charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @return the diag charge
   */
  public Map<String, BigDecimal> getDiagCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    boolean isValidTest = checkIfValidTest(itemId);
    if (!isValidTest)
      return null;
    BasicDynaBean rateBean = testsService.getTestDetails(itemId, bedType, (String) orgId);
    rateMap.put("item_rate", (BigDecimal) rateBean.get("charge"));
    rateMap.put("discount", ((BigDecimal) rateBean.get("discount")).multiply(itemQty));
    return rateMap;
  }

  /**
   * Gets the service charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @param actDeptId
   *          the act dept id
   * @return the service charge
   */
  public Map<String, BigDecimal> getServiceCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty, String actDeptId) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    // Service charges always have a department, unless its an Other charge.
    if (actDeptId == null || actDeptId.isEmpty())
      return null;
    BasicDynaBean rateBean = servicesService.getServiceChargeBean(itemId, bedType, orgId);

    rateMap.put("item_rate", (BigDecimal) rateBean.get("unit_charge"));
    rateMap.put("discount", ((BigDecimal) rateBean.get("discount")).multiply(itemQty));
    return rateMap;
  }

  /**
   * Gets the dietary charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @return the dietary charge
   */
  public Map<String, BigDecimal> getDietaryCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    boolean validMeal = checkIfValidDietCharge(itemId);
    if (!validMeal)
      return null;
    BasicDynaBean rateBean = dietaryService.getChargeForMeal(orgId, Integer.parseInt(itemId), bedType);

    rateMap.put("item_rate", (BigDecimal) rateBean.get("charge"));
    rateMap.put("discount", ((BigDecimal) rateBean.get("discount")).multiply(itemQty));
    return rateMap;
  }

  /**
   * Gets the equipment charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param chargeHead
   *          the charge head
   * @param chargeId
   *          the charge id
   * @param itemQty
   *          the item qty
   * @param actUnit
   *          the act unit
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param hasactivity
   *          the hasactivity
   * @return the equipment charge
   */
  public Map<String, BigDecimal> getEquipmentCharge(String orgId, String bedType, String itemId,
      String chargeHead, String chargeId, BigDecimal itemQty, String actUnit, Timestamp fromDate,
      Timestamp toDate, boolean hasactivity) {

    Boolean isOperation = chargeHead.equals("EQOPE");

    Boolean isValidEquipCharge = checkIfValidEquipCharge(itemId);
    if (!isValidEquipCharge) {
      return null;
    }
    BasicDynaBean equipDetails = equipmentService.getEquipmentCharge(itemId, bedType, orgId);

    if (hasactivity) {
      BasicDynaBean bean = billActivityChargeService
          .findByKey(Collections.singletonMap("charge_id", (Object) chargeId));

      BasicDynaBean equipPres = equipmentOrderItemService.findByKey(PRESCRIBED_ID,
          Integer.parseInt((String) bean.get("activity_id")));

      Timestamp from = (Timestamp) equipPres.get("used_from");
      Timestamp to = (Timestamp) equipPres.get("used_till");
      String units = (String) equipPres.get("units");

      return orderRatesService.getEquipmentCharges(equipDetails, from, to, units, isOperation,
          itemQty, isInsurance, planIds, visitType, visitId, false);
    } else {
      return orderRatesService.getEquipmentCharges(equipDetails, fromDate, toDate, actUnit,
          isOperation, itemQty, isInsurance, planIds, visitType, visitId, false);
    }
  }

  /**
   * Gets the other charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @return the other charge
   */
  public Map<String, BigDecimal> getOtherCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    BasicDynaBean rateBean = commonChargesService.getCommonCharge(itemId);

    rateMap.put("item_rate", (BigDecimal) rateBean.get("charge"));
    rateMap.put("discount", BigDecimal.ZERO);
    return rateMap;
  }

  /**
   * Gets the package charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @return the package charge
   */
  public Map<String, BigDecimal> getPackageCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty) {
    Map<String, BigDecimal> rateMap = new HashMap<>();
    boolean validPackage = checkIfValidPackage(itemId);
    if (!validPackage)
      return null;
    BasicDynaBean rateBean = packagesService.getPackageDetails(Integer.parseInt(itemId), orgId,
        bedType);

    rateMap.put("item_rate", (BigDecimal) rateBean.get("charge"));
    rateMap.put("discount", ((BigDecimal) rateBean.get("discount")).multiply(itemQty));
    return rateMap;

  }

  /**
   * Gets the bed charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @param chargeGroup
   *          the charge group
   * @param chargeHead
   *          the charge head
   * @return the bed charge
   */
  public Map<String, BigDecimal> getBedCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty, String chargeGroup, String chargeHead) {

    boolean isBedCharge = checkIfValidBedCharge(itemId);

    BasicDynaBean bedBean = bedDetailsService.getBedDetailsBean(itemId);

    String bedTypeForBedCharges = bedType;

    if (null != bedBean && null != bedBean.get("bed_type"))
      bedTypeForBedCharges = (String) bedBean.get("bed_type");

    if (!isBedCharge)
      return null;

    Map<String, BigDecimal> rateMap = new HashMap<>();

    BasicDynaBean bedRates = null;
    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;
    if (chargeGroup.equals("ICU")) {
      bedRates = bedDetailsService.getIcuBedCharges(Integer.parseInt(itemId), orgId,
          bedTypeForBedCharges);
      if (bedRates == null)
        return null;
      if (chargeHead.equals("BICU")) {
        rate = (BigDecimal) bedRates.get("bed_charge");
        discount = (BigDecimal) bedRates.get("bed_charge_discount");
      } else if (chargeHead.equals("PCICU")) {
        rate = (BigDecimal) bedRates.get("maintainance_charge");
        discount = (BigDecimal) bedRates.get("maintainance_charge_discount");
      } else if (chargeHead.equals("DDICU")) {
        rate = (BigDecimal) bedRates.get("duty_charge");
        discount = (BigDecimal) bedRates.get("duty_charge_discount");
      } else if (chargeHead.equals("NCICU")) {
        rate = (BigDecimal) bedRates.get("nursing_charge");
        discount = (BigDecimal) bedRates.get("nursing_charge_discount");
      }
    } else {
      bedRates = bedDetailsService.getNormalBedChargesBean(bedTypeForBedCharges, orgId);
      if (bedRates == null)
        return null;
      if (chargeHead.equals("PCBED")) {
        rate = (BigDecimal) bedRates.get("maintainance_charge");
        discount = (BigDecimal) bedRates.get("maintainance_charge_discount");
      } else if (chargeHead.equals("BYBED")) {
        BasicDynaBean prefs = ipPreferencesService.getPreferences();

        if (prefs.get("bystander_bed_charges_applicable_on").equals("W")) {
          rate = ((BigDecimal) bedRates.get("bed_charge"))
              .add((BigDecimal) bedRates.get("nursing_charge"))
              .add((BigDecimal) bedRates.get("duty_charge"))
              .add((BigDecimal) bedRates.get("maintainance_charge"));

          discount = ((BigDecimal) bedRates.get("bed_charge_discount"))
              .add((BigDecimal) bedRates.get("nursing_charge_discount"))
              .add((BigDecimal) bedRates.get("duty_charge_discount"))
              .add((BigDecimal) bedRates.get("maintainance_charge_discount"));
        } else {
          rate = (BigDecimal) bedRates.get("bed_charge");
          discount = (BigDecimal) bedRates.get("bed_charge_discount");
        }
      } else if (chargeHead.equals("BBED")) {
        rate = (BigDecimal) bedRates.get("bed_charge");
        discount = (BigDecimal) bedRates.get("bed_charge_discount");
      } else if (chargeHead.equals("NCBED")) {
        rate = (BigDecimal) bedRates.get("nursing_charge");
        discount = (BigDecimal) bedRates.get("nursing_charge_discount");
      } else if (chargeHead.equals("DDBED")) {
        rate = (BigDecimal) bedRates.get("duty_charge");
        discount = (BigDecimal) bedRates.get("duty_charge_discount");
      }
    }

    rateMap.put("item_rate", rate);
    rateMap.put("discount", discount.multiply(itemQty));
    return rateMap;
  }

  /**
   * Gets the surgery charge.
   *
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param itemId
   *          the item id
   * @param itemQty
   *          the item qty
   * @param chargeId
   *          the charge id
   * @param chargeHead
   *          the charge head
   * @param actUnit
   *          the act unit
   * @param from
   *          the from
   * @param to
   *          the to
   * @param opId
   *          the op id
   * @return the surgery charge
   */
  public Map<String, BigDecimal> getSurgeryCharge(String orgId, String bedType, String itemId,
      BigDecimal itemQty, String chargeId, String chargeHead, String actUnit, Timestamp from,
      Timestamp to, String opId) {

    Map<String, BigDecimal> rateMap = new HashMap<>();

    String units = actUnit != null && (actUnit).equals("Days") ? "D" : "H";

    BasicDynaBean operationBean = operationsService.getOperationCharge(opId, bedType, orgId);

    if (operationBean == null) {
      BasicDynaBean bean = billActivityChargeService
          .findByKey(Collections.singletonMap("charge_id", (Object) chargeId));

      if (bean.get("activity_code").equals("OTC"))
        return null;
      BasicDynaBean bedOpBean = bedOperationScheduleService.findByKey(PRESCRIBED_ID,
          bean.get("activity_id"));
      if (bedOpBean != null)
        operationBean = operationsService
            .getOperationCharge((String) bedOpBean.get("operation_name"), bedType, orgId);
    }

    if (chargeHead.equals("TCOPE")) {
      BasicDynaBean theaterBean = theatreChargesService.getTheatreChargeDetails(itemId, bedType,
          orgId);
      rateMap = orderRatesService.getOperationChargesForTheaterTCOPE(theaterBean, from, to, units,
          itemQty);
    } else if (chargeHead.equals("SUOPE")) {
      BasicDynaBean surgeonDocBean = doctorService.getOtDoctorChargesBean(itemId, bedType, orgId);
      rateMap = orderRatesService.getSurgeonChargesSUOPE(operationBean, surgeonDocBean);
    } else if (chargeHead.equals("ANAOPE")) {
      BasicDynaBean anaDocBean = doctorService.getOtDoctorChargesBean(itemId, bedType, orgId);
      rateMap = orderRatesService.getAnestiatistChargesANAOPE(operationBean, anaDocBean);
    } else if (chargeHead.equals("ANATOPE")) {
      BasicDynaBean anaTypeBean = anesthesiaTypeChargesService.getAnesthesiaTypeCharge(itemId,
          bedType, orgId);
      rateMap = orderRatesService.getAnaesthesiaTypeChargesANATOPE(anaTypeBean, from, to);
    } else if (chargeHead.equals("SACOPE")) {
      rateMap = orderRatesService.getSurgicalAssistanceChargeSACOPE(operationBean);
    } else if (chargeHead.equals("COSOPE")) {
      DynaBean dcBean = null;
      dcBean = doctorConsultationService.getDoctorConsultationCharge(chargeId);
      BasicDynaBean doctor = doctorService.getDoctorCharges((String) dcBean.get("doctor_name"),
          orgId, (String) bedType);
      rateMap = orderRatesService.getCoOperateSurgeonFeeCOSOPE(operationBean, doctor);
    } else if (chargeHead.equals("ASUOPE")) {
      DynaBean dcBean = null;
      dcBean = doctorConsultationService.getDoctorConsultationCharge(chargeId);
      BasicDynaBean doctor = doctorService.getDoctorCharges((String) dcBean.get("doctor_name"),
          orgId, (String) bedType);
      rateMap = orderRatesService.getAsstSurgeonFeeASUOPE(operationBean, doctor);
    } else if (chargeHead.equals("AANOPE")) {
      DynaBean dcBean = null;
      dcBean = doctorConsultationService.getDoctorConsultationCharge(chargeId);
      BasicDynaBean doctor = doctorService.getDoctorCharges((String) dcBean.get("doctor_name"),
          orgId, (String) bedType);
      rateMap = orderRatesService.getAsstAnaesthetistFeeAANOPE(operationBean, doctor);
    }
    return rateMap;
  }

  // ----------------------------supporintg methods------------------------------

  /**
   * Check if valid registration charge.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public static boolean checkIfValidRegistrationCharge(String actDescriptionId) {
    return actDescriptionId.equals("GREG") || actDescriptionId.equals("IPREG")
        || actDescriptionId.equals("OPREG") || actDescriptionId.equals("MLREG")
        || actDescriptionId.equals("EMREG");
  }

  /**
   * Check if valid doctor charge.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public boolean checkIfValidDoctorCharge(String actDescriptionId) {
    BasicDynaBean docBean = doctorService.getDoctorById(actDescriptionId);
    return docBean != null;
  }

  /**
   * Check if valid test.
   *
   * @param actDeptId
   *          the act dept id
   * @return true, if successful
   */
  public static boolean checkIfValidTest(String actDeptId) {
    return actDeptId != null && !actDeptId.isEmpty();
  }

  /**
   * Check if valid diet charge.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public boolean checkIfValidDietCharge(String actDescriptionId) {
    if (actDescriptionId == null || actDescriptionId.equals(""))
      return false;
    return dietaryService.findByKey("diet_id", Integer.parseInt(actDescriptionId)) != null;
  }

  /**
   * Check if valid equip charge.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public boolean checkIfValidEquipCharge(String actDescriptionId) {
    if (actDescriptionId == null || actDescriptionId.equals(""))
      return false;
    return equipmentService.findByKey("eq_id", actDescriptionId) != null;
  }

  /**
   * Check if valid package.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public boolean checkIfValidPackage(String actDescriptionId) {
    if (actDescriptionId == null || actDescriptionId.equals(""))
      return false;
    int id = 0;
    try {
      id = Integer.parseInt(actDescriptionId);
    } catch (NumberFormatException ne) {
      return false;
    }
    return packagesService.findByKey("package_id", id) != null;
  }

  /**
   * Check if valid bed charge.
   *
   * @param actDescriptionId
   *          the act description id
   * @return true, if successful
   */
  public boolean checkIfValidBedCharge(String actDescriptionId) {
    BasicDynaBean bedBean = bedDetailsService.getBedDetailsBean(actDescriptionId);
    return bedBean != null;
  }
}
