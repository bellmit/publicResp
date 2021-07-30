package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.segment.MSH;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaMSH.
 * 
 * @author yashwant
 */
@Component
public class InstaMSH {

  /**
   * Creates the MSH.
   *
   * @param msh
   *          the msh
   * @param data
   *          the data
   * @throws DataTypeException
   *           the data type exception
   */
  public void createMSH(MSH msh, Map<String, String> data) throws DataTypeException {
    // MSH-3.1
    msh.getSendingApplication().getNamespaceID()
        .setValue(data.get("sending_application_namespace_id")); // "ADT04"
    // MSH-3.2
    msh.getSendingApplication().getUniversalID()
        .setValue(data.get("sending_application_universal_id")); // "ADT"
    // MSH-4.1
    msh.getSendingFacility().getNamespaceID().setValue("InstaHMS");
    // MSH-4.2
    msh.getSendingFacility().getUniversalID().setValue("ADT");
    // MSH-5.1
    msh.getReceivingApplication().getNamespaceID().setValue(data.get("receiving_application"));
    // MSH-6.1
    msh.getReceivingFacility().getNamespaceID().setValue(data.get("receiving_facility"));
    // MSH-7.1
    msh.getDateTimeOfMessage().getTimeOfAnEvent().setValue((String) data.get("event_date_time"));
    if (data.get("message_type") != null && data.get("event_type") != null) {
      // MSH-9.1
      msh.getMsh9_MessageType().getCm_msg1_MessageType()
          .setValue((String) data.get("message_type"));
      msh.getMsh9_MessageType().getCm_msg2_TriggerEvent().setValue((String) data.get("event_type"));
    }

  }
}
