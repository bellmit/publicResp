package com.insta.hms.integration.hl7.v2.segments;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.ClinicalFormHl7Adapter;
import com.insta.hms.integration.hl7.v2.InstaHl7Segment;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OBXSegment extends InstaHl7Segment {

  @LazyAutowired
  private ClinicalFormHl7Adapter clinicalFormHl7Adapter;

  @SuppressWarnings("unchecked")
  @Override
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {
    boolean repeatSegment = (boolean) segmentBean.get("repeat_segment");
    AbstractGroup msg = (AbstractGroup) message;
    String msgClassName = message.getClass().getSimpleName();
    List<Map<String, Object>> dataList = getSegmentData(dataMap);
    segmentTemplate = getSegmentTemplate("OBX", messageVersion, (int) dataMap.get(INTERFACE_ID));
    int count = 0;
    for (Map<String, Object> obxData : dataList) {
      if (msgClassName.contains("ADT") 
          && !StringUtil.isNullOrEmpty((String) dataMap.get("code_system_name")) 
          && "NABIDH".equals((String) dataMap.get("code_system_name"))) {
        HashMap<String, Object> codeSet = (HashMap<String, Object>) dataMap.get("code");
        HashMap<String, Object> nabidhCodeSets = (HashMap<String, Object>) codeSet.get("nabidh");
        HashMap<String, Object> vitalMap = (HashMap<String, Object>)
            nabidhCodeSets.get("vital_parameter_master");
        if (vitalMap.get(obxData.get("param_id").toString()) == null) { 
          continue; 
        }
      }
      segDataMap = new HashMap<>();
      segDataMap.putAll(obxData);
      segDataMap.putAll(dataMap);
      segDataMap.put("sequence", String.valueOf(count + 1));
      try {
        if (validateSegmentData("OBX", segmentTemplate, segDataMap)) {
          if (msgClassName.equals("ORU_R01")) {
            addSegmentData((AbstractSegment) ((AbstractGroup) ((AbstractGroup) ((AbstractGroup) msg
                  .get("PATIENT_RESULT")).get("ORDER_OBSERVATION")).get("OBSERVATION", count))
                        .get("OBX"), segDataMap, segmentTemplate);
          } else if (msgClassName.equals("MDM_T02") || msgClassName.equals("MDM_T04")) {
            addSegmentData((AbstractSegment) ((AbstractGroup) msg.get("OBSERVATION", count))
                 .get("OBX"), segDataMap, segmentTemplate);
          } else {
            addSegmentData((AbstractSegment) msg.get("OBX", count), segDataMap, segmentTemplate);
          }
        }
      } catch (Exception exception) {
        log.error("Exception in adding segment to OBX segment for message {} : {}", msgClassName,
            exception);
        throw new HL7Exception(
            "Exception in adding segment to OBX segment " + exception.getMessage());
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
    if (INVESTIGATION.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(INVESTIGATION_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(INVESTIGATION_DATA);
      } else {
        segDataMapList = ConversionUtils.copyListDynaBeansToMap(
            hl7Repository.getInvestigationData((int) dataMap.get("presc_id")));
        dataMap.put(INVESTIGATION_DATA, segDataMapList);
        return segDataMapList;
      }
    } else if (FORM.equals(dataMap.get(ITEM_TYPE))) {
      if (null != dataMap.get(FORM_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(FORM_DATA);
      } else {
        segDataMapList = clinicalFormHl7Adapter.getClinicalFormSegmentData(dataMap);
        dataMap.put(FORM_DATA, segDataMapList);
        return segDataMapList;
      }
    } else {
      if (null != dataMap.get(VITAL_DATA)) {
        return (List<Map<String, Object>>) dataMap.get(VITAL_DATA);
      } else {
        segDataMapList = ConversionUtils.copyListDynaBeansToMap(
            hl7Repository.getVitalReadingData((String) dataMap.get("visit_id")));
        dataMap.put(VITAL_DATA, segDataMapList);
        return segDataMapList;
      }
    }
  }
}
