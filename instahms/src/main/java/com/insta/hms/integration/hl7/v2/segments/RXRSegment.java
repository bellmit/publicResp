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
public class RXRSegment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("RXR", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> rxrData : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(rxrData);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (("OMP_O09".equals(msgClassName) || "VXU_V04".equals(msgClassName))
            && validateSegmentData("RXR", segmentTemplate, segDataMap)) {
          addSegmentData((AbstractSegment) ((AbstractGroup) msg.get("ORDER", count)).get("RXR"),
              segDataMap, segmentTemplate);
        }
      } catch (Exception exception) {
        log.error("Exception in adding segment to RXR segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to RXR segment " + exception.getMessage());
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
    if (MEDICINE_PRESC.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(MEDICINE_PRESC_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(MEDICINE_PRESC_DATA);
      } else {
        segDataMapList =
            hl7Repository.getMedicinePrescription((List<Integer>) dataMap.get("item_ids"),
                (int) dataMap.get(Constants.CENTER_ID),
                "delete".equals(dataMap.get("item_operation")), false);
        dataMap.put(MEDICINE_PRESC_DATA, segDataMapList);
        return segDataMapList;
      }
    } else if (MEDICINE_DISPENSE.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(MEDICINE_DISPENSE_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(MEDICINE_DISPENSE_DATA);
      } else {
        segDataMapList = 
            hl7Repository.getMedicinePrescription((List<Integer>) dataMap.get("item_ids"),
                (int) dataMap.get(Constants.CENTER_ID), false, true);
        dataMap.put(MEDICINE_DISPENSE_DATA, segDataMapList);
        return segDataMapList;
      }
    } else if (VACCINATION_ADMINISTERED.equals(dataMap.get(ITEM_TYPE))) {
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
