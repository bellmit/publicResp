package com.insta.hms.integration.backload;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.multiuser.MultiUserRedisRepository;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBackloadScheduleJob extends GenericJob {

  @Autowired
  private DataBackloadService dataBackloadService;

  @LazyAutowired
  private MultiUserRedisRepository redisRepository;

  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;

  @SuppressWarnings("unchecked")
  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    Map<String,Object> backloadData = new HashMap<String,Object>();
    backloadData.putAll((Map<String,Object>)jobDataMap.get("backloadData"));
    RequestContext.setConnectionDetails(new String[] {
        null, 
        null,
        jobDataMap.getString("schema").trim(),
        jobDataMap.getString("userName").trim(),
        jobDataMap.get("centerId").toString(),
        null,
        null,
        jobDataMap.get("userLocale").toString().trim()
    });
    String searchId = (String)backloadData.get("searchId");
    long recordNum = (long) backloadData.get("recordNum");
    long count = 0;
    String key = null;
    Map<String,Object> hl7EventDataMap = new HashMap<String,Object>();
    List<Map<String,Object>> messages = (List<Map<String, Object>>) backloadData.get("messages");
    for (int i = 0; i < recordNum;i++) {
      key = dataBackloadService.generateRedisKey(searchId, i);
      hl7EventDataMap.put("center_id", jobDataMap.get("centerId"));
      hl7EventDataMap.putAll(redisRepository.getHashKeyData(key, searchId));
      if (dataBackloadService.backloadEvent(hl7EventDataMap, messages)) {
        count++;
      }
      redisRepository.deleteKey(key);
      hl7EventDataMap.clear();
    }
    backloadData.put("records_processed", count);
    backloadData.put("status", "COMPLETED");
    dataBackloadService.updateBackloadAuditEntry((int)backloadData.get("backload_audit_id"), 
        backloadData);
  }
}
