package com.insta.hms.mdm.sections;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.section.fields.FieldsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SectionsService.
 */
@Service
public class SectionsService extends MasterService {

  /** The fields service. */
  @LazyAutowired
  private FieldsService fieldsService;

  /**
   * Instantiates a new sections service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   */
  public SectionsService(SectionsRepository repo, SectionsValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the sections list.
   *
   * @param formType
   *          the form type
   * @return the sections list
   */
  public List<BasicDynaBean> getSectionsList(String formType) {
    return ((SectionsRepository) getRepository()).getSectionsList(formType);
  }

  /**
   * Gets the added section master details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param formId
   *          the form id
   * @param itemType
   *          the item type
   * @return the added section master details
   */
  public List<BasicDynaBean> getAddedSectionMasterDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return ((SectionsRepository) getRepository()).getAddedSectionMasterDetails(mrNo, patientId,
        itemId, genericFormId, formId, itemType);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }

  /**
   * Gets the section.
   *
   * @param sectionId
   *          the section id
   * @return the section
   */
  public List<BasicDynaBean> getSection(int sectionId) {
    return ((SectionsRepository) getRepository()).getSectionDefinition(sectionId);
  }

  /**
   * Gets the section definition.
   *
   * @param sectionId
   *          the section id
   * @return the section definition
   */
  public Map getSectionDefinition(int sectionId) {
    List<Object> structure = new ArrayList<Object>();
    structure.add("section_id");
    List<Object> fieldsList = new ArrayList<Object>();

    fieldsList.add("field_id");
    fieldsList.add("field_name");
    fieldsList.add("field_display_order");
    fieldsList.add("field_type");
    fieldsList.add("allow_others");
    fieldsList.add("allow_normal");
    fieldsList.add("normal_text");
    fieldsList.add("no_of_lines");
    fieldsList.add("markers");
    fieldsList.add("is_mandatory");
    fieldsList.add("field_phrase_category_id");
    fieldsList.add("field_pattern_id");
    fieldsList.add("default_to_current_datetime");
    fieldsList.add("date");
    fieldsList.add("date_time");
    fieldsList.add("image_id");
    fieldsList.add("field_remarks");
    fieldsList.add("field_detail_id");

    List<Object> optionsList = new ArrayList<Object>();
    optionsList.add("option_id");
    optionsList.add("option_remarks");
    optionsList.add("option_display_order");
    optionsList.add("option_value");
    optionsList.add("option_phrase_category_id");
    optionsList.add("option_pattern_id");
    optionsList.add("available");
    optionsList.add("option_detail_id");
    Map<String, List<Object>> optionsMap = new LinkedHashMap<String, List<Object>>();
    optionsMap.put("options", optionsList);

    fieldsList.add(optionsMap);

    List markerDetailsList = new ArrayList();
    markerDetailsList.add("coordinate_x");
    markerDetailsList.add("coordinate_y");
    markerDetailsList.add("marker_id");
    markerDetailsList.add("notes");
    markerDetailsList.add("marker_detail_id");
    Map markerDetails = new LinkedHashMap();
    markerDetails.put("marker_details", markerDetailsList);
    fieldsList.add(markerDetails);

    Map<String, List<Object>> fieldsMap = new LinkedHashMap<String, List<Object>>();
    fieldsMap.put("fields", fieldsList);
    structure.add(fieldsMap);

    return ConversionUtils.convertToStructeredMap(getSection(sectionId), structure, "field_id");
  }

  /**
   * Gets the reference lists.
   *
   * @param params
   *          the params
   * @return the reference lists
   */
  public Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> response = new HashMap<>();
    if (params.get("section_id") != null) {
      Map<String, Object> filter = new HashMap<>();
      filter.put("section_id", Integer.parseInt(((String[]) params.get("section_id"))[0]));
      filter.put("is_mandatory", true);
      response.put("mandatory_fields", fieldsService.getFieldsbyFilter(filter));
    }
    return response;
  }

  /**
   * Gets the record.
   *
   * @param sectionId the section id
   * @return the record
   */
  public BasicDynaBean getRecord(int sectionId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("section_id", sectionId);
    return ((SectionsRepository) getRepository()).findByKey(params);
  }

}
