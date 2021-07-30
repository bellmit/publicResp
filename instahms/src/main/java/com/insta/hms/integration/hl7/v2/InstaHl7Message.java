package com.insta.hms.integration.hl7.v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InstaHl7Message {

  static Logger log = LoggerFactory.getLogger(InstaHl7Message.class);

  @LazyAutowired
  private Hl7Service hl7Service;

  @LazyAutowired
  private Hl7Validator hl7Validator;

  @LazyAutowired
  private Hl7Repository hl7Repository;

  @LazyAutowired
  protected InstaHl7Segment segmentObject;

  /**
   * Creates a message.
   * 
   * @param messageType the message type
   * @param messageVersion the message version
   * @param eventData the map
   * @return Message
   */
  @SuppressWarnings("unchecked")
  public Message createMessage(Message message, String messageType, String messageVersion,
      Map<String, Object> eventData) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.putAll(eventData);
    dataMap.putAll(hl7Service.getCurrentTimeMap());
    dataMap.putAll(hl7Service.getCodeSets());
    dataMap.putAll(hl7Repository.getCenterDetails((int) eventData.get("center_id")).getMap());
    dataMap.put("message_type", messageType);
    dataMap.put("message_version", messageVersion);

    String eventProcessingId = String.valueOf(eventData.get("eventProcessingId"));
    String msgCtrlId = String.valueOf(eventData.get("msgCtrlId"));
    try {
      List<BasicDynaBean> segmentsList = hl7Service.getSegmentsListForMessage(
          (int) eventData.get("interface_id"), messageType, messageVersion);

      // Validates if message needs to be generated or not.
      if (!getDataMap(segmentsList, dataMap)) {
        log.info("Dropping off the {} message generation for event processing id {}", messageType,
            eventProcessingId);
        return null;
      }

      getMessage(message, segmentsList, messageVersion, dataMap, eventProcessingId, msgCtrlId);

      return message;
    } catch (HL7Exception exception) {
      log.error("Exception in creating hl7 message {} : {}", msgCtrlId, exception);
      return null;
    }
  }

  private boolean getDataMap(List<BasicDynaBean> segmentsList, Map<String, Object> dataMap) {
    hl7Service.verifyBasicRequiredDataForCreatingMessage(dataMap);
    // Gets the data related to the associated segments.
    for (BasicDynaBean segBean : segmentsList) {
      segmentObject = hl7Service.getSegmentObject((String) segBean.get("segment"));
      segmentObject.getSegmentData(dataMap);
    }
    return hl7Validator.checkIncludeExcludeRule(dataMap);
  }

  private void getMessage(Message message, List<BasicDynaBean> segmentsList, String messageVersion,
      Map<String, Object> dataMap, String eventProcessingId, String msgCtrlId) throws HL7Exception {
    for (BasicDynaBean segBean : segmentsList) {
      segmentObject = hl7Service.getSegmentObject((String) segBean.get("segment"));
      segmentObject.getSegment(message, messageVersion, dataMap, segBean);
    }
    log.info("Message created succesfully : {} - {}", eventProcessingId, msgCtrlId);
  }
}
