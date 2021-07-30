package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.bob.hms.common.Constants;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RXASegment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("RXA", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> rxaData : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(rxaData);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if ("VXU_V04".equals(msgClassName)) {
          addSegmentData((AbstractSegment) ((AbstractGroup) msg.get("ORDER", count)).get("RXA"),
              segDataMap, segmentTemplate);
        }
      } catch (Exception exception) {
        log.error("Exception in adding segment to RXA segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to RXA segment " + exception.getMessage());
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
    if (VACCINATION_ADMINISTERED.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(VACCINATION_ADMINISTERED_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(VACCINATION_ADMINISTERED_DATA);
      } else {
        segDataMapList = ConversionUtils.copyListDynaBeansToMap(
            hl7Repository.getVaccinationAdministeredData((List<Integer>) dataMap.get("item_ids"),
                (Integer) dataMap.get(Constants.CENTER_ID)));
        dataMap.put(VACCINATION_ADMINISTERED_DATA, segDataMapList);
        return segDataMapList;
      }
    } else {
      return Collections.emptyList();
    }
  }
}
