package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PIDSegment extends InstaHl7Segment {

  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("PID", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> pidData : dataList) {
      segDataMap = new HashMap<>();
      segDataMap.putAll(pidData);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (msgClassName.equals("ORU_R01")) {
          addSegmentData(
              (AbstractSegment) ((AbstractGroup) ((AbstractGroup) msg.get("PATIENT_RESULT", count))
                  .get("PATIENT")).get("PID"),
              segDataMap, segmentTemplate);
        } else if (msgClassName.equals("OMP_O09") || msgClassName.equals("RDS_013")) {
          addSegmentData((AbstractSegment) ((AbstractGroup) msg.get("PATIENT")).get("PID"),
              segDataMap, segmentTemplate);
        } else {
          addSegmentData((AbstractSegment) msg.get("PID"), segDataMap, segmentTemplate);
        }
      } catch (HL7Exception exception) {
        log.error("HL7 Exception in adding segment to PID segment for message {} : {}",
            msgClassName, exception);
        throw new HL7Exception(
            "HL7 Exception in adding segment to PID segment " + exception.getMessage());
      } catch (Exception exception) {
        log.error("Exception in adding segment to PID segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to PID segment " + exception.getMessage());
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
    if (null != dataMap.get(PATIENT_DATA)) {
      return (List<Map<String, Object>>) dataMap.get(PATIENT_DATA);
    } else {
      segDataMapList = ConversionUtils
          .copyListDynaBeansToMap(hl7Repository.getPatientData((String) dataMap.get("mr_no")));
      dataMap.put(PATIENT_DATA, segDataMapList);
      return segDataMapList;
    }
  }
}
