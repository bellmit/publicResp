package com.insta.hms.core.patient.registration;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.validation.EmailIdRule;
import com.insta.hms.common.validation.PhoneNumberRule;
import com.insta.hms.core.clinical.order.master.OrderValidator;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.outpatientlist.PatientSearchRepository;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.govtidentifiers.GovtIdentifierService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplantypes.InsurancePlanTypeService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.mdm.sponsors.SponsorTypeService;
import com.insta.hms.mdm.tpas.TpaService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class RegistrationValidator.
 *
 * @author chetan
 */
@Component
public class RegistrationValidator {

  /** The reg pref service. */
  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;

  /** The govt identifier service. */
  @LazyAutowired
  private GovtIdentifierService govtIdentifierService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The patient category service. */
  @LazyAutowired
  private PatientCategoryService patientCategoryService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The phone no validator. */
  @LazyAutowired
  private PhoneNumberRule phoneNoValidator;

  /** The email validator. */
  @LazyAutowired
  private EmailIdRule emailValidator;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The dept service. */
  @LazyAutowired
  private DepartmentService deptService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The doc consulatation service. */
  @LazyAutowired
  private DoctorConsultationService docConsulatationService;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The pat ins plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patInsPlansService;

  /** The pat ins plan details service. */
  @LazyAutowired
  private PatientInsurancePlanDetailsService patInsPlanDetailsService;

  /** The pat ins policy details service. */
  @LazyAutowired
  private PatientInsurancePolicyDetailsService patInsPolicyDetailsService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The sponsor type service. */
  @LazyAutowired
  private SponsorTypeService sponsorTypeService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The insurance plan type service. */
  @LazyAutowired
  private InsurancePlanTypeService insurancePlanTypeService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The patient document service. */
  @LazyAutowired
  private PatientDocumentService patientDocumentService;

  /** The patient search repository. */
  @LazyAutowired
  private PatientSearchRepository patientSearchRepository;

  /** The center preferences service. */
  @LazyAutowired
  CenterPreferencesService centerPreferencesService;

  /** The order validator. */
  @LazyAutowired
  private OrderValidator orderValidator;

  /**
   * Validate visit info new visit.
   *
   * @param visitBean
   *          the visit bean
   * @param patientBean
   *          the patient bean
   * @param errors
   *          the errors
   * @return true, if successful
   * @throws ParseException
   *           the parse exception
   */
  public boolean validateVisitInfoNewVisit(BasicDynaBean visitBean, BasicDynaBean patientBean,
      List<Map> insuranceList, ValidationErrorMap errors) throws ParseException {
    if (visitBean == null || errors == null) {
      return false;
    }
    boolean success = true;
    boolean newReg = false;
    if (patientBean != null && patientBean.get("mr_no") != null
        && !"".equals(patientBean.get("mr_no"))) {
      newReg = true;
    }

    if (visitBean.get("dept_name") == null || "".equals(visitBean.get("dept_name"))) {
      errors.addError("dept_name", "js.registration.patient.consulting.department.required");
      success = false;
    }

    success = validatevisitFields(visitBean, patientBean, errors) && success;

    success = validateVisitInfoPrefFields(visitBean, errors) && success;

    success = validateCustomFields(visitBean, "visit", errors,
        (Boolean) visitBean.get("is_er_visit"))
        && success;
    Boolean isUnidentifiedPatient = patientBean.get("is_unidentified_patient") != null 
        ? (Boolean) patientBean.get("is_unidentified_patient") : false;
    if (!newReg) {
      if (visitBean.get("op_type") != null && "D".equals(visitBean.get("op_type"))
          && (visitBean.get("doctor") == null || "".equals(visitBean.get("doctor")))
          && (Boolean) visitBean.get("is_er_visit")
          && isUnidentifiedPatient) {

        errors.addError("doctor_name", "js.registration.patient.follow.up.doctor.required");
        success = false;
      }
      
      String sponsorId = null;      
      if (!insuranceList.isEmpty()) {
        sponsorId = (String) insuranceList.get(0).get("sponsor_id");
      }

      success = success
          && validateVisitValidity(visitBean, (String) patientBean.get("mr_no"), sponsorId, errors);
    }

    return success;
  }

  /**
   * Validate prescribing doctor.
   *
   * @param params
   *          the params
   * @param visitParams
   *          the visit params
   * @param nestedException
   *          the nested exception
   * @param errors
   *          the errors
   */
  public void validatePrescribingDoctor(Map<String, Object> params,
      Map<String, Object> visitParams, Map<String, Object> nestedException,
      ValidationErrorMap errors) {
    try {
      Map<String, Object> validateParams = new HashMap<String, Object>();
      Map<String, Map<String, List<Map<String, Object>>>> orderParams1 =
          (Map<String, Map<String, List<Map<String, Object>>>>) params.get("ordered_items");
      Map<String, Object> params1 = new HashMap<String, Object>();
      params1.put("bill_no", (Map<String, Map<String, List<Map<String, Object>>>>) orderParams1);
      validateParams.put("ordered_items", orderParams1);
      validateParams.put("visit", visitParams);
      Map<String, List<Object>> orderObjectList = orderValidator
          .setPrescribedDateAndDoctor(orderParams1);
      orderValidator.validatePrescDoctor(validateParams);
    } catch (ValidationException ex) {
      List<Map> orderErrors = new ArrayList<Map>();
      errors.addError("prescribing_doctor", ex.getMessage());
      ValidationException ex1 = new ValidationException(errors);
      orderErrors.add(ex1.getErrors());
      nestedException.put("ordered_items", orderErrors);
    }
  }

  /**
   * Validatevisit fields.
   *
   * @param visitBean
   *          the visit bean
   * @param patientBean
   *          the patient bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validatevisitFields(BasicDynaBean visitBean, BasicDynaBean patientBean,
      ValidationErrorMap errors) {
    if (visitBean == null || errors == null || patientBean == null) {
      return false;
    }
    Boolean isUnidentifiedPatient = patientBean.get("is_unidentified_patient") != null 
        ? (Boolean) patientBean.get("is_unidentified_patient") : false;
    boolean success = true;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    if ("Y".equals(regPref.get("admitting_doctor_mandatory"))
        && visitBean.get("doctor") == null
        && !((Boolean) visitBean.get("is_er_visit") || isUnidentifiedPatient)) {
      errors.addError("doctor_name", "js.registration.patient.consulting.doctor.required");
      success = false;
    }

    // F = Followup without consulation; D = Followup with consultation
    if (("F".equals(visitBean.get("op_type")) || "D".equals(visitBean.get("op_type")))
        && visitBean.get("doctor") == null) {
      errors.addError("doctor_name", "js.registration.patient.consulting.doctor.required");
      success = false;
    }

    success = validateDepartmentGender((String) visitBean.get("dept_name"),
        (String) patientBean.get("patient_gender"), errors)
        && success;

    return success;
  }

  /**
   * Validate visit validity.
   *
   * @param bean
   *          the bean
   * @param mrNo
   *          the mr no
   * @param errors
   *          the errors
   * @return true, if successful
   * @throws ParseException
   *           the parse exception
   */
  private boolean validateVisitValidity(BasicDynaBean bean, 
      String mrNo, String sponsorId, ValidationErrorMap errors)
      throws ParseException {
    if (bean == null || errors == null) {
      return false;
    }
    boolean success = true;

    if (bean.get("doctor") != null && !"".equals(bean.get("doctor"))) {
      Map<String, Object> visitMap = registrationService.getPatientVisitType(mrNo,
          (String) bean.get("doctor"), sponsorId, false,null);
      if (!visitMap.get("op_type").equals("F") && "F".equals(bean.get("op_type"))) {
        errors.addError("op_type",
            "js.registration.patient.selected.doctor.visit.validity.has.expired.string");
      }
    }

    return success;
  }

