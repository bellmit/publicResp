package com.insta.hms.integration.hl7.v2;

import ca.uhn.hl7v2.model.Message;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.configuration.ExportMessageQueueRepository;
import com.insta.hms.integration.configuration.InterfaceConfigService;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class Hl7MessageGeneratorJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(Hl7MessageGeneratorJob.class);

  @LazyAutowired
  private Hl7Service hl7Service;

  @LazyAutowired
  private InstaHl7Message instaHl7Message;

  @LazyAutowired
  private InterfaceConfigService interfaceConfigService;

  @LazyAutowired
  private ExportMessageQueueRepository exportMsgQueueRepository;

  private static final String INTERFACE_ID = "interface_id";

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    String schema = jobDataMap.getString("schema").trim();

    RequestContext.setConnectionDetails(new String[] {null, null, schema, "_system",
        jobDataMap.get("centerId").toString(), null, jobDataMap.get("roleId").toString(),
        jobDataMap.get("userLocale").toString().trim()});

    BasicDynaBean messageQueueBean;
    List<BasicDynaBean> messageQueueBeanList = new ArrayList<>();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    Random random = new Random();

    long eventProcessingId = Long.parseLong((String) jobDataMap.get("eventProcessingId"));
    
    Set<Integer> interfaceIdSet = new HashSet<>();
    List<Map> messages = (List<Map>) jobDataMap.get("messages");
    for (Map message : messages) {
      if (!hl7Service.validateMsgApplicability((String) message.get("applicable_visit"),
          (String) ((Map<String, Object>) jobDataMap.get("eventData")).get("visit_id"))) {
        continue;
      }

      long msgCtrlId = Long.parseLong(formatter.format(new Date()) + random.nextInt(99));

      updateEventData(((Map<String, Object>) jobDataMap.get("eventData")), eventProcessingId,
          msgCtrlId, (int) message.get(INTERFACE_ID), (int) message.get("event_mapping_id"));
      
      String messageType = (String) message.get("message_type");
      String messageVersion = (String) message.get("message_version");

      Message msg =
          instaHl7Message.createMessage(hl7Service.getNewMessage(messageType, messageVersion),
              messageType, messageVersion, (Map<String, Object>) jobDataMap.get("eventData"));
      if (null != msg) {
        interfaceIdSet.add((int) message.get(INTERFACE_ID));

        message.put("hl7_message", msg);

        messageQueueBean = createMessageQueue(eventProcessingId, msgCtrlId,
            (int) message.get(INTERFACE_ID), (new JSONObject(message)).toString());

        messageQueueBeanList.add(messageQueueBean);

        logger.info(
            "Schema -> {}, EventProcessingId -> {}, EventMappingId -> {}, Hl7 Message -> {}",
            schema, eventProcessingId, message.get("event_mapping_id"), msg.getMessage());
      }
    }
    if (!messageQueueBeanList.isEmpty()) {
      exportMsgQueueRepository.batchInsert(messageQueueBeanList);
      logger.info("Added {} event messages triggered having event processing id {} to queue",
          jobDataMap.get("event"), eventProcessingId);
      for (int intId : interfaceIdSet) {
        interfaceConfigService.scheduleSendingMsg(schema, (int) jobDataMap.get("centerId"), intId,
            false, null);
      }
    }
  }

  private BasicDynaBean createMessageQueue(long eventProcessingId, long msgCtrlId,
      int interfaceId, String jobData) {
    BasicDynaBean messageQueueBean = exportMsgQueueRepository.getBean();;
    messageQueueBean.set(ExportMessageQueueRepository.MSG_ID, msgCtrlId);
    messageQueueBean.set(ExportMessageQueueRepository.INTERFACE_ID, interfaceId);
    messageQueueBean.set(ExportMessageQueueRepository.STATUS,
          ExportMessageQueueRepository.STATUS_QUEUED);
    messageQueueBean.set(ExportMessageQueueRepository.COUNT, 0);
    messageQueueBean.set(ExportMessageQueueRepository.EVENT_PROCESSING_ID, eventProcessingId);
    messageQueueBean.set(ExportMessageQueueRepository.JOB_DATA, jobData);
    return messageQueueBean;
  }

  @SuppressWarnings("unchecked")
  private void updateEventData(Map<String, Object> eventData, long eventProcessingId,
      long msgCtrlId, int interfaceId, int eventMappingId) {
    eventData.put("msgCtrlId", msgCtrlId);
    eventData.put("eventProcessingId", eventProcessingId);
    eventData.put(INTERFACE_ID, interfaceId);
    eventData.put("event_mapping_id", eventMappingId);
    BasicDynaBean sendingReceivingInfo =
            interfaceConfigService.getSenderReceiverDetails(interfaceId);
    eventData.putAll(sendingReceivingInfo.getMap());
  }
}
