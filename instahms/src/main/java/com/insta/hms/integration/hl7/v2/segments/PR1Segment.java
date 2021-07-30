package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PR1Segment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("PR1", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> pr1Data : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(pr1Data);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (validateSegmentData("PR1", segmentTemplate, segDataMap)) {
          addSegmentData((AbstractSegment) ((AbstractGroup) msg.get("PROCEDURE", count)).get("PR1"),
              segDataMap, segmentTemplate);
        }
      } catch (HL7Exception exception) {
        log.error("HL7 Exception in adding segment to PR1 segment for message {} : {}",
            msgClassName, exception);
        throw new HL7Exception(
            "HL7 Exception in adding segment to PR1 segment " + exception.getMessage());
      } catch (Exception exception) {
        log.error("Exception in adding segment to PR1 segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to PR1 segment " + exception.getMessage());
      }
      if (!repeatSegment) {
        break;
      }
      count++;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Map<String, Object>> getSegmentData(Map<String, Object> dataMap) {
    if (null != dataMap.get(SURGERY_DATA)) {
      return (List<Map<String, Object>>) dataMap.get(SURGERY_DATA);
    } else {
      segDataMapList = hl7Repository.getSurgeryData((String) dataMap.get("visit_id"),
          (Integer) dataMap.get("operation_id"));
      dataMap.put(SURGERY_DATA, segDataMapList);
      return segDataMapList;
    }
  }
}
