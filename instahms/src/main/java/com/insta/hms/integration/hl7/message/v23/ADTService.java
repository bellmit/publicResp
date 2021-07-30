package com.insta.hms.integration.hl7.message.v23;

import com.insta.hms.messaging.MessageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ADTService.
 * 
 * @author yashwant
 */
@Service("adtService")
public class ADTService {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ADTService.class);

  /**
   * Creates the and send ADT message.
   *
   * @param eventType
   *          the event type
   * @param adtData
   *          the adt data
   */
  public void createAndSendADTMessage(String eventType, Map<String, Object> adtData) {

    logger.debug("****************************************");
    logger.info("SCHEDULING HL7 jobs EVENT_TYPES :" + eventType);
    logger.info("PATIENT_DATA : " + adtData);
    Map<String, Object> jobData = new HashMap<>();
    jobData.putAll(adtData);
    MessageManager mgr = new MessageManager();
    try {
      mgr.processEvent(eventType, jobData);
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }
}