  /**
   * Validate visit info pref fields.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validateVisitInfoPrefFields(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null || errors == null) {
      return false;
    }
    boolean success = true;
    Boolean isErVisit = (Boolean) bean.get("is_er_visit");
    Map regPrefs = regPrefService.getRegistrationPreferences().getMap();
    String complaintValidate = (String) regPrefs.get("complaint_field_validate");
    String visitType = (String) bean.get("visit_type");
    if ("A".equals(complaintValidate) || ("I".equals(complaintValidate) && "I".equals(visitType))
        || ("O".equals(complaintValidate) && "O".equals(visitType))) {
      if ((bean.get("complaint") == null || "".equals(bean.get("complaint"))) && !isErVisit) {
        errors.addError("complaint", "js.registration.patient.complaint.required");
        success = false;
      }
    }
    String referredbyValidate = (String) regPrefs.get("referredby_field_validate");
    if ("A".equals(referredbyValidate) 
        || ("I".equals(referredbyValidate) && "I".equals(visitType))
        || ("O".equals(referredbyValidate) && "O".equals(visitType))) {

      if ((bean.get("reference_docto_id") == null || "".equals(bean.get("reference_docto_id")))
          && !isErVisit) {
        errors.addError("refdoctorname", "js.registration.patient.referral.required");
        success = false;
      }
    }
    return success;
  }

  /**
   * Validate patient demography new visit.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validatePatientDemographyNewVisit(BasicDynaBean bean, 
      ValidationErrorMap errors) {
    return validatePatientDemographyNewVisit(bean, errors, false,0);
  }

  /**
   * Validate patient demography new visit.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  public boolean validatePatientDemographyNewVisit(BasicDynaBean bean, ValidationErrorMap errors,
      Boolean isErVisit, int centerId) {
    if (bean == null || errors == null) {
      return false;
    }
    Map genPrefs = genPrefService.getPreferences().getMap();
    Map regPrefs = regPrefService.getRegistrationPreferences().getMap();
    boolean success = true;
    
    if (centerId == 0 && RequestContext.getHttpRequest() != null) {
      centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    }
    if ((genPrefs.get("max_centers_inc_default") != null && (Integer) genPrefs
        .get("max_centers_inc_default") > 1) && centerId == 0) {
      success = false;
      errors.addError("patient_name",
          "js.registration.patient.registration.allowed.only.for.center.users");
    }

    if (bean.get("mr_no") != null && !"".equals(bean.get("mr_no"))) {
      String allowMultipleActiveVisits = (String) regPrefs.get("allow_multiple_active_visits");
      allowMultipleActiveVisits = (allowMultipleActiveVisits != null && !allowMultipleActiveVisits
          .equals("")) ? allowMultipleActiveVisits : "N";
      if (allowMultipleActiveVisits.equals("N")) {
        BasicDynaBean activeVisit = patientRegistrationRepository.getPatientLatestVisit(
            (String) bean.get("mr_no"), true, "o", null);
        if (activeVisit != null) {
          success = false;
          errors.addError("patient_name",
              "registration.patient.action.message.error.active.visit.exists");
        }
      }
    }

    success = validatePatientBasicInfo(bean, "O", errors, isErVisit) && success;
    if (RequestContext.getHttpRequest() != null) {
      success = validatePatientAddnlFields(bean, "O", errors, isErVisit) && success;
      success = validateCustomFields(bean, "patient", errors, isErVisit) && success;
    }

    return success;
  }

  /**
   * Validate patient visit info update.
   *
   * @param newBean
   *          the new bean
   * @param oldBean
   *          the old bean
   * @param gender
   *          the gender
   * @param errors
   *          the errors
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean validatePatientVisitInfoUpdate(BasicDynaBean newBean, BasicDynaBean oldBean,
      String gender, ValidationErrorMap errors) {
    if (newBean == null) {
      return false;
    }
    boolean success = true;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();

    if (newBean.get("mr_no") == null) {
      errors.addError("mr_no", "js.scheduler.doctorscheduler.nameWithMRNO.required");
      success = false;
    }

    if (newBean.get("patient_id") == null) {
      errors.addError("patient_id", "exception.registration.patient.visit.id");
      success = false;
    }

    if (!"O".equals(newBean.get("op_type"))) {
      if (newBean.get("dept_name") == null) {
        errors.addError("dept_name", "js.registration.editvisitdetails.departmentisrequired");
        success = false;
      }
      if ("Y".equals(regPref.get("admitting_doctor_mandatory")) 
          && newBean.get("doctor") == null) {
        errors.addError("doctor_name", "js.registration.editvisitdetails.doctorisrequired");
        success = false;
      }
    }

    if (newBean.get("reg_date") == null) {
      errors.addError("reg_date", "js.registration.editvisitdetails.registrationdateisrequired");
      success = false;
    } else {
      if (!validateDate(newBean, "reg_date", false, errors)) {
        success = false;
      }
    }

    if (newBean.get("reg_time") == null) {
      errors.addError("reg_time", "js.registration.editvisitdetails.registrationtimeisrequired");
      success = false;
    }

    success = validateDepartmentGender((String) newBean.get("dept_name"), gender, errors)
        && success;

    success = validateOPType(newBean, oldBean, errors) && success;

    success = validateCustomFields(newBean, "visit", errors, (Boolean) newBean.get("is_er_visit"))
        && success;

    return success;
  }

  /**
   * Validate OP type.
   *
   * @param newBean
   *          the new bean
   * @param oldBean
   *          the old bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateOPType(BasicDynaBean newBean, BasicDynaBean oldBean,
      ValidationErrorMap errors) {
    if (newBean == null || oldBean == null) {
      return false;
    }

    if (oldBean.get("op_type") == null || "".equals(oldBean.get("op_type"))) {
      return true;
    }
    boolean success = true;

    if ("o".equals(newBean.get("visit_type")) && !"O".equals(oldBean.get("op_type"))) {
      success = validateDoctorDepartment(newBean, oldBean, errors) && success;
      success = validateSelectedOpType(newBean, oldBean, errors) && success;
    }

    return success;
  }

  /**
   * Validate selected op type.
   *
   * @param newBean
   *          the new bean
   * @param oldBean
   *          the old bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateSelectedOpType(BasicDynaBean newBean, BasicDynaBean oldBean,
      ValidationErrorMap errors) {
    if (newBean == null || oldBean == null) {
      return false;
    }
    boolean success = true;
    String newOpType = (String) newBean.get("op_type");
    String existingOpType = (String) oldBean.get("op_type");
    if (newOpType.equals(existingOpType)) {
      return true;
    }

    if ("M".equals(existingOpType)) {
      if ("F".equals(newOpType)) {
        if (!validateMainToFollowUp(newBean, errors)) {
          return false;
        }
      } else if ("D".equals(newOpType)) {
        if (!validateMainToFollowUpNoCons(newBean, errors)) {
          return false;
        }
      } else if ("R".equals(newOpType)) {
        if (!validateMainToRevisit(newBean, errors)) {
          return false;
        }
      }
    } else if ("F".equals(existingOpType)) {
      if ("D".equals(newOpType)) {
        if (!validateFollowUpToFollowUpNoCons(newBean, errors)) {
          return false;
        }
      } else if ("R".equals(newOpType)) {
        if (!validateFollowUpToRevisit(newBean, errors)) {
          return false;
        }
      } else if ("M".equals(newOpType)) {
        if (!validateFollowUpToMain(newBean, errors)) {
          return false;
        }
      }
    } else if ("D".equals(existingOpType)) {

      if ("F".equals(newOpType)) {
        if (!validateFollowUpNoConsToFollowUp(newBean, errors)) {
          return false;
        }
      } else if ("R".equals(newOpType)) {
        if (!validateFollowUpNoConsToRevisit(newBean, errors)) {
          return false;
        }
      } else if ("M".equals(newOpType)) {
        if (!validateFollowUpNoConsToMain(newBean, errors)) {
          return false;
        }
      }
    } else if ("R".equals(existingOpType)) {
      if ("M".equals(newOpType)) {
        if (!validateRevisitToMain(newBean, errors)) {
          return false;
        }
      } else if ("F".equals(newOpType)) {
        if (!validateRevisitToFollowUp(newBean, errors)) {
          return false;
        }
      } else if ("D".equals(newOpType)) {
        if (!validateRevisitToFollowUpNoCons(newBean, errors)) {
          return false;
        }
      }
    }

    return success;
  }

  /**
   * Validate revisit to follow up no cons.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateRevisitToFollowUpNoCons(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, 
        doctorId, "Follow up (without Consultation)", errors)) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", mainVisit.get("main_visit_id"));
    return true;
  }

  /**
   * Validate revisit to follow up.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateRevisitToFollowUp(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, doctorId, "Follow up (with Consultation)", errors)) {
      return false;
    } else if (!isVisitWithinValidity(previousDoctorVisits, doctorId)) {
      errors
          .addError("op_type", "exception.registration.patient.visitvalidityexpired", Arrays
              .asList(mainVisit.get("main_visit_id").toString(), "Follow up (with Consultation)"));
      return false;
    }
    return true;
  }

  /**
   * Validate revisit to main.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateRevisitToMain(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", bean.get("patient_id"));
    return true;
  }

  /**
   * Validate follow up no cons to main.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpNoConsToMain(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, "Main Visit", errors)) {
      return false;
    }
    if (doctorId == null) {
      return true;
    }

    // setting the main visit id.
    bean.set("main_visit_id", bean.get("patient_id"));
    return true;
  }

  /**
   * Validate follow up no cons to revisit.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpNoConsToRevisit(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, "Revisit", errors)) {
      return false;
    }
    if (doctorId == null) {
      return true;
    }
    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, doctorId, "Revisit", errors)) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", bean.get("patient_id"));
    return true;
  }

  /**
   * Validate follow up no cons to follow up.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpNoConsToFollowUp(BasicDynaBean bean, 
      ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = 
        registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, 
        "Follow up (with Consultation)", errors)) {
      return false;
    }
    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, 
        doctorId, "Follow up (with Consultation)", errors)) {
      return false;
    } else if (!isVisitWithinValidity(previousDoctorVisits, doctorId)) {
      errors
          .addError("op_type", 
              "exception.registration.patient.visitvalidityexpired", Arrays
              .asList(mainVisit.get("main_visit_id").toString(), 
                  "Follow up (with Consultation)"));
      return false;
    }
    return true;
  }

  /**
   * Validate follow up to main.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpToMain(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");

    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, "Main Visit", errors)) {
      return false;
    }
    if (doctorId == null) {
      return true;
    }
    // setting the main visit id.
    bean.set("main_visit_id", bean.get("patient_id"));
    return true;
  }

  /**
   * Validate follow up to revisit.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpToRevisit(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, "Revisit", errors)) {
      return false;
    }

    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, doctorId, "Revisit", errors)) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", bean.get("patient_id"));
    return true;
  }

  /**
   * Validate follow up to follow up no cons.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateFollowUpToFollowUpNoCons(BasicDynaBean bean, 
      ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousDoctorVisits = 
        registrationService.getPatientPreviousMainVisits(
        mrNo, doctorId);
    if (!isPreviousVisitDoctorExists(previousDoctorVisits, 
        "Follow up (without Consultation)",
        errors)) {
      return false;
    }

    BasicDynaBean mainVisit = getMainVisit(previousDoctorVisits);
    if (!validateDoctorConsultation(mainVisit, doctorId, 
        "Follow up (without Consultation)", errors)) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", mainVisit.get("main_visit_id"));
    return true;
  }

  /**
   * Validate main to revisit.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateMainToRevisit(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousMainVisits = registrationService.getPreviousMainVisits(mrNo,
        doctorId);
    if (!isPreviousMainExists(previousMainVisits, doctorId, (String) bean.get("dept_name"),
        "Revisit", errors)) {
      return false;
    }
    BasicDynaBean mainVisit = getMainVisit(previousMainVisits);
    String mainVisitId = mainVisit != null ? (String) mainVisit.get("main_visit_id") : null;
    if (!validateDoctorConsultation(mainVisit, doctorId, "Revisit", errors)) {
      return false;
    } else if (!isVisitWithinValidity((String) bean.get("mr_no"), doctorId)) {
      errors.addError("op_type", "exception.registration.patient.visitvalidityexpired",
          Arrays.asList(mainVisitId, "Revisit"));
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", mainVisit.get("main_visit_id"));
    return true;
  }

  /**
   * Validate main to follow up no cons.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateMainToFollowUpNoCons(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousMainVisits = registrationService.getPreviousMainVisits(mrNo,
        doctorId);
    BasicDynaBean mainVisit = getMainVisit(previousMainVisits);
    if (!isPreviousMainExists(previousMainVisits, doctorId, (String) bean.get("dept_name"),
        "Follow up (without Consultation)", errors)) {
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", mainVisit != null ? (String) mainVisit.get("main_visit_id") : null);
    return true;
  }

  /**
   * Validate main to follow up.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateMainToFollowUp(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String doctorId = (String) bean.get("doctor");
    String mrNo = (String) bean.get("mr_no");
    List<BasicDynaBean> previousMainVisits = registrationService.getPreviousMainVisits(mrNo,
        doctorId);
    if (!isPreviousMainExists(previousMainVisits, doctorId, (String) bean.get("dept_name"),
        "Follow up (with Consultation)", errors)) {
      return false;
    }
    BasicDynaBean mainVisit = getMainVisit(previousMainVisits);
    String mainVisitId = mainVisit != null ? (String) mainVisit.get("main_visit_id") : null;
    if (!validateDoctorConsultation(mainVisit, doctorId, "Follow up (with Consultation)", errors)) {
      return false;
    } else if (!isVisitWithinValidity((String) bean.get("mr_no"), doctorId)) {
      errors.addError("op_type", "exception.registration.patient.visitvalidityexpired",
          Arrays.asList(mainVisitId, "Follow up (with Consultation)"));
      return false;
    }
    // setting the main visit id.
    bean.set("main_visit_id", mainVisitId);
    return true;
  }

  /**
   * Checks if is visit within validity.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @return true, if is visit within validity
   */
  private boolean isVisitWithinValidity(String mrNo, String doctorId) {
    List<BasicDynaBean> previousDocVisits = registrationService.getPatientPreviousMainVisits(mrNo,
        doctorId);
    return isVisitWithinValidity(previousDocVisits, doctorId);
  }

