package com.insta.hms.integration.hl7.v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractComposite;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Varies;

import com.insta.hms.common.FtlProcessor;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public abstract class InstaHl7Segment {

  protected static Logger log = LoggerFactory.getLogger(InstaHl7Segment.class);

  protected static final String NABIDH = "NABIDH";

  @LazyAutowired
  protected Hl7Service hl7Service;

  @LazyAutowired
  protected Hl7Repository hl7Repository;

  @LazyAutowired
  private FtlProcessor ftlProcessor;

  protected List<Map<String, Object>> segDataMapList;

  protected Map<String, Object> segmentTemplate;

  protected Map<String, Object> segDataMap;

  protected static final String GENERIC_SEGMENT = "GenericSegment";

  protected static final String INTERFACE_ID = "interface_id";

  protected static final String ITEM_TYPE = "item_type";

  protected static final String CHRONIC_PROBLEMS = "chronic_problems";

  protected static final String INVESTIGATION = "investigation";

  protected static final String FORM = "form";

  protected static final String MEDICINE_PRESC = "medicine_prescription";

  protected static final String MEDICINE_DISPENSE = "medicine_dispense";

  protected static final String VACCINATION_ADMINISTERED = "vaccination_administered";

  protected static final String PATIENT_DATA = "patient_data";

  protected static final String VISIT_DATA = "visit_data";

  protected static final String INSURANCE_DATA = "insurance_data";

  protected static final String DIAGNOSIS_DATA = "diagnosis_data";

  protected static final String ALLERGIES_DATA = "allergies_data";

  protected static final String CON_ADM_DATA = "con_adm_data";

  protected static final String CHRONIC_PROBLEMS_DATA = "chronic_problems_data";

  protected static final String INVESTIGATION_DATA = "investigation_data";

  protected static final String FORM_DATA = "form_data";

  protected static final String SURGERY_DATA = "surgery_data";

  protected static final String MEDICINE_PRESC_DATA = "medicine_prescription_data";

  protected static final String MEDICINE_DISPENSE_DATA = "medicine_dispense_data";

  protected static final String VACCINATION_ADMINISTERED_DATA = "vaccination_administered_data";

  protected static final String VITAL_DATA = "vital_data";

  /**
   * Get Segment filled with data.
   * 
   * @param message the message
   * @param messageVersion the message version
   * @param dataMap the map
   * @param segmentBean the bean
   * @throws HL7Exception exception
   */
  public void getSegment(Message message, String messageVersion, Map<String, Object> dataMap,
      BasicDynaBean segmentBean) throws HL7Exception {}

  protected List<Map<String, Object>> getSegmentData(Map<String, Object> dataMap) {
    return Collections.emptyList();
  }

  protected Map<String, Object> getSegmentTemplate(String segmentName, String version,
      int interfaceId) {
    Map<String, Object> segmentTemplateMap = new HashMap<>();
    List<BasicDynaBean> templateBeanList =
        hl7Service.getSegmentTemplate(segmentName.toLowerCase(), version, interfaceId);
    for (BasicDynaBean bean : templateBeanList) {
      if (!StringUtils.isEmpty(bean.get("field_template"))) {
        segmentTemplateMap.put(String.valueOf(bean.get("segment_field_number")),
            bean.get("field_template"));
      }
    }
    return segmentTemplateMap;
  }

  protected void addSegmentData(AbstractSegment segment, Map<String, Object> segmentData,
      Map<String, Object> segTemplate) throws HL7Exception {
    int totalFields = GENERIC_SEGMENT.equals(segment.getClass().getSimpleName())
         ? segTemplate.size() : segment.numFields();
    String value;
    String fieldTemplate;
    String segmentName = segment.getName().toUpperCase();
    for (int fieldCount = 1; fieldCount <= totalFields; fieldCount++) {
      if (!StringUtils.isEmpty(segTemplate.get(segmentName + "_" + fieldCount))) {
        fieldTemplate = (String) segTemplate.get(segmentName + "_" + fieldCount);
        value = ftlProcessor.process(fieldTemplate, segmentData);
        try {
          if (segment.getField(fieldCount, 0) instanceof AbstractPrimitive) {
            ((AbstractPrimitive) segment.getField(fieldCount, 0)).setValue(value);
          } else if (segment.getField(fieldCount, 0) instanceof AbstractComposite) {
            String[] subFields = value.split("\\~");
            int subFieldCount = subFields.length;
            for (int extraCompCount = 0;extraCompCount < subFieldCount;extraCompCount++) {
              ((AbstractComposite) segment.getField(fieldCount, extraCompCount))
                    .parse(subFields[extraCompCount]);
            }
          } else if (segment.getField(fieldCount, 0) instanceof Varies) {
            String[] subFields = value.split("\\^");
            int subFieldCount = subFields.length;
            ((AbstractPrimitive) ((Varies) segment.getField(fieldCount, 0)).getData())
                .setValue(subFields[0]);
            if (subFieldCount > 1) {
              for (int extraCompCount = 1;extraCompCount < subFieldCount;extraCompCount++) {
                ((Varies) ((Varies) segment.getField(fieldCount, 0)).getExtraComponents()
                    .getComponent(extraCompCount - 1)).parse(subFields[extraCompCount]);
              }
            }
          } else {
            throw new HL7Exception("Neither an AbstractPrimitive nor an AbstractComposite field");
          }
        } catch (Exception exception) {
          throw new HL7Exception(exception);
        }
      }
    }
  }

  protected boolean validateSegmentData(String segName, Map<String, Object> segTemplate,
      Map<String, Object> segDataMap) {
    if (!StringUtils.isEmpty(segTemplate.get(segName + "_0"))) {
      return "true".equalsIgnoreCase(
          ftlProcessor.process((String) segTemplate.get(segName + "_0"), segDataMap));
    }
    return true;
  }
}
