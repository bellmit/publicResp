package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ROLSegment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("ROL", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> rolData : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(rolData);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        addSegmentData(
            (AbstractSegment) ((AbstractGroup) ((AbstractGroup) msg.get("PROBLEM", count))
                .get("PROBLEM_ROLE")).get("ROL"),
            segDataMap, segmentTemplate);
      } catch (HL7Exception exception) {
        log.error("HL7 Exception in adding segment to PV2 segment for message {} : {}",
            msgClassName, exception);
        throw new HL7Exception(
            "HL7 Exception in adding segment to PV2 segment " + exception.getMessage());
      } catch (Exception exception) {
        log.error("Exception in adding segment to PV2 segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to PV2 segment " + exception.getMessage());
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
    if (CHRONIC_PROBLEMS.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(CHRONIC_PROBLEMS_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(CHRONIC_PROBLEMS_DATA);
      } else {
        segDataMapList =
            hl7Repository.getChronicProblemsData((List<Integer>) dataMap.get("item_ids"));
        dataMap.put(CHRONIC_PROBLEMS_DATA, segDataMapList);
        return segDataMapList;
      }
    } else {
      return Collections.emptyList();
    }
  }
}