  /**
   * Checks if is visit within validity.
   *
   * @param previousDocVisits
   *          the previous doc visits
   * @param doctorId
   *          the doctor id
   * @return true, if is visit within validity
   */
  private boolean isVisitWithinValidity(List<BasicDynaBean> previousDocVisits, String doctorId) {
    if (previousDocVisits == null) {
      return false;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(params);
    if (doctorBean == null) {
      return false;
    }
    String deptId = (String) doctorBean.get("dept_id");

    BigDecimal validityDays = (BigDecimal) doctorBean.get("op_consultation_validity");
    Integer maxVisits = (Integer) doctorBean.get("allowed_revisit_count");

    int revisitCount = 0;
    boolean visitWithinValidity = false;
    String visitTypeDependence = (String) regPrefService.getRegistrationPreferences().get(
        "visit_type_dependence");
    for (int i = 0; i < previousDocVisits.size(); i++) {
      BasicDynaBean cons = previousDocVisits.get(i);

      // Based on visit type dependence (Doctor/Speciality) the op-type is
      // determined.
      if (("D".equals(visitTypeDependence) && doctorId.equals(cons.get("doctor_name")))
          || ("S".equals(visitTypeDependence) && deptId.equals(cons.get("dept_name")))) {
        Date visitDate = new Date(((java.sql.Timestamp) cons.get("visited_date")).getTime());
        revisitCount++;
        BigDecimal dayDiff = new BigDecimal((new Date().getTime() - visitDate.getTime()) / 60 / 60
            / 24 / 1000);
        if (dayDiff.compareTo(validityDays) <= 0) {
          visitWithinValidity = true;
        }
        if (!visitWithinValidity) {
          break;
        }
      }
    }
    return visitWithinValidity && (revisitCount <= maxVisits);
  }

  /**
   * Validate doctor consultation.
   *
   * @param visit
   *          the visit
   * @param doctorId
   *          the doctor id
   * @param opType
   *          the op type
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateDoctorConsultation(BasicDynaBean visit, String doctorId, String opType,
      ValidationErrorMap errors) {
    if (visit == null || errors == null) {
      return false;
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(params);
    List<BasicDynaBean> docConsultations = docConsulatationService
        .listVisitConsultations((String) visit.get("patient_id"));
    BasicDynaBean oldMainDoctorCons = getConsultation(docConsultations, doctorBean);
    if (oldMainDoctorCons == null) {
      errors.addError("op_type",
          "exception.registration.patient.cannot.change.op.type.no.consultations",
          Arrays.asList(visit.get("visited_date").toString(), opType));
      return false;
    } else if (oldMainDoctorCons.get("cancel_status") != null
        && "C".equals(oldMainDoctorCons.get("cancel_status"))) {
      errors.addError(
          "op_type",
          "exception.registration.patient.cannot.change.op.type.consultations.cancelled",
          Arrays.asList(visit.get("visited_date").toString(),
              (String) doctorBean.get("doctor_name"), opType));
      return false;
    }
    return true;
  }

  /**
   * Gets the consultation.
   *
   * @param docConsultations
   *          the doc consultations
   * @param doctorBean
   *          the doctor bean
   * @return the consultation
   */
  private BasicDynaBean getConsultation(List<BasicDynaBean> docConsultations,
      BasicDynaBean doctorBean) {
    if (docConsultations == null || docConsultations.isEmpty()) {
      return null;
    }
    BasicDynaBean oldMainVisitDoctorCons = null;
    if (doctorBean == null) {
      return null;
    }
    String newDept = (String) doctorBean.get("dept_id");
    String visitTypeDependence = (String) regPrefService.getRegistrationPreferences().get(
        "visit_type_dependence");

    for (int i = 0; i < docConsultations.size(); i++) {
      BasicDynaBean docCons = docConsultations.get(i);
      // Based on visit type dependence the op-type is determined.
      if (("D".equals(visitTypeDependence) && docCons.get("doctor_id").equals(
          doctorBean.get("doctor_id")))
          || ("S".equals(visitTypeDependence) && docCons.get("dept_id").equals(newDept))) {
        oldMainVisitDoctorCons = docCons;
        break;
      }
    }
    return oldMainVisitDoctorCons;
  }

  /**
   * Gets the main visit.
   *
   * @param previousVisits
   *          the previous visits
   * @return the main visit
   */
  private BasicDynaBean getMainVisit(List<BasicDynaBean> previousVisits) {
    BasicDynaBean oldMainVisit = null;

    if (previousVisits != null && previousVisits.size() != 0) {
      for (int i = 0; i < previousVisits.size(); i++) {
        BasicDynaBean patVisit = previousVisits.get(i);
        if ("M".equals(patVisit.get("op_type")) || "R".equals(patVisit.get("op_type"))) {
          oldMainVisit = patVisit;
          break;
        }
      }
    }
    return oldMainVisit;
  }

  /**
   * Checks if is previous visit doctor exists.
   *
   * @param doctorVisits
   *          the doctor visits
   * @param OpType
   *          the op type
   * @param errors
   *          the errors
   * @return true, if is previous visit doctor exists
   */
  private boolean isPreviousVisitDoctorExists(
      List<BasicDynaBean> doctorVisits, String opType,
      ValidationErrorMap errors) {
    if (doctorVisits == null || doctorVisits.size() == 0) {
      errors.addError("op_type", 
          "exception.registration.patient.patienthasnodoctor.previousvisit",
          Arrays.asList(opType));
      return false;
    }
    return true;
  }

  /**
   * Checks if is previous main exists.
   *
   * @param previousVisits
   *          the previous visits
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param opType
   *          the op type
   * @param errors
   *          the errors
   * @return true, if is previous main exists
   */
  @SuppressWarnings("rawtypes")
  private boolean isPreviousMainExists(List<BasicDynaBean> previousVisits, String doctorId,
      String deptId, String opType, ValidationErrorMap errors) {
    if (previousVisits == null || previousVisits.size() == 0) {
      Map regPref = regPrefService.getRegistrationPreferences().getMap();
      Map<String, Object> params = new HashMap<String, Object>();

      if ("D".equals(regPref.get("visit_type_dependence"))) {
        params.put("doctor_id", doctorId);
        BasicDynaBean doctorBean = doctorService.findByPk(params);
        String doctorName = doctorBean != null 
            ? (String) doctorBean.get("doctor_name") : "-";
        errors.addError("op_type",
            "exception.registration.patient.cannot.change.op.type.for.doctor",
            Arrays.asList(doctorName, opType));
      } else if ("S".equals(regPref.get("visit_type_dependence"))) {
        params.put("dept_id", deptId);
        BasicDynaBean deptBean = deptService.findByPk(params);
        String deptName = deptBean != null ? (String) deptBean.get("dept_name") : "-";
        errors.addError("op_type", 
            "exception.registration.patient.cannot.change.op.type.for.dept",
            Arrays.asList(deptName, opType));
      }
      return false;
    }
    return true;
  }

  /**
   * Validate doctor department.
   *
   * @param newBean
   *          the new bean
   * @param oldBean
   *          the old bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validateDoctorDepartment(BasicDynaBean newBean, BasicDynaBean oldBean,
      ValidationErrorMap errors) {
    if (newBean == null || oldBean == null) {
      return false;
    }
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    boolean success = true;
    String opType = (String) newBean.get("op_type");
    if ("F".equals(opType) || "D".equals(opType)) {
      if (newBean.get("doctor") == null) {
        errors.addError("doctor_name",
            "js.registration.editvisitdetails.consultingdoctorisrequired");
        success = false;
      }
      if ("D".equals(regPref.get("visit_type_dependence"))) {
        String admitttingDoc = (String) oldBean.get("doctor");
        String selectedDoc = (String) newBean.get("doctor");
        if (admitttingDoc != null 
            && selectedDoc != null 
            && !admitttingDoc.equals(selectedDoc)) {
          // getting the doctor names.
          Map<String, Object> params = new HashMap<String, Object>();
          params.put("doctor_id", admitttingDoc);
          BasicDynaBean admittingDocBean = doctorService.findByPk(params);
          String admittingDocName = 
              admittingDocBean != null ? (String) admittingDocBean
              .get("doctor_name") : "";
          params.put("doctor_id", selectedDoc);
          BasicDynaBean selectedDocBean = doctorService.findByPk(params);
          String selectedDocName = 
              admittingDocBean != null ? (String) selectedDocBean
              .get("doctor_name") : "";
          errors.addError("doctor_name",
              "exception.registration.patient.selected.doctor.not.same.as.admitting.doctor",
              Arrays.asList(selectedDocName, admittingDocName));
          success = false;
        }
      } else if ("S".equals(regPref.get("visit_type_dependence"))) {
        String admitttingDept = (String) oldBean.get("dept_name");
        String selectedDept = (String) newBean.get("dept_name");
        if (admitttingDept != null 
            && selectedDept != null 
            && !admitttingDept.equals(selectedDept)) {
          // getting the department names.
          Map<String, Object> params = new HashMap<String, Object>();
          params.put("dept_id", admitttingDept);
          BasicDynaBean admittingDocBean = deptService.findByPk(params);
          String admittingDeptName = 
              admittingDocBean != null ? (String) admittingDocBean
              .get("dept_name") : "";
          params.put("dept_id", selectedDept);
          BasicDynaBean selectedDocBean = deptService.findByPk(params);
          String selectedDeptName = admittingDocBean != null ? (String) selectedDocBean
              .get("dept_name") : "";

          errors.addError("dept_name",
              "exception.registration.patient.selected.dept.not.same.as.admitting.dept",
              Arrays.asList(selectedDeptName, admittingDeptName));
          success = false;
        }
      }
    }
    return success;
  }

  /**
   * Validate department gender.
   *
   * @param deptId
   *          the dept id
   * @param genderId
   *          the gender id
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateDepartmentGender(String deptId, 
      String genderId, ValidationErrorMap errors) {
    if (deptId != null) {
      Map<String, Object> deptParams = new HashMap<String, Object>();
      deptParams.put("dept_id", deptId);
      BasicDynaBean deptBean = deptService.findByPk(deptParams);
      if (deptBean != null) {
        String allowedGender = (String) deptBean.get("allowed_gender");
        String patientGender = getGenderMapping().get(genderId);
        if (!deptBean.get("allowed_gender").equals("ALL") && !allowedGender.equals(genderId)) {
          errors.addError("dept_name", "exception.registration.patient.department.gender",
              Arrays.asList((String) deptBean.get("dept_name"), patientGender));
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Validate patient demography update.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return the boolean
   */
  public Boolean validatePatientDemographyUpdate(BasicDynaBean bean, ValidationErrorMap errors,
      Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    boolean success = true;

    if (bean.get("mr_no") == null) {
      errors.addError("mr_no", "js.scheduler.doctorscheduler.nameWithMRNO.required");
      success = false;
    }

    success = validatePatientBasicInfo(bean, null, errors, isErVisit) && success;
    if (RequestContext.getHttpRequest() != null) {
      success = validatePatientAddnlFields(bean, null, errors, isErVisit) && success;
      success = validateCustomFields(bean, "patient", errors, isErVisit) && success;
    }

    return success;
  }

  /**
   * Validate custom fields.
   *
   * @param bean
   *          the bean
   * @param customFieldType
   *          the custom field type
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("unchecked")
  public boolean validateCustomFields(BasicDynaBean bean, String customFieldType,
      ValidationErrorMap errors, Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    boolean success = true;
    Boolean isUnidentifiedPatient = 
        PropertyUtils.isReadable(bean, "is_unidentified_patient") ? bean
        .get("is_unidentified_patient") != null 
        ? (Boolean) bean.get("is_unidentified_patient") : false : false;
    
    Map<String, Object> customFields = registrationService.getCustomFields();
    ArrayList<Map<String, Object>> customFieldList = (ArrayList<Map<String, Object>>) customFields
        .get(customFieldType + "_custom_fields");
    String customFieldName;
    String fieldType;
    for (Map<String, Object> entry : customFieldList) {
      customFieldName = (String) entry.get("name");
      fieldType = (String) entry.get("type");
      if (bean.get(customFieldName) == null) {
        if (entry.get("mandatory").equals("Y") && !(isUnidentifiedPatient || isErVisit)) {
          errors.addError(customFieldName, "js.registration.patient.is.required.string");
          success = false;
        } else {
          // if its null and not required no need to check for regex
          continue;
        }
      }
      if (!fieldType.equals("date") && bean.get(customFieldName) != null) {
        Pattern pattern = Pattern.compile((String) entry.get("validation"));
        Matcher matcher = pattern.matcher(bean.get(customFieldName).toString());
        if (!matcher.find()) {
          if (fieldType.equals("number")) {
            errors.addError(customFieldName,
                "js.registration.patient.allowed.only.eight.digit.number");
          }
          success = false;
        }
      }
    }
    return success;
  }

  /**
   * Validate patient addnl fields.
   *
   * @param bean
   *          the bean
   * @param preferenceType
   *          specify 'O' to check for Required for Op only 
   *          and 'I' for Required for Ip or null for
   *          nothing
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean validatePatientAddnlFields(BasicDynaBean bean, String preferenceType,
      ValidationErrorMap errors, Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    boolean success = true;
    Boolean isUnidentifiedPatient = bean.get("is_unidentified_patient") != null 
        ? (Boolean) bean.get("is_unidentified_patient") : false;

    Map regPref = regPrefService.getRegistrationPreferences().getMap();

    if (regPref.get("nextofkin_field_validate") != null
        && !(isUnidentifiedPatient || isErVisit)
        && (regPref.get("nextofkin_field_validate").equals("A") || regPref.get(
            "nextofkin_field_validate").equals(preferenceType))) {
      if (bean.get("relation") == null || bean.get("relation").toString().trim().isEmpty()) {
        errors.addError("relation", "js.registration.patient.next.of.kin.relation.name.required");
        success = false;
      }
      if (bean.get("patient_care_oftext") == null) {
        errors.addError("patient_care_oftext",
            "js.registration.patient.next.kin.relation.contact.required");
        success = false;
      }
      if (bean.get("patient_careof_address") == null) {
        errors.addError("patient_careof_address", "js.registration.patient.address.ph.required");
        success = false;
      }
    }

    Boolean portalAccess = (Boolean) bean.get("portal_access");
    Boolean mobileAccess = (Boolean) bean.get("mobile_access");
    if ((portalAccess != null && portalAccess)
        || (mobileAccess != null && mobileAccess)
        || (regPref.get("validate_email_id") != null
            && (regPref.get("validate_email_id").equals("A") || regPref.get("validate_email_id")
                .equals(preferenceType)) && !isErVisit)) {
      if (bean.get("email_id") == null) {
        errors.addError("email_id", "js.registration.patient.email.id.required");
        success = false;
      }
    }

    success = validatePassportDetails(bean, errors, true, isErVisit) && success;

    if (regPref.get("family_id") != null && !regPref.get("family_id").equals("")
        && regPref.get("family_id_show") != null
        && (regPref.get("family_id_show").equals("M") 
            || regPref.get("family_id_show").equals("D"))
        && bean.get("family_id") == null && !(isUnidentifiedPatient || isErVisit)) {
      if (regPref.get("family_id_validate") != null
          && (regPref.get("family_id_validate").equals("A") || regPref.get("family_id_validate")
              .equals(preferenceType))) {
        errors.addError("family_id", "exception.single.field.required.placeholder",
            Arrays.asList((regPref.get("family_id").toString())));
        success = false;
      }
    }

    if (regPref.get("nationality") != null && !regPref.get("nationality").equals("")
        && bean.get("nationality_id") == null && !(isUnidentifiedPatient || isErVisit)) {
      if (regPref.get("nationality_validate") != null
          && (regPref.get("nationality_validate").equals("A") || regPref
              .get("nationality_validate").equals(preferenceType))) {
        errors.addError("nationality_id", "exception.single.field.required.placeholder",
            Arrays.asList(regPref.get("nationality").toString()));
        success = false;
      }
    }
    
    if ((regPref.get("marital_status_required").equals("Y")
        || regPref.get("marital_status_required").equals("O"))
        && bean.get("marital_status_id") == null && !(isUnidentifiedPatient || isErVisit)) {
      errors.addError("marital_status_id", "js.registration.patient.marital.status.required");
      success = false;
    }

    if ((regPref.get("religion_required").equals("O")
        || regPref.get("religion_required").equals("Y")) && bean.get("religion_id") == null
        && !(isUnidentifiedPatient || isErVisit)) {
      errors.addError("religion_id", "js.registration.patient.religionrequired");
      success = false;
    }

    success = validatePatientIdentification(bean, errors, isErVisit) && success;

    success = validateGovtIdentifier(bean, errors, isErVisit) && success;

    return success;
  }

  /**
   * Format phone number.
   *
   * @param bean
   *          the bean
   * @param phoneField
   *          the phone field
   * @param defaultCodeField
   *          the default code field
   */
  public void formatPhoneNumber(BasicDynaBean bean, String phoneField, String defaultCodeField) {
    if (bean == null) {
      return;
    }
    String phoneNumber = (String) bean.get(phoneField);
    List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(phoneNumber, null);
    if (parts != null && !parts.isEmpty() && !parts.get(0).isEmpty()) {
      bean.set(defaultCodeField, "+" + parts.get(0));
    } else {
      int centerId = 0;
      if (RequestContext.getHttpRequest() != null) {
        centerId = RequestContext.getCenterId();
      }
      String defaultCode = centerService.getCountryCode(centerId);
      if (defaultCode == null) {
        defaultCode = centerService.getCountryCode(0);
      }
      if (defaultCode != null) {
        bean.set(defaultCodeField, "+" + defaultCode);
        if (!phoneNumber.startsWith("+")) {
          bean.set(phoneField, "+" + defaultCode + phoneNumber);
        }
      }
    }
  }

  /**
   * Validates patient basic information: - Phone number validation(If Enabled) - Validates
   * mandatory fields that are Salutation,Gender,Patient Name. - Validates optional fields(which
   * depend on preferences) that are Area and Address
   *
   * @param bean
   *          the bean
   * @param preferenceType
   *          specify 'O' to check for 
   *          Required for Op only and 'I' for Required for Ip or null for
   *          nothing
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean validatePatientBasicInfo(BasicDynaBean bean, String preferenceType,
      ValidationErrorMap errors, Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    Boolean success = true;
    Boolean isUnidentifiedPatient = bean.get("is_unidentified_patient") != null 
        ? (Boolean) bean.get("is_unidentified_patient") : false;
    Boolean mobileAccess = bean.get("mobile_access") != null ? (Boolean) bean.get("mobile_access")
        : false;
    // Phone number validation
    Boolean phoneValidationEnabled = genPrefService.getAllPreferences()
        .get("mobile_number_validation").equals("Y");
    String patientPhoneValidate = ((String) regPref.get("patientphone_field_validate"));
    if (((patientPhoneValidate != null
        && (patientPhoneValidate.equals("O") || patientPhoneValidate.equals("A"))
        && phoneValidationEnabled) || mobileAccess) && RequestContext.getHttpRequest() != null
        && bean.get("patient_phone") == null && !(isUnidentifiedPatient || isErVisit)) {
      errors.addError("patient_phone", "js.registration.patient.phone.no.required");
      success = false;
    }
    if (phoneValidationEnabled != null && phoneValidationEnabled) {
      if (bean.get("patient_phone") != null
          && !phoneNoValidator.apply(bean, new String[] { "patient_phone" }, errors)) {
        success = false;
      }
      if (bean.get("patient_care_oftext") != null
          && !phoneNoValidator.apply(bean, new String[] { "patient_care_oftext" }, errors)) {
        success = false;
      }
    }

    // Format Patient - Phone number
    String phoneNumber = (String) bean.get("patient_phone");
    if (phoneNumber != null && !phoneNumber.isEmpty()) {
      formatPhoneNumber(bean, "patient_phone", "patient_phone_country_code");
      if (bean.get("patient_phone").toString().length() > 16) {
        errors.addError("patient_phone", "js.registration.patient.invalid.phoneno.long");
        success = false;
      }
    }

    // Format Patient Care of text - Phone number
    String patientCareOfText = (String) bean.get("patient_care_oftext");
    if (patientCareOfText != null && !patientCareOfText.isEmpty()) {
      formatPhoneNumber(bean, "patient_care_oftext", "patient_care_oftext_country_code");
      if (bean.get("patient_care_oftext").toString().length() > 16) {
        errors.addError("patient_care_oftext", "js.registration.patient.invalid.phoneno.long");
        success = false;
      }
    }

    if (bean.get("salutation") == null || bean.get("salutation").toString().trim().isEmpty()) {
      errors.addError("salutation", "js.registration.patient.title.required");
      success = false;
    }

    if (bean.get("patient_name") == null 
        || bean.get("patient_name").toString().trim().isEmpty()) {
      errors.addError("patient_name", "js.registration.patient.first.name.required");
      success = false;
    }

    if (bean.get("patient_gender") == null || bean.get("patient_gender").equals("N")) {
      errors.addError("patient_gender", "js.registration.patient.gender.required");
      success = false;
    }

    if (!validateSalutationGender(bean, errors)) {
      success = false;
    }
    if (!validatePatientAge(bean, errors)) {
      success = false;
    }

    if (bean.get("patient_city") == null 
        || bean.get("patient_city").toString().trim().isEmpty()) {
      boolean isDistrictEnabled = (regPref.get("enable_district") != null && ((String) regPref
          .get("enable_district")).equals("Y")) ? Boolean.TRUE : Boolean.FALSE;
      errors.addError("cityname",
          isDistrictEnabled ? "js.registration.patient.city.subdistrict.required"
              : "js.registration.patient.city.required");

      success = false;
    }

    if (regPref.get("area_field_validate") != null
        && (regPref.get("area_field_validate").equals("A")
            || (regPref.get("area_field_validate").equals(preferenceType)))
        && (bean.get("patient_area") == null
            || bean.get("patient_area").toString().trim().isEmpty())
        && !(isUnidentifiedPatient || isErVisit)) {
      boolean isDistrictEnabled = (regPref.get("enable_district") != null 
          && ((String) regPref
          .get("enable_district")).equals("Y")) ? Boolean.TRUE : Boolean.FALSE;
      errors.addError("patient_area",
          isDistrictEnabled ? "js.registration.patient.area.village.required"
              : "js.registration.patient.area.required");
      success = false;
    }

    if (regPref.get("address_field_validate") != null
        && (regPref.get("address_field_validate").equals("A")
            || (regPref.get("address_field_validate").equals(preferenceType))
                && !(isUnidentifiedPatient || isErVisit))
        && (bean.get("patient_address") == null
            || bean.get("patient_address").toString().trim().isEmpty())) {
      errors.addError("patient_address", "js.registration.patient.address.required");
      success = false;
    }

    if (bean.get("patient_address") != null
        && ((String) bean.get("patient_address")).length() > 250) {
      errors.addError("patient_address", 
          "js.registration.patient.address.length.check.string");
      success = false;
    }

    if (bean.get("patient_area") != null && ((String) bean.get("patient_area")).length() > 70) {
      errors.addError("patient_area", "exception.registration.length.cannot.be.more.than",
          Arrays.asList("70"));
      success = false;
    }

    if ((bean.get("email_id") != null && !"".equals(bean.get("email_id")))
        && !emailValidator.apply(bean, new String[] { "email_id" }, errors)) {
      success = false;
    }
    
    if ((regPref.get("last_name_required").equals("Y")
        || regPref.get("last_name_required").equals("O"))
        && (bean.get("last_name") == null
            || (bean.get("last_name") != null && bean.get("last_name").equals("")))) {
      errors.addError("last_name", "js.registration.patient.last_name_required");
      success = false;
    }
    
    if (RequestContext.getHttpRequest() != null) {
      success = validatePatientCategory(bean, errors, isErVisit) && success;
    }
    return success;
  }

  /**
   * Validate patient category.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  private boolean validatePatientCategory(BasicDynaBean bean, ValidationErrorMap errors,
      Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    Integer categoryId = (Integer) bean.get("patient_category_id");
    if (categoryId == null) {
      // required.
      errors.addError("patient_category_id", "js.registration.patient.is.required.string");
      return false;
    }

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("category_id", categoryId);

    BasicDynaBean patientCategoryBean = patientCategoryService.findByPk(key);
    Integer categoryCenterId = (Integer) patientCategoryBean.get("center_id");
    Integer centerId = 0;
    if (RequestContext.getHttpRequest() != null) {
      centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    }
    String categoryPassportRequired = 
        (String) patientCategoryBean.get("passport_details_required");
    if ((categoryCenterId == centerId 
        || categoryCenterId == 0) && categoryPassportRequired != null
        && categoryPassportRequired.equals("Y")) {
      return validatePassportDetails(bean, errors, false, isErVisit);
    }

    return true;
  }

  /**
   * Validate passport details.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param checkValidate
   *          the check validate
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validatePassportDetails(BasicDynaBean bean, ValidationErrorMap errors,
      boolean checkValidate, Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    boolean success = true;
    Boolean isUnidentifiedPatient = bean.get("is_unidentified_patient") != null 
        ? (Boolean) bean.get("is_unidentified_patient") : false;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    if (regPref.get("passport_no") != null
        && !regPref.get("passport_no").equals("")
        && regPref.get("passport_no_show") != null
        && (regPref.get("passport_no_show").equals("M") || regPref.get("passport_no_show").equals(
            "D")) 
            && bean.get("passport_no") == null 
            && !(isUnidentifiedPatient || isErVisit)) {
      if (regPref.get("passport_no_validate") != null
          && (regPref.get("passport_no_validate").equals("A") || regPref
              .get("passport_no_validate").equals("O")) && !checkValidate) {
        errors.addError("passport_no", "js.registration.patient.is.required.string");
        success = false;
      }
    }

    if (regPref.get("passport_validity") != null
        && !regPref.get("passport_validity").equals("")
        && regPref.get("passport_validity_show") != null
        && (regPref.get("passport_validity_show").equals("M") || regPref.get(
            "passport_validity_show").equals("D"))) {
      if (regPref.get("passport_validity_validate") != null
          && (regPref.get("passport_validity_validate").equals("A") || regPref.get(
              "passport_validity_validate").equals("O")) && !checkValidate) {
        if (bean.get("passport_validity") == null) {
          if (!(isUnidentifiedPatient || isErVisit)) {
            errors.addError("passport_validity", "js.registration.patient.is.required.string");
            success = false;
          }
        } else {
          if (!validateDate(bean, "passport_validity", true, errors)) {
            success = false;
          }
        }
      }
    }

    if (regPref.get("passport_issue_country") != null
        && !regPref.get("passport_issue_country").equals("")
        && regPref.get("passport_issue_country_show") != null
        && (regPref.get("passport_issue_country_show").equals("M") || regPref.get(
            "passport_issue_country_show").equals("D"))
        && bean.get("passport_issue_country") == null 
        && !(isUnidentifiedPatient || isErVisit)) {
      if (regPref.get("passport_issue_country_validate") != null
          && (regPref.get("passport_issue_country_validate").equals("A") || regPref.get(
              "passport_issue_country_validate").equals("O")) && !checkValidate) {
        errors.addError("passport_issue_country", "js.registration.patient.is.required.string");
        success = false;
      }
    }

    if (regPref.get("visa_validity") != null
        && !regPref.get("visa_validity").equals("")
        && regPref.get("visa_validity_show") != null
        && (regPref.get("visa_validity_show").equals("M") || regPref.get("visa_validity_show")
            .equals("D"))) {
      if (regPref.get("visa_validity_validate") != null
          && (regPref.get("visa_validity_validate").equals("A") || regPref.get(
              "visa_validity_validate").equals("O")) && !checkValidate) {
        if (bean.get("visa_validity") == null) {
          if (!(isUnidentifiedPatient || isErVisit)) {
            errors.addError("visa_validity", "js.registration.patient.is.required.string");
            success = false;
          }
        } else {
          if (!validateDate(bean, "visa_validity", true, errors)) {
            success = false;
          }
        }
      }
    }
    return success;
  }

  /**
   * Validate patient identification.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validatePatientIdentification(BasicDynaBean bean, ValidationErrorMap errors,
      Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    Boolean isUnidentifiedPatient = bean.get("is_unidentified_patient") != null 
        ? (Boolean) bean.get("is_unidentified_patient") : false;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    Integer centerId = 0;
    if (RequestContext.getHttpRequest() != null) {
      centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    }
    BasicDynaBean centerPrefs = centerPreferencesService.getCenterPreferences(centerId);
    String patientIdentification = (String) centerPrefs.get("patient_identification");
    if (patientIdentification != null && patientIdentification.equals("GP")) {
      if (isUnidentifiedPatient
          || isErVisit
          || (regPref.get("passport_no") != null
              && !regPref.get("passport_no").equals("")
              && regPref.get("passport_no_show") != null
              && (regPref.get("passport_no_show").equals("M") || regPref.get("passport_no_show")
                  .equals("D")) && bean.get("passport_no") != null)
          || (regPref.get("government_identifier_type_label") != null
              && !regPref.get("government_identifier_type_label").equals("") && bean
              .get("identifier_id") != null)) {
        return true;
      } else {
        String passportLabelString = "";
        if (regPref.get("passport_no") != null
            && !regPref.get("passport_no").equals("")
            && regPref.get("passport_no_show") != null
            && (regPref.get("passport_no_show").equals("M") || regPref.get("passport_no_show")
                .equals("D"))) {
          passportLabelString = (String) regPref.get("passport_no");
        } else {
          passportLabelString = messageUtil.getMessage("js.registration.patient.passport.label",
              null);
        }
        String govtIDLabelString;
        if (regPref.get("government_identifier_type_label") != null
            && !regPref.get("government_identifier_type_label").equals("")) {
          govtIDLabelString = (String) regPref.get("government_identifier_type_label");
        } else {
          govtIDLabelString = messageUtil.getMessage(
              "js.registration.patient.govtidentifier.label", null);
        }
        errors.addError("passport_no", "exception.registration.either.either.required",
            Arrays.asList(passportLabelString, govtIDLabelString));
        errors.addError("identifier_id", "exception.registration.either.either.required",
            Arrays.asList(govtIDLabelString, passportLabelString));
        return false;
      }
    } else if (patientIdentification != null && patientIdentification.equals("G")) {
      if (isUnidentifiedPatient
          || isErVisit
          || (regPref.get("government_identifier_type_label") != null
              && !regPref.get("government_identifier_type_label").equals("") && bean
              .get("identifier_id") != null)) {
        return true;
      } else {
        String govtIDLabelString;
        if (regPref.get("government_identifier_type_label") != null
            && !regPref.get("government_identifier_type_label").equals("")) {
          govtIDLabelString = (String) regPref.get("government_identifier_type_label");
        } else {
          govtIDLabelString = messageUtil.getMessage(
              "js.registration.patient.govtidentifier.label", null);
        }
        errors.addError("identifier_id", "exception.single.field.required.placeholder",
            Arrays.asList(govtIDLabelString));
        return false;
      }
    }
    return true;
  }

  /**
   * Validate govt identifier.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @param isErVisit
   *          the is er visit
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validateGovtIdentifier(BasicDynaBean bean, ValidationErrorMap errors,
      Boolean isErVisit) {
    if (bean == null) {
      return false;
    }
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    Integer identifierId = (Integer) bean.get("identifier_id");
    boolean success = true;
    String governmentIdentifierLabel = (String) regPref.get("government_identifier_label");
    String governmentIdentifierValue = (String) bean.get("government_identifier");

    if (identifierId != null) {
      Map<String, Object> key = new HashMap<String, Object>();
      key.put("identifier_id", identifierId);
      BasicDynaBean govtIdentfierBean = govtIdentifierService.findByPk(key);
      String govtIdPattern = (String) govtIdentfierBean.get("govt_id_pattern");
      if (governmentIdentifierLabel != null && !governmentIdentifierLabel.equals("")
          && governmentIdentifierValue != null && !governmentIdentifierValue.isEmpty()
          && govtIdPattern != null && !govtIdPattern.isEmpty()) {

        String regExPattern = getGovtIdRegEx(govtIdPattern);
        Pattern pattern = Pattern.compile(regExPattern);
        Matcher matcher = pattern.matcher(governmentIdentifierValue);
        if (!matcher.find()) {
          // governmentIdentifierLabel error , wrong pattern.
          errors.addError("government_identifier",
              "ui.error.govtid.pattern.mismatch.single.placeholder", 
              Arrays.asList(govtIdPattern));
          success = false;
        }
      }
      if (govtIdentfierBean != null) {
        if (govtIdentfierBean.get("value_mandatory") != null
            && govtIdentfierBean.get("value_mandatory").equals("Y")
            && governmentIdentifierLabel != null && !governmentIdentifierLabel.equals("")
            && governmentIdentifierValue == null && !isErVisit) {
          errors.addError("government_identifier", "ui.error.required.single.placeholder",
              Arrays.asList(governmentIdentifierLabel));
          success = false;
        } else if (governmentIdentifierValue != null && govtIdentfierBean.get("unique_id") != null
            && govtIdentfierBean.get("unique_id").equals("Y")) {
          if (!validateGovtIdentifierForBaby(bean, errors)) {

            return success;
          }
          if (patientDetailsService.isUniqueGovtID((String) governmentIdentifierValue,
              (String) bean.get("mr_no"))) {
            errors.addError("government_identifier", "ui.error.exists.double.placeholder",
                Arrays.asList(governmentIdentifierLabel, (String) governmentIdentifierValue));
            success = false;
          }

        }
      }
    }
    return success;
  }

  /**
   * Validate govt identifier for baby.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return the boolean
   */
  private Boolean validateGovtIdentifierForBaby(BasicDynaBean bean, ValidationErrorMap errors) {

    Map<String, Object> babyInfo = registrationService.getBabyDOBAndMemberIdValidityDetails(
        (String) bean.get("mr_no"), (String) bean.get("visit_id"), null);
    if (babyInfo != null) {
      return registrationService.babyGovtIdtDetailsHandler(babyInfo,
          (String) bean.get("government_identifier"));
    }

    return true;
  }

  /**
   * Pattern to reg ex.
   *
   * @param pattern
   *          the pattern
   * @return the string
   */
  private String patternToRegEx(String pattern) {
    return "^"
        + pattern.replaceAll("x", "[A-Za-z]").replaceAll("X", "[A-Za-z]").replaceAll("9", "[0-9]")
        + "$";
  }

  /**
   * Gets the govt id reg ex.
   *
   * @param pattern
   *          the pattern
   * @return the govt id reg ex
   */
  private String getGovtIdRegEx(String pattern) {
    String regexp = "";
    for (int i = 0; i < pattern.length(); i++) {
      String str1 = "";
      str1 = str1 + pattern.charAt(i);
      str1 = str1.trim();
      if (str1.equals("9")) {
        regexp = regexp + "(\\d{1})";
      } else if (str1.equals("x") || str1.equals("X")) {
        regexp = regexp + "([A-Za-z]{1})";
      } else {
        regexp = regexp + "([" + str1.toLowerCase() + "" + str1.toUpperCase() + "]{1})";
      }
    }
    return "^" + regexp + "$";
  }

  /**
   * Validate patient age.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  private boolean validatePatientAge(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    Date now = new Date();
    Date dob;
    Map ageMap;
    if (bean.get("dateofbirth") == null) {
      // if there is no date of birth then check using age.
      dob = new Date(((java.sql.Date) bean.get("expected_dob")).getTime());
      ageMap = DateUtil.getAgeBetweenDates(dob, now);
      if (((BigDecimal) ageMap.get("age")).compareTo(BigDecimal.valueOf(120)) > 0) {
        errors.addError("age", "js.registration.patient.age.validation.more.than.120.years");
        return false;
      }
      return true;
    } else {
      // if there is date of birth then check using age.
      dob = new Date(((java.sql.Date) bean.get("dateofbirth")).getTime());
      ageMap = DateUtil.getAgeBetweenDates(dob, now);
      if (((BigDecimal) ageMap.get("age")).compareTo(BigDecimal.valueOf(120)) > 0) {
        errors
            .addError("dateofbirth", 
                "js.registration.patient.age.validation.more.than.120.years");
        return false;
      }
      return true;
    }
  }

  /**
   * Validate salutation gender.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return the boolean
   */
  private Boolean validateSalutationGender(BasicDynaBean bean, ValidationErrorMap errors) {
    if (bean == null) {
      return false;
    }
    String salutation = (String) bean.get("salutation");
    if (salutation == null) {
      return true;
    }
    String gender = (String) bean.get("patient_gender");
    Map<String, String> genderMap = getGenderMapping();
    Map<String, Object> key = new HashMap<String, Object>();
    key.put("salutation_id", salutation);
    BasicDynaBean salutationBean = salutationService.findByPk(key);
    String salutationGender = (String) salutationBean.get("gender");
    if (salutationGender != null && !salutationGender.equals(gender)) {
      if (salutationGender.equals("N")) {
        return true;
      }
      errors.addError("patient_gender", "exception.registration.patient.salutation.gender",
          Arrays.asList((String) salutationBean.get("salutation"), genderMap.get(gender)));
      return false;
    }
    return true;
  }

  /**
   * Gets the gender mapping.
   *
   * @return the gender mapping
   */
  private Map<String, String> getGenderMapping() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("N", "Empty");
    map.put("M", "Male");
    map.put("F", "Female");
    map.put("C", "Couple");
    map.put("O", "Others");
    return map;
  }

