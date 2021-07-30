package com.insta.hms.integration.configuration;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.integration.hl7.v2.Hl7MessageGeneratorJob;
import com.insta.hms.integration.hl7.v2.Hl7Repository;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;
import org.apache.commons.beanutils.BasicDynaBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class InterfaceEventMappingService extends MasterService {

  static Logger logger = LoggerFactory.getLogger(InterfaceEventMappingService.class);

  @LazyAutowired
  private InterfaceEventMappingRepository repository;

  @LazyAutowired
  private InterfaceConfigService interfaceConfigService;

  @LazyAutowired
  private JobService jobService;

  @LazyAutowired
  private Hl7Repository hl7Repository;

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private RegistrationService registrationService;
  
  @LazyAutowired
  private HIEEventsRepository hieEventsRepository;

  private static Map<String, Integer> eventsMap = new HashMap<>();

  private Map<String, Object> eventDataMap;

  private static final String MOD_HIE = "mod_hie";
  
  private static final String VISIT_ID = "visit_id";

  private static final String ITEM_TYPE = "item_type";

  /**
   * Constructor.
   * 
   * @param repository the repo
   * @param validator the validator
   */
  public InterfaceEventMappingService(InterfaceEventMappingRepository repository,
      InterfaceEventMappingValidator validator) {
    super(repository, validator);
  }

  private Map<String, Integer> getEvent() {
    if (!eventsMap.isEmpty()) {
      return eventsMap;
    }
    List<BasicDynaBean> beanList = hieEventsRepository.listAll();
    for (BasicDynaBean bean : beanList) {
      eventsMap.put(((String) bean.get("event_name")).toUpperCase(), (int) bean.get("event_id"));
    }
    return eventsMap;
  }

  private boolean modIntegrationEnabled() {
    return securityService.getActivatedModules().contains(MOD_HIE);
  }

  /**
   * Process Event.
   * 
   * @param eventId the eventId
   * @param eventData the eventData
   * @return boolean is event processed
   */
  public boolean processEvent(int eventId, Map<String, Object> eventData) {
    return processEvent(eventId, eventData,"");
  }
  
  /**
   * Process Event with a particular action.
   * 
   * @param eventId the eventId
   * @param eventData the eventData
   * @param action the action
   * @return boolean is event processed
   */
  public boolean processEvent(int eventId, Map<String, Object> eventData, String action) {
    boolean status = Boolean.FALSE;
    List<Map<String,Object>> scheduledMessages = getMessagesByEvent(eventId, eventData);
    if (!scheduledMessages.isEmpty()) {
      scheduleJobForMessageList(eventId, eventData, scheduledMessages, action);
      status = Boolean.TRUE;
    }
    return status;
  }
  
  /**
   * Process Event with a particular action.
   * 
   * @param eventId the eventId
   * @param eventData the eventData
   * @param action the action
   * @return boolean is event processed
   */
  public boolean processEvent(int eventId, Map<String, Object> eventData, 
      List<Map<String,Object>> messages, String action) {
    boolean status = Boolean.FALSE;
    List<Map<String,Object>> scheduledMessages = new ArrayList<Map<String,Object>>();
    if (!messages.isEmpty()) {
      scheduledMessages = messages;
    } else {
      scheduledMessages = getMessagesByEvent(eventId, eventData);
    }
    if (!scheduledMessages.isEmpty()) {
      scheduleJobForMessageList(eventId, eventData, scheduledMessages, action);
      status = Boolean.TRUE;
    }
    return status;
  }
  
  /**
   * For messages by event and selected center.
   * 
   * @param eventId the eventId
   * @return List of Map
   */
  public List<Map<String,Object>> getMessagesByEvent(int eventId) {
    return getMessagesByEvent(eventId, new HashMap<String, Object>());
  }
  
  /**
   * get messages by Id.
   * 
   * @param eventId the eventId
   * @param eventData the eventData
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  private List<Map<String,Object>> getMessagesByEvent(int eventId, Map<String, Object> eventData) {
    if (null == eventData.get("center_id")) {
      eventData.put("center_id", ((null != RequestContext.getCenterId())
          ? RequestContext.getCenterId() : 0));
    }
    List<BasicDynaBean> messages = 
        interfaceConfigService.getMessagesListByEventAndCenterId(eventId, 
        (Integer)eventData.get("center_id"));
    return ConversionUtils.copyListDynaBeansToLinkedMap(messages);
  }
  
  /**
   * Schedule job for the message list passed.
   * 
   * @param eventId the eventId
   * @param eventData the eventData
   * @param scheduledMessages the scheduledMessages
   * @param action the source
   */
  private void scheduleJobForMessageList(int eventId, Map<String, Object> eventData, 
      List<Map<String,Object>> scheduledMessages, String action) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("event", eventId);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", RequestContext.getUserName());
    jobData.put("centerId", eventData.get("center_id"));
    jobData.put("eventData", eventData);
    jobData.put("userLocale", 
        (null != RequestContext.getLocale()) ? RequestContext.getLocale() : "en_GB");
    jobData.put("messages", scheduledMessages); 
    jobData.put("roleId", (null != RequestContext.getRoleId()) ? RequestContext.getRoleId() : 1);
    Random random = new Random();
    String eventProcessingId = formatter.format(new Date()) + random.nextInt(99);
    jobData.put("eventProcessingId", eventProcessingId);
    jobService.scheduleImmediate(buildJob("Hl7MessageGenerator" + action + eventId 
        + eventProcessingId, Hl7MessageGeneratorJob.class, jobData));
    logger.info(
        "Scheduled Event : Schema - {} , Username - {} , EventId - {}, EventProcessingId - {}"
        + ((!StringUtil.isNullOrEmpty(action)) ? ", Action - {}" : ""), RequestContext.getSchema(), 
        RequestContext.getUserName(), eventId, eventProcessingId, action);
  }
  
  /**
   * Pre reg event.
   * 
   * @param mrNo the mr no
   */
  public void preRegistrationEvent(String mrNo) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put("mr_no", mrNo);
      processEvent(getEvent().get("PRE_REGISTRATION"), eventDataMap);
    }
  }

  /**
   * Merge patient.
   * 
   * @param mrNo the mr no
   * @param duplicateMrNo the duplicate mr no
   */
  public void mergePatientsEvent(String mrNo, String duplicateMrNo) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put("mr_no", mrNo);
      eventDataMap.put("duplicate_mr_no", duplicateMrNo);
      processEvent(getEvent().get("MERGE_PATIENT"), eventDataMap);
    }
  }

  /**
   * Visit reg event.
   * 
   * @param visitId the visit id
   * @param newPatient new or existing patient
   */
  public boolean visitRegistrationEvent(String visitId, boolean newPatient) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put("visit_id", visitId);
      return processEvent(
          newPatient ? getEvent().get("NEW_PATIENT_REG") : getEvent().get("EXISTING_PATIENT_REG"),
          eventDataMap);
    }
    return false;
  }

  /**
   * HIE patient consent event.
   *
   * @param patientConsent the patientConsent
   * @param consentTimeStamp the consentTimeStamp
   * @param mrNo the mrNo
   * @param centerId the centerId
   * @throws ParseException the exception
   */
  public void patientConsentEvent(Integer patientConsent, String consentTimeStamp,
       String mrNo, Integer centerId) throws ParseException {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put("patient_consent", patientConsent);
      DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      Date date = (Date) formatter.parse(consentTimeStamp);
      eventDataMap.put("consent_timestamp",new Timestamp(date.getTime()));
      eventDataMap.put("mr_no", mrNo);
      eventDataMap.put("center_id", centerId );
      processEvent(getEvent().get("PATIENT_CONSENT"), eventDataMap);
    }
  }

  /**
   * Edit visit event.
   * 
   * @param visitId the visit id
   */
  public void editVisitEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("UPDATE_VISIT"), eventDataMap);
    }
  }

  /**
   * Edit patient event.
   * 
   * @param mrNo the mr no
   */
  public void editPatientEvent(String mrNo) {
    editPatientEvent(mrNo, null);
  }

  /**
   * Edit patient event.
   * 
   * @param mrNo the mr no
   * @param visitId the visit id
   */
  public void editPatientEvent(String mrNo, String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put("mr_no", mrNo);
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("UPDATE_PATIENT"), eventDataMap);

      //check if confidentiality group is updated/inserted
      if (hl7Repository.isPatientAddedToPCG(mrNo).size() > 0) {
        processEvent(getEvent().get("ADD_PATIENT_TO_PCG"), eventDataMap);
      }
    }
  }

  /**
   * Visit Close event.
   * 
   * @param visitId the visit id
   * @param centerId the center id
   */
  public void visitCloseEvent(String visitId, Integer centerId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      if (null != centerId) {
        eventDataMap.put("center_id", centerId);
      }
      processEvent(hl7Repository.diagnosisExists(visitId) ? getEvent().get("VISIT_CLOSE")
          : getEvent().get("VISIT_CLOSE_HAVING_NO_DIAGNOSIS"), eventDataMap);
    }
  }

  /**
   * Physical discarge event.
   * 
   * @param visitId the visit id
   */
  public void physicalDischargeEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(hl7Repository.diagnosisExists(visitId) ? getEvent().get("PHYSICAL_DISCHARGE")
          : getEvent().get("PHYSICAL_DISCHARGE_HAVING_NO_DIAGNOSIS"), eventDataMap);
    }
  }

  /**
   * Opto Ip onversion event.
   * 
   * @param visitId the visit id
   */
  public void opToIpConvertionEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("OP_IP_CONVERTION"), eventDataMap);
    }
  }

  /**
   * Readmit event.
   * 
   * @param visitId the visit id
   */
  public void readmitEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("READMIT_PATIENT"), eventDataMap);
    }
  }

  /**
   * Diagnosis event.
   * 
   * @param visitId the visit id
   */
  public void diagnosisEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(registrationService.isVisitActive(visitId) ? getEvent().get("DIAGNOSIS")
          : getEvent().get("DIAGNOSIS_OF_INACTIVE_VISIT"), eventDataMap);
    }
  }

  /**
   * Allergies event.
   * 
   * @param visitId the visit id
   */
  public void allergiesEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("ALLERGIES"), eventDataMap);
    }
  }

  /**
   * Edit insurance event.
   * 
   * @param visitId the visit id
   */
  public void editInsuranceDetailsEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("UPDATE_INSURANCE_DETAILS"), eventDataMap);
    }
  }

  /**
   * Patient problem event which triggeres on different operations insert, update and delete.
   * 
   * @param patientId the visit id
   * @param centerId the center id
   * @param itemOperation specifies operation of problem insert, update Or delete
   * @param itemIds the problem id's which are operated on
   */
  public void chronicProblemsEvent(String patientId, int centerId, String itemOperation,
      List<Integer> itemIds) {
    if (modIntegrationEnabled()) {
      clinicalEvent(patientId, centerId, itemOperation, itemIds, "chronic_problems");
    }
  }

  /**
   * Medicine prescription event which triggeres on different operations insert, update and delete.
   * 
   * @param patientId the visit id
   * @param centerId the center id
   * @param itemOperation specifies operation of problem insert, update Or delete
   * @param itemIds the problem id's which are operated on
   */
  public void medicinePrescriptionEvent(String patientId, int centerId, String itemOperation,
      List<Integer> itemIds) {
    if (modIntegrationEnabled()) {
      clinicalEvent(patientId, centerId, itemOperation, itemIds, "medicine_prescription");
    }
  }

  /**
   * Patient problem event which triggeres on different operations insert, update and delete.
   * 
   * @param visitId the visit id
   * @param centerId the center id
   * @param itemOperation specifies operation of problem insert, update Or delete
   * @param itemIds the problem id's which are operated on
   * @param itemType the type of the item cronic_problems, investigation
   */
  private void clinicalEvent(String visitId, int centerId, String itemOperation,
      List<Integer> itemIds, String itemType) {
    if (StringUtils.isEmpty(visitId) || itemIds == null || itemIds.isEmpty()) {
      return;
    }
    eventDataMap = new HashMap<>();
    eventDataMap.put("center_id", centerId);
    eventDataMap.put(ITEM_TYPE, itemType);
    eventDataMap.put(VISIT_ID, visitId);
    eventDataMap.put("item_ids", itemIds);
    eventDataMap.put("item_operation", itemOperation);
    if ("chronic_problems".equals(itemType)) {
      if ("insert".equals(itemOperation)) {
        processEvent(getEvent().get("PATIENT_PROBLEM_ADD"), eventDataMap);
      } else if ("update".equals(itemOperation)) {
        processEvent(getEvent().get("PATIENT_PROBLEM_UPDATE"), eventDataMap);
      } else if ("delete".equals(itemOperation)) {
        processEvent(getEvent().get("PATIENT_PROBLEM_DELETE"), eventDataMap);
      }
    } else if ("medicine_prescription".equals(itemType)) {
      if ("insert".equals(itemOperation)) {
        processEvent(getEvent().get("MEDICINE_PRESCRIPTION_ADD"), eventDataMap);
      } else if ("update".equals(itemOperation)) {
        processEvent(getEvent().get("MEDICINE_PRESCRIPTION_UPDATE"), eventDataMap);
      } else if ("delete".equals(itemOperation)) {
        processEvent(getEvent().get("MEDICINE_PRESCRIPTION_DELETE"), eventDataMap);
      }
    }
  }

  /**
   * Surgery event.
   * 
   * @param visitId the visit id
   */
  public void surgeryEvent(String visitId, int operationDetailId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      eventDataMap.put("operation_id", operationDetailId);
      processEvent(getEvent().get("SURGERY"), eventDataMap);
    }
  }

  /**
   * Investigation Sign off trigger.
   * 
   * @param visitId the visit id
   * @param prescId the presc id
   * @param isLab to identify lab or rad
   */
  public void investigationSignOff(String visitId, int prescId, boolean isLab) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      eventDataMap.put("presc_id", prescId);
      eventDataMap.put(ITEM_TYPE, "investigation");
      if (isLab) {
        processEvent(getEvent().get("LABORATORY_SIGNOFF"), eventDataMap);
      } else {
        processEvent(getEvent().get("RADIOLOGY_SIGNOFF"), eventDataMap);
      }
    }
  }

  /**
   * Medicine presc dispense trigger.
   * 
   * @param visitId the visit id
   * @param itemIds the presc id
   */
  public void medicinePrescDispenseEvent(String visitId, List<Integer> itemIds) {
    if (modIntegrationEnabled() && null != itemIds && !itemIds.isEmpty()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      eventDataMap.put(ITEM_TYPE, "medicine_dispense");
      processEvent(getEvent().get("MEDICINE_PRESCRIPTION_DISPENSE"), eventDataMap);
    }
  }

  /**
   * Save and Finalise trigger for clinical forms.
   * @param event event type.
   * @param eventData event data map.
   */
  public void saveAndFinaliseFormEvent(String event, Map<String, Object> eventData) {
    eventDataMap = new HashMap<>(eventData);
    processEvent(getEvent().get(event), eventDataMap);
  }

  /**
   * Edit vital reading event.
   * 
   * @param visitId the visit id
   */
  public void vitalReadingEvent(String visitId) {
    if (modIntegrationEnabled()) {
      eventDataMap = new HashMap<>();
      eventDataMap.put(VISIT_ID, visitId);
      processEvent(getEvent().get("VITALS"), eventDataMap);
    }
  }

  /**
   * Trigger for HL7 for Add/Edit Vaccination.
   * 
   * @param visitId the patientId
   * @param patientVaccineIdList list of all patientVaccineId
   * @param isNew whether vaccination is newly added / updated
   */
  public void vaccinationAddOrEditEvent(String visitId, List<Integer> patientVaccineIdList,
      boolean isNew) {
    eventDataMap = new HashMap<>();
    eventDataMap.put("item_ids", patientVaccineIdList);
    eventDataMap.put(VISIT_ID, visitId);
    eventDataMap.put(ITEM_TYPE, "vaccination_administered");
    if (isNew) {
      processEvent(getEvent().get("PATIENT_VACCINATION_ADD"), eventDataMap);
    } else {
      processEvent(getEvent().get("PATIENT_VACCINATION_UPDATE"), eventDataMap);
    }
  }
}
