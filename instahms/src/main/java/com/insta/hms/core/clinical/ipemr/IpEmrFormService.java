package com.insta.hms.core.clinical.ipemr;

import static com.insta.hms.core.prints.PrintService.PRINT_TYPE_PATIENT;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.core.clinical.careteam.CareTeamService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.forms.SectionDetailsValidator;
import com.insta.hms.core.clinical.mar.MarService;
import com.insta.hms.core.clinical.mar.MarSetupService;
import com.insta.hms.core.clinical.multiuser.MultiUserFormService;
import com.insta.hms.core.clinical.multiuser.MultiUserRedisRepository;
import com.insta.hms.core.clinical.notes.NotesService;
import com.insta.hms.core.clinical.patientactivities.PatientActivitesListModel;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.inventory.patientindent.PatientIndentService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.documents.PrintConfigurationRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import com.insta.hms.integration.insurance.erxprescription.ERxStatus;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.mdm.consumptionuom.ConsumptionUOMService;
import com.insta.hms.mdm.dailyrecurrences.RecurrenceDailyService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagnosisstatus.DiagnosisStatusService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.itemforms.ItemFormService;
import com.insta.hms.mdm.ivinfusionsites.IvInfusionsitesService;
import com.insta.hms.mdm.medicationservingremarks.MedicationServingRemarksService;
import com.insta.hms.mdm.medicinedosage.MedicineDosageService;
import com.insta.hms.mdm.medicineroute.MedicineRouteService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.packageuom.PackageUomService;
import com.insta.hms.mdm.phrasesuggestions.PhraseSuggestionsService;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;
import com.insta.hms.mdm.prescriptioninstructions.PrescriptionInstructionsService;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.strengthunits.StrengthUnitService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * The Class IpEmrFormService.
 *
 * @author sonam
 */
@Service
@Qualifier("ipEmrFormSvc")
public class IpEmrFormService extends MultiUserFormService {

  private static Logger logger = LoggerFactory.getLogger(IpEmrFormService.class);
  /** The reg service. */
  @LazyAutowired
  private RegistrationService regService;

  /** The care team service. */
  @LazyAutowired
  private CareTeamService careTeamService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The vital param mas service. */
  @LazyAutowired
  private VitalParameterService vitalParamMasService;

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

  /** The prescription instructions service. */
  @LazyAutowired
  private PrescriptionInstructionsService prescriptionInstructionsService;

  /** The reference ranges service. */
  @LazyAutowired
  private ReferenceRangesService referenceRangesService;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /** The stn dtls service. */
  @LazyAutowired
  private SectionDetailsService stnDtlsService;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The clinical pref service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPrefService;

  /** The diagnosis status service. */
  @LazyAutowired
  private DiagnosisStatusService diagnosisStatusService;

  /** The health auth pref service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;

  /** The strength unit service. */
  @LazyAutowired
  private StrengthUnitService strengthUnitService;

  /** The item form service. */
  @LazyAutowired
  private ItemFormService itemFormService;

  /** The recurrence daily service. */
  @LazyAutowired
  private RecurrenceDailyService recurrenceDailyService;

  @LazyAutowired
  private AllergyTypeService allergyTypeService;

  /** The medicine route service. */
  @LazyAutowired
  private MedicineRouteService medicineRouteService;

  /** The image markers service. */
  @LazyAutowired
  private ImageMarkersService imageMarkersService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The notes service. */
  @LazyAutowired
  private NotesService notesService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;

  /** The prescription service. */
  @LazyAutowired
  private PrescriptionsService prescriptionService;

  /** The clinical preferences service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;

  /** The order sets service. */
  @LazyAutowired
  private OrderSetsService orderSetsService;

  /** The ip pref service. */
  @LazyAutowired
  private IpPreferencesService ipPrefService;

  /** The section details validator. */
  @LazyAutowired
  private SectionDetailsValidator sectionDetailsValidator;

  /** The redis repository. */
  @LazyAutowired
  private MultiUserRedisRepository redisRepository;

  /** The Constant WEBSOCKET_PUSH_CHANNEL. */
  private static final String WEBSOCKET_PUSH_CHANNEL = "/topic/ipemr/section/save";

  /** The prescriptions service. */
  @LazyAutowired
  private MarService marService;

  @LazyAutowired
  private MarSetupService marSetupService;

  /** The medication serving remarks service. */
  @LazyAutowired
  private MedicationServingRemarksService medicationServingRemarksService;

  /** The iv infusionsites service. */
  @LazyAutowired
  private IvInfusionsitesService ivInfusionsitesService;

  /** The patient indent service. */
  @LazyAutowired
  private PatientIndentService patientIndentService;

  @LazyAutowired
  private MedicineDosageService medicineDosageService;
  @LazyAutowired
  private PrescriptionsService presService;
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;
  @LazyAutowired
  private PrescriptionFavouritesService presFavService;
  @LazyAutowired
  private ERxService erxService;
  @LazyAutowired
  private PackageUomService packageUomService;

  @LazyAutowired
  private ConsumptionUOMService consumptionUOMService;
  
  /** The Constant REDIS_KEY_PATTERN. */
  private static final String REDIS_KEY_PATTERN = "_IPEMR";
  
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  @LazyAutowired
  private PatientFormDetailsRepository patientFormDetailsRepository;

  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;