  /**
   * Validate date.
   *
   * @param bean
   *          the bean
   * @param field
   *          the field
   * @param future
   *          the future
   * @param errors
   *          the errors
   * @return true, if successful
   */
  private boolean validateDate(BasicDynaBean bean, String field, boolean future,
      ValidationErrorMap errors) {
    java.sql.Date fieldDate = (java.sql.Date) bean.get(field);
    java.sql.Date now = DateUtil.getCurrentDate();
    if (future) {
      if (now.after(fieldDate) && !now.equals(fieldDate)) {
        errors.addError(field, "js.common.date.can.not.be.in.past.string");
        return false;
      }
    } else {
      if (now.before(fieldDate) && !now.equals(fieldDate)) {
        errors.addError(field, "js.common.date.can.not.be.in.future.string");
        return false;
      }
    }
    return true;
  }

  /**
   * Validate insurance.
   *
   * @param params
   *          the params
   * @param validationErrorMap
   *          the validation error map
   * @param visitValidationErrors
   *          the visit validation errors
   * @param visitParams
   *          the visit params
   * @return true, if successful
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean validateInsurance(Map<String, Object> params,
      List<ValidationErrorMap> validationErrorMap, ValidationErrorMap visitValidationErrors,
      Map<String, Object> visitParams) {
    boolean success = true;
    Map regPref = regPrefService.getRegistrationPreferences().getMap();
    ArrayList insuranceList = (ArrayList) params.get("insurance");
    Map<String, Object> patientParams = (Map<String, Object>) params.get("patient");
    List<BasicDynaBean> planBeans = new ArrayList<BasicDynaBean>();
    ValidationErrorMap validationErrors = null;
    for (int i = 0; i < insuranceList.size(); i++) {
      validationErrors = new ValidationErrorMap();
      Map insParams = (Map) insuranceList.get(i);
      BasicDynaBean patPlanBean = patInsPlansService.getBean();
      ConversionUtils.copyJsonToDynaBean(insParams, patPlanBean, null, false);
      planBeans.add(patPlanBean);
      BasicDynaBean patPolicyDetailsBean = patInsPolicyDetailsService.getBean();
      ConversionUtils.copyJsonToDynaBean(insParams, patPolicyDetailsBean, null, false);

      ArrayList<BasicDynaBean> patInsPlanDetailBeanList = new ArrayList<BasicDynaBean>();
      ArrayList categoryIds = (ArrayList) insParams.get("insurance_plan_details");
      for (int j = 0; j < categoryIds.size(); j++) {
        Map insuDetailsParams = (Map) categoryIds.get(j);
        BasicDynaBean patInsPlanDetailBean = patInsPlanDetailsService.getBean();
        ConversionUtils.copyJsonToDynaBean(insuDetailsParams, 
            patInsPlanDetailBean, null, false);
        patInsPlanDetailBeanList.add(patInsPlanDetailBean);
      }

      if ((null != patPlanBean.get("sponsor_id") && !((String) patPlanBean.get("sponsor_id"))
          .equals(""))
          || (null != patPlanBean.get("insurance_co") 
          && !((String) patPlanBean.get("insurance_co"))
              .equals(""))) {
        BasicDynaBean bean = null;
        Map<String, Object> keys = null;
        if (null == patPlanBean.get("sponsor_id")
            || "".equals((String) patPlanBean.get("sponsor_id"))) {
          success = false;
          validationErrors.addError("sponsor_name", 
              "js.registration.patient.is.required.string");
        }

        if (null != patPlanBean.get("sponsor_id")
            && !((String) patPlanBean.get("sponsor_id")).equals("")) {
          keys = new HashMap<String, Object>();
          keys.put("tpa_id", patPlanBean.get("sponsor_id"));
          bean = tpaService.findByPk(keys);
          if (null != bean 
              && !bean.getMap().isEmpty() 
              && !bean.get("status").equals("A")) {
            success = false;
            validationErrors.addError("sponsor_name", 
                "exception.data.isnotactive");
          } else if (null != bean && !bean.getMap().isEmpty()
              && bean.get("scanned_doc_required").equals("Y") 
              && null == insParams.get("doc_id")) {
            success = false;
            validationErrors.addError("doc_id", "exception.doc.missing");
          }

          if (null != insParams.get("doc_id") 
              && !insParams.get("doc_id").toString().isEmpty()) {
            BasicDynaBean patientDocBean = 
                patientDocumentService.findByKey((Integer) insParams
                .get("doc_id"));
            if (patientDocBean == null 
                || !patientDocBean.get("doc_type").equals("SYS_RG")) {
              success = false;
              validationErrors.addError("doc_type", 
                  "exception.document.upload");
            }
          }
        }

        if (null == patPlanBean.get("insurance_co")
            || "".equals(
                (String) patPlanBean.get("insurance_co"))) {
          success = false;
          validationErrors.addError("insurance_co", 
              "js.registration.patient.is.required.string");
        }

        if (null != patPlanBean.get("insurance_co")
            && !"".equals((String) patPlanBean.get("insurance_co"))) {
          keys = new HashMap<String, Object>();
          keys.put("insurance_co_id", (String) patPlanBean.get("insurance_co"));
          bean = insuranceCompanyService.findByPk(keys);
          if (bean != null && !bean.getMap().isEmpty() 
              && !bean.get("status").equals("A")) {
            success = false;
            validationErrors.addError("insurance_co", "exception.data.isnotactive");
          }
        }

        if (null == patPlanBean.get("plan_id") || "" == patPlanBean.get("plan_id")) {
          success = false;
          validationErrors.addError("plan_id", "js.registration.patient.is.required.string");
        }

        if (null != patPlanBean.get("plan_id") && patPlanBean.get("plan_id") != "") {
          keys = new HashMap<String, Object>();
          keys.put("plan_id", patPlanBean.get("plan_id"));
          bean = insurancePlanService.findByPk(keys);
          if (bean != null && !bean.getMap().isEmpty() && !bean.get("status").equals("A")) {
            success = false;
            validationErrors.addError("plan_id", "exception.data.isnotactive");
          }
        }

        if (null == patPlanBean.get("plan_type_id") || patPlanBean.get("plan_type_id") == "") {
          success = false;
          validationErrors.addError("plan_type_id", "js.registration.patient.is.required.string");
        }

        if (null != patPlanBean.get("plan_type_id") 
            && patPlanBean.get("plan_type_id") != "") {
          keys = new HashMap<String, Object>();
          keys.put("category_id", patPlanBean.get("plan_type_id"));
          bean = insurancePlanTypeService.findByPk(keys);
          if (bean != null && !bean.getMap().isEmpty() 
              && !bean.get("status").equals("A")) {
            success = false;
            validationErrors.addError("plan_type_id", "exception.data.isnotactive");
          }
        }

        if (null != patPlanBean.get("sponsor_id") && !patPlanBean.get("sponsor_id").equals("")) {
          String sponsorId = (String) patPlanBean.get("sponsor_id");
          String memberId = (String) patPolicyDetailsBean.get("member_id");
          BasicDynaBean sponsor = tpaService.getDetails(sponsorId);
          Integer sponsorTypeId = (Integer) sponsor.get("sponsor_type_id");
          Boolean duplicateMemberCheck = ((String) sponsor.get("tpa_member_id_validation_type"))
              .equalsIgnoreCase("B");
          Boolean duplicateMemberCheckForChild = ((String) sponsor
              .get("tpa_member_id_validation_type")).equalsIgnoreCase("C");
          keys = new HashMap();
          keys.put("sponsor_type_id", sponsorTypeId);
          BasicDynaBean sponsorTypeBean = sponsorTypeService.findByPk(keys);
          String mrNo = (String) patientParams.get("mr_no");
          String memberIdPattern = (String) sponsor.get("member_id_pattern");
          if (sponsorTypeBean != null && !sponsorTypeBean.getMap().isEmpty()
              && null != patPolicyDetailsBean && !patPolicyDetailsBean.getMap().equals("")) {
            if (sponsorTypeBean.get("member_id_mandatory").equals("Y")
                && (null == memberId || memberId.isEmpty())) {
              success = false;
              validationErrors.addError("member_id", 
                  "exception.single.field.required.placeholder",
                  Arrays.asList((String) sponsorTypeBean.get("member_id_label")));
            }
            if (null != memberId && !memberId.isEmpty() && memberIdPattern != null
                && !memberIdPattern.isEmpty()) {
              String regExPattern = patternToRegEx(memberIdPattern);
              Pattern pattern = Pattern.compile(regExPattern);
              Matcher matcher = pattern.matcher(memberId);
              if (!matcher.find()) {
                validationErrors
                    .addError("member_id",
                        "ui.message.member.id.pattern.mismatch.double.placeholder", 
                        Arrays.asList(
                            (String) sponsorTypeBean.get("member_id_label"), memberIdPattern));
                success = false;
              }
            }
            if ((duplicateMemberCheckForChild || duplicateMemberCheck) 
                && null != memberId
                && !memberId.isEmpty()) {
              List<Map<String, Object>> memberIdsMap = patientSearchRepository
                  .searchUsedTpaMemberIdsMap(memberId, sponsorId, mrNo);
              if (memberIdsMap != null && memberIdsMap.size() > 0) {
                if (duplicateMemberCheck) {
                  success = false;
                  validationErrors.addError("member_id",
                      "ui.message.duplicate.member.id.detected.triple.placeholder", Arrays.asList(
                          (String) sponsorTypeBean.get("member_id_label"), memberId, memberIdsMap
                              .get(0).get("mrno").toString()));
                } else {
                  boolean notParentChild = false;
                  boolean isParentMrNo = false;
                  for (Map item : memberIdsMap) {
                    if (item.get("is_parent_child") != null
                        && !(Boolean) item.get("is_parent_child")) {
                      notParentChild = true;
                    }
                    if (item.get("is_parent_mr_no") != null
                        && (Boolean) item.get("is_parent_mr_no")) {
                      isParentMrNo = true;
                    }
                  }
                  if (notParentChild) {
                    success = false;
                    validationErrors.addError("member_id",
                        "ui.message.duplicate.member.id.detected.triple.placeholder", Arrays
                            .asList((String) sponsorTypeBean.get("member_id_label"), memberId,
                                memberIdsMap.get(0).get("mrno").toString()));
                  } else if (isParentMrNo) {
                    if (!(patientParams.get("agein") != null
                        && patientParams.get("agein").equals("D")
                        && patientParams.get("age") != null 
                        && (Integer) patientParams.get("age") <= (Integer) sponsor
                        .get("child_dup_memb_id_validity_days"))) {
                      success = false;
                      validationErrors.addError("member_id",
                          "ui.message.duplicate.member.id.detected.triple.placeholder", Arrays
                              .asList((String) sponsorTypeBean.get("member_id_label"), memberId,
                                  memberIdsMap.get(0).get("mrno").toString()));
                    }
                  }
                }

              }
            }

            if (sponsorTypeBean.get("policy_id_mandatory").equals("Y")) {
              if ((null == patPolicyDetailsBean.get("policy_holder_name") || ""
                  .equals((String) patPolicyDetailsBean.get("policy_holder_name")))) {
                success = false;
                validationErrors.addError("policy_holder_name", "exception.data.mandatory");
              }
              if ((null == patPolicyDetailsBean.get("policy_number") || ""
                  .equals((String) patPolicyDetailsBean.get("policy_number")))) {
                success = false;
                validationErrors.addError("policy_number", "exception.data.mandatory");
              }
              if ((null == patPolicyDetailsBean.get("patient_relationship") || ""
                  .equals((String) patPolicyDetailsBean.get("patient_relationship")))) {
                success = false;
                validationErrors.addError("patient_relationship", "exception.data.mandatory");
              }
            }
            if (sponsorTypeBean.get("validity_period_mandatory").equals("Y")) {
              if ((null == patPolicyDetailsBean.get("policy_validity_start") 
                  || "" == patPolicyDetailsBean
                  .get("policy_validity_start"))) {
                success = false;
                validationErrors.addError("policy_validity_start", "exception.data.mandatory");
              }
              if ((null == patPolicyDetailsBean.get("policy_validity_end") 
                  || "" == patPolicyDetailsBean
                  .get("policy_validity_end"))) {
                success = false;
                validationErrors.addError("policy_validity_end", "exception.data.mandatory");
              }
            }
            if ((null != patPolicyDetailsBean.get("policy_validity_start") 
                && patPolicyDetailsBean
                .get("policy_validity_start") != "")) {
              java.sql.Date fieldDate = (java.sql.Date) patPolicyDetailsBean
                  .get("policy_validity_start");
              java.sql.Date now = DateUtil.getCurrentDate();
              if (now.before(fieldDate) && !now.equals(fieldDate)) {
                validationErrors.addError("policy_validity_start",
                    "js.common.date.can.not.be.in.future.string");
                success = false;
              }
            }
            if ((null != patPolicyDetailsBean.get("policy_validity_end") && patPolicyDetailsBean
                .get("policy_validity_end") != "")) {
              java.sql.Date fieldDate = (java.sql.Date) patPolicyDetailsBean
                  .get("policy_validity_end");
              java.sql.Date now = DateUtil.getCurrentDate();
              if (now.after(fieldDate) && !now.equals(fieldDate)) {
                validationErrors.addError("policy_validity_end",
                    "js.common.date.can.not.be.in.past.string");
                success = false;
              }
            }
            if ((null != patPolicyDetailsBean.get("policy_validity_start") && patPolicyDetailsBean
                .get("policy_validity_start") != "")) {
              if (null == patPolicyDetailsBean.get("policy_validity_end")
                  || "" == patPolicyDetailsBean.get("policy_validity_end")) {
                validationErrors.addError("policy_validity_end",
                    "js.registration.patient.is.required.string");
                success = false;
              }
            }

            if ((null != patPolicyDetailsBean.get("policy_validity_end") && patPolicyDetailsBean
                .get("policy_validity_end") != "")) {
              if (null == patPolicyDetailsBean.get("policy_validity_start")
                  || "" == patPolicyDetailsBean.get("policy_validity_start")) {
                validationErrors.addError("policy_validity_start",
                    "js.registration.patient.is.required.string");
                success = false;
              }
            }
          }
        }
        if (null != patPlanBean.get("visit_copay_percentage")
            && "" != patPlanBean.get("visit_copay_percentage")
            && new BigDecimal(100)
                .compareTo((BigDecimal) patPlanBean.get("visit_copay_percentage")) < -1) {
          validationErrors.addError("visit_copay_percentage",
              "exception.data.percentage.not.greater.than");
          success = false;
        }

        if (null != patInsPlanDetailBeanList && !patInsPlanDetailBeanList.isEmpty()) {
          for (BasicDynaBean patInsPlanDetailsBean : patInsPlanDetailBeanList) {
            if (new BigDecimal(100).compareTo((BigDecimal) patInsPlanDetailsBean
                .get("patient_percent")) < -1) {
              validationErrors.addError("patient_percent",
                  "exception.data.percentage.not.greater.than");
              success = false;
              break;
            }
          }
        }
        if (null != patPlanBean.get("use_drg") && !patPlanBean.get("use_drg").equals("")
            && patPlanBean.get("use_drg").equals("Y")) {
          if (visitParams.get("bill_type") != null && visitParams.get("bill_type").equals("P")) {
            visitValidationErrors.addError("bill_type",
                "exception.patient.drg.bill.type.is.bill.later.required");
            success = false;
          }
          if (patPlanBean.get("plan_id") == null && (Integer) patPlanBean.get("plan_id") == 0) {
            validationErrors.addError("use_drg", "exception.patient.drg.bill.plan.is.required");
            success = false;
          }
        }
      }

      validationErrorMap.add(i, validationErrors);
    }
    if (validationErrors == null) {
      validationErrors = new ValidationErrorMap();
    }

    if (planBeans != null && planBeans.size() > 1) {
      if (planBeans.get(0).get("plan_id") == planBeans.get(1).get("plan_id")) {
        validationErrors.addError("plan_id", "exception.patient.plans.unique");
        success = false;
      }

      if (visitParams != null && "P".equals(visitParams.get("bill_type"))
          && genPrefService.getPreferences()
          .getMap().get("allow_bill_now_insurance").equals("N")) {
        visitValidationErrors.addError("bill_type", "exception.patient.bill.later.required");
        success = false;
      }
    }

    return success;
  }

  /**
   * Validate ipemr status.
   *
   * @param status the status
   * @return true, if successful
   */
  public boolean validateIpemrStatus(String status) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!status.equals("C")) {
      errMap.addError("ipemr_status", "exception.ipemr.status.isnotclosed");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }
  
  /**
   * Validate get rate plan.
   *
   * @param categoryId the category id
   * @param planId the plan id
   */

  public void validateGetRatePlan(String categoryId, String planId) {
    if (categoryId != null && !categoryId.equals("") && !NumberUtils.isDigits(categoryId)) {
      throw new ValidationException("exception.invalid.parameter", new String[] { "category_id" });
    }
    if (planId != null && !planId.equals("") && !NumberUtils.isDigits(planId)) {
      throw new ValidationException("exception.invalid.parameter", new String[] { "plan_id" });
    }

  }
  
  /**
   * Validate patient category.
   *
   * @param bean
   *          the bean
   * @param errors
   *          the errors
   * @return true, if successful
   */
  public boolean validatePatientCategoryAndPassport(BasicDynaBean bean, ValidationErrorMap errors,
      Integer centerId) {
    if (bean == null) {
      return false;
    }
    Integer categoryId = (Integer) bean.get("patient_category_id");
    if (categoryId == null) {
      // required.
      errors.addError("patient_category_id", "js.registration.patient.is.required.string");
      return false;
    }

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("category_id", categoryId);

    BasicDynaBean patientCategoryBean = patientCategoryService.findByPk(key);
    Integer categoryCenterId = (Integer) patientCategoryBean.get("center_id");
    String categoryPassportRequired = (String) patientCategoryBean.get("passport_details_required");
    if ((categoryCenterId == centerId || categoryCenterId == 0) && categoryPassportRequired != null
        && categoryPassportRequired.equals("Y")) {
      return validatePassportDetails(bean, errors, false, false);
    }

    return true;
  }
  
  /**
   * Validate visit details.
   *
   * @param params the params
   * @param errors the errors
   * @return true, if successful
   */
  public  boolean validateVisitDetails(Map<String,Object> params, ValidationErrorMap errors) {
    boolean success = true;
    if (StringUtils.isEmpty(params.get("center_id"))) {
      success = false;
      errors.addError("center_id",
          "exception.vital.notnull.center.id");
    }
    if (StringUtils.isEmpty(params.get("dept_name"))) {
      success = false;
      errors.addError("dept_name",
          "js.registration.editvisitdetails.departmentisrequired");
    }
    
    return success;
  }

  /**
   * Validate insurance details.
   *
   * @param params the params
   * @param errors the errors
   * @return true, if successful
   */
  public boolean validateInsuranceDetails(Map params, ValidationErrorMap errors) {
    boolean success = true;
    if (StringUtils.isEmpty(params.get("insurance_co"))) {
      success = false;
      errors.addError("insurance_co", "ui.label.insurance.company");
    }
    if (StringUtils.isEmpty(params.get("sponsor_id"))) {
      success = false;
      errors.addError("sponsor_id", "ui.label.insurance.sponsor");
    }
    if (StringUtils.isEmpty(params.get("plan_id"))) {
      success = false;
      errors.addError("plan_id", "ui.label.insurance.plan.required");
    }
    if (StringUtils.isEmpty(params.get("plan_type_id"))) {
      success = false;
      errors.addError("plan_type_id", "ui.label.insurance.plan.type");
    }
    return success;
  }

}
