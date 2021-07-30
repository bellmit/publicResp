package com.insta.hms.core.clinical.consultation;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.builders.PrescriptionPHRJob;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.complaints.ComplaintsService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionEAuthorization;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.ClinicalFormService;
import com.insta.hms.core.clinical.forms.DynamicSectionService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.forms.SectionFormService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.followupdetails.FollowUpService;
import com.insta.hms.core.patient.registration.MessageUtilSms;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.SchedulerService;
import com.insta.hms.documents.DocPrintConfigurationRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import com.insta.hms.integration.insurance.erxprescription.ERxStatus;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.allergy.AllergyTypeService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.consumptionuom.ConsumptionUOMService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.diagnosisstatus.DiagnosisStatusService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.formcomponents.FormComponentsService.FormType;
import com.insta.hms.mdm.generalmessagetypes.GeneralMessageTypesService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.itemforms.ItemFormService;
import com.insta.hms.mdm.medicinedosage.MedicineDosageService;
import com.insta.hms.mdm.medicineroute.MedicineRouteService;
import com.insta.hms.mdm.optypes.OpTypeNameService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.perdiemcodes.PerDiemCodesService;
import com.insta.hms.mdm.phrasesuggestions.PhraseSuggestionsService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;
import com.insta.hms.mdm.prescriptionfavourites.PrescriptionFavouritesService;
import com.insta.hms.mdm.prescriptioninstructions.PrescriptionInstructionsService;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.stores.genericnames.GenericNamesService;
import com.insta.hms.mdm.strengthunits.StrengthUnitService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;
import com.insta.hms.messaging.MessageManager;
import com.mims.cds.FastTrackDSM;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat.
 *
 */
@Service
@Scope("prototype")
@Qualifier("consFormSvc")
public class ConsultationFormService extends ClinicalFormService {

  private static Logger logger = LoggerFactory.getLogger(ConsultationFormService.class);
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  @LazyAutowired
  PerDiemCodesService perDiemCodesService;
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  @LazyAutowired
  private SectionDetailsService stnDtlsService;
  @LazyAutowired
  private VitalReadingService vitalService;
  @LazyAutowired
  private VitalParameterService vitalParamMasService;
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  @LazyAutowired
  private RegistrationService regService;
  @LazyAutowired
  private PrescriptionsService presService;
  @LazyAutowired
  private DiagnosisStatusService diagnosisStatusService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthPrefService;
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private RegistrationService registrationService;
  @LazyAutowired
  private OpTypeNameService opTypeNameService;
  @LazyAutowired
  private AllergiesService allergiesService;
  @LazyAutowired
  private StrengthUnitService strengthUnitService;
  @LazyAutowired
  private PrescriptionFavouritesService presFavService;
  @LazyAutowired
  private ItemFormService itemFormService;
  @LazyAutowired
  private MedicineDosageService medicineDosageService;
  @LazyAutowired
  private MedicineRouteService medicineRouteService;
  @LazyAutowired
  private BillService billService;
  @LazyAutowired
  private MessageUtil messageUtil;
  @LazyAutowired
  private PhraseSuggestionsService phraseSuggestionsService;
  @LazyAutowired
  private DoctorService doctorService;
  @LazyAutowired
  private RegularExpressionService regExpService;
  @LazyAutowired
  private ERxService erxService;
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;
  @LazyAutowired
  private SystemGeneratedSectionsService sysGenSectionsService;
  @LazyAutowired
  private PrescriptionInstructionsService prescriptionInstructionsService;
  @LazyAutowired
  private ImageMarkersService imageMarkersService;
  @LazyAutowired
  ReferenceRangesService referenceRangesService;
  @LazyAutowired
  private FollowUpService followUpService;
  @LazyAutowired
  private PrescriptionEAuthorization prescriptionEAuthorization;
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerMapService;
  @LazyAutowired
  private DepartmentService departmentService;
  @LazyAutowired
  private GeneralMessageTypesService generalMessageTypesService;
  @LazyAutowired
  private MessageUtilSms messageUtilSms;
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;
  @LazyAutowired
  private ComplaintsService complaintsService;
  @LazyAutowired
  private MRDObservationsService mrdObservationsService;
  @LazyAutowired
  private SectionFormService sectionFormService;
  @LazyAutowired
  private OrderSetsService orderSetsService;
  @LazyAutowired
  private PatientDetailsService patientDetailsService;
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;
  @LazyAutowired
  private StoreItemDetailsService storeItemDetailsService;
  @LazyAutowired
  private GenericNamesService genericNamesService;
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;
  @LazyAutowired
  private ConsumptionUOMService consumptionUOMService;
  @LazyAutowired
  private AllergyTypeService allergyTypeService;
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  @LazyAutowired
  private AppointmentService appointmentService;
  @LazyAutowired
  private SchedulerService schedulerService;
    
  public ConsultationFormService() {
    super(FormType.Form_CONS);
  }
  
