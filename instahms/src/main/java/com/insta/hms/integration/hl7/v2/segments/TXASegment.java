package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.ClinicalFormHl7Adapter;
import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TXASegment extends InstaHl7Segment {

  @LazyAutowired
  private ClinicalFormHl7Adapter clinicalFormHl7Adapter;

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("TXA", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> txaData : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(txaData);
      segDataMap.putAll(dataMap);   
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (msgClassName.equals("MDM_T02") || msgClassName.equals("MDM_T04")) {
          addSegmentData((AbstractSegment) msg.get("TXA", count), segDataMap, segmentTemplate);
        }
      } catch (HL7Exception exception) {
        log.error("HL7 Exception in adding segment to TXA segment for message {} : {}",
            msgClassName, exception);
        throw new HL7Exception(
            "HL7 Exception in adding segment to TXA segment " + exception.getMessage());
      } catch (Exception exception) {
        log.error("Exception in adding segment to OBX segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
          "Exception in adding segment to OBX segment " + exception.getMessage());
      }
      if (!repeatSegment) {
        count++; 
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Map<String, Object>> getSegmentData(Map<String, Object> dataMap) {
    if (null != dataMap.get(FORM_DATA)) {
      return (List<Map<String, Object>>) dataMap.get(FORM_DATA);
    } else {
      segDataMapList = clinicalFormHl7Adapter.getClinicalFormSegmentData(dataMap);
      dataMap.put(FORM_DATA, segDataMapList);
      return segDataMapList;
    }
  }
}
