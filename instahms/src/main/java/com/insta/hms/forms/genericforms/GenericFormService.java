package com.insta.hms.forms.genericforms;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.documentsforms.DocumentsFormsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.forms.FormService;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.mdm.allergy.AllergenMasterService;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagnosisstatus.DiagnosisStatusService;
import com.insta.hms.mdm.documenttypecategory.DocumentCategoryMappingRepository;
import com.insta.hms.mdm.formcomponents.FormComponentsRepository;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.formcomponents.FormComponentsService.FormType;
import com.insta.hms.mdm.formcomponents.FormComponentsService.GENFormColumns;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.perdiemcodes.PerDiemCodesService;
import com.insta.hms.mdm.phrasesuggestions.PhraseSuggestionsService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.strengthunits.StrengthUnitService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenericFormService extends FormService {

  public GenericFormService() {
    super(FormType.Form_Gen);
  }

  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private PatientFormDetailsRepository patientFormDetailsRepository;
  @LazyAutowired
  private DocumentCategoryMappingRepository docCatMappingRepo;
  @LazyAutowired
  private FormComponentsRepository formCompRepo;
  @LazyAutowired
  private FormComponentsService formComponentsService;
  @LazyAutowired
  private PatientRegistrationService patRegService;
  @LazyAutowired
  private SectionDetailsService secDetailsService;
  @LazyAutowired
  private DocumentsFormsService docFormService;
  @LazyAutowired
  private VitalReadingService vitalService;
  @LazyAutowired
  private SystemGeneratedSectionsService sysGenSectionsService;
  @LazyAutowired
  private PhraseSuggestionsService phraseSuggestionsService;
  @LazyAutowired
  private RegularExpressionService regExpService;
  @LazyAutowired
  private VitalParameterService vitalParamMasService;
  @LazyAutowired
  private ReferenceRangesService referenceRangesService;
  @LazyAutowired
  private SectionDetailsService stnDtlsService;
  @LazyAutowired
  private DepartmentService departmentService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  @LazyAutowired
  private PerDiemCodesService perDiemCodesService;
  @LazyAutowired
  private DiagnosisStatusService diagnosisStatusService;
  @LazyAutowired
  private StrengthUnitService strengthUnitService;
  @LazyAutowired
  private ImageMarkersService imageMarkersService;
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerMapService;
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;
  @LazyAutowired
  private AllergyTypeService allergyTypeService;
  @LazyAutowired
  private AllergenMasterService allergenMasterService;

  @Override
  public BasicDynaBean getRecord(Object formKeyValue) {
    BasicDynaBean bean = patientFormDetailsRepository.findByFormId((int) formKeyValue, formType);
    if (bean != null) {
      return bean;
    } else {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("patient_id", "exception.genericform.id.notvalid");
      throw new ValidationException(errMap);
    }
  }

  @Override
  public Map<String, Object> getSummary(FormParameter parameter) {
    BasicDynaBean summary =
        patientFormDetailsRepository.getGenericFormSummary((int) parameter.getId());
    if (summary == null) {
      return Collections.emptyMap();
    }
    return summary.getMap();
  }

  @Override
  public List<BasicDynaBean> getSectionsFromMaster(FormParameter parameters,
      BasicDynaBean formSpecificBean) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<GENFormColumns, Object> columns = new HashMap<>();
    columns.put(GENFormColumns.center_id, sessionAttributes.get("centerId"));
    columns.put(GENFormColumns.role_id, sessionAttributes.get("roleId"));
    columns.put(GENFormColumns.id, formSpecificBean.get("form_master_id"));
    return formComponentsService.getGenericForm(columns);
  }

  @Override
  public Map<String, Object> preFormSave(Map<String, Object> requestBody, FormParameter parameter,
      Map<String, Object> errorMap) {
    String formStatus = (String) requestBody.get("form_status");
    int genFormId = (Integer) parameter.getId();
    BasicDynaBean pfdBean = patientFormDetailsRepository.findByFormId(genFormId, formType);
    if (pfdBean.get("form_status").equals("N")) {
      pfdBean.set("created_date", DateUtil.getCurrentTimestamp());
    }
    pfdBean.set("mod_time", DateUtil.getCurrentTimestamp());
    pfdBean.set("revision_number", new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    pfdBean.set("user_name", (String) sessionService.getSessionAttributes().get("userId"));
    pfdBean.set("form_status", formStatus);
    Map<String, Object> keys = new HashMap<>();
    keys.put("form_detail_id", genFormId);
    keys.put("form_type", formType);
    if (patientFormDetailsRepository.update(pfdBean, keys) == 0) {
      errorMap.put("other", "exception.genericform.saved");
    }
    Map<String, Object> responseData = new HashMap<>();
    responseData.putAll(pfdBean.getMap());
    return responseData;
  }

  @Override
  public void postFormSave(Map<String, Object> requestBody, FormParameter parameters,
      Map<String, Object> response, Map<String, Object> errorMap) {

  }

  @Override
  public List<BasicDynaBean> getTemplateForms(FormParameter parameter,
      BasicDynaBean formSpecificBean) {

    return null;
  }

  @Override
  public FormParameter getFormParameter(Object id, BasicDynaBean formSpecificBean) {
    if (formSpecificBean == null) {
      formSpecificBean = getRecord(id);
    }
    return new FormParameter(formType, itemType, (String) formSpecificBean.get("mr_no"),
        (String) formSpecificBean.get("patient_id"), id, formKeyField);
  }

  @Override
  public Map<String, Object> reopenForm(Object genFormId, ModelMap requestBody) {
    BasicDynaBean bean = patientFormDetailsRepository.findByFormId((int) genFormId, formType);
    String reopenRemarks = (String) requestBody.get("reopen_remarks");
    Map<String, Object> response = new HashMap<>();
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (bean != null) {
      if (reopenRemarks == null || reopenRemarks.equals("")) {
        errMap.addError("reopen_remarks", "exception.reopen.remarks.notnull");
        throw new ValidationException(errMap);
      }
      bean.set("user_name", (String) sessionService.getSessionAttributes().get("userId"));
      bean.set("form_status", "P");
      bean.set("reopen_remarks", reopenRemarks);
      bean.set("mod_time", DateUtil.getCurrentTimestamp());
      Map<String, Object> keys = new HashMap<>();
      keys.put("form_detail_id", genFormId);
      keys.put("form_type", formType);
      if (patientFormDetailsRepository.update(bean, keys) > 0) {
        response.put("form_status", "P");
      }
    } else {
      errMap.addError("generic_form_id", "exception.genericform.id.not.valid");
      throw new ValidationException(errMap);
    }
    return response;
  }

  @Override
  public Map<String, Object> metadata(Object id) {
    FormParameter parameter = getFormParameter(id, null);
    BasicDynaBean bean = patientFormDetailsRepository.findByKey("form_detail_id", id);
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> metadata = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> defaultVitals = null;
    List<Map<String, Object>> userNoteTypes = null;
    if (bean != null) {
      //patient reg visit id department
      BasicDynaBean patRegBean = patRegService.findByKey("patient_id", (String) bean.get("patient_id"));
      String deptId = (String) patRegBean.get("dept_name");
      // TO DO: need to use default master service
      defaultVitals = vitalService.getDefaultApplicableVitals(centerId, deptId,
          (String) patRegBean.get("visit_type"));
      metadata.put("phrase", ConversionUtils
          .copyListDynaBeansToMap(phraseSuggestionsService.getPhraseSuggestionsDeptWise(deptId)));
      metadata.put("edd_expression_value",
          sysGenSectionsService.findByKey(-14).get("edd_expression_value"));
      metadata.put("regexp_patterns",
          ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
      metadata.put("default_vitals", ConversionUtils.copyListDynaBeansToMap(defaultVitals));
      // Get all the vitals Params,filter applied at UI to handle migration issue
      List<BasicDynaBean> vitalBeanList = vitalParamMasService.getAllParams("I", centerId, deptId);
      metadata.put("all_vitals",
          referenceRangesService.getReferenceRangeList(vitalBeanList, (String) bean.get("mr_no")));
      metadata.put("availableSections",
          ConversionUtils.listBeanToListMap(stnDtlsService.getAllMasterSections(
              (Integer) sessionAttributes.get("roleId"), parameter.getFormType())));
      metadata.put("all_departments",
          ConversionUtils.listBeanToListMap(departmentService.listAll(null, null, null, null)));
      List<String> fields = new ArrayList<>();
      fields.add("emp_username");
      metadata.put("active_users",
          ConversionUtils.copyListDynaBeansToMap(userService.getActiveEmployees(fields)));
    } else {
      errMap.addError("generic_form_id", "exception.genericform.id.notvalid");
      throw new ValidationException(errMap);
    }
    return metadata;
  }

  @Override
  public Map<String, Object> metadata() {
    BasicDynaBean genPrefBean = genericPreferencesService.getAllPreferences();
    Map refRangeColorCodeMap = new HashMap<>();
    refRangeColorCodeMap.put("normal_color_code", genPrefBean.get("normal_color_code"));
    refRangeColorCodeMap.put("abnormal_color_code", genPrefBean.get("abnormal_color_code"));
    refRangeColorCodeMap.put("critical_color_code", genPrefBean.get("critical_color_code"));
    refRangeColorCodeMap.put("improbable_color_code", genPrefBean.get("improbable_color_code"));

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    Map<String, Object> commonConsultationData = new HashMap<String, Object>();
    commonConsultationData.put("perdiem",
        ConversionUtils.copyListDynaBeansToMap(perDiemCodesService.getPerDiemCodes()));
    commonConsultationData.put("diagnosis_status",
        ConversionUtils.copyListDynaBeansToMap(diagnosisStatusService.getDiagnosisStatusList()));
    commonConsultationData.put("health_authority_preferences",
        healthAuthPrefService.listBycenterId(centerId).getMap());
    commonConsultationData.put("strength_units",
        ConversionUtils.copyListDynaBeansToMap(strengthUnitService.lookup(true)));
    commonConsultationData.put("image_markers_list",
        ConversionUtils.copyListDynaBeansToMap(imageMarkersService.lookup(false)));
    commonConsultationData.put("regexp_patterns",
        ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
    commonConsultationData.put("reference_range_color_code", refRangeColorCodeMap);
    commonConsultationData.put("practitioner_consultation_mapping",
        ConversionUtils.listBeanToListMap(practitionerMapService.listByCenterId(centerId)));
    commonConsultationData.put("clinical_preferences",
        clinicalPreferencesService.getClinicalPreferences().getMap());
    commonConsultationData.put("allergy_types", ConversionUtils.copyListDynaBeansToMap(
        allergyTypeService.listAll(null, "status", "A")));

    return commonConsultationData;
  }

  public boolean isGenericFormIdValid(Integer genFormId) {
    return patientFormDetailsRepository.exist("form_detail_id", genFormId);
  }

  public String getGenFormAssociatedMrNo(Integer genFormId) {
    BasicDynaBean bean = patientFormDetailsRepository.findByFormId(genFormId, formType);
    return (String) bean.get("mr_no");
  }

  public int getGenericFormIdNextVal() {
    return patientFormDetailsRepository.getGenericFormIdNextVal();
  }


  public Map<String, Object> add(Integer instaFormId, String visitId) {
    if (!formCompRepo.exist("id", instaFormId)) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("insta_form_id", "exception.genericform.insta.form.id.not.valid");
      throw new ValidationException(errMap);
    }
    Map<String, Object> response = new HashMap<>();
    int newGenFormId = getGenericFormIdNextVal();
    BasicDynaBean bean = patientFormDetailsRepository.getBean();
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    String mrNo = (String) patRegService.findByKey("patient_id", visitId).get("mr_no");
    bean.set("patient_id", visitId);
    bean.set("form_master_id", instaFormId);
    bean.set("form_detail_id", newGenFormId);
    bean.set("created_by", userId);
    bean.set("user_name", userId);
    bean.set("form_type", formType);
    bean.set("form_status", "N");
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    bean.set("mr_no", mrNo);
    if (patientFormDetailsRepository.insert(bean) == 0) {
      throw new HMSException("exception.genericform.insert.failed");
    }
    response.put("generic_form_id", newGenFormId);
    return response;
  }

  public Boolean discard(Integer genFormId) {
    if (patientFormDetailsRepository.delete("form_detail_id", genFormId) == 0) {
      throw new HMSException("exception.genericform.discard.failed");
    }
    return true;
  }

  public Map<String, Object> edit(Object genFormId, Map<String, Object> requestBody) {
    Map<String, Object> response = new HashMap<>();
    String editRemarks = (String) requestBody.get("reopen_remarks");
    ModelMap reopenRemarks = new ModelMap();
    reopenRemarks.put("reopen_remarks", editRemarks);
    Map<String, Object> reopenStatus = reopenForm(genFormId, reopenRemarks);
    response.put("form_status", reopenStatus.get("form_status"));
    List<Map<String, Object>> unfinalizeSections =
        (List<Map<String, Object>>) requestBody.get("unfinalize_sections");
    List<Integer> unfinalizeSectionsWithRights = new ArrayList<>();
    for (Map<String, Object> map : unfinalizeSections) {
      if ((Boolean) map.get("section_rights")) {
        unfinalizeSectionsWithRights.add((Integer) map.get("section_id"));
      }
    }
    FormParameter parameter = getFormParameter(genFormId, null);
    if(secDetailsService.updateSectionsUnFinalizeStatus(unfinalizeSectionsWithRights, parameter)) {
      List<BasicDynaBean> sectionBeans = getSections(parameter);
      response.put("sections", ConversionUtils.listBeanToListMap(sectionBeans));
    } else {
      throw new HMSException("exception.genericform.edit.failed");
    }
    return response;
  }

  /**
   * Get Allergies list.
   * 
   * @param parameters the parameters
   * @return map
   */
  public Map<String, Object> getAllergies(Map<String, String[]> parameters) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    String filterText = (null != parameters && parameters.containsKey("filterText")) ? parameters
        .get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = (null != filterText) ? allergenMasterService.autocomplete(
        "allergen_description", filterText, true, parameters) : allergenMasterService.lookup(true);
    responseMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    responseMap.put("listSize", searchSet.size());
    return responseMap;
  }
}