  public ConsultationFormService(FormComponentsService.FormType formType) {
    super(formType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> preFormSave(Map<String, Object> requestBody, FormParameter parameters,
      Map<String, Object> errorsMap) {
    Map<String, Object> responseData = new HashMap<>();
    Map<String, Object> params = (Map<String, Object>) requestBody.get("consultation_data");
    ValidationErrorMap validationErrMap = new ValidationErrorMap();
    if (params != null) {
      try {
        responseData = doctorConsultationService.saveSpecificConsultationData(params, parameters,
            validationErrMap);
      } catch (ParseException exception) {
        logger.error("", exception);
        throw new HMSException(exception);
      }
    } else {
      validationErrMap.addError("consultation_data", "exception.consultation.summary.data.notnull");
    }

    if (!validationErrMap.getErrorMap().isEmpty()) {
      ValidationException ex = new ValidationException(validationErrMap);
      errorsMap.put("other_details", ex.getErrors());
    }
    // save follow up details
    Map<String, Object> followupErrorMap = new HashMap<>();
    Map<String, Object> followupResponse = null;
    try {
      followupResponse = followUpService.saveFollowUpDetails(requestBody, parameters,
          followupErrorMap);
    } catch (ParseException exception) {
      logger.error("", exception);
      throw new HMSException(exception);
    }
    if (followupResponse != null) {
      responseData.putAll(followupResponse);
    }
    if (!followupErrorMap.isEmpty()) {
      errorsMap.put("followup_details", followupErrorMap);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("consultation_data", responseData);
    return response;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void postFormSave(Map<String, Object> requestBody, FormParameter params,
      Map<String, Object> response, Map<String, Object> errorMap) {

    Map<String, Object> reqParam = (Map<String, Object>) requestBody.get("consultation_data");
    String closeConsultation = (String) reqParam.get("close_consultation");
    if (!StringUtils.isEmpty(closeConsultation) && closeConsultation.equals("Y")) {
      clinicalFormHl7Adapter.consultationSaveAndFinaliseEvent((int) params.getId(), 
          (String) params.getPatientId(), (String) params.getMrNo());
    }
    // PrescriptionPHR
    if (messageUtilSms.allowMessageNotification("general_message_send")) {
      BasicDynaBean messageTypeBean = generalMessageTypesService.findByKey("message_type_id",
          "email_phr_prescription");
      if (messageTypeBean != null && messageTypeBean.get("status").equals("A")) {
        // phr practo drive
        String path = RequestContext.getRequest().getServletContext().getRealPath("");
        try {
          schedulePrescriptionPHR("Consultation_id_" + params.getId(), "PrescriptionPHRJob", path);
        } catch (ParseException exception) {
          // TODO Auto-generated catch block
          exception.printStackTrace();
        }
      }
      messageTypeBean = generalMessageTypesService.findByKey("message_type_id",
          "email_prescription_auto");
      int consultationId = (int) params.getId();
      Integer centerId = RequestContext.getCenterId();
    
      String conStatus = (String) doctorConsultationService.findByKey(consultationId).get("status");
      if (messageTypeBean != null && messageTypeBean.get("status").equals("A")
          && conStatus.equals("C")) {
       
        String mrNo = doctorConsultationService.getMrNoForConsultationId(consultationId);
        ArrayList emailId = new ArrayList();
        emailId.add((String) registrationService.getPatientDemography(mrNo).get("email_id"));
        MessageManager msg = new MessageManager();
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("consultationId", consultationId);
        prescriptionData.put("centerId", centerId);
        prescriptionData.put("schema",  RequestContext.getSchema());
        prescriptionData.put("userName",  RequestContext.getUserName());
        prescriptionData.put("messageTo", emailId);
        try {
          msg.processEvent("prescription_share_auto", prescriptionData, true);
        } catch (SQLException | ParseException | IOException exp) {
          logger.error("Exception caused while triggering auto sharing the patient prescription ",
              exp);
          throw new HMSException("exception.unable.send.message");
        }
      }
    }

    // Mrd Observations Presenting Complaint
    String chargeId = billActivityChargeService.getChargeId("DOC", params.getId().toString());
    if (chargeId != null) {
      StringBuilder complaintVal = new StringBuilder();

      String chiefComplaint = complaintsService.getChiefComplaint(params.getPatientId());
      complaintVal.append(chiefComplaint != null && !chiefComplaint.trim().equals("")
          ? "Chief Complaint :- " + chiefComplaint
          : "");

      String secComplaints = complaintsService.getSecondaryComplaints(params.getPatientId());
      complaintVal.append(secComplaints != null && !secComplaints.trim().equals("")
          ? " Other Complaints :- " + secComplaints
          : "");

      String consFieldValues = doctorConsultationService
          .getConsultationFieldValues((Integer) params.getId());
      complaintVal.append(
          consFieldValues != null && !consFieldValues.trim().equals("") ? consFieldValues : "");

      String consSectionFieldValues = ((DynamicSectionService) sectionFactory
          .getDynamicSectionService(0)).getConsInstaSectionFieldValues((Integer) params.getId());
      complaintVal
          .append(consSectionFieldValues != null && !consSectionFieldValues.trim().equals("")
              ? consSectionFieldValues
              : "");

      BasicDynaBean presentingComplaintbean = mrdObservationsService
          .getPresentingComplaint(chargeId);

      boolean success = false;
      if (presentingComplaintbean == null) {
        success = mrdObservationsService.insertPresentingComplaint(complaintVal.toString(),
            chargeId);
      } else {
        success = mrdObservationsService.updatePresentingComplaint(complaintVal.toString(),
            (Integer) presentingComplaintbean.get("observation_id"));
      }

      if (!success) {
        ValidationErrorMap errMap = new ValidationErrorMap();
        errMap.addError("presenting_complaint", "exception.failed.to.save.presentingComplaint");
        errorMap.put("others", new ValidationException(errMap).getErrors());
      }
    }

    // Erx for Prescriptions
    BasicDynaBean erxBean = pbmPrescriptionsService.getLatestConsErxBean(params.getId());
    boolean sendForErx = requestBody.get("send_for_erx") == null ? false
        : (boolean) requestBody.get("send_for_erx");

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
      Map<String, Object> temp = erxService.scheduleErxJob(params, schema, userName, centerId);
      if ((Boolean) temp.get("success")) {
        Integer pbmPrescId = presService.getErxConsPBMId(params.getId());
        BasicDynaBean erxdetailsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
        temp.put("details", erxdetailsBean.getMap());
      }
      response.put("erx", temp);
    }

    // Prior Authorization for Prescriptions
    List<Map<String, Object>> sectionsResponse = (List<Map<String, Object>>) response
        .get("sections");
    Map<String, Object> prescriptionResponse = null;
    for (Map<String, Object> m : sectionsResponse) {
      if ((Integer) m.get("section_id") == -7) {
        prescriptionResponse = m;
      }
    }
    if (prescriptionResponse != null) {
      List<Integer> newPrescriptionIds = new ArrayList<>();
      List<Integer> deletePrescriptionIds = new ArrayList<>();
      if (prescriptionResponse.get("insert") != null) {
        Map<String, Map<String, Object>> test = 
            (Map<String, Map<String, Object>>) prescriptionResponse.get("insert");
        for (Map.Entry<String, Map<String, Object>> entry : test.entrySet()) {
          newPrescriptionIds.add((Integer) entry.getValue().get("item_prescribed_id"));
        }
      }

      if (prescriptionResponse.get("delete") != null) {
        Map<String, Map<String, Object>> test = 
            (Map<String, Map<String, Object>>) prescriptionResponse.get("delete");
        for (Map.Entry<String, Map<String, Object>> entry : test.entrySet()) {
          deletePrescriptionIds.add((Integer) entry.getValue().get("item_prescribed_id"));
        }
      }
      Map<String, Object> priorAuthRequiredData = new HashMap<>();
      priorAuthRequiredData.put("insert_ids", newPrescriptionIds);
      priorAuthRequiredData.put("delete_ids", deletePrescriptionIds);
      response.put("prior_auth",
          prescriptionEAuthorization.initiate(priorAuthRequiredData, params, errorMap));
    }
    return;
  }

  private void schedulePrescriptionPHR(String uniString, String jobName, String path)
      throws ParseException {
    String params = uniString + ";" + RequestContext.getCenterId();
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("params", params);
    jobData.put("path", path);
    jobData.put("schema", RequestContext.getSchema());
    JobService jobService = JobSchedulingService.getJobService();
    jobService.scheduleImmediate(buildJob(uniString, PrescriptionPHRJob.class, jobData));

  }

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
    Map<String, Object> commonConsultationData = new HashMap<String, Object>();
    commonConsultationData.put("perdiem",
        ConversionUtils.copyListDynaBeansToMap(perDiemCodesService.getPerDiemCodes()));
    commonConsultationData.put("diagnosis_status",
        ConversionUtils.copyListDynaBeansToMap(diagnosisStatusService.getDiagnosisStatusList()));
    commonConsultationData.put("health_authority_preferences",
        healthAuthPrefService.listBycenterId(centerId).getMap());
    commonConsultationData.put("optype_name",
        ConversionUtils.copyListDynaBeansToMap(opTypeNameService.lookup(false)));
    commonConsultationData.put("strength_units",
        ConversionUtils.copyListDynaBeansToMap(strengthUnitService.lookup(true)));
    commonConsultationData.put("item_form_list",
        ConversionUtils.copyListDynaBeansToMap(itemFormService.listAll(null, "status", "A")));
    commonConsultationData.put("frequencies",
        ConversionUtils.copyListDynaBeansToMap(medicineDosageService.listAll()));
    commonConsultationData.put("routes_list",
        ConversionUtils.copyListDynaBeansToMap(medicineRouteService.lookup(true)));
    commonConsultationData.put("allergy_types", ConversionUtils.copyListDynaBeansToMap(
        allergyTypeService.listAll(null, "status", "A")));
    commonConsultationData.put("image_markers_list",
        ConversionUtils.copyListDynaBeansToMap(imageMarkersService.lookup(false)));
    commonConsultationData.put("priorities", getPriorities());
    commonConsultationData.put("duration_units", getDurationUnits());
    commonConsultationData.put("item_types", getItemTypes());
    commonConsultationData.put("regexp_patterns",
        ConversionUtils.copyListDynaBeansToMap(regExpService.listAll()));
    commonConsultationData.put("prescription_instructions",
        ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsService.listAll()));
    commonConsultationData.put("prior_auth_types", getPriorAuthTypes());
    commonConsultationData.put("reference_range_color_code", refRangeColorCodeMap);
    commonConsultationData.put("practitioner_consultation_mapping",
        ConversionUtils.listBeanToListMap(practitionerMapService.listByCenterId(centerId)));
    commonConsultationData.put("clinical_preferences",
        clinicalPreferencesService.getClinicalPreferences().getMap());
    commonConsultationData.put("message_type", generalMessageTypesService
        .findByKey("message_type_id", "email_prescription_manual").get("status"));
    return commonConsultationData;
  }

  @Override
  public Map<String, Object> metadata(Object consultationId) {
    BasicDynaBean bean = doctorConsultationService.findByKey((int) consultationId);
    FormParameter parameter = getFormParameter(consultationId, null);
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

      String orgId = (String) regBean.get("org_id");
      metadata.put("consultation_types",
          ConversionUtils.copyListDynaBeansToMap(getConsultationTypesForRateplan(orgId)));
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
      List<BasicDynaBean> vitalBeanList = vitalParamMasService.getAllParams("O", centerId, deptId);
      metadata.put("all_vitals",
          referenceRangesService.getReferenceRangeList(vitalBeanList, (String) bean.get("mr_no")));
      metadata.put("availableSections",
          ConversionUtils.listBeanToListMap(stnDtlsService.getAllMasterSections(
              (Integer) sessionAttributes.get("roleId"), parameter.getFormType())));
      metadata.put("all_departments",
          ConversionUtils.listBeanToListMap(departmentService.listAll(null, null, null, null)));
      metadata.put("center_preferences",
          centerPreferencesService.getCenterPreferences(centerId).getMap());
      metadata.put("consumption_uom_list",
          ConversionUtils.listBeanToListMap(consumptionUOMService.listAll(null, "status", "A")));
      
      String userId = (String) sessionAttributes.get("userId");
      String sendERxKey = String.format(ERxService.redisKeyTemplateForSendingERx,
          RequestContext.getSchema(), consultationId);
      Object sendERxJobValue = redisTemplate.opsForValue().get(sendERxKey);
      String sendERxStatus = null;
      if (sendERxJobValue != null) {
        sendERxStatus = sendERxJobValue.toString().split(";")[0].substring(7);
      } else {
        sendERxStatus = ERxStatus.FAILED.getStatus();
      }
      metadata.put("erx_sent_job_status", sendERxStatus);
      String cancelERxKey = String.format(ERxService.redisKeyTemplateForCancellingERx,
          RequestContext.getSchema(), consultationId);
      Object cancelERxJobValue = redisTemplate.opsForValue().get(cancelERxKey);
      String cancelERxStatus = null;
      if (cancelERxJobValue != null) {
        cancelERxStatus = cancelERxJobValue.toString().split(";")[0].substring(7);        
      } else {
        cancelERxStatus = ERxStatus.FAILED.getStatus();
      }
      metadata.put("erx_cancel_job_status", cancelERxStatus);
      
      BasicDynaBean docCon = doctorConsultationService.findByKey((int) consultationId);
      Integer appointmentId = (Integer) docCon.get("appointment_id");
      metadata.put("visit_mode", docCon.get("visit_mode"));
      if (!StringUtils.isEmpty(String.valueOf(appointmentId)) && appointmentId != 0) {
        AppointmentCategory appointmentCategory = schedulerService.getAppCategory("DOC");
        Map<String, Object> apptBean = appointmentService.getAppointmentDetails(appointmentCategory,
            appointmentId);
        Map<String,Object> appointment =  (Map<String, Object>) apptBean.get("appointment");
        metadata.put("teleconsult_url", appointment.get("teleconsult_url"));
      }
    } else {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    return metadata;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSummary(FormParameter parameter) {
    BasicDynaBean consInfo = null;
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String notetaker = (String) userService.findByKey("emp_username", userName)
        .get("prescription_note_taker");
    consInfo = doctorConsultationService.consultationSummaryInfo((int) parameter.getId());
    if (consInfo.get("consultation_start_datetime") == null) {
      consInfo.set("consultation_start_datetime", DateUtil.getCurrentTimestamp());
    }
    consInfo.set("prescription_note_taker", notetaker);
    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("doctor_id", loggedInDoctorId);
    String loggedInDoc = "";
    if (loggedInDoctorId != null && !loggedInDoctorId.equals("")) {
      loggedInDoc = (String) doctorService.findByPk(filterMap).get("doctor_name");
    }
    consInfo.set("logged_in_doctor", loggedInDoc);
    consInfo.set("logged_in_doctor_id", loggedInDoctorId);
    return consInfo.getMap();
  }

  @Override
  public Map<String, Object> getActivitySpecificDetails(FormParameter parameter,
      BasicDynaBean formSpecificBean) {

    Map<String, Object> keyMap = new HashMap<>();
    // calculate no of sec for closed consultation
    Timestamp closingTime = (Timestamp) formSpecificBean.get("consultation_complete_time");
    long numberOfSecTillDate = 0;
    if (closingTime != null) {
      long diff = ((new Timestamp(new java.util.Date().getTime())).getTime()
          - closingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    keyMap.put("sec_till_date", numberOfSecTillDate);
    keyMap.put("erx_data", null);
    Integer pbmPrescId = presService.getErxConsPBMId(parameter.getId());
    if (pbmPrescId != null) {
      BasicDynaBean erxdetailsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
      keyMap.put("erx_data", erxdetailsBean.getMap());
    }

    keyMap.put("followup_details", ConversionUtils
        .copyListDynaBeansToMap(followUpService.getfollowUpDetails(parameter.getPatientId())));

    return keyMap;
  }

  /**
   * Get consultation details.
   * @param mrNo the string
   * @return map
   */
  public Map<String, Object> getPatientConsultationDetailsList(String mrNo) {
    Map<String, Object> consultationMap = new HashMap<>();
    List<BasicDynaBean> conslistBeans = null;
    BasicDynaBean clinicalPrefBean = clinicalPreferencesService.getClinicalPreferences();
    String consultationEditAcrossDoctors = (String) clinicalPrefBean
        .get("op_consultation_edit_across_doctors");
    Integer roleId = (Integer) sessionService.getSessionAttributes().get("roleId");
    String userName = (String) sessionService.getSessionAttributes().get("userId");

    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");
    boolean isDoctorLogin = loggedInDoctorId != null && !loggedInDoctorId.equals("");
    conslistBeans = doctorConsultationService.getPatientConsultationDetailsList(mrNo,
        isDoctorLogin);
    if (roleId != 1 && roleId != 2 && consultationEditAcrossDoctors.equals("N")
        && !isDoctorLogin) {
      consultationMap.put("allowed_editing_consultation", false);
    } else {
      consultationMap.put("allowed_editing_consultation", true);
    }
    consultationMap.put("consultations", ConversionUtils.listBeanToListMap(conslistBeans));
    BasicDynaBean genPrefBean = genPrefService.getAllPreferences();
    int maxCenterIncDefault = (Integer) genPrefBean.get("max_centers_inc_default");
    consultationMap.put("is_multicenter_schema", maxCenterIncDefault > 1);
    return consultationMap;
  }

  /**
   * Get consultation type for rate plan.
   * @param ordId the string
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getConsultationTypesForRateplan(String ordId) {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);
    String healthAuthority = (String) centerBean.get("health_authority");
    healthAuthority = healthAuthority == null ? "" : healthAuthority;

    return consultationTypesService.getConsultationTypes("o", ordId, healthAuthority);
  }

  /**
   * Get presc items.
   * 
   * @param presType the string
   * @param patientId the string
   * @param invIsPrescribable the boolean
   * @param searchQuery the string
   * @param isDischargeMedication indicator for discharge medication section
   * @return map
   */
  public Map<String, Object> getPrescriptionItems(String presType, String patientId,
      Boolean invIsPrescribable, String searchQuery, boolean isDischargeMedication) {
    BasicDynaBean patientBean = regService.findByKey(patientId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (patientBean == null) {
      errMap.addError("patient_id", "exception.form.notvalid.patient.id");
      throw new ValidationException(errMap);
    }
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
        presService.getPrescriptionItems((String) patientBean.get("mr_no"),
            (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (String) patientBean.get("visit_type"),
            (String) patientDetailsBean.get("patient_gender"), (Integer) patientBean.get("plan_id"),
            presType, (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), (String) patientBean.get("dept_name"), age,
            ageIn, invIsPrescribable, searchQuery, isDischargeMedication));
    return items;
  }

  /**
   * Inv conduction date.
   * @param patientId the string
   * @param idList the list
   * @return map
   */
  public Map<String, Object> invConductionDate(String patientId, List<String> idList) {
    BasicDynaBean patientBean = regService.findByKey(patientId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (patientBean == null) {
      errMap.addError("patient_id", "exception.form.notvalid.patient.id");
      throw new ValidationException(errMap);
    }
    Map<String, Object> items = new HashMap<>();
    items.put("item",
        presService.invConductionDateByIds((String) patientBean.get("mr_no"), idList));
    return items;
  }

  /**
   * Get recent allergies.
   */
  public Map<String, Object> getPatientRecentAllergies(String mrNo) {
    Map<String, Object> mapData = new HashMap<String, Object>();
    mapData.put("patient_recent_allergies",
        ConversionUtils.copyListDynaBeansToMap(allergiesService.getPatientRecentAllergies(mrNo)));
    return mapData;
  }

  /**
   * Get all prescription items.
   * 
   * @param patientId the string
   * @param searchQuery the string
   * @return map
   */
  public Map<String, Object> getALLPrescriptionItems(String patientId, String searchQuery,
      Boolean isDischargeMedication) {
    BasicDynaBean patientBean = regService.findByKey(patientId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (patientBean == null) {
      errMap.addError("patient_id", "exception.form.notvalid.patient.id");
      throw new ValidationException(errMap);
    }
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
    Integer planId = (Integer) patientBean.get("plan_id");
    String primarySponsorId = (String) patientBean.get("primary_sponsor_id");
    items.put("items",
        presService.getALLPrescriptionItems(true, true, true, true, true, true, true,
            (String) patientBean.get("visit_type"),
            (String) patientDetailsBean.get("patient_gender"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("center_id"), (String) patientBean.get("dept_name"), age,
            ageIn, planId, primarySponsorId, searchQuery, isDischargeMedication));

    return items;
  }

  /**
   * Get previous consultation.
   * 
   * @param consId the integer
   * @return map
   */
  public Map<String, Object> getPreviousConsultations(Integer consId,
      Boolean isCurrentConsRequired) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("consultations",
        doctorConsultationService.getPreviousConsultations(consId, isCurrentConsRequired));
    return data;
  }

  /**
   * Get previous prescriptions.
   * 
   * @param consId the integer
   * @return map
   */
  public Map<String, Object> getPrescriptions(Integer consId, Boolean isDischargeMedication) {
    BasicDynaBean consBean = doctorConsultationService.findByKey(consId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (consBean == null) {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    BasicDynaBean patientBean = regService.findByKey((String) consBean.get("patient_id"));
    Map<String, Object> items = new HashMap<String, Object>();
    items.put("items",
        presService.getPresriptions(consId, (String) patientBean.get("bed_type"),
            (String) patientBean.get("org_id"), (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), isDischargeMedication));
    return items;
  }

  /**
   * Get doctor fav prescriptions.
   * 
   * @param presType the string
   * @param consId the integer
   * @param searchQuery the string
   * @param pageNo the integer
   * @param nonHospMedicine the boolean
   * @return map
   */
  public Map<String, Object> getDoctorFavouritePrescriptions(String presType, Integer consId,
      String searchQuery, Integer pageNo, Boolean nonHospMedicine) {
    presType = (presType == null || presType.equals("")) ? "All" : presType;
    pageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
    BasicDynaBean consBean = doctorConsultationService.findByKey(consId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (consBean == null) {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    BasicDynaBean patientBean = regService.findByKey((String) consBean.get("patient_id"));
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String loggedInDoctorId = userName == null ? ""
        : (String) userService.findByKey("emp_username", userName).get("doctor_id");
    Map<String, Object> items = new HashMap<String, Object>();
    items.put("items",
        presFavService.getPrescriptionsForConsultation(presType,
            StringUtils.isEmpty(loggedInDoctorId) ? (String) consBean.get("doctor_name")
                : loggedInDoctorId,
            "o", (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
            (Integer) patientBean.get("plan_id"), (String) patientBean.get("primary_sponsor_id"),
            (Integer) patientBean.get("center_id"), searchQuery, pageNo, nonHospMedicine));
    return items;
  }

  /**
   * Get priorities.
   * @return list of map
   */
  public List<Map<String, Object>> getPriorities() {
    List<Map<String, Object>> prioritiesList = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.PRIORITY_VALUES) {
      Map<String, Object> record = new HashMap<String, Object>();
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
   * Get duration units.
   * @return list of map
   */
  public List<Map<String, Object>> getDurationUnits() {
    List<Map<String, Object>> durationUnitsList = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.DURATION_UNIT_VALUES) {
      Map<String, Object> record = new HashMap<String, Object>();
      record.put("name",
          messageUtil.getMessage(PrescriptionsService.DURATION_UNITS_CONSTANT_KEY + value, null));
      record.put("value", value);
      durationUnitsList.add(record);
    }
    return durationUnitsList;
  }

  /**
   * Get prior auth types.
   * @return list of map
   */
  public List<Map<String, Object>> getPriorAuthTypes() {
    List<Map<String, Object>> priorAuthTypes = new ArrayList<Map<String, Object>>();
    Map<String, Object> record = new HashMap<String, Object>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.N", null));
    record.put("value", "N");
    priorAuthTypes.add(record);
    record = new HashMap<String, Object>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.A", null));
    record.put("value", "A");
    priorAuthTypes.add(record);
    record = new HashMap<String, Object>();
    record.put("name", messageUtil.getMessage("constant.prescription.prior.auth.S", null));
    record.put("value", "S");
    priorAuthTypes.add(record);
    return priorAuthTypes;
  }

  /**
   * Get item types.
   * @return list of Map
   */
  public List<Map<String, Object>> getItemTypes() {
    List<Map<String, Object>> itemTypes = new ArrayList<Map<String, Object>>();
    for (String value : PrescriptionsService.ITEM_TYPES) {
      Map<String, Object> record = new HashMap<String, Object>();
      record.put("short_key",
          messageUtil.getMessage(PrescriptionsService.ITEM_TYPE_CONSTANT_SHORT_KEY + value, null));
      record.put("value", value);
      itemTypes.add(record);
    }
    return itemTypes;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getPrescriptionsEstimatedAmount(Map<String, Object> reqBody)
      throws SQLException {
    return billService.estimateAmount(reqBody);
  }

  /**
   * Gets previous prescriptions.
   * 
   * @param consId the visit id
   * @param isDischargeMedication indicator for discharge medication
   * @return map of previous prescription
   */
  public Map<String, Object> getPreviousPrescriptions(Integer consId,
      Boolean isDischargeMedication) {
    BasicDynaBean consBean = doctorConsultationService.findByKey(consId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (consBean == null) {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    BasicDynaBean patientBean = regService.findByKey((String) consBean.get("patient_id"));
    List<Map<String, Object>> previousConsultations =
        doctorConsultationService.getPreviousConsultationsByDoctor(consId,
            (String) consBean.get("mr_no"), (String) consBean.get("doctor_name"));
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("previous_prescriptions", new ArrayList<Map<String, Object>>());
    if (!previousConsultations.isEmpty()) {
      List<Map<String, Object>> records =
          presService.getPresriptions((Integer) previousConsultations.get(0).get("consultation_id"),
              (String) patientBean.get("bed_type"), (String) patientBean.get("org_id"),
              (String) patientBean.get("primary_sponsor_id"),
              (Integer) patientBean.get("center_id"), isDischargeMedication);
      Iterator<Map<String, Object>> iterator = records.iterator();
      while (iterator.hasNext()) {
        if ("I".equals(iterator.next().get("status"))) {
          iterator.remove();
        }
      }
      response.put("previous_prescriptions", records.size() > 5 ? records.subList(0, 5) : records);
    }

    List<Integer> presIds =
        presService.getRecentPrescriptionIds((String) consBean.get("doctor_name"));
    response.put("recent_prescriptions", new ArrayList<Map<String, Object>>());
    if (!presIds.isEmpty()) {
      List<Map<String, Object>> recentRecords =
          presService.getPresriptions(presIds, (String) patientBean.get("bed_type"),
              (String) patientBean.get("org_id"), (String) patientBean.get("primary_sponsor_id"),
              (Integer) patientBean.get("center_id"), isDischargeMedication);
      Iterator<Map<String, Object>> iterator = recentRecords.iterator();
      while (iterator.hasNext()) {
        if ("I".equals(iterator.next().get("status"))) {
          iterator.remove();
        }
      }
      response.put("recent_prescriptions", recentRecords);
    }
    return response;
  }

  /**
   * Cancel ERX requests.
   * @param consId the integer
   * @return map
   */
  public Map<String, Object> cancelERxRequest(Integer consId) {
    BasicDynaBean consBean = doctorConsultationService.findByKey(consId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (consBean == null) {
      errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      throw new ValidationException(errMap);
    }
    FormParameter parameters = new FormParameter(formType, itemType, (String) consBean.get("mr_no"),
        (String) consBean.get("patient_id"), consId, formKeyField);
    BasicDynaBean erxBean = pbmPrescriptionsService.getLatestConsErxBean(consId);

    if (!(erxBean != null && "eRxRequest".equals(erxBean.get("erx_request_type")))) {
      Map<String, Object> returnData = new HashMap<>();
      returnData.put("success", false);
      returnData.put("message", messageUtil.getMessage("exception.erx.request.not.exists"));
      return returnData;
    }
    return erxService.scheduleCancelERxJob(parameters);
  }

  /**
   * Convert visit.
   * @param consId the integer
   * @param genericFormId the integer
   * @param params the map
   * @return map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> convertVisit(Integer consId, Integer genericFormId,
      Map<String, Object> params) {

    BasicDynaBean consBean = getRecord(consId);
    FormParameter parameter = getFormParameter(consId, consBean);
    String doctorId = (String) consBean.get("doctor_name");
    Integer centerId = (Integer) registrationService.findByKey((String) consBean.get("patient_id"))
        .get("center_id");
    Map<String, Object> key = new HashMap<>();
    key.put("doctor_id", doctorId);
    String deptId = (String) doctorService.findByPk(key).get("dept_id");
    Map<String, Object> response = getMergedSections(parameter, doctorId, deptId, centerId, params);
    List<BasicDynaBean> forms = formComponentsService.getFromTemplatesForConsultation(doctorId,
        deptId, centerId, formType);
    response.put("forms", ConversionUtils.listBeanToListMap(forms));
    return response;
  }

  /**
   * Get merged sections.
   * @param parameter the form param
   * @param doctorId the string
   * @param deptId the string
   * @param centerId the integer
   * @param params the map
   * @return map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMergedSections(FormParameter parameter, String doctorId,
      String deptId, Integer centerId, Map<String, Object> params) {

    List<BasicDynaBean> forms = formComponentsService.getFormsForConsultation(doctorId, deptId,
        centerId, formType);
    FormParameter followUpParameter = new FormParameter(
        FormComponentsService.FormType.Form_OP_FOLLOW_UP_CONS.toString(), parameter.getItemType(),
        parameter.getMrNo(), parameter.getPatientId(), parameter.getId(),
        parameter.getFormFieldName());
    Map<String, Object> mergedSections = changeForm(followUpParameter,
        (Integer) forms.get(0).get("id"));
    boolean trxData = false;
    List<Map<String, Object>> sections = (List<Map<String, Object>>) mergedSections.get("sections");
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    List<Object> updateKeys = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    for (Map<String, Object> m : sections) {
      BasicDynaBean bean = sectionFormService.getBean();
      Integer sectionDetailId = (Integer) m.get(SECTION_DETAIL_ID_KEY);
      if (sectionDetailId != null && sectionDetailId != 0) {
        trxData = true;
        bean.set("section_detail_id", sectionDetailId);
        bean.set("form_type", "Form_CONS");
        bean.set("form_id", (Integer) m.get("form_id"));
        bean.set("display_order", (Integer) m.get("display_order"));
        updateBeans.add(bean);
        updateKeys.add(sectionDetailId);
      }
    }
    updateKeysMap.put("section_detail_id", updateKeys);
    if (!updateBeans.isEmpty()) {
      sectionFormService.updateSectionFormType(updateBeans, updateKeysMap);
    }

    ValidationErrorMap validationErrors = new ValidationErrorMap();
    doctorConsultationService.updateVisitAndConsultationType(params, parameter, trxData,
        validationErrors);
    if (!validationErrors.getErrorMap().isEmpty()) {
      throw new ValidationException(validationErrors);
    }
    return mergedSections;
  }

  /**
   * Get sections saved status.
   * @param consultationId the integer
   * @return map
   */
  public Map<String, Object> getSectionsSavedStatus(Integer consultationId) {
    Map<String, Object> metadata = new HashMap<>();
    FormParameter parameter = getFormParameter(consultationId, null);
    metadata.put("sectionsStatus", ConversionUtils
        .listBeanToListMap(sectionDetailsRepo.getSectionsWithSavedStatus(parameter, getRoleId())));
    return metadata;
  }

  /**
   * Get sections count.
   * @param consId the integer
   * @param requestBody the map
   * @return map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSectionsCount(Integer consId, Map<String, Object> requestBody) {

    BasicDynaBean consBean = getRecord(consId);
    FormParameter parameters = getFormParameter(consId, consBean);
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

  @Override
  public List<BasicDynaBean> getSectionsFromMaster(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    BasicDynaBean patientBean = registrationService.findByKey(parameter.getPatientId());
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<String, Object> doctorfilter = new HashMap<>();
    doctorfilter.put("doctor_id", formSpecificBean.get("doctor_name"));
    if (parameter.getFormType()
        .equals(FormComponentsService.FormType.Form_OP_FOLLOW_UP_CONS.toString())) {
      Map<FormComponentsService.FollowupFormColumns, Object> columns = new HashMap<>();
      columns.put(FormComponentsService.FollowupFormColumns.center_id,
          patientBean.get("center_id"));
      columns.put(FormComponentsService.FollowupFormColumns.dept_id,
          doctorService.findByPk(doctorfilter).get("dept_id"));
      columns.put(FormComponentsService.FollowupFormColumns.doctor_id,
          formSpecificBean.get("doctor_name"));
      columns.put(FormComponentsService.FollowupFormColumns.role_id,
          sessionAttributes.get("roleId"));
      return formComponentsService.getFollowUpForm(columns);
    }

    Map<FormComponentsService.OPFormColumns, Object> columns = new HashMap<>();
    columns.put(FormComponentsService.OPFormColumns.center_id, patientBean.get("center_id"));
    columns.put(FormComponentsService.OPFormColumns.dept_id,
        doctorService.findByPk(doctorfilter).get("dept_id"));
    columns.put(FormComponentsService.OPFormColumns.doctor_id, formSpecificBean.get("doctor_name"));
    columns.put(FormComponentsService.OPFormColumns.role_id, sessionAttributes.get("roleId"));
    return formComponentsService.getOPForm(columns);
  }

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

  @Override
  public FormParameter getFormParameter(Object id, BasicDynaBean formSpecificBean) {
    if (formSpecificBean == null) {
      formSpecificBean = getRecord(id);
    }
    return new FormParameter(formType, itemType, (String) formSpecificBean.get("mr_no"),
        (String) formSpecificBean.get("patient_id"), (Integer) id, formKeyField);
  }

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

  /**
   * Get package content.
   * @param packageId the integer
   * @return map
   */
  public Map<String, Object> packageContents(Integer packageId) {
    Map<String, Object> responce = new HashMap<>();
    responce.put("items",
        ConversionUtils.listBeanToListMap(orderSetsService.getPackageComponents(packageId)));
    return responce;
  }

  public BasicDynaBean getImageMarkerByKey(Map<String, Object> map) {
    return imageMarkersService.findByPk(map);
  }

  /**
   * Get content type.
   */
  public String getContentType(Map<String, Object> parameterMap, BasicDynaBean bean) {
    if (parameterMap.containsKey("content_type")) {
      return (String) parameterMap.get("content_type");
    } else if (bean != null && null != bean.get("content_type")) {
      return (String) bean.get("content_type");
    } else {
      return MediaType.IMAGE_JPEG_VALUE;
    }
  }

  @Override
  public Map<String, Object> reopenForm(Object consultationId, ModelMap requestBody) {
    getRecord(consultationId);
    String reopenRemarks = (String) requestBody.get("reopen_remarks");
    if (reopenRemarks == null || "".equals(reopenRemarks)) {
      ValidationErrorMap validationErrMap = new ValidationErrorMap();
      validationErrMap.addError("reopen_remarks", "exception.reopen.remarks.notnull");
      throw new ValidationException(validationErrMap);
    }

    String consStatus = doctorConsultationService.reopenConsultation((int) consultationId,
        reopenRemarks);
    Map<String, Object> map = new HashMap<>();
    map.put("status", consStatus);
    return map;
  }

  /**
   * Get drug to drug interaction.
   * @param medicineIds the list of integer
   * @param icdCodes list of string
   * @param allergies list of string
   * @return string
   */
  public String getcimsDrugToDrugInteraction(List<Integer> medicineIds, List<String> icdCodes,
      List<String> allergies) {
    List<BasicDynaBean> medicines = storeItemDetailsService.getMedicinesByIds(medicineIds);
    String requestXml = "<Request><Interaction><Prescribing>"
        + "#products#</Prescribing><HealthIssueCodes>"
        + "#healthissuecodes#</HealthIssueCodes><Allergies>"
        + "#molecules#</Allergies><References /></Interaction></Request>";
    String products = "";
    for (BasicDynaBean bean : medicines) {
      if (bean.get("cims_guid") != null) {
        products = products.concat("<Product reference=\"{" + bean.get("cims_guid") + "}\" />");
      }
    }
    String healthIssueCodes = "";
    if (!icdCodes.isEmpty()) {
      for (String icdCode : icdCodes) {
        healthIssueCodes = healthIssueCodes
            .concat("<HealthIssueCode code=\"" + icdCode + "\" codeType=\"ICD10\" />");
      }
    }
    String molecules = "";
    if (!allergies.isEmpty()) {
      List<BasicDynaBean> generics = genericNamesService.getGenericsBYNames(allergies);
      for (BasicDynaBean bean : generics) {
        if (bean.get("cims_guid") != null) {
          molecules = molecules
              .concat("<Molecule reference=\"{" + bean.get("cims_guid") + "}\" />");
        }
      }
    }
    FastTrackDSM ft = new FastTrackDSM();
    String realPath = RequestContext.getRequest().getServletContext().getRealPath("");
    if (!ft.open(realPath + "/WEB-INF/custom/FastTrackData.mr2", "GDDBDEMO")) {
      logger.error("Unable to open data file");
    } else {
      return ft.requestXml(requestXml.replace("#products#", products)
          .replace("#healthissuecodes#", healthIssueCodes).replace("#molecules#", molecules));
    }
    return "";
  }
  
  /**
   * Send HL7 Message.
   * 
   * @param visitId the visit id
   * @param response the response data
   */
  @Override
  public void triggerEvents(String visitId, Map<String, Object> response) {
    triggerDiagnosisEvent(visitId, response);
    triggerAllergiesEvent(visitId, response);
    triggerVitalEvent(visitId,response);
    triggerPatientProblemEvent(visitId, response);
    triggerMedicinePrescEvent(visitId, response, false);
    triggerMedicinePrescEvent(visitId, response, true);
  }
  
  @Override
  public void updateAndSendPrescriptionEmail(Object consultationId,
      Map<String, Object> requestBody) {
    FormParameter parameter = getFormParameter(consultationId, null);

    String mrNo = parameter.getMrNo();
    BasicDynaBean patDetails = patientDetailsService.findByKey(mrNo);
    String oldemailId = (String) patDetails.get("email_id");

    String schema = RequestContext.getSchema();
    String userName = RequestContext.getUserName();
    Integer centerId = RequestContext.getCenterId();
    if (StringUtils.isEmpty(oldemailId)) {
      List<String> newemailIds = (List<String>) requestBody.get("emailIds");
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put(Constants.MR_NO, mrNo);
      Object[] emailIds = newemailIds.toArray();
      String[] email = emailIds[0].toString().split(",");
      patDetails.set("email_id", email[0] );
     
      patientDetailsService.update(patDetails, keys);
    }
    if (messageUtilSms.allowMessageNotification("general_message_send")) {
      BasicDynaBean messageTypeBean = generalMessageTypesService.findByKey("message_type_id",
          "email_prescription_manual");
      if (messageTypeBean != null && messageTypeBean.get("status").equals("A")) {
        MessageManager msg = new MessageManager();
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("consultationId", consultationId);
        prescriptionData.put("centerId", centerId);
        prescriptionData.put("messageTo", requestBody.get("emailIds"));
        prescriptionData.put("schema", schema);
        prescriptionData.put("userName", userName);
        
        try {
          msg.processEvent("prescription_share", prescriptionData,true);

        } catch (SQLException | ParseException | IOException exp) {
          logger.error("Exception caused while triggering patient prescription  ", exp);
          throw new HMSException("exception.unable.send.message");
        }
      }
    }

  }

  @Override
  public String getFormDataEncodedByteArray(Object id) {
    int consultationId = (int) id;
    BasicDynaBean printPreferences =
        DocPrintConfigurationRepository.getPrescriptionPrintPreferences("BUILTIN_HTML", 6);
    OutputStream os = new ByteArrayOutputStream();
    try {
      byte[]  consultationReport = printService
          .getConsultationReport(consultationId, "BUILTIN_HTML", PrintService.ReturnType.PDF_BYTES,
              printPreferences, os);
      os.close();
      encodedBase64String = Base64.getEncoder().encodeToString(consultationReport);
    } catch (Exception ex) {
      logger.error("Error while encoding consultation data: ", ex);
    }
    return encodedBase64String;
  }

  @Override
  public Map<String, Object> getFormSegmentInformation(Object id) {
    return doctorConsultationService.getConsultationSaveEventSegmentData((int) id);
  }

  @Override
  public boolean isFormReopened(Object id) {
    BasicDynaBean bean = doctorConsultationService.findByKey((int) id);
    return bean != null ? (boolean) bean.get("cons_reopened") : false;
  }
}
