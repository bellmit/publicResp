package com.insta.hms.integration.backload;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.clinical.multiuser.MultiUserRedisRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.integration.configuration.InterfaceConfigRepository;
import com.insta.hms.integration.configuration.InterfaceConfigValidator;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataBackloadService extends MasterService {

  private static final String MOD_HIE = "mod_hie";

  private static final String KEY_IDENTIFIER = "Databackload";

  private static final String SEPERATOR = "-";

  static Logger logger = LoggerFactory.getLogger(DataBackloadService.class);

  @LazyAutowired
  private DataBackloadAuditRepository repository;

  @LazyAutowired
  private InterfaceConfigRepository interfaceConfigRepository;

  @LazyAutowired
  private MessageMetaDataRepository messageMetaRepository;

  @LazyAutowired
  private DataBackloadValidator validator;

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;

  @LazyAutowired
  private MultiUserRedisRepository redisRepository;

  @LazyAutowired
  private JobService jobService;

  /**
   * The constructor.
   * 
   * @param repository Data backload audit repository
   * @param validator the validator
   */
  public DataBackloadService(InterfaceConfigRepository repository, 
      InterfaceConfigValidator validator) {
    super(repository, validator);
  }

  private boolean modIntegrationEnabled() {
    return securityService.getActivatedModules().contains(MOD_HIE);
  }

  /**
   * Search records by date range provided and the message type passed.
   * 
   * @param startDate the start date
   * @param endDate the end date
   * @param messageType the message type
   * @return list of map with records
   */
  @SuppressWarnings("unchecked")
  public List<Map<String,Object>> searchRecordsByDateRangeAndMessageType(String startDate, 
      String endDate, String messageType) {
    List<Map<String,Object>> itemsListMap = new ArrayList<>();
    try {
      itemsListMap = ConversionUtils.copyListDynaBeansToLinkedMap(
        messageMetaRepository.getRecordsByDateRange(startDate, endDate, 
          RequestContext.getCenterId(), messageType));
      messageMetaRepository.formatItemListMapIfRequired(itemsListMap, messageType);
    } catch (ParseException exception) {
      exception.printStackTrace();
    }
    return itemsListMap;
  }

  private String generateSearchID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate a redis key with given params.
   * 
   * @param searchId the unique search identifier
   * @param recordNum the record number
   * @return String value
   */
  public String generateRedisKey(String searchId, int recordNum) {
    return (KEY_IDENTIFIER + SEPERATOR + searchId + SEPERATOR + recordNum);
  }

  /**
   * Backload event trigger.
   * 
   * @param eventMap the event map
   * @param messages the messages 
   * @return status pass or fail
   */
  public boolean backloadEvent(Map<String,Object> eventMap, List<Map<String,Object>> messages) {
    if (modIntegrationEnabled()) {
      return interfaceEventMappingService.processEvent(0, eventMap, 
        messages,"DataBackload-" + eventMap.get("visit_id"));
    }
    return false;
  }

  private void placeRecordsInRedis(String searchId, List<Map<String,Object>> records) {
    for (int i = 0; i < records.size();i++) {
      redisRepository.addData(generateRedisKey(searchId, i), searchId, records.get(i));
    }
  }
  
  private String createBackloadJob(Map<String,Object> backloadData) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("event", 0);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", RequestContext.getUserName());
    jobData.put("centerId", RequestContext.getCenterId());
    jobData.put("backloadData", backloadData);
    jobData.put("userLocale", RequestContext.getLocale());
    JobDetail jobObject = buildJob("DataBackloadScheduleJob#" + backloadData.get("searchId"), 
        "HL7DataBackload", DataBackloadScheduleJob.class, jobData);
    jobService.scheduleImmediate(jobObject);
    logger.info(
        "Scheduled Event : Schema - {} , Username - {} , EventId - {}, SearchId - {}",
        RequestContext.getSchema(), RequestContext.getUserName(), 0, backloadData.get("searchId"));
    return jobObject.getKey().toString();
  }

  /**
   * Create backload audit table entry.
   * 
   * @param backloadData the backload data
   * @return baisc dyna bean
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean createBackloadAuditJobEntry(Map<String,Object> backloadData) {
    BasicDynaBean backloadAuditJobBean = repository.getBean();
    List<Map<String,Object>> interfaceDetails = 
        (List<Map<String, Object>>) backloadData.get("messages");
    Integer interfaceId = (Integer)interfaceDetails.get(0).get("interface_id");
    int backloadId = repository.getNextSequence();
    backloadAuditJobBean.set("records_found", backloadData.get("recordNum"));
    backloadAuditJobBean.set("center_id", RequestContext.getCenterId());
    backloadAuditJobBean.set("record_start_date", backloadData.get("record_start_date"));
    backloadAuditJobBean.set("record_end_date", backloadData.get("record_end_date"));
    backloadAuditJobBean.set("interface_id", interfaceId);
    backloadAuditJobBean.set("backload_audit_id", backloadId);
    backloadAuditJobBean.set("created_by", RequestContext.getUserName());
    return backloadAuditJobBean;
  }

  /**
   * Update the backload audit table entry with current status.
   * 
   * @param backloadId the backload job id
   * @param backloadData the backload data
   * @return basic dyna bean
   */
  public BasicDynaBean updateBackloadAuditEntry(int backloadId, Map<String, Object> backloadData) {
    BasicDynaBean backloadAuditBean = repository.findByKey("backload_audit_id", backloadId);
    if (backloadAuditBean != null) {
      Long processed = (Long)backloadData.get("records_processed");
      backloadAuditBean.set("records_processed",processed);
      String status = (String)backloadData.get("status");
      if (!StringUtil.isNullOrEmpty(status)) {
        backloadAuditBean.set("status",status);
      }
      Map<String,Object> keys = new HashMap<String, Object>();
      keys.put("backload_audit_id", backloadId);
      repository.update(backloadAuditBean, keys);
    } else {
      throw new HMSException("No record found");
    }
    return backloadAuditBean;
  }

  /**
   * Start backload of data.
   * 
   * @param startDate the start date
   * @param endDate the end date
   * @param interfaceId the interface id
   * @return response entity with status
   */
  @SuppressWarnings("unchecked")
  public ResponseEntity<?> startBackload(String startDate, String endDate, int interfaceId) {
    //Create a Backload Request
    Map<String,Object> result = new HashMap<String,Object>();
    if (RequestContext.getCenterId() == 0) {
      return ResponseEntity.notFound().build();
    }
    //If Backload is already initiated for a center 
    BasicDynaBean backloadAuditBean = repository.checkIfBackloadInitiatedForCenter();
    if (backloadAuditBean != null) {
      return ResponseEntity.ok(backloadAuditBean.getMap());
    }
    //Step1: Get interface details
    List<BasicDynaBean> interfaceDetailsBean = 
        interfaceConfigRepository.getInterfaceDetailsByEventCenterAndId(0, 
        RequestContext.getCenterId(), interfaceId);
    List<Map<String,Object>> interfaceDetails = 
        ConversionUtils.copyListDynaBeansToLinkedMap(interfaceDetailsBean);
    //To-Do: Check for Interface type and write implementation for different types of interfaces
    // Currently handling HL7 interface type for backload 
    if (interfaceDetails.size() == 0) {
      return ResponseEntity.notFound().build();
    }
    String getApplicableMessageType = (String)interfaceDetails.get(0).get("message_type");
    //Step1: Search records by date range provided
    List<Map<String,Object>> records = searchRecordsByDateRangeAndMessageType(startDate, 
        endDate,getApplicableMessageType);
    Map<String,Object> backloadData = new HashMap<String,Object>();
    String searchId = null;
    if (!records.isEmpty()) {
      searchId = generateSearchID();
      backloadData.put("messages", interfaceDetails);
      backloadData.put("searchId", searchId);
      backloadData.put("recordNum", (long)records.size());
      backloadData.put("record_start_date", startDate);
      backloadData.put("record_end_date", endDate);
    } else {
      return ResponseEntity.ok(result);
    }
    //Step2: Add data to redis with key generated on search id and number of records
    if (!StringUtil.isNullOrEmpty(searchId)) {
      placeRecordsInRedis(searchId, records);
    }
    //Step3: Create BackloadAudit Entry 
    BasicDynaBean backloadAuditEntryBean = createBackloadAuditJobEntry(backloadData);
    backloadData.put("backload_audit_id", backloadAuditEntryBean.get("backload_audit_id"));
    //Step4: Schedule Job for backload ; update backloadAudit Entry with Job key generated 
    String jobKey = createBackloadJob(backloadData);
    //Later the Job Key will help in tracking Job log and get start and end time for job.
    backloadAuditEntryBean.set("job_key", jobKey);
    repository.insert(backloadAuditEntryBean);
    //Fetch saved records
    BasicDynaBean backloadRecord = 
        repository.getBackloadAuditDetails((int) backloadAuditEntryBean.get("backload_audit_id"));
    result.putAll(backloadRecord.getMap());
    return ResponseEntity.ok(result);
  }

  /**
   * Gets the interface details.
   *
   * @param paramMap
   *          the param map
   * @return the note types details
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public PagedList getBackloadJobDetails(Map<String, String[]> paramMap) {
    PagedList jobs = repository.getBackloadJobDetails(paramMap);
    List<Object> interfacelist = new ArrayList<>();
    for (Map<String, Object> map : (List<Map<String, Object>>) jobs.getDtoList()) {
      Map<String, Object> interfaceMap = new HashMap();
      interfaceMap.putAll(map);
      interfacelist.add(interfaceMap);
    }
    jobs.setDtoList(interfacelist);
    return jobs;
  }
}

