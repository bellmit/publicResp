package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.formcomponents.FormComponentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PrescriptionsValidator {

  private static Logger logger = LoggerFactory.getLogger(PrescriptionsValidator.class);

  @LazyAutowired
  private GenericPreferencesService genprefs;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private PatientActivitiesService patientActivitiesService;

  private List<String> erxStatusValues = Arrays.asList(new String[] {"O", "D", "C"});

  private List<String> timeofintakeTypes = Arrays.asList(new String[] {"N", "B", "A", "W"});

  private List<String> medicationTypes = Arrays.asList(new String[] {"M", "A", "IV"});

  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Common validator.
   *
   * @param prescription the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param errMap the ValidationErrorMap
   * @return boolean value
   */
  public boolean validateCommon(Map<String, Object> prescription, boolean generics,
      boolean usesStores, ValidationErrorMap errMap) {
    String itemtype = (String) prescription.get("item_type");
    if (itemtype == null || itemtype.equals("")) {
      errMap.addError("item_type", "exception.notnull.prescription.itemtype");
      return false;
    } else if (!PrescriptionsService.ITEM_TYPES.contains(itemtype)) {
      errMap.addError("item_type", "exception.notvalid.prescription.itemtype");
      return false;
    }
    if (itemtype.equals("Medicine")) {
      if (prescription.get("non_hosp_medicine") == null) {
        errMap.addError("non_hosp_medicine", "exception.notnull.prescription.nonHospitalMedicine");
        return false;
      }
    }
    return true;
  }

  /**
   * Validate insert.
   *
   * @param prescription the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param sectionParams the form param
   * @param errMap the ValidationErrorMap
   * @param healthAuthority the string
   * @return the boolean value
   */
  public boolean validateInsert(Map<String, Object> prescription, boolean generics,
      boolean usesStores, FormParameter sectionParams, Integer sectionId, ValidationErrorMap errMap,
      String healthAuthority) {
    if (!validateCommon(prescription, generics, usesStores, errMap)) {
      return false;
    }
    String itemType = (String) prescription.get("item_type");
    if (!itemType.equals("NonHospital") && !itemType.equals(PrescriptionsService.NON_BILLABLE)
        && !(itemType.equals(PrescriptionsService.MEDICINE)
            && (boolean) prescription.get("non_hosp_medicine"))
        && (prescription.get("item_id") == null || prescription.get("item_id").equals(""))) {
      if (!itemType.equals("Medicine") || (usesStores && !generics)) {
        errMap.addError("item_id", "exception.notnull.prescription.itemId");
        return false;
      }
    }

    if (itemType.equals("Medicine")) {
      if (!(boolean) prescription.get("non_hosp_medicine")) {
        return validateMedicine(prescription, generics, usesStores, sectionId, sectionParams,
            errMap, "insert", healthAuthority);
      } else {
        return validateNonHospital(prescription, sectionId, sectionParams, errMap, "insert");
      }
    } else if (itemType.equals("Inv.")) {
      return validateInvestigation(prescription, errMap, "insert");
    } else if (itemType.equals("Service")) {
      return validateService(prescription, errMap, "insert");
    } else if (itemType.equals("Operation")) {
      return validateOperation(prescription, errMap, "insert");
    } else if (itemType.equals("Doctor")) {
      return validateDoctor(prescription, errMap, "insert");
    } else if (itemType.equals("NonHospital")) {
      return validateNonHospital(prescription, sectionId, sectionParams, errMap, "insert");
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validate update.
   *
   * @param prescription the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param sectionParams the form param
   * @param errMap the ValidationErrorMap
   * @param healthAuthority the string
   * @return the boolean value
   */
  public boolean validateUpdate(Map<String, Object> prescription, boolean generics,
      boolean usesStores, FormParameter sectionParams, Integer sectionId, ValidationErrorMap errMap,
      String healthAuthority) {
    if (!validateCommon(prescription, generics, usesStores, errMap)) {
      return false;
    }

    String itemType = (String) prescription.get("item_type");

    if (prescription.get("item_prescribed_id") == null
        || (Integer) prescription.get("item_prescribed_id") == 0) {
      errMap.addError("item_prescribed_id", "exception.notnull.prescription.presid");
      return false;
    } else if (!ValidationUtils.isKeyValid("patient_prescription",
        prescription.get("item_prescribed_id"), "patient_presc_id")) {
      errMap.addError("item_prescribed_id", "exception.notvalid.prescription.presid");
      return false;
    }
    if (itemType.equals("Medicine")) {
      if (!(boolean) prescription.get("non_hosp_medicine")) {
        return validateMedicine(prescription, generics, usesStores, sectionId, sectionParams,
            errMap, "update", healthAuthority);
      } else {
        return validateNonHospital(prescription, sectionId, sectionParams, errMap, "update");
      }
    } else if (itemType.equals("Inv.")) {
      return validateInvestigation(prescription, errMap, "update");
    } else if (itemType.equals("Service")) {
      return validateService(prescription, errMap, "update");
    } else if (itemType.equals("Operation")) {
      return validateOperation(prescription, errMap, "update");
    } else if (itemType.equals("Doctor")) {
      return validateDoctor(prescription, errMap, "update");
    } else if (itemType.equals("NonHospital")) {
      return validateNonHospital(prescription, sectionId, sectionParams, errMap, "update");
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validate delete.
   *
   * @param prescription the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param errMap the ValidationErrorMap
   * @return the boolean value
   */
  public boolean validateDelete(Map<String, Object> prescription, boolean generics,
      boolean usesStores, ValidationErrorMap errMap) {
    if (!validateCommon(prescription, generics, usesStores, errMap)) {
      return false;
    }

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("patient_presc_id", prescription.get("item_prescribed_id"));
    BasicDynaBean item = ValidationUtils.isKeyValid("patient_prescription", params);
    if (item == null) {
      errMap.addError("item_prescribed_id", "exception.notvalid.prescription.presid");
    } else if (item.get("status") != null && item.get("status").equals("O")) {
      errMap.addError("errMsg", "exception.delete.ordered.prescription");
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validates the medicines.
   *
   * @param prescription the map
   * @param generics the boolean
   * @param usesStores the boolean
   * @param errMap the ValidationErrorMap
   * @return the boolean value
   */
  public boolean validateMedicine(Map<String, Object> prescription, boolean generics,
      boolean usesStores, Integer sectionId, FormParameter sectionParameters,
      ValidationErrorMap errMap, String operation, String healthAuthority) {

    Map<String, Object> sessionnAttributes =
        sessionService.getSessionAttributes(new String[] {"mod_eclaim_erx"});
    if ((Boolean) sessionnAttributes.get("mod_eclaim_erx") && healthAuthority.equals("DHA")
        && !sectionParameters.getFormType()
            .equals(FormComponentsService.FormType.Form_IP.toString())) {
      if (prescription.get("route_id") == null) {
        errMap.addError("route_id", "exception.notnull.prescription.routeId");
      }
      if (prescription.get("duration") == null) {
        errMap.addError("duration", "exception.notnull.prescription.duration");
      } else if ((Integer) prescription.get("duration") <= 0) {
        errMap.addError("duration", "exception.notvalid.prescription.duration");
      }
      if (prescription.get("frequency") == null || "".equals(prescription.get("frequency"))) {
        errMap.addError("frequency", "exception.notnull.prescription.frequency");
      }
      if (prescription.get("item_remarks") == null || "".equals(prescription.get("item_remarks"))) {
        errMap.addError("item_remarks", "exception.notnull.prescription.itemRemarks");
      }
    }
    String erxStatus = (String) prescription.get("erx_status");
    if (prescription.containsKey("erx_status") && !erxStatusValues.contains(erxStatus)) {
      errMap.addError("erx_status", "exception.notValid.prescription.erxStatus");
    }

    if (prescription.containsKey("time_of_intake")
        && !timeofintakeTypes.contains(prescription.get("time_of_intake"))) {
      errMap.addError("time_of_intake", "exception.notvalid.prescription.timeofintake");
    }

    if (prescription.containsKey("priority")
        && !PrescriptionsService.PRIORITY_VALUES.contains(prescription.get("priority"))) {
      errMap.addError("priority", "exception.notvalid.prescription.priority");
    }

    if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
      if (prescription.get("start_date") == null || prescription.get("start_date").equals("")) {
        errMap.addError("start_date", "exception.notnull.prescription.startdate");
      }
    }

    if (prescription.get("start_date") != null && !prescription.get("start_date").equals("")
        && !sectionParameters.getFormType()
            .equals(FormComponentsService.FormType.Form_IP.toString())) {
      Map<String, Object> params = new HashMap<>();
      params.put("consultation_id", sectionParameters.getId());
      BasicDynaBean consBean = ValidationUtils.isKeyValid("doctor_consultation", params);
      try {
        Date startDate = DateUtil.parseDate((String) prescription.get("start_date"));
        Date consDate = DateUtil.getDatePart((java.sql.Timestamp) consBean.get("start_datetime"));
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        consDate = dateFormatter.parse(consDate.toString());
        if (consDate.after(startDate)) {
          errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        }
        if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
          Date endDate = DateUtil.parseDate((String) prescription.get("end_date"));
          if (startDate.after(endDate)) {
            errMap.addError("end_date", "exception.notvalid.prescription.enddate");
          }
        }
      } catch (java.text.ParseException exe) {
        errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        logger.error("", exe);
      }
    }

    if (!StringUtils.isEmpty((String) prescription.get("start_date"))
        && sectionParameters.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())
        && sectionId.equals(-22)) {

      Map<String, Object> params = new HashMap<>();
      params.put("patient_id", sectionParameters.getId());
      BasicDynaBean visitBean = ValidationUtils.isKeyValid("patient_registration", params);
      try {
        Date startDate = DateUtil.parseDate((String) prescription.get("start_date"));

        Date regDate = (Date) visitBean.get("reg_date");

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        regDate = dateFormatter.parse(regDate.toString());
        if (regDate.after(startDate)) {
          errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        }
        if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
          Date endDate = DateUtil.parseDate((String) prescription.get("end_date"));
          if (startDate.after(endDate)) {
            errMap.addError("end_date", "exception.notvalid.prescription.enddate");
          }
        }
      } catch (java.text.ParseException exe) {
        errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        logger.error("", exe);
      }
    }
    try {
      if (prescription.get("duration") != null && (prescription.get("duration_units") == null
          || !PrescriptionsService.DURATION_UNIT_VALUES
              .contains((String) prescription.get("duration_units")))) {
        errMap.addError("duration_units", "exception.notValid.prescription.durationUnits");
      }

      if (StringUtils.isEmpty((String) prescription.get("medication_type"))) {
        errMap.addError("medication_type", "exception.notnull.prescription.medicationType");
      } else if (!medicationTypes.contains(prescription.get("medication_type"))) {
        errMap.addError("medication_type", "exception.notvalid.prescription.medicationType");
      }
    } catch (Exception exe) {
      logger.error("", exe);
    }
    if (prescription.get("duration") != null
        && (prescription.get("duration_units") == null || !PrescriptionsService.DURATION_UNIT_VALUES
            .contains((String) prescription.get("duration_units")))) {
      errMap.addError("duration_units", "exception.notValid.prescription.durationUnits");
    }

    if (prescription.get("medication_type") == null
        || "".equals(prescription.get("medication_type"))) {
      errMap.addError("medication_type", "exception.notnull.prescription.medicationType");
    } else if (!medicationTypes.contains(prescription.get("medication_type"))) {
      errMap.addError("medication_type", "exception.notvalid.prescription.medicationType");
    }

    if (prescription.get("prescribed_qty") == null) {
      errMap.addError("prescribed_qty", "exception.notnull.prescription.prescribedQty");
    } else if ((Integer) prescription.get("prescribed_qty") <= 0) {
      errMap.addError("prescribed_qty", "exception.negative.prescription.prescribedQty");
    }

    if (generics) {
      if (prescription.get("generic_code") == null || prescription.get("generic_code").equals("")) {
        errMap.addError("generic_code", "exception.notnull.prescription.genericCode");
      } else if (!ValidationUtils.isKeyValid("generic_name", prescription.get("generic_code"),
          "generic_code")) {
        errMap.addError("generic_code", "exception.notValid.prescription.genericCode");
      }
    } else if (usesStores && operation.equals("insert")) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("medicine_id", Integer.parseInt((String) prescription.get("item_id")));
      BasicDynaBean item = ValidationUtils.isKeyValid("store_item_details", params);
      if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
        errMap.addError("item_id", "exception.notValid.prescription.itemId");
        return false;
      }
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validates investigation.
   *
   * @param prescription the map
   * @param errMap the ValidationErrorMap
   * @param operation the string
   * @return the boolean value
   */
  public boolean validateInvestigation(Map<String, Object> prescription, ValidationErrorMap errMap,
      String operation) {
    if (prescription.containsKey("priority")
        && !PrescriptionsService.PRIORITY_VALUES.contains(prescription.get("priority"))) {
      errMap.addError("priority", "exception.notvalid.prescription.priority");
    }
    if (operation.equals("insert")) {
      if (prescription.get("is_package") == null) {
        errMap.addError("is_package", "exception.notnull.prescription.isPackage");
        return false;
      }
      BasicDynaBean item;
      Integer packCat = null;
      Map<String, Object> params = new HashMap<String, Object>();
      if (!(boolean) prescription.get("is_package")) {
        params.put("test_id", prescription.get("item_id"));
        item = ValidationUtils.isKeyValid("diagnostics", params);
        if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
          errMap.addError("item_id", "exception.notValid.prescription.itemId");
        }
      } else {
        params.put("package_id", Integer.parseInt((String) prescription.get("item_id")));
        item = ValidationUtils.isKeyValid("packages", params);
        packCat = (Integer) item.get("package_category_id");
        if (item == null || !item.get("status").equals("A")) {
          errMap.addError("item_id", "exception.notValid.prescription.itemId");
        } else if (!(packCat == -3) && !(packCat == -2)) {
          errMap.addError("errMsg", "exception.diagnosticspackage.prescription");
        }
      }
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validates service.
   *
   * @param prescription the map
   * @param errMap the ValidationErrorMap
   * @param operation the string
   * @return the boolean value
   */
  public boolean validateService(Map<String, Object> prescription, ValidationErrorMap errMap,
      String operation) {
    if (prescription.containsKey("priority")
        && !PrescriptionsService.PRIORITY_VALUES.contains(prescription.get("priority"))) {
      errMap.addError("priority", "exception.notvalid.prescription.priority");
    }
    if (prescription.get("prescribed_qty") == null) {
      errMap.addError("prescribed_qty", "exception.notnull.prescription.prescribedQty");
    } else if ((Integer) prescription.get("prescribed_qty") <= 0) {
      errMap.addError("prescribed_qty", "exception.negative.prescription.prescribedQty");
    }

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("service_id", prescription.get("item_id"));
    BasicDynaBean item = ValidationUtils.isKeyValid("services", params);
    if (item.get("tooth_num_required").equals("Y")) {
      if (genprefs.getAllPreferences().get("tooth_numbering_system").equals("U")) {
        if (prescription.get("tooth_unv_number") == null
            || prescription.get("tooth_unv_number").equals("")) {
          errMap.addError("tooth_unv_number", "exception.notnull.prescription.toothnumber");
        }
      } else if (prescription.get("tooth_fdi_number") == null
          || prescription.get("tooth_fdi_number").equals("")) {
        errMap.addError("tooth_fdi_number", "exception.notnull.prescription.toothnumber");
      }
    }
    if (operation.equals("insert")) {
      if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
        errMap.addError("item_id", "exception.notValid.prescription.itemId");
        return false;
      }
    }
    return errMap.getErrorMap().isEmpty();
  }

  /**
   * Validate operation.
   *
   * @param prescription the map
   * @param errMap the ValidationErrorMap
   * @param operation the string
   * @return the boolean value
   */
  public boolean validateOperation(Map<String, Object> prescription, ValidationErrorMap errMap,
      String operation) {
    if (operation.equals("insert")) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("op_id", prescription.get("item_id"));
      BasicDynaBean item = ValidationUtils.isKeyValid("operation_master", params);
      if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
        errMap.addError("item_id", "exception.notValid.prescription.itemId");
        return false;
      }
    }
    return true;
  }

  /**
   * Validate doctor.
   *
   * @param prescription the map
   * @param errMap the ValidationErrorMap
   * @param operation the string
   * @return the boolean value
   */
  public boolean validateDoctor(Map<String, Object> prescription, ValidationErrorMap errMap,
      String operation) {
    if (operation.equals("insert")) {
      Map<String, Object> params = new HashMap<>();
      String referralType = (String) prescription.get("presc_activity_type");
      if (referralType != null && referralType.equals("DOC")) {
        params.put("doctor_id", prescription.get("item_id"));
        BasicDynaBean item = ValidationUtils.isKeyValid("doctors", params);
        if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
          errMap.addError("item_id", "exception.notValid.prescription.itemId");
          return false;
        }
      } else if (referralType != null && referralType.equals("DEPT")) {
        params.put("dept_id", prescription.get("item_id"));
        BasicDynaBean item = ValidationUtils.isKeyValid("department", params);
        if (item == null || (item.get("status") != null && !item.get("status").equals("A"))) {
          errMap.addError("item_id", "exception.notValid.prescription.itemId");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Validate non hospital items.
   *
   * @param prescription the map
   * @param sectionParameters the form param
   * @param errMap the ValidationErrorMap
   * @param operation the string
   * @return the boolean value
   */
  public boolean validateNonHospital(Map<String, Object> prescription, Integer sectionId,
      FormParameter sectionParameters, ValidationErrorMap errMap, String operation) {
    if (prescription.get("item_name") == null || "".equals(prescription.get("item_name"))) {
      errMap.addError("item_name", "exception.notnull.prescription.itemname");
    }
    if (prescription.get("prescribed_qty") != null
        && (Integer) prescription.get("prescribed_qty") <= 0) {
      errMap.addError("prescribed_qty", "exception.negative.prescription.prescribedQty");
    }

    if (prescription.containsKey("time_of_intake")
        && !timeofintakeTypes.contains(prescription.get("time_of_intake"))) {
      errMap.addError("time_of_intake", "exception.notvalid.prescription.timeofintake");
    }

    if (prescription.containsKey("priority")
        && !PrescriptionsService.PRIORITY_VALUES.contains(prescription.get("priority"))) {
      errMap.addError("priority", "exception.notvalid.prescription.priority");
    }

    if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
      if (prescription.get("start_date") == null || prescription.get("start_date").equals("")) {
        errMap.addError("start_date", "exception.notnull.prescription.startdate");
      }
    }

    if (!StringUtils.isEmpty((String) prescription.get("start_date")) && !sectionParameters
        .getFormType().equals(FormComponentsService.FormType.Form_IP.toString())) {
      Map<String, Object> params = new HashMap<>();
      params.put("consultation_id", sectionParameters.getId());
      BasicDynaBean consBean = ValidationUtils.isKeyValid("doctor_consultation", params);
      try {
        Date startDate = DateUtil.parseDate((String) prescription.get("start_date"));
        Date consDate = DateUtil.getDatePart((Timestamp) consBean.get("start_datetime"));
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        consDate = dateFormatter.parse(consDate.toString());
        if (consDate.after(startDate)) {
          errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        }
        if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
          Date endDate = DateUtil.parseDate((String) prescription.get("end_date"));
          if (startDate.after(endDate)) {
            errMap.addError("end_date", "exception.notvalid.prescription.enddate");
          }
        }
      } catch (java.text.ParseException exe) {
        logger.error("", exe);
      }
    }

    if (prescription.get("start_date") != null && !prescription.get("start_date").equals("")
        && sectionParameters.getFormType().equals(FormComponentsService.FormType.Form_IP.toString())
        && sectionId.equals(-22)) {

      Map<String, Object> params = new HashMap<>();
      params.put("patient_id", sectionParameters.getId());
      BasicDynaBean visitBean = ValidationUtils.isKeyValid("patient_registration", params);
      try {
        Date startDate = DateUtil.parseDate((String) prescription.get("start_date"));

        Date regDate = (Date) visitBean.get("reg_date");

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        regDate = dateFormatter.parse(regDate.toString());
        if (regDate.after(startDate)) {
          errMap.addError("start_date", "exception.notvalid.prescription.startdate");
        }
        if (prescription.get("end_date") != null && !prescription.get("end_date").equals("")) {
          Date endDate = DateUtil.parseDate((String) prescription.get("end_date"));
          if (startDate.after(endDate)) {
            errMap.addError("end_date", "exception.notvalid.prescription.enddate");
          }
        }
      } catch (java.text.ParseException exe) {
        logger.error("", exe);
      }
    }

    if (prescription.get("duration") != null
        && (prescription.get("duration_units") == null || !PrescriptionsService.DURATION_UNIT_VALUES
            .contains((String) prescription.get("duration_units")))) {
      errMap.addError("duration_units", "exception.notValid.prescription.durationUnits");
    }

    return errMap.getErrorMap().isEmpty();
  }
}