  /**
   * Instantiates a new ip emr form service.
   */
  public IpEmrFormService() {
    super(FormComponentsService.FormType.Form_IP, WEBSOCKET_PUSH_CHANNEL);
  }

  /**
   * Gets the patient visit list.
   *
   * @param mrNo the mr no
   * @return the patient visit list
   */
  public Map<String, Object> getPatientVisitList(String mrNo) {
    Map<String, Object> visitMap = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    BasicDynaBean clinPrefBean = clinicalPreferencesService.getClinicalPreferences();
    String doctorId = (String) (userService
        .findByKey("emp_username", (String) sessionAttributes.get("userId")).get("doctor_id"));
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    if (clinPrefBean.get("ip_cases_across_doctors").equals("Y")) {
      doctorId = null;
    }
    visitMap.put("ipEmrVisits", ConversionUtils
        .listBeanToListMap(regService.getIpPatientVisitList(mrNo, doctorId, centerId)));
    return visitMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getRecord(java.lang.Object)
   */
  @Override
  public BasicDynaBean getRecord(Object formKeyValue) {
    BasicDynaBean bean = regService.findByKey((String) formKeyValue);
    if (bean != null) {
      return bean;
    } else {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("patient_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getSummary(com.insta.hms.core.clinical.
   * forms. FormParameter)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSummary(FormParameter parameter) {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("doctor_id", loggedInDoctorId);
    String loggedInDoc = "";
    if (loggedInDoctorId != null && !loggedInDoctorId.equals("")) {
      loggedInDoc = (String) doctorService.findByPk(filterMap).get("doctor_name");
    }
    String notetaker = (String) userService.findByKey("emp_username", userName)
        .get("prescription_note_taker");
    BasicDynaBean visitInfo = regService.getPatientVisitSummaryInfo(parameter.getPatientId());
    visitInfo.set("prescription_note_taker", notetaker);
    visitInfo.set("logged_in_doctor", loggedInDoc);
    visitInfo.set("logged_in_doctor_id", loggedInDoctorId);
    return visitInfo.getMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getActivitySpecificDetails(com.insta.hms.
   * core .clinical.forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, Object> getActivitySpecificDetails(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    Map<String, Object> keyMap = new HashMap<>();
    List<BasicDynaBean> vistcareTeamList = careTeamService
        .careTeamVisitList(parameter.getPatientId());
    String key = parameter.getPatientId().concat(REDIS_KEY_PATTERN);
    keyMap.put("careTeamList", ConversionUtils.listBeanToListMap(vistcareTeamList));
    keyMap.put("sectionsLock", redisRepository.getData(key));
    keyMap.put("erx_data", null);
    Integer pbmPrescId = presService.getErxConsPBMId(parameter.getId());
    if (pbmPrescId != null) {
      BasicDynaBean erxdetailsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
      keyMap.put("erx_data", erxdetailsBean.getMap());
    }
    return keyMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormService#getSectionsFromMaster(com.insta.hms.core.
   * clinical .forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getSectionsFromMaster(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<FormComponentsService.IPFormColumns, Object> columns = new HashMap<>();
    columns.put(FormComponentsService.IPFormColumns.center_id, formSpecificBean.get("center_id"));
    columns.put(FormComponentsService.IPFormColumns.dept_id, formSpecificBean.get("admitted_dept"));
    columns.put(FormComponentsService.IPFormColumns.role_id, sessionAttributes.get("roleId"));
    return formComponentsService.getIPForm(columns);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#preFormSave(java.util.Map,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> preFormSave(Map<String, Object> requestBody, FormParameter parameter,
      Map<String, Object> errorMap) {
    Map<String, Object> careTeamErrMap = new HashMap<>();
    Map<String, Object> responseData = new HashMap<>();
    BasicDynaBean regBean = regService.getBean();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String closeForm = (String) requestBody.get("close_ipemr_form");
    regBean.set("user_name", userName);
    if (closeForm != null && closeForm.equals("Y")) {
      regBean.set("ipemr_status", "C");
      regBean.set("ipemr_complete_time", new java.sql.Timestamp((new java.util.Date()).getTime()));
    } else {
      regBean.set("ipemr_status", "P");
    }
    Map<String, Object> regUpdateKeys = new HashMap<>();
    regUpdateKeys.put("ipemr_revision_number", requestBody.get("ipemr_revision_number"));
    regUpdateKeys.put("patient_id", parameter.getId());
    regBean.set("ipemr_revision_number", new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    //Saving discharge prescription notes
    if (requestBody.containsKey(Constants.DISCHARGE_PRESCRIPTION_NOTES)
            && requestBody.get(Constants.DISCHARGE_PRESCRIPTION_NOTES) != null) {
      regBean.set(Constants.DISCHARGE_PRESCRIPTION_NOTES,
              requestBody.get(Constants.DISCHARGE_PRESCRIPTION_NOTES));
    }
    if (regService.update(regBean, regUpdateKeys) == 0) {
      errorMap.put("other", messageUtil.getMessage("exception.ipemr.saved"));
    }
    List<Map<String, Object>> params = (List<Map<String, Object>>) requestBody.get("careTeam");
    if (params != null) {
      responseData = careTeamService.saveCareTeamData(params, parameter, careTeamErrMap);
      if (!careTeamErrMap.isEmpty()) {
        errorMap.put("careTeamError", careTeamErrMap);
      }
    }
    responseData.putAll(regBean.getMap());
    return responseData;
  }

  /**
   * Saves IP EMR form status without saving sections and care team data.
   * @param requestBody the request body
   * @param parameter the parameters
   * @param errorMap the error map
   * @return response map
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> saveIpEmrWithoutData(Map<String, Object> requestBody,
                                                  FormParameter parameter,
                                                  Map<String, Object> errorMap) {
    BasicDynaBean regBean = regService.getBean();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String closeForm = (String) requestBody.get("close_ipemr_form");
    regBean.set("user_name", userName);
    boolean closeIpEmr = !StringUtils.isEmpty(closeForm) && closeForm.equals("Y");
    if (closeIpEmr) {
      regBean.set("ipemr_status", "C");
      regBean.set("ipemr_complete_time", new java.sql.Timestamp((new java.util.Date()).getTime()));
    } else {
      regBean.set("ipemr_status", "P");
    }
    Map<String, Object> regUpdateKeys = new HashMap<>();
    regUpdateKeys.put("patient_id", parameter.getId());
    regBean.set("ipemr_revision_number", new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    //Saving discharge prescription notes
    if (requestBody.containsKey(Constants.DISCHARGE_PRESCRIPTION_NOTES)
            && requestBody.get(Constants.DISCHARGE_PRESCRIPTION_NOTES) != null) {
      regBean.set(Constants.DISCHARGE_PRESCRIPTION_NOTES,
              requestBody.get(Constants.DISCHARGE_PRESCRIPTION_NOTES));
    }
    if (regService.update(regBean, regUpdateKeys) == 0) {
      errorMap.put("other", messageUtil.getMessage("exception.ipemr.saved"));
    }
    Map<String, Object> responseData = new HashMap<>();
    responseData.putAll(regBean.getMap());
    if (closeIpEmr) {
      clinicalFormHl7Adapter.ipemrSaveAndFinaliseEvent(parameter.getPatientId());
    }
    return responseData;
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

    // Erx for Discharge Medication
    boolean sendForErx = requestBody.get("send_for_erx") == null ? false
        : (boolean) requestBody.get("send_for_erx");
    BasicDynaBean erxBean = pbmPrescriptionsService.getLatestConsErxBean(parameters.getId());
    Map<String, Object> returnData = new HashMap<>();
    if (sendForErx
        && !(erxBean == null || "eRxCancellation".equals(erxBean.get("erx_request_type")))) {
      returnData.put("success", false);
      returnData.put("message", messageUtil.getMessage("exception.erx.request.already.sent"));
      response.put("erx", returnData);
    } else if (sendForErx) {
      String schema = RequestContext.getSchema();
      String userName = RequestContext.getUserName();
      Integer centerId = RequestContext.getCenterId();
      Map<String, Object> temp = erxService.scheduleErxJob(parameters, schema, userName, centerId);
      if ((Boolean) temp.get("success")) {
        Integer pbmPrescId = presService.getErxConsPBMId(parameters.getId());
        BasicDynaBean erxdetailsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
        temp.put("details", erxdetailsBean.getMap());
      }
      response.put("erx", temp);
    }
    String closeForm = (String) requestBody.get("close_ipemr_form");
    if ((!StringUtils.isEmpty(closeForm) && closeForm.equals("Y"))) {
      clinicalFormHl7Adapter.ipemrSaveAndFinaliseEvent(parameters.getPatientId());
    }
    return;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#getTemplateForms(com.insta.hms.core.
   * clinical. forms.FormParameter, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getTemplateForms(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    return formComponentsService.getInPatientTemplateForms(
        (String) formSpecificBean.get("dept_name"), (Integer) formSpecificBean.get("center_id"));
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
        (String) formSpecificBean.get("patient_id"), id, formKeyField);
  }

  /**
   * Gets the item types.
   *
   * @return the item types
   */
  public List<Map<String, Object>> getItemTypes() {
    List<Map<String, Object>> itemTypes = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.ITEM_TYPES) {
      Map<String, Object> record = new HashMap<>();
      record.put("short_key",
          messageUtil.getMessage(PrescriptionsService.ITEM_TYPE_CONSTANT_SHORT_KEY + value, null));
      record.put("value", value);
      itemTypes.add(record);
    }
    return itemTypes;
  }

  /**
   * Gets the prior auth types.
   *
   * @return the prior auth types
   */
  public List<Map<String, Object>> getPriorAuthTypes() {
    List<Map<String, Object>> priorAuthTypes = new ArrayList<Map<String, Object>>();
    Map<String, Object> record = new HashMap<>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.N", null));
    record.put("value", "N");
    priorAuthTypes.add(record);
    record = new HashMap<>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.A", null));
    record.put("value", "A");
    priorAuthTypes.add(record);
    record = new HashMap<>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.S", null));
    record.put("value", "S");
    priorAuthTypes.add(record);
    return priorAuthTypes;
  }

  /**
   * Gets the priorities.
   *
   * @return the priorities
   */
  public List<Map<String, Object>> getPriorities() {
    List<Map<String, Object>> prioritiesList = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.PRIORITY_VALUES) {
      Map<String, Object> record = new HashMap<>();
      record.put("name",
          messageUtil.getMessage(PrescriptionsService.PRIORITY_CONSTANT_KEY + value, null));
      record.put("short_key",
          messageUtil.getMessage(PrescriptionsService.PRIORITY_CONSTANT_SHORT_KEY + value, null));
      record.put("value", value);
      prioritiesList.add(record);
    }
    return prioritiesList;
  }

  /**
   * Gets the duration units.
   *
   * @return the duration units
   */
  public List<Map<String, Object>> getDurationUnits() {
    List<Map<String, Object>> durationUnitsList = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.DURATION_UNIT_VALUES) {
      Map<String, Object> record = new HashMap<>();
      record.put("name",
          messageUtil.getMessage(PrescriptionsService.DURATION_UNITS_CONSTANT_KEY + value, null));
      record.put("value", value);
      durationUnitsList.add(record);
    }
    return durationUnitsList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#metadata(java.lang.Object)
   */
  @Override
  public Map<String, Object> metadata(Object visitId) {
    FormParameter parameter = getFormParameter(visitId, null);
    BasicDynaBean bean = regService.findByKey((String) visitId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> metadata = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> defaultVitals = null;
    List<Map<String, Object>> userNoteTypes = null;
    if (bean != null) {
      String deptId = (String) bean.get("dept_name");
      // TO DO: need to use default master service
      defaultVitals = vitalService.getDefaultApplicableVitals(centerId, deptId,
          (String) bean.get("visit_type"));
      metadata.put("phrase", ConversionUtils
          .copyListDynaBeansToMap(phraseSuggestionsService.getPhraseSuggestionsDeptWise(deptId)));
      metadata.put("item_types", getItemTypes());
      metadata.put("edd_expression_value",
          sysGenSectionsService.findByKey(-14).get("edd_expression_value"));
      metadata.put("regexp_patterns",
          ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
      metadata.put("prescription_instructions",
          ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsService.listAll()));
      metadata.put("prior_auth_types", getPriorAuthTypes());
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
      userNoteTypes = notesService.getUserNoteTypes();
      metadata.put("note_types", userNoteTypes);
      metadata.put("sort_date",notesService.getSortByDateFields());
      metadata.put("billedNotesConsCount",
          notesService.getBilledConsCountPerDay((String) visitId, centerId));
      metadata.put("consultation_types", ConversionUtils.copyListDynaBeansToMap(
          notesService.getConsultationTypesForRateplan((String) bean.get("org_id"), centerId)));

      String userId = (String) sessionAttributes.get("userId");

      String sendERxKey = String.format(ERxService.redisKeyTemplateForSendingERx,
          RequestContext.getSchema(), visitId);
      Object sendERxJobValue = redisTemplate.opsForValue().get(sendERxKey);
      String sendERxStatus = null;
      if (sendERxJobValue != null) {
        sendERxStatus = sendERxJobValue.toString().split(";")[0].substring(7);
      } else {
        sendERxStatus = ERxStatus.FAILED.getStatus();
      }
      metadata.put("erx_sent_job_status", sendERxStatus);

      String cancelERxKey = String.format(ERxService.redisKeyTemplateForCancellingERx,
          RequestContext.getSchema(), visitId);
      Object cancelERxJobValue = redisTemplate.opsForValue().get(cancelERxKey);
      String cancelERxStatus = null;
      if (cancelERxJobValue != null) {
        cancelERxStatus = cancelERxJobValue.toString().split(";")[0].substring(7);
      } else {
        cancelERxStatus = ERxStatus.FAILED.getStatus();
      }
      metadata.put("erx_cancel_job_status", cancelERxStatus);
      metadata.put("consumption_uom_list", ConversionUtils.listBeanToListMap(consumptionUOMService
          .listAll(null, "status", "A")));
      metadata.put("reg_date",(bean.get("reg_date").toString() + " "
              + bean.get("reg_time").toString()));
    } else {
      errMap.addError("visit_id", "exception.consultation.id.notvalid");
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
  @SuppressWarnings("unchecked")
  public Map<String, Object> metadata() {
    BasicDynaBean genPrefBean = genPrefService.getAllPreferences();
    Map refRangeColorCodeMap = new HashMap<>();
    refRangeColorCodeMap.put("normal_color_code", genPrefBean.get("normal_color_code"));
    refRangeColorCodeMap.put("abnormal_color_code", genPrefBean.get("abnormal_color_code"));
    refRangeColorCodeMap.put("critical_color_code", genPrefBean.get("critical_color_code"));
    refRangeColorCodeMap.put("improbable_color_code", genPrefBean.get("improbable_color_code"));

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    Map<String, Object> commonIpEmrData = new HashMap<>();
    commonIpEmrData.put("diagnosis_status",
        ConversionUtils.copyListDynaBeansToMap(diagnosisStatusService.getDiagnosisStatusList()));
    commonIpEmrData.put("health_authority_preferences",
        healthAuthPrefService.listBycenterId(centerId).getMap());
    commonIpEmrData.put("strength_units",
        ConversionUtils.copyListDynaBeansToMap(strengthUnitService.lookup(true)));
    commonIpEmrData.put("item_form_list",
        ConversionUtils.copyListDynaBeansToMap(itemFormService.listAll(null, "status", "A")));
    commonIpEmrData.put("frequencies",
        ConversionUtils
            .copyListDynaBeansToMap(recurrenceDailyService.listAll(null, "status", "A")));
    commonIpEmrData.put("allergy_types",
        ConversionUtils
            .copyListDynaBeansToMap(allergyTypeService.listAll(null, "status", "A")));
    commonIpEmrData.put("op_frequencies",
        ConversionUtils.copyListDynaBeansToMap(medicineDosageService.listAll()));
    commonIpEmrData.put("routes_list",
        ConversionUtils.copyListDynaBeansToMap(medicineRouteService.lookup(true)));
    commonIpEmrData.put("image_markers_list",
        ConversionUtils.copyListDynaBeansToMap(imageMarkersService.lookup(false)));
    commonIpEmrData.put("priorities", getPriorities());
    commonIpEmrData.put("duration_units", getDurationUnits());
    commonIpEmrData.put("item_types", getItemTypes());
    commonIpEmrData.put("regexp_patterns",
        ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
    commonIpEmrData.put("prescription_instructions",
        ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsService.listAll()));
    commonIpEmrData.put("prior_auth_types", getPriorAuthTypes());
    commonIpEmrData.put("reference_range_color_code", refRangeColorCodeMap);
    commonIpEmrData.put("clinical_preferences",
        clinicalPreferencesService.getClinicalPreferences().getMap());
    commonIpEmrData.put("ip_preferences", ipPrefService.getPreferences().getMap());
    commonIpEmrData.put("medication_serving_remarks",
        ConversionUtils.listBeanToListMap(medicationServingRemarksService.listAll()));
    commonIpEmrData.put("iv_infusionsites",
        ConversionUtils.listBeanToListMap(ivInfusionsitesService.listAll()));
    commonIpEmrData.put("package_uom",
        ConversionUtils.listBeanToListMap(packageUomService.listDistinctPackageUom()));

    return commonIpEmrData;
  }

  /**
   * Gets the pat insurance info.
   *
   * @param visitId the visit id
   * @return the pat insurance info
   */
  public Map<String, Object> getPatInsuranceInfo(String visitId) {
    Map<String, Object> insuranceInfo = new HashMap<>();
    BasicDynaBean insurancedetails = patientInsurancePlansService.getPatInsuranceInfo(visitId);
    insuranceInfo.put("insurancedetails", insurancedetails.getMap());
    return insuranceInfo;
  }

  /**
   * Gets the prescription items.
   *
   * @param presType the pres type
   * @param patientId the patient id
   * @param searchQuery the search query
   * @param isDischargeMedication indicator for discharge medication section
   * @return the prescription items
   */
  public Map<String, Object> getPrescriptionItems(String presType, String patientId,
      Boolean invIsPrescribable, String searchQuery, boolean isDischargeMedication) {
    BasicDynaBean patientBean = getRecord(patientId);
    BasicDynaBean patientDetailsBean = patientDetailsService
        .findByKey((String) patientBean.get("mr_no"));
    Map<String, Object> items = new HashMap<>();
    String dateOfBirth = null;
    if (patientDetailsBean.get("dateofbirth") != null) {
      dateOfBirth = patientDetailsBean.get("dateofbirth").toString();
    } else if (patientDetailsBean.get("expected_dob") != null) {
      dateOfBirth = patientDetailsBean.get("expected_dob").toString();
    }
    Map map;
    try {
      map = DateUtil.getAgeForDate(dateOfBirth.toString(), "yyyy-MM-dd");
    } catch (ParseException exception) {
      logger.error("", exception);
      throw new HMSException(exception);
    }
    Integer age = (map.get("age") != null) ? Integer.parseInt(map.get("age").toString()) : null;
    String ageIn = (map.get("ageIn") != null) ? map.get("ageIn").toString() : "";
    items.put("items",
        prescriptionService.getPrescriptionItems((String) patientBean.get("mr_no"),
            (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (String) patientBean.get("visit_type"),
            (String) patientDetailsBean.get("patient_gender"), (Integer) patientBean.get("plan_id"),
            presType, (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), (String) patientBean.get("dept_name"), age,
            ageIn, invIsPrescribable, searchQuery, isDischargeMedication));
    return items;
  }

  /**
   * Gets the ALL prescription items.
   *
   * @param patientId the patient id
   * @param searchQuery the search query
   * @return the ALL prescription items
   */
  public Map<String, Object> getALLPrescriptionItems(String patientId, String searchQuery,
      Boolean isDischargeMedication) {
    BasicDynaBean patientBean = getRecord(patientId);
    BasicDynaBean patientDetailsBean =
        patientDetailsService.findByKey((String) patientBean.get("mr_no"));
    Map<String, Object> items = new HashMap<>();
    String dateOfBirth = null;
    if (patientDetailsBean.get("dateofbirth") != null) {
      dateOfBirth = patientDetailsBean.get("dateofbirth").toString();
    } else if (patientDetailsBean.get("expected_dob") != null) {
      dateOfBirth = patientDetailsBean.get("expected_dob").toString();
    }
    Map map;
    try {
      map = DateUtil.getAgeForDate(dateOfBirth.toString(), "yyyy-MM-dd");
    } catch (ParseException exception) {
      logger.error("", exception);
      throw new HMSException(exception);
    }
    Integer age = (map.get("age") != null) ? Integer.parseInt(map.get("age").toString()) : null;
    String ageIn = (map.get("ageIn") != null) ? map.get("ageIn").toString() : "";
    items.put("items",
        prescriptionService.getALLPrescriptionItems(true, true, true, true, false, true, false,
            (String) patientBean.get("visit_type"),
            (String) patientDetailsBean.get("patient_gender"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("center_id"), (String) patientBean.get("dept_name"), age,
            ageIn, (Integer) patientBean.get("plan_id"),
            (String) patientBean.get("primary_sponsor_id"), searchQuery, isDischargeMedication));

    return items;
  }

  /**
   * Gets prescription for the visit.
   * 
   * @param visitId the visit id
   * @param isDischargeMedication indicator for discharge medication section
   * @return prescriptions for the visit
   */
  public Map<String, Object> getPrescriptionsForVisit(String visitId,
      boolean isDischargeMedication) {
    BasicDynaBean patientBean = getRecord(visitId);
    Map<String, Object> items = new HashMap<String, Object>();
    items.put("items",
        presService.getPresriptionsForVisit(visitId, (String) patientBean.get("bed_type"),
            (String) patientBean.get("org_id"), (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), isDischargeMedication));
    return items;
  }

  /**
   * Gets doctor favorite prescriptions.
   * 
   * @param presType the prescription type
   * @param patientId the patient id
   * @param searchQuery the query to be searched
   * @param pageNo the page number
   * @param nonHospitalMedicine indicator for non hospital medicine
   * @return doctor favorite prescriptions
   */
  public Map<String, Object> getDoctorFavouritePrescriptions(String presType, String patientId,
      String searchQuery, Integer pageNo, Boolean nonHospitalMedicine) {
    presType = (presType == null || presType.equals("")) ? "All" : presType;
    pageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
    BasicDynaBean patientBean = regService.findByKey(patientId);
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");
    Map<String, Object> items = new HashMap<>();
    items.put("items",
        presFavService.getPrescriptionsForConsultation(presType,
            StringUtils.isEmpty(loggedInDoctorId) ? (String) patientBean.get("doctor")
                : loggedInDoctorId,
            "i", (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("plan_id"), (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), searchQuery, pageNo, nonHospitalMedicine));
    return items;
  }

  /**
   * Gets the pat notes info.
   *
   * @param patientId the patient id
   * @param paramMap  the param map
   * @return the pat notes info
   */
  public Map<String, Object> getPatNotesInfo(String patientId, Map<String, String[]> paramMap) {
    Map<String, Object> responseMap = new HashMap<>();
    PagedList pagedList = notesService.getPatientNotes(patientId, paramMap);
    responseMap.put("records", pagedList.getDtoList());
    Map<String, Object> pageInfo = new HashMap<>();
    pageInfo.put("total_records", pagedList.getTotalRecords());
    pageInfo.put("page_size", pagedList.getPageSize());
    pageInfo.put("page_number", pagedList.getPageNumber());
    pageInfo.put("num_pages", pagedList.getNumPages());
    pageInfo.put("total_notes", notesService.getTotalNotesCoun((String) patientId));
    responseMap.put("page_info", pageInfo);
    return responseMap;
  }

  /**
   * Gets the sections saved status.
   *
   * @param patientId the patient id
   * @return the sections saved status
   */
  public Map<String, Object> getSectionsSavedStatus(String patientId) {
    Map<String, Object> metadata = new HashMap<>();
    FormParameter parameter = getFormParameter(patientId, null);
    metadata.put("sectionsStatus", ConversionUtils
        .listBeanToListMap(sectionDetailsRepo.getSectionsWithSavedStatus(parameter, getRoleId())));
    return metadata;
  }

  /**
   * Gets the sections count.
   *
   * @param patientId   the patient id
   * @param requestBody the request body
   * @return the sections count
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSectionsCount(String patientId, Map<String, Object> requestBody) {

    BasicDynaBean consBean = getRecord(patientId);
    FormParameter parameters = getFormParameter(patientId, consBean);
    List<Map<String, Object>> sections = new ArrayList<>();
    List<Map<String, Object>> paramSections = (List<Map<String, Object>>) requestBody
        .get("sections");
    List<Integer> sectionIds = new ArrayList<>();
    for (Map<String, Object> section : paramSections) {
      sectionIds.add((Integer) section.get("section_id"));
    }
    List<BasicDynaBean> carryFrowardSections = sectionDetailsRepo
        .getCarryForwardSectionsBySectionIds(parameters, sectionIds);
    Integer sectionsLength = paramSections.size();
    for (int i = 0; i < sectionsLength; i++) {
      Map<String, Object> temp = new HashMap<>();
      temp.putAll(paramSections.get(i));
      sections.add(temp);
      for (BasicDynaBean section : carryFrowardSections) {
        if (section.get("section_id").equals(paramSections.get(i).get("section_id"))
            && (Long) section.get("count") > 1) {
          for (int j = 1; j < (Long) section.get("count"); j++) {
            temp = new HashMap<>();
            temp.putAll(paramSections.get(i));
            sections.add(temp);
          }
        }
      }
    }
    Map<String, Object> keyMap = new HashMap<>();
    keyMap.put("sections", sections);
    return keyMap;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#reopenForm(java.lang.Integer,
   * org.springframework.ui.ModelMap)
   */
  @Override
  public Map<String, Object> reopenForm(Object visitId, ModelMap requestBody) {
    getRecord(visitId);
    String reopenRemarks = (String) requestBody.get("reopen_remarks");
    if (reopenRemarks == null || "".equals(reopenRemarks)) {
      ValidationErrorMap validationErrMap = new ValidationErrorMap();
      validationErrMap.addError("reopen_remarks", "exception.reopen.remarks.notnull");
      throw new ValidationException(validationErrMap);
    }

    String ipEmrStatus = regService.reopenIpEmrForm((String) visitId, reopenRemarks);
    Map<String, Object> map = new HashMap<>();
    map.put("status", ipEmrStatus);
    return map;
  }

  /**
   * Package contents.
   *
   * @param packageId the package id
   * @return the map
   */
  public Map<String, Object> packageContents(Integer packageId) {
    Map<String, Object> responce = new HashMap<>();
    responce.put("items",
        ConversionUtils.listBeanToListMap(orderSetsService.getPackageComponents(packageId)));
    return responce;
  }

  /**
   * Mar setup.
   *
   * @param patientId      the patient id
   * @param prescriptionId the prescription id
   * @return the map
   */
  public Map<String, Object> marSetup(String patientId, Integer prescriptionId) {
    BasicDynaBean prescriptionBean = prescriptionService.findById(prescriptionId);
    if (prescriptionBean == null || !patientId.equals(prescriptionBean.get("visit_id"))) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("prescription_id", "exception.patient.id.prescription.id.miss.match");
      throw new ValidationException(errMap);
    }
    marService.marSetup(prescriptionBean, getFormParameter(patientId, null));
    Map<String, Object> webSocketResMap = new HashMap<>();
    webSocketResMap.put("sectionUpdate", true);
    webSocketResMap.put("patientId", patientId);
    webSocketResMap.put("section_id", -19);
    webSocketResMap.put("userName", sessionService.getSessionAttributes().get("userId"));
    webSocketResMap.put("sectionsLock",
        redisRepository.getData(patientId.concat(REDIS_KEY_PATTERN)));
    Map<String, Object> webSocketResponse = new HashMap<>();
    webSocketResponse.put("sectionUpdate", webSocketResMap);
    pushSectionResponseToWebSocket(webSocketResponse, patientId);
    return null;
  }

  /**
   * Gets the mar activities.
   *
   * @param patientId    the patient id
   * @param fromDate     the from date
   * @param toDate       the to date
   * @param prescriptionId prescription id
   * @return the mar activities
   */
  public Map<String, Object> getMarActivities(String patientId, String fromDate, String toDate,
      Integer prescriptionId) {
    getRecord(patientId);
    String pattern = "dd-MM-yyyy HH:mm:ss";
    return marService.get(patientId, DateHelper.getTimeStamp(fromDate, pattern),
        DateHelper.getTimeStamp(toDate, pattern), prescriptionId);
  }

  /**
   * Save mar activities.
   *
   * @param patientId the patient id
   * @param activites the activites
   * @return the patient activites list model
   */
  @Transactional
  public PatientActivitesListModel saveMarActivities(String patientId,
      List<PatientActivitiesModel> activites) {
    PatientActivitesListModel response = new PatientActivitesListModel();
    FormParameter parameter = getFormParameter(patientId, null);
    response.setActivities(marService.save(parameter, activites));
    Map<String, Object> webSocketResMap = new HashMap<>();
    webSocketResMap.put("sectionUpdate", true);
    webSocketResMap.put("patientId", patientId);
    webSocketResMap.put("section_id", -19);
    webSocketResMap.put("userName", sessionService.getSessionAttributes().get("userId"));
    webSocketResMap.put("sectionsLock",
        redisRepository.getData(patientId.concat(REDIS_KEY_PATTERN)));
    Map<String, Object> webSocketResponse = new HashMap<>();
    webSocketResponse.put("sectionUpdate", webSocketResMap);
    pushSectionResponseToWebSocket(webSocketResponse, patientId);
    return response;
  }

  /**
   * Gets the medicine batch details for patient.
   *
   * @param patientId   the patient id
   * @param medicineIds the medicine ids
   * @return the medicine batch details for patient
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMedicineBatchDetailsForPatient(String patientId,
      List<Integer> medicineIds) {
    getRecord(patientId);
    Map<Integer, Object> batchDetails = ConversionUtils.listBeanToMapListMap(
        patientIndentService.getMedicineBatchDetailsForPatient(patientId, medicineIds),
        "medicine_id");
    Map<String, Object> response = new HashMap<>();
    for (Entry<Integer, Object> entry : batchDetails.entrySet()) {
      response.put(entry.getKey().toString(), entry.getValue());
    }
    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.multiuser.MultiUserFormService#getSectionsLock(java.lang.
   * String, java.lang.Integer)
   */
  @Override
  public boolean getSectionsLock(String patientId, Integer sectionId) {
    Map<String, Object> sectionDataMap = new HashMap<>();
    // prepare section data
    sectionDataMap.put("user_name", (String) sessionService.getSessionAttributes().get("userId"));
    String key = patientId.concat(REDIS_KEY_PATTERN);
    redisRepository.addData(key, sectionId, sectionDataMap);
    Map<String, Object> result = redisRepository.getData(key);
    Map<String, Object> webSocketResponse = new HashMap<>();
    webSocketResponse.put("sectionsLock", result);
    pushSectionResponseToWebSocket(webSocketResponse, patientId);
    return true;
  }

  /**
   * Cancel erx request.
   * 
   * @param patientId the visit id
   * @return returns the map that contains message for user
   */
  public Map<String, Object> cancelERxRequest(String patientId) {
    BasicDynaBean regBean = regService.findByKey(patientId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (regBean == null) {
      errMap.addError("patient_id", "exception.diagdetail.notvalid.visitid");
      throw new ValidationException(errMap);
    }
    FormParameter parameters = new FormParameter(formType, itemType, (String) regBean.get("mr_no"),
        (String) regBean.get("patient_id"), patientId, formKeyField);
    BasicDynaBean erxBean = pbmPrescriptionsService.getLatestConsErxBean(patientId);

    if (!(erxBean != null && "eRxRequest".equals(erxBean.get("erx_request_type")))) {
      Map<String, Object> returnData = new HashMap<>();
      returnData.put("success", false);
      returnData.put("message", messageUtil.getMessage("exception.erx.request.not.exists"));
      return returnData;
    }
    return erxService.scheduleCancelERxJob(parameters);
  }

  /**
   * Get MAR Setup details.
   * @param patientId visit id
   * @param prescriptionId prescription id
   * @return Map containing setup details
   */
  public Map<String, Object> marSetupDetails(String patientId, Integer prescriptionId) {
    BasicDynaBean prescriptionBean = prescriptionService.findById(prescriptionId);
    if (prescriptionBean == null || !patientId.equals(prescriptionBean.get("visit_id"))) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("prescription_id", "exception.patient.id.prescription.id.miss.match");
      throw new ValidationException(errMap);
    }
    return marService.marSetupDetails(prescriptionBean, getFormParameter(patientId, null));
  }

  @Transactional
  public Map<String, Object> saveMarSetup(String patientId, Integer prescriptionId,
      ModelMap requestBody) throws ParseException {
    return marSetupService.saveMarSetup(patientId, prescriptionId, requestBody);
  }

  @Transactional
  public Map<String, Object> stopMedication(String patientId, Integer prescriptionId,
      ModelMap requestBody) {
    return marService.stopMedication(patientId, prescriptionId, requestBody);
  }

  @Override
  public String getSectionLockRedisKey(FormParameter parameter) {
    return parameter.getPatientId().concat(REDIS_KEY_PATTERN);
  }

  @Override
  public void deleteSectionLock(String userName) {
    Set<String> keys = redisRepository.getKeys("*" + REDIS_KEY_PATTERN + "*");
    for (String key : keys) {
      Map<String, Object> data = redisRepository.getData(key);
      for (Entry<String, Object> sectionEntry : data.entrySet()) {
        Map<String, Object> sectionData = (Map<String, Object>) sectionEntry.getValue();
        if (sectionData.get("user_name").equals(userName)) {
          deleteFromRedisAndPush(key, sectionEntry.getKey());
        }
      }
    }
  }

  @Override
  public void deleteSectionLock(Integer sectionId, String visitId) {
    String redisKey = visitId.concat(REDIS_KEY_PATTERN);
    Map<String, Object> data = redisRepository.getData(redisKey);
    deleteFromRedisAndPush(redisKey, sectionId);
  }

  private void deleteFromRedisAndPush(String key, Object hashKey) {
    redisRepository.deleteData(key, hashKey);
    Map<String, Object> result = redisRepository.getData(key);
    Map<String, Object> webSocketResponse = new HashMap<>();
    webSocketResponse.put("sectionsLock", result);
    String patientId = key.split("_")[0];
    pushSectionResponseToWebSocket(webSocketResponse, patientId);
  }
  
  /**
   * Trigger events.
   * 
   * @param visitId the visit id
   * @param response the response data
   */
  @Override
  public void triggerEvents(String visitId, Map<String, Object> response) {
    triggerDiagnosisEvent(visitId, response);
    triggerAllergiesEvent(visitId, response);
    triggerVitalEvent(visitId, response);
    triggerPatientProblemEvent(visitId, response);
    triggerMedicinePrescEvent(visitId, response, true);
  }

  @Override
  public String getFormDataEncodedByteArray(Object id) {
    String patientId = (String) id;
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    try {
      BasicDynaBean prefs = PrintConfigurationRepository.getPageOptions(PRINT_TYPE_PATIENT,
          6, centerId);
      OutputStream os = new ByteArrayOutputStream();
      byte[] ipEmrReport = printService.getIpEmrReport(
          patientId, PrintService.ReturnType.PDF_BYTES, prefs, os, false);
      encodedBase64String = Base64.getEncoder().encodeToString(ipEmrReport);
      os.close();
    } catch (Exception ex) {
      logger.error("Error while triggering event of the IP EMR report", ex);
    }
    return encodedBase64String;
  }

  @Override
  public Map getFormSegmentInformation(Object id) {
    return regService.getIpEmrFormSaveEventSegmentData((String) id);
  }


  @Override
  public boolean isFormReopened(Object id) {
    return regService.isFormReopened((String) id, "ipemr_reopened");
  }

  /**
   * Save IP EMR form status without sections and care team.
   *
   * @param formKeyValue the form key value
   * @param params the params
   * @return the map
   * @throws ParseException the parse exception
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> finalizeForm(Object formKeyValue, Map<String, Object> params) {
    FormParameter parameter = getFormParameter(formKeyValue, null);
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> errMap = new HashMap<>();
    Map<String, Object> nestedException = new HashMap<>();

    // To maintain same date time for transaction
    Timestamp transactionStartDateTime = new Timestamp((new Date()).getTime());
    params.put("transaction_start_date", transactionStartDateTime);

    response.putAll(saveIpEmrWithoutData(params, parameter, errMap));
    if (!errMap.isEmpty()) {
      nestedException.putAll(errMap);
      throw new NestableValidationException(nestedException);
    }

    return response;
  }
}
