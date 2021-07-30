package com.insta.hms.core.clinical.forms;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class SectionService.
 *
 * @author krishnat
 */
public abstract class SectionService {

  /** The section id. */
  public int sectionId;

  /**
   * Save's the section records (details).
   *
   * @param requestBody the request body
   * @param sdbean the "patient_section_details" bean
   * @param parameter the @FormParameter
   * @param errorMap the error map
   * @return the map
   */
  public abstract Map<String, Object> saveSection(Map<String, Object> requestBody,
      BasicDynaBean sdbean, FormParameter parameter, Map<String, Object> errorMap);

  /**
   * Delete's the section records (details).
   *
   * @param sectiondetailId the "patient_section_details" id
   * @param parameter the @FormParameter
   * @param errorMap the error map
   * @return the boolean
   */
  public abstract Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap);

  /**
   * Processes template data.
   *
   * @param parameter the @FormParameter
   * @param templateData the template data
   * @param responseData the response data
   * @param formId the form id
   */
  @SuppressWarnings("unchecked")
  public void processTemplateData(FormParameter parameter, Map<String, Object> templateData,
      Map<String, Object> responseData, Integer formId) {
    //TODO As Documents & Forms Section is readOnly temporarily adding condition to skip
    // Need to check for solution
    if ((responseData.get("records") == null || templateData != null
        && responseData.get("section_id") != null && (int) responseData.get("section_id") != -20
        && ((List<Map<String, Object>>) responseData.get("records")).isEmpty())) {
      responseData.put("records", (templateData != null && !templateData.isEmpty()) 
          ? templateData.get("records") : Collections.EMPTY_LIST );
      responseData.put("isTemplateRecords", true);
    }
  }

  /**
   * Gets the section details from last saved form (Carry forward data based on section level).
   *
   * @param parameter the @FormParameter
   * @return the section records (details)
   */
  public abstract Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter);

  /**
   * Gets the section details from current form.
   *
   * @param parameter the parameter
   * @return the section records (details)
   */
  public abstract Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter);

}
