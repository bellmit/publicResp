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
public class DG1Segment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("DG1", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> dg1Data : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(dg1Data);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (validateSegmentData("DG1", segmentTemplate, segDataMap)) {
          addSegmentData((AbstractSegment) msg.get("DG1", count), segDataMap, segmentTemplate);
        }
      } catch (Exception exception) {
        log.error("Exception in adding segment to DG1 segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to DG1 segment " + exception.getMessage());
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
    if (null != dataMap.get(DIAGNOSIS_DATA)) {
      return (List<Map<String, Object>>) dataMap.get(DIAGNOSIS_DATA);
    } else {
      segDataMapList = ConversionUtils
          .copyListDynaBeansToMap(hl7Repository.getDiagnosisData((String) dataMap.get("visit_id")));
      if (!segDataMapList.isEmpty() && NABIDH.equals(dataMap.get("code_system_name"))) {
        segDataMapList.add(0, Collections.emptyMap());
      }
      dataMap.put(DIAGNOSIS_DATA, segDataMapList);
      return segDataMapList;
    }
  }
}