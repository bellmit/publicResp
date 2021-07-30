package com.insta.hms.core.clinical.initialassessment;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.clinical.consultation.ConsultationFormService;
import com.insta.hms.core.clinical.forms.ClinicalFormService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagnosisstatus.DiagnosisStatusService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.optypes.OpTypeNameService;
import com.insta.hms.mdm.perdiemcodes.PerDiemCodesService;
import com.insta.hms.mdm.phrasesuggestions.PhraseSuggestionsService;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InitialAssessmentFormService.
 */
@Service
@Qualifier("initialAssessmentFormSvc")
public class InitialAssessmentFormService extends ClinicalFormService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConsultationFormService.class);
  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The vital parameter service. */
  @LazyAutowired
  private VitalParameterService vitalParameterService;

  /** The vital service. */
  @LazyAutowired
  private VitalReadingService vitalService;

  /** The phrase suggestions service. */
  @LazyAutowired
  private PhraseSuggestionsService phraseSuggestionsService;

  /** The sys gen sections service. */
  @LazyAutowired
  private SystemGeneratedSectionsService sysGenSectionsService;

  /** The reg exp service. */
  @LazyAutowired
  private RegularExpressionService regExpService;

  /** The reference ranges service. */
  @LazyAutowired
  ReferenceRangesService referenceRangesService;

  /** The stn dtls service. */
  @LazyAutowired
  private SectionDetailsService stnDtlsService;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The per diem codes service. */
  @LazyAutowired
  PerDiemCodesService perDiemCodesService;

  /** The diagnosis status service. */
  @LazyAutowired
  private DiagnosisStatusService diagnosisStatusService;

  /** The health auth pref service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;

  /** The op type name service. */
  @LazyAutowired
  private OpTypeNameService opTypeNameService;

  /** The image markers service. */
  @LazyAutowired
  private ImageMarkersService imageMarkersService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The clinical prefs service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPrefsService;

  /** The allergy type service. */
  @LazyAutowired
  private AllergyTypeService allergyTypeService;

  /**
   * Instantiates a new initial assessment form service.
   */
  public InitialAssessmentFormService() {
    super(FormComponentsService.FormType.Form_IA);
  }

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
  @Override
  public Map<String, Object> getSummary(FormParameter parameter) {
    BasicDynaBean initialAssessmentSummary = doctorConsultationService
        .getInitialAssessmentSummary((Integer) parameter.getId());
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String loggedInDoctorId = userName == null ? "" : (String) userService.findByKey(
        "emp_username", userName).get("doctor_id");

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("doctor_id", loggedInDoctorId);
    String loggedInDoc = "";
    if (loggedInDoctorId != null && !loggedInDoctorId.equals("")) {
      loggedInDoc = (String) doctorService.findByPk(filterMap).get("doctor_name");
    }
    initialAssessmentSummary.set("logged_in_doctor", loggedInDoc);
    initialAssessmentSummary.set("logged_in_doctor_id", loggedInDoctorId);
    return initialAssessmentSummary.getMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getActivitySpecificDetails(com.insta.hms.core
   * .clinical.forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, Object> getActivitySpecificDetails(FormParameter parameter,
      BasicDynaBean formSpecificBean) {

    Map<String, Object> keyMap = new HashMap<>();
    Timestamp iaClosingTime = (Timestamp) formSpecificBean.get("ia_complete_time");
    long numberOfSecTillDate = 0;
    if (iaClosingTime != null) {
      long diff =
          (DateUtil.getCurrentTimestamp().getTime() - iaClosingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    keyMap.put("sec_till_date", numberOfSecTillDate);
    return keyMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getSectionsFromMaster(com.insta.hms.core.clinical
   * .forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getSectionsFromMaster(FormParameter parameters,
      BasicDynaBean formSpecificBean) {
    BasicDynaBean patientBean = regService.findByKey(parameters.getPatientId());
    Map<String, Object> doctorfilter = new HashMap<>();
    doctorfilter.put("doctor_id", formSpecificBean.get("doctor_name"));
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<FormComponentsService.IAFormColumns, Object> columns = new HashMap<>();
    columns.put(FormComponentsService.IAFormColumns.center_id, patientBean.get("center_id"));
    columns.put(FormComponentsService.IAFormColumns.dept_id, doctorService.findByPk(doctorfilter)
        .get("dept_id"));
    columns.put(FormComponentsService.IAFormColumns.role_id, sessionAttributes.get("roleId"));

    return formComponentsService.getIAForm(columns);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#preFormSave(java.util.Map,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @Override
  public Map<String, Object> preFormSave(Map<String, Object> requestBody, FormParameter parameter,
      Map<String, Object> errorMap) {
    Map<String, Object> response = new HashMap<>();
    ValidationErrorMap valErrMap = new ValidationErrorMap();
    try {
      response.put("initial_assessment_summary", doctorConsultationService.saveInitialAssessment(
          (Map<String, Object>) requestBody
          .get("initial_assessment_summary"), parameter, valErrMap));
    } catch (ParseException err) {
      logger.error("", err);
      throw new HMSException(err);
    }
    if (!valErrMap.getErrorMap().isEmpty()) {
      ValidationException ex = new ValidationException(valErrMap);
      errorMap.put("initial_assessment_summary", ex.getErrors());
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
      Map<String, Object> response, Map<String, Object> errorMap) {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub
    return null;
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
   * @see com.insta.hms.core.clinical.forms.FormService#metadata(java.lang.Object)
   */
  @Override
  public Map<String, Object> metadata(Object id) {
    FormParameter parameter = getFormParameter(id, null);
    BasicDynaBean bean = doctorConsultationService.findByKey((int) id);
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> metadata = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> defaultVitals = null;

    if (bean != null) {
      BasicDynaBean regBean = regService.findByKey((String) bean.get("patient_id"));
      String deptId = (String) doctorConsultationService.getConsultation((Integer) id).get(
          "dept_id");
      // TO DO: need to use default master service
      defaultVitals = vitalService.getDefaultApplicableVitals(centerId, deptId,
          (String) regBean.get("visit_type"));
      String orgId = (String) regBean.get("org_id");
      metadata.put("consultation_types",
          ConversionUtils.copyListDynaBeansToMap(getConsultationTypesForRateplan(orgId)));
      metadata.put("phrase", ConversionUtils.copyListDynaBeansToMap(phraseSuggestionsService
          .getPhraseSuggestionsDeptWise(deptId)));
      metadata.put("edd_expression_value",
          sysGenSectionsService.findByKey(-14).get("edd_expression_value"));
      metadata.put("regexp_patterns",
          ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
      metadata.put("default_vitals", ConversionUtils.copyListDynaBeansToMap(defaultVitals));
      // Get all the vitals Params,filter applied at UI to handle migration issue
      List<BasicDynaBean> vitalBeanList = vitalParameterService.getAllParams("O", centerId, deptId);
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
    commonConsultationData.put("health_authority_preferences", healthAuthPrefService
        .listBycenterId(centerId).getMap());
    commonConsultationData.put("optype_name",
        ConversionUtils.copyListDynaBeansToMap(opTypeNameService.lookup(false)));
    commonConsultationData.put("image_markers_list",
        ConversionUtils.copyListDynaBeansToMap(imageMarkersService.lookup(false)));
    commonConsultationData.put("regexp_patterns",
        ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
    commonConsultationData.put("reference_range_color_code", refRangeColorCodeMap);
    commonConsultationData.put("clinical_preferences",
        clinicalPrefsService.getClinicalPreferences().getMap());
    commonConsultationData.put("allergyTypes", ConversionUtils.copyListDynaBeansToMap(
        allergyTypeService.listAll(null, "status", "A")));
    return commonConsultationData;
  }

  /**
   * Gets the patient initial assessment details list.
   *
   * @param mrNo
   *          the mr no
   * @return the patient initial assessment details list
   */
  public Map<String, Object> getPatientInitialAssessmentDetailsList(String mrNo) {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String doctorId = (String) (userService.findByKey("emp_username",
        (String) sessionAttributes.get("userId")).get("doctor_id"));
    response.put("initial_assessment", ConversionUtils.listBeanToListMap(doctorConsultationService
        .getPatientInitialAssessmentDetailsList(mrNo, doctorId,
            (Integer) sessionAttributes.get("centerId"))));
    return response;
  }

  /**
   * Gets the consultation types for rateplan.
   *
   * @param ordId
   *          the ord id
   * @return the consultation types for rateplan
   */
  public List<BasicDynaBean> getConsultationTypesForRateplan(String ordId) {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    Map<String, Object> params = new HashMap<>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);
    String healthAuthority = (String) centerBean.get("health_authority");
    healthAuthority = healthAuthority == null ? "" : healthAuthority;

    return consultationTypesService.getConsultationTypes("o", ordId, healthAuthority);
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
      if (doctorConsultationService.reopenInitialAssessment((int)consultationId, reopenRemarks)) {
        response.put("initial_assessment_status", "P");
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
