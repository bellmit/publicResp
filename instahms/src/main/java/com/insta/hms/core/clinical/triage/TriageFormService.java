package com.insta.hms.core.clinical.triage;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionEAuthorization;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.ClinicalFormService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagnosiscodefavourites.DiagnosisCodeFavouritesService;
import com.insta.hms.mdm.diagnosisstatus.DiagnosisStatusService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.itemforms.ItemFormService;
import com.insta.hms.mdm.medicinedosage.MedicineDosageService;
import com.insta.hms.mdm.medicineroute.MedicineRouteService;
import com.insta.hms.mdm.optypes.OpTypeNameService;
import com.insta.hms.mdm.perdiemcodes.PerDiemCodesService;
import com.insta.hms.mdm.phrasesuggestions.PhraseSuggestionsService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;
import com.insta.hms.mdm.prescriptioninstructions.PrescriptionInstructionsService;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.strengthunits.StrengthUnitService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TriageFormService.
 */
@Service
@Qualifier("triageFormSvc")
public class TriageFormService extends ClinicalFormService {

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The per diem codes service. */
  @LazyAutowired
  PerDiemCodesService perDiemCodesService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The diag code fav service. */
  @LazyAutowired
  private DiagnosisCodeFavouritesService diagCodeFavService;

  /** The icd supported codes service. */
  @LazyAutowired
  private IcdSupportedCodesService icdSupportedCodesService;

  /** The mrd diagnosis service. */
  @LazyAutowired
  private MRDDiagnosisService mrdDiagnosisService;

  /** The stn dtls service. */
  @LazyAutowired
  private SectionDetailsService stnDtlsService;

  /** The vital service. */
  @LazyAutowired
  private VitalReadingService vitalService;

  /** The vital param mas service. */
  @LazyAutowired
  private VitalParameterService vitalParamMasService;

  /** The pres service. */
  @LazyAutowired
  private PrescriptionsService presService;

  /** The diagnosis status service. */
  @LazyAutowired
  private DiagnosisStatusService diagnosisStatusService;

  /** The health auth pref service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The op type name service. */
  @LazyAutowired
  private OpTypeNameService opTypeNameService;

  /** The allergies service. */
  @LazyAutowired
  private AllergiesService allergiesService;

  /** The strength unit service. */
  @LazyAutowired
  private StrengthUnitService strengthUnitService;

  /** The pres fav service. */
  @LazyAutowired
  private PrescriptionFavouritesService presFavService;

  /** The item form service. */
  @LazyAutowired
  private ItemFormService itemFormService;

  /** The medicine dosage service. */
  @LazyAutowired
  private MedicineDosageService medicineDosageService;

  /** The medicine route service. */
  @LazyAutowired
  private MedicineRouteService medicineRouteService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The phrase suggestions service. */
  @LazyAutowired
  private PhraseSuggestionsService phraseSuggestionsService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The reg exp service. */
  @LazyAutowired
  private RegularExpressionService regExpService;

  /** The erx service. */
  @LazyAutowired
  private ERxService erxService;

  /** The pbm prescriptions service. */
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  /** The sys gen sections service. */
  @LazyAutowired
  private SystemGeneratedSectionsService sysGenSectionsService;

  /** The prescription instructions service. */
  @LazyAutowired
  private PrescriptionInstructionsService prescriptionInstructionsService;

  /** The image markers service. */
  @LazyAutowired
  private ImageMarkersService imageMarkersService;

  /** The reference ranges service. */
  @LazyAutowired
  ReferenceRangesService referenceRangesService;

  /** The follow up service. */
  @LazyAutowired
  private FollowUpService followUpService;

  /** The prescription E authorization. */
  @LazyAutowired
  private PrescriptionEAuthorization prescriptionEAuthorization;

  /** The practitioner map service. */
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerMapService;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The clinical prefs service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPrefsService;

  /** The allergy type service. */
  @LazyAutowired
  private AllergyTypeService allergyTypeService;

  /**
   * Instantiates a new triage form service.
   */
  public TriageFormService() {
    super(FormComponentsService.FormType.Form_TRI);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#loadForm(java.lang.Object)
   */
  @Override
  public Map<String, Object> loadForm(Object id) {
    Map<String, Object> data = super.loadForm(id);
    data.put("triage_summary", data.get("summary"));
    data.remove("summary");
    return data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getActivitySpecificDetails(com.insta.hms.core.
   * clinical.forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, Object> getActivitySpecificDetails(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    /*
     * Calculate number of seconds passed for closed Triage
     */
    Map<String, Object> keyMap = new HashMap<>();
    Timestamp triageClosingTime = (Timestamp) formSpecificBean.get("triage_complete_time");
    long numberOfSecTillDate = 0;
    if (triageClosingTime != null) {
      long diff =
          ((new Timestamp(new java.util.Date().getTime())).getTime() - triageClosingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    keyMap.put("sec_till_date", numberOfSecTillDate);
    return keyMap;
  }

  /**
   * Gets the patient triage details list.
   *
   * @param mrNo the mr no
   * @return the patient triage details list
   */
  public Map<String, Object> getPatientTriageDetailsList(String mrNo) {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String doctorId = (String) (userService
        .findByKey("emp_username", (String) sessionAttributes.get("userId")).get("doctor_id"));
    response.put("triages", ConversionUtils.listBeanToListMap(doctorConsultationService
        .getPatientTriageDetailsList(mrNo, doctorId, (Integer) sessionAttributes.get("centerId"))));
    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#metadata(java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> metadata(Object consultationId) {
    FormParameter parameter = getFormParameter(consultationId, null);
    BasicDynaBean bean = doctorConsultationService.findByKey((int) consultationId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> metadata = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> defaultVitals = null;

    if (bean != null) {
      BasicDynaBean regBean = registrationService.findByKey((String) bean.get("patient_id"));
      String deptId = (String) doctorConsultationService.getConsultation((Integer) consultationId)
          .get("dept_id");
      // TO DO: need to use default master service
      defaultVitals = vitalService.getDefaultApplicableVitals(centerId, deptId,
          (String) regBean.get("visit_type"));

      metadata.put("phrase", ConversionUtils
          .copyListDynaBeansToMap(phraseSuggestionsService.getPhraseSuggestionsDeptWise(deptId)));
      metadata.put("edd_expression_value",
          sysGenSectionsService.findByKey(-14).get("edd_expression_value"));
      metadata.put("regexp_patterns",
          ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
      metadata.put("default_vitals", ConversionUtils.copyListDynaBeansToMap(defaultVitals));
      // Get all the vitals Params,filter applied at UI to handle migration issue
      List<BasicDynaBean> vitalBeanList = vitalParamMasService.getAllParams("O", centerId, deptId);
      metadata.put("all_vitals",
          referenceRangesService.getReferenceRangeList(vitalBeanList, (String) bean.get("mr_no")));
      metadata.put("availableSections",
          ConversionUtils.listBeanToListMap(stnDtlsService.getAllMasterSections(
              (Integer) sessionAttributes.get("roleId"), parameter.getFormType())));
      metadata.put("all_departments",
          ConversionUtils.listBeanToListMap(departmentService.listAll(null, null, null, null)));
    } else {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    return metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#metadata()
   */
  @Override
  public Map<String, Object> metadata() {
    BasicDynaBean genPrefBean = genPrefService.getAllPreferences();
    Map<String, Object> refRangeColorCodeMap = new HashMap<>();
    refRangeColorCodeMap.put("normal_color_code", genPrefBean.get("normal_color_code"));
    refRangeColorCodeMap.put("abnormal_color_code", genPrefBean.get("abnormal_color_code"));
    refRangeColorCodeMap.put("critical_color_code", genPrefBean.get("critical_color_code"));
    refRangeColorCodeMap.put("improbable_color_code", genPrefBean.get("improbable_color_code"));

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    Map<String, Object> commonConsultationData = new HashMap<>();
    commonConsultationData.put("perdiem",
        ConversionUtils.copyListDynaBeansToMap(perDiemCodesService.getPerDiemCodes()));
    commonConsultationData.put("diagnosis_status",
        ConversionUtils.copyListDynaBeansToMap(diagnosisStatusService.getDiagnosisStatusList()));
    commonConsultationData.put("health_authority_preferences",
        healthAuthPrefService.listBycenterId(centerId).getMap());
    commonConsultationData.put("allergy_types", ConversionUtils.copyListDynaBeansToMap(
        allergyTypeService.listAll(null, "status", "A")));
    commonConsultationData.put("optype_name",
        ConversionUtils.copyListDynaBeansToMap(opTypeNameService.lookup(false)));
    commonConsultationData.put("image_markers_list",
        ConversionUtils.copyListDynaBeansToMap(imageMarkersService.lookup(false)));
    commonConsultationData.put("regexp_patterns",
        ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
    commonConsultationData.put("reference_range_color_code", refRangeColorCodeMap);
    commonConsultationData.put("clinical_preferences",
        clinicalPrefsService.getClinicalPreferences().getMap());
    return commonConsultationData;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#preFormSave(java.util.Map,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> preFormSave(Map<String, Object> requestBody, FormParameter parameters,
      Map<String, Object> errorMap) {
    Map<String, Object> response = new HashMap<>();
    ValidationErrorMap validationErrMap = new ValidationErrorMap();
    try {
      response.put("triage_summary", doctorConsultationService.saveTriageData(
          (Map<String, Object>) requestBody.get("triage_summary"), parameters, validationErrMap));
    } catch (ParseException exception) {
      throw new HMSException(exception);
    }
    if (!validationErrMap.getErrorMap().isEmpty()) {
      ValidationException ex = new ValidationException(validationErrMap);
      errorMap.put("triage_summary", ex.getErrors());
    }
    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#postFormSave(java.util.Map,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map, java.util.Map)
   */
  @Override
  public void postFormSave(Map<String, Object> requestBody, FormParameter parameters,
      Map<String, Object> response, Map<String, Object> errorMap) {}

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getRecord(java.lang.Object)
   */
  @Override
  public BasicDynaBean getRecord(Object formKeyValue) {
    BasicDynaBean bean = doctorConsultationService.findByKey((Integer) formKeyValue);
    if (bean != null) {
      return bean;
    } else {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getSummary(com.insta.hms.core.clinical.forms.
   * FormParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSummary(FormParameter parameter) {
    BasicDynaBean triageSummary =
        doctorConsultationService.getTriageSummary((Integer) parameter.getId());
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("doctor_id", loggedInDoctorId);
    String loggedInDoc = "";
    if (loggedInDoctorId != null && !loggedInDoctorId.equals("")) {
      loggedInDoc = (String) doctorService.findByPk(filterMap).get("doctor_name");
    }
    triageSummary.set("logged_in_doctor", loggedInDoc);
    triageSummary.set("logged_in_doctor_id", loggedInDoctorId);
    return triageSummary.getMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getSectionsFromMaster(com.insta.hms.core.clinical
   * .forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getSectionsFromMaster(FormParameter parameter,
      BasicDynaBean formSpecificBean) {

    Map<String, Object> doctorfilter = new HashMap<>();
    doctorfilter.put("doctor_id", formSpecificBean.get("doctor_name"));

    Map<FormComponentsService.TriageFormColumns, Object> columns = new HashMap<>();
    columns.put(FormComponentsService.TriageFormColumns.center_id, 0);
    columns.put(FormComponentsService.TriageFormColumns.dept_id,
        doctorService.findByPk(doctorfilter).get("dept_id"));
    columns.put(FormComponentsService.TriageFormColumns.doctor_id,
        formSpecificBean.get("doctor_name"));
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    columns.put(FormComponentsService.TriageFormColumns.role_id, sessionAttributes.get("roleId"));
    return formComponentsService.getTriageForm(columns);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getTemplateForms(com.insta.hms.core.clinical.
   * forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getTemplateForms(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    BasicDynaBean patientBean = registrationService.findByKey(parameter.getPatientId());
    Map<String, Object> doctorfilter = new HashMap<>();
    doctorfilter.put("doctor_id", formSpecificBean.get("doctor_name"));
    return formComponentsService.getFromTemplatesForConsultation(
        (String) formSpecificBean.get("doctor_name"),
        (String) doctorService.findByPk(doctorfilter).get("dept_id"),
        (Integer) patientBean.get("center_id"), formType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getFormParameter(java.lang.Object,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public FormParameter getFormParameter(Object id, BasicDynaBean formSpecificBean) {
    if (formSpecificBean == null) {
      formSpecificBean = getRecord(id);
    }
    return new FormParameter(formType, itemType, (String) formSpecificBean.get("mr_no"),
        (String) formSpecificBean.get("patient_id"), (Integer) id, formKeyField);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getImageMarkerByKey(java.util.Map)
   */
  public BasicDynaBean getImageMarkerByKey(Map<String, Object> map) {
    return imageMarkersService.findByPk(map);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getContentType(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public String getContentType(Map<String, Object> parameterMap, BasicDynaBean bean) {
    if (parameterMap.containsKey("content_type")) {
      return (String) parameterMap.get("content_type");
    } else if (null != bean.get("content_type")) {
      return (String) bean.get("content_type");
    } else {
      return MediaType.IMAGE_JPEG_VALUE;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#reopenForm(java.lang.Integer,
   * org.springframework.ui.ModelMap)
   */
  @Override
  public Map<String, Object> reopenForm(Object consultationId, ModelMap requestBody) {
    BasicDynaBean bean = doctorConsultationService.findByKey((int)consultationId);
    Map<String, Object> response = new HashMap<>();
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (bean != null) {
      String reopenRemarks = (String) requestBody.get("reopen_remarks");
      if (reopenRemarks == null || "".equals(reopenRemarks)) {
        errMap.addError("reopen_remarks", "exception.reopen.remarks.notnull");
        throw new ValidationException(errMap);
      }
      if (doctorConsultationService.reopenTriage((int)consultationId, reopenRemarks)) {
        response.put("triage_status", "P");
      }

    } else {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    return response;
  }
  
  /**
   * Send HL7 Message.
   * 
   * @param visitId the visit id
   * @param response the response data
   */
  @Override
  public void triggerEvents(String visitId, Map<String, Object> response) {
    triggerAllergiesEvent(visitId, response);
    triggerVitalEvent(visitId, response);
  }
}
