package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AL1Segment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("AL1", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> al1Data : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(al1Data);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (validateSegmentData("AL1", segmentTemplate, segDataMap)) {
          addSegmentData((AbstractSegment) msg.get("AL1", count), segDataMap, segmentTemplate);
        }
      } catch (Exception exception) {
        log.error("Exception in adding segment to AL1 segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to AL1 segment " + exception.getMessage());
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
    if (null != dataMap.get(ALLERGIES_DATA)) {
      return (List<Map<String, Object>>) dataMap.get(ALLERGIES_DATA);
    } else {
      segDataMapList = ConversionUtils
          .copyListDynaBeansToMap(hl7Repository.getAllergiesData((String) dataMap.get("visit_id")));
      if (NABIDH.equals(dataMap.get("code_system_name"))) {
        segDataMapList.add(0, Collections.emptyMap());
      }
      dataMap.put(ALLERGIES_DATA, segDataMapList);
      return segDataMapList;
    }
  }
}