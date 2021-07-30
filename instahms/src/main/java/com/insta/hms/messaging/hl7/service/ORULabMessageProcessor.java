package com.insta.hms.messaging.hl7.service;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.primitive.ID;
import ca.uhn.hl7v2.model.primitive.TSComponentOne;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_VISIT;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.PV1;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.messaging.Message;
import com.insta.hms.messaging.processor.GenericMessageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ORULabMessageProcessor.
 * 
 * @author yashwant
 */
public class ORULabMessageProcessor extends GenericMessageProcessor {

  private static Logger logger = LoggerFactory.getLogger(ORULabMessageProcessor.class);

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.processor.MessageProcessor#process(java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean process(Map dataMap) {
    Map<String, Object> errorMap = new HashMap<>();
    String visitId = (String)dataMap.get("patient_id");
    logger.info("CALLING Vital Service for VISIT_ID  : " + visitId);
    ((VitalReadingService) ApplicationContextProvider.getApplicationContext()
        .getBean("vitalReadingService")).saveVitals(dataMap, errorMap);
    Boolean status = errorMap.isEmpty();
    logger.info("COMPLETED STATUS : " + status + ", VISIT_ID : " + visitId);
    logger.info("####################################");
    return status;
  }


  @Override
  @SuppressWarnings("rawtypes")
  public Map parse(Message msg) {
    ca.uhn.hl7v2.model.Message hl7Message = msg.getHl7ModelMessage();
    ORU_R01 oruMsg = (ORU_R01) hl7Message;
    ORU_R01_VISIT ourVisit = oruMsg.getRESPONSE().getPATIENT().getVISIT();
    PV1 pv1 = ourVisit.getPV1();
    String visitId = pv1.getPv119_VisitNumber().getCx1_ID().getValue();
    Map<String, Object> insertRecords = new HashMap<>();
    insertRecords.put("patient_id", visitId);
    try {
      MSH msh = oruMsg.getMSH();
      Map<String, Object> insertMap = new HashMap<>();
      TSComponentOne timeOfEvent = msh.getMsh7_DateTimeOfMessage().getTimeOfAnEvent();

      insertMap.put("vital_date", timeOfEvent.getValueAsDate());
      insertMap.put("vital_time", timeOfEvent.getHour());
      insertMap.put("date_time", timeOfEvent.getValueAsCalendar());
      // insertMap.put("vital_status", "P");
      List<Map<String, Object>> insertList = new ArrayList<>();
      insertList.add(insertMap);

      List<Map<String, Object>> recordsList = new ArrayList<>();
      insertMap.put("records", recordsList);
      insertRecords.put("insert", insertList);

      List<ORU_R01_OBSERVATION> observations = oruMsg.getRESPONSE().getORDER_OBSERVATION()
          .getOBSERVATIONAll();
      for (ORU_R01_OBSERVATION observation : observations) {
        Map<String, Object> recordMap = new HashMap<>();

        OBX obx = observation.getOBX();
        ID paramId = obx.getObx3_ObservationIdentifier().getCe1_Identifier();
        ST paramLabel = obx.getObx3_ObservationIdentifier().getCe2_Text();
        Varies[] paramValues = obx.getObx5_ObservationValue();
        AbstractPrimitive nmParamValue = (AbstractPrimitive) paramValues[0].getData();
        String paramValue = nmParamValue.getValue();
        // recordMap.put("param_remarks", "Testing");
        if (paramId != null) {
          recordMap.put("param_id", paramId.getValue());
        }
        if (paramLabel != null) {
          recordMap.put("param_label", paramLabel.getValue());
        }
        if (paramValue != null) {
          recordMap.put("param_value", paramValue);
        }
        recordsList.add(recordMap);
      }
    } catch (HL7Exception ex) {
      ex.printStackTrace();
    }
    logger.info("HL7 Vitals Payload :" + insertRecords);
    return insertRecords;
  }

}
