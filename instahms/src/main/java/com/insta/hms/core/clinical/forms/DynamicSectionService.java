package com.insta.hms.core.clinical.forms;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.regularexpression.RegularExpressionService;
import com.insta.hms.mdm.section.fields.FieldsService;
import com.insta.hms.mdm.sections.SectionsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DynamicSectionService.
 *
 * @author krishnat
 */
@Service
@Scope(value = "prototype")
public class DynamicSectionService extends SectionService {

  /** The sec mas service. */
  @LazyAutowired
  SectionsService secMasService;

  /** The sec val repo. */
  @LazyAutowired
  SectionValuesRepository secValRepo;

  /** The sec field options repo. */
  @LazyAutowired
  SectionFieldOptionsRepository secFieldOptionsRepo;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The sec image repo. */
  @LazyAutowired
  SectionImageRepository secImageRepo;

  /** The sec val validator. */
  @LazyAutowired
  SectionValuesValidator secValValidator;

  /** The sec field opt validator. */
  @LazyAutowired
  SectionFieldOptionsValidator secFieldOptValidator;

  /** The sec img validator. */
  @LazyAutowired
  SectionImageValidator secImgValidator;

  /** The obsvr service. */
  @LazyAutowired
  MRDObservationsService obsvrService;

  /** The field service. */
  @LazyAutowired
  FieldsService fieldService;

  /** The regular exp service. */
  @LazyAutowired
  RegularExpressionService regularExpService;

  /** The insta section image service. */
  @LazyAutowired
  InstaSectionImageService instaSectionImageService;

  /** The sd service. */
  @LazyAutowired
  SectionDetailsService sdService;

  /** The log. */
  Logger log = LoggerFactory.getLogger(DynamicSectionService.class);

  /** The Constant field_id_key. */
  // these keys are used multiple times, and hence declared as a constants.
  private static final String field_id_key = "field_id";

  /** The Constant section_id_key. */
  private static final String section_id_key = "section_id";

  /** The Constant image_id_key. */
  private static final String image_id_key = "image_id";

  /** The Constant options_key. */
  private static final String options_key = "options";

  /** The Constant marker_details_key. */
  private static final String marker_details_key = "marker_details";

  /** The Constant marker_id_key. */
  private static final String marker_id_key = "marker_id";

  /** The Constant available_key. */
  private static final String available_key = "available";

  /**
   * Instantiates a new dynamic section service.
   *
   * @param secId the sec id
   */
  public DynamicSectionService(Integer secId) {
    this.sectionId = secId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#saveSection(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean, java.util.Map,
   * com.insta.hms.core.clinical.forms.SectionParameter)
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    Integer sectionDetailId = (Integer) sdbean.get("section_detail_id");

    Map response = new LinkedHashMap();
    response.put(section_id_key, sectionId);
    List<Map> responseFieldsList = new ArrayList<>();
    response.put("fields", responseFieldsList);

    ValidationErrorMap errMap = new ValidationErrorMap();
    Map regExpPatterns =
        ConversionUtils.listBeanToMapBean(regularExpService.lookup(false), "pattern_id");
    List<BasicDynaBean> markersList = new ArrayList<>();
    List<BasicDynaBean> optionsList = new ArrayList<>();
    List<BasicDynaBean> insertFieldsList = new ArrayList<>();
    List<BasicDynaBean> updateFieldsList = new ArrayList<>();
    boolean errorsExists = false;
    Map<Integer, List<BasicDynaBean>> fieldDescList = new HashMap<>();
    List<Integer> imagesList = new ArrayList<>();
    Map<Integer, Integer> imagesMap = new HashMap<>();
    List<Map> fields = (List<Map>) requestBody.get("fields");
    Boolean forceSave = (Boolean) requestBody.get("force_save");
    forceSave = forceSave == null ? false : forceSave;
    boolean sectionHasData = false;
    if (fields != null && !fields.isEmpty()) {
      for (Map map : fields) {
        Map responseFields = new LinkedHashMap();
        responseFields.put(field_id_key, map.get(field_id_key));
        responseFieldsList.add(responseFields);

        Map fieldParams = new HashMap();
        fieldParams.put(field_id_key, map.get(field_id_key));
        List<BasicDynaBean> fieldBeans =
            fieldService.getFieldOptions((Integer) map.get(field_id_key));
        fieldDescList.put((Integer) map.get(field_id_key), fieldBeans);

        Map optionsFromMaster = ConversionUtils.listBeanToMapBean(fieldBeans, "option_id");
        String fieldType = (String) fieldBeans.get(0).get("field_type");
        Map fmap = new HashMap();

        BasicDynaBean vbean = secValRepo.getBean();
        vbean.set(field_id_key, map.get(field_id_key));
        vbean.set("section_detail_id", sectionDetailId);
        List conversionErrors = new ArrayList();
        if (fieldType.equals("datetime")) {
          String dateTime = (String) map.get("date_time");
          if (dateTime != null && !dateTime.trim().equals("") && dateTime.length() <= 11) {
            String[] dateTimeSplit = dateTime.split("\\s");
            String date =
                (dateTimeSplit != null && dateTimeSplit.length > 0) ? dateTimeSplit[0] : null;
            String time =
                (dateTimeSplit != null && dateTimeSplit.length > 1) ? dateTimeSplit[1] : null;
            String finalDateTime = null;
            if (date != null) {
              finalDateTime = date;
              time = (time == null ? "00:00" : time);
              finalDateTime = date + " " + time;
            }
            map.put("date_time", finalDateTime);
          }
        }
        ConversionUtils.copyJsonToDynaBean(map, vbean, conversionErrors, false);
        secValValidator.validate(vbean, errMap, regExpPatterns, fieldBeans.get(0));

        if (!errMap.getErrorMap().isEmpty() || !conversionErrors.isEmpty()) {
          if (errorMap.get("fields") == null) {
            errorMap.put("fields", new HashMap<String, Object>());
          }
          errorsExists = true;
          Map<String, Object> tmp = (Map<String, Object>) errorMap.get("fields");
          fmap.putAll((new com.insta.hms.exception.ValidationException(ValidationUtils
              .copyCoversionErrors(errMap, conversionErrors))).getErrors());
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
          tmp.put(((Integer) fields.indexOf(map)).toString(), fmap);
          continue;
        }

        Integer fieldDetailId = (Integer) vbean.get("field_detail_id");
        boolean fieldValueExists = false;
        if (fieldDetailId == null || fieldDetailId.intValue() == 0) {
          fieldDetailId = secValRepo.getNextSequence();
          insertFieldsList.add(vbean);
        } else {
          updateFieldsList.add(vbean);
        }

        Integer imageId = (Integer) vbean.get(image_id_key);
        if (fieldType.equals("image") && imageId != null && !imagesList.contains(imageId)
            && imageId != 0) {
          imagesList.add(imageId);
          imagesMap.put(imageId, (Integer) vbean.get(field_id_key));
        }
        vbean.set("mod_time", DateUtil.getCurrentTimestamp());
        responseFields.put("field_detail_id", fieldDetailId);
        vbean.set("field_detail_id", fieldDetailId);
        List responseOptionsList = new ArrayList();
        List responseMarkersList = new ArrayList();
        responseFields.put(options_key, responseOptionsList);
        responseFields.put(marker_details_key, responseMarkersList);

        if (fieldType.equals("image")) {
          List<Map> markers = (List<Map>) map.get(marker_details_key);
          Map markerErrors = new HashMap();
          Integer markerIndex = 0;
          for (Map markerMap : markers) {
            BasicDynaBean imgbean = secImageRepo.getBean();

            conversionErrors = new ArrayList();
            ConversionUtils.copyJsonToDynaBean(markerMap, imgbean, conversionErrors, false);
            Integer markerDetailId = (Integer) imgbean.get("marker_detail_id");
            if (markerDetailId == null || markerDetailId.intValue() == 0) {
              markerDetailId = secImageRepo.getNextSequence();
              imgbean.set("marker_detail_id", markerDetailId);
            }

            imgbean.set("field_detail_id", fieldDetailId);

            secImgValidator.validate(imgbean, errMap);

            if (!errMap.getErrorMap().isEmpty() || !conversionErrors.isEmpty()) {
              errorsExists = true;
              markerErrors.put(
                  (markerIndex).toString(),
                  (new com.insta.hms.exception.ValidationException(ValidationUtils
                      .copyCoversionErrors(errMap, conversionErrors))).getErrors());
              errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
              continue;
            }
            Map markersMap = new LinkedHashMap();
            markersMap.put("marker_detail_id", markerDetailId);
            markersMap.put(marker_id_key, imgbean.get(marker_id_key));
            responseMarkersList.add(markersMap);

            imgbean.set("user_name", sessionService.getSessionAttributes().get("userId"));
            imgbean.set("mod_time", DateUtil.getCurrentTimestamp());
            markersList.add(imgbean);

            markerIndex++;
          }
          if (!markerErrors.isEmpty()) {
            fmap.put(marker_details_key, markerErrors);
          }

        } else {

          List<Map> options = (List<Map>) map.get(options_key);

          Map optionErrors = new HashMap();
          for (Map optionmap : options) {

            BasicDynaBean obean = secFieldOptionsRepo.getBean();

            conversionErrors = new ArrayList();
            ConversionUtils.copyJsonToDynaBean(optionmap, obean, conversionErrors, false);
            Integer optionDetailId = (Integer) obean.get("option_detail_id");
            if (optionDetailId == null || optionDetailId.intValue() == 0) {
              optionDetailId = secFieldOptionsRepo.getNextSequence();
            }
            obean.set("field_detail_id", fieldDetailId);
            obean.set("option_detail_id", optionDetailId);

            Integer optionId = (Integer) obean.get("option_id");
            BasicDynaBean optionbean = (BasicDynaBean) optionsFromMaster.get(optionId);
            secFieldOptValidator.validate(obean, errMap, regExpPatterns, optionbean);

            if (optionId != null) {
              // for dropdown, remarks allowed to saved only for 'others' option.
              if (fieldType.equals("dropdown") && optionId != -1
                  && obean.get("option_remarks") != null
                  && !obean.get("option_remarks").equals("")) {
                obean.set("option_remarks", "");
              }

              if (optionId >= -1) {
                fieldValueExists = true;
              }
            }
            Map optionsMap = new LinkedHashMap();
            optionsMap.put("option_detail_id", optionDetailId);
            optionsMap.put("option_id", obean.get("option_id"));
            responseOptionsList.add(optionsMap);

            obean.set("user_name", sessionService.getSessionAttributes().get("userId"));
            obean.set("mod_time", DateUtil.getCurrentTimestamp());

            if (!errMap.getErrorMap().isEmpty() || !conversionErrors.isEmpty()) {
              errorsExists = true;
              optionErrors.put(
                  ((Integer) options.indexOf(optionsMap)).toString(),
                  new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrors)).getErrors());
              errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
              continue;
            }
            if (obean.get(available_key) != null) {
              optionsList.add(obean);
            }
          }

          if (!optionErrors.isEmpty()) {
            fmap.put(options_key, optionErrors);
          }

        }
        if (fieldType.equals("text") || fieldType.equals("wide text")) {
          if (vbean.get("field_remarks") != null && !vbean.get("field_remarks").equals("")) {
            fieldValueExists = true;
          }
        } else if (fieldType.equals("date")) {
          if (vbean.get("date") != null) {
            fieldValueExists = true;
          }
        } else if (fieldType.equals("datetime")) {
          if (vbean.get("date_time") != null) {
            fieldValueExists = true;
          }
        }
        if (!sectionHasData) {
          sectionHasData = fieldValueExists;
        }
        Boolean fieldMandatory = (Boolean) fieldBeans.get(0).get("is_mandatory");
        if (fieldMandatory && !fieldValueExists && !forceSave) {
          errMap.addError("field_name", "exception.instasection.field.mandatory");
        }
        if (!errMap.getErrorMap().isEmpty()) {
          errorsExists = true;
          fmap.putAll(new ValidationException(errMap).getErrors());
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        }
        if (!fmap.isEmpty()) {
          if (errorMap.get("fields") == null) {
            errorMap.put("fields", new HashMap<String, Object>());
          }
          Map<String, Object> tmp = (Map<String, Object>) errorMap.get("fields");
          tmp.put(((Integer) fields.indexOf(map)).toString(), fmap);
        }
      }
    }

    if (!sectionHasData && !forceSave) {
      Map sectionkeys = new HashMap();
      sectionkeys.put(section_id_key, sectionId);

      BasicDynaBean sectionBean = secMasService.findByPk(sectionkeys);
      if ((Boolean) sectionBean.get("section_mandatory")) {
        errMap.addError("others", "exception.instasection.mandatory");
        errorMap.putAll(new ValidationException(errMap).getErrors());
      }
    }
    if (errorMap.isEmpty()
        && (!optionsList.isEmpty() || !markersList.isEmpty() || !insertFieldsList.isEmpty()
            || !updateFieldsList.isEmpty())) {

      if (!errorsExists) {
        secImageRepo.deleteMarkers(sectionDetailId);
        secFieldOptionsRepo.markNotAvailable(optionsList, sectionDetailId);

        if (!insertFieldsList.isEmpty()) {
          secValRepo.batchInsert(insertFieldsList);
        }

        Map<String, Object> updateFieldsKeyMap = new HashMap();
        List<Integer> fieldkeys = new ArrayList<>();
        for (BasicDynaBean b : updateFieldsList) {
          fieldkeys.add((Integer) b.get("field_detail_id"));
        }
        updateFieldsKeyMap.put("field_detail_id", fieldkeys);
        secValRepo.batchUpdate(updateFieldsList, updateFieldsKeyMap);

        Map<String, Object> updateOptionsKeyMap = new HashMap<>();
        List<Integer> optionKeys = new ArrayList<>();
        List<Integer> optionFieldKeys = new ArrayList<>();
        for (BasicDynaBean obean : optionsList) {
          optionKeys.add((Integer) obean.get("option_id"));
          optionFieldKeys.add((Integer) obean.get("field_detail_id"));
        }
        updateOptionsKeyMap.put("option_id", optionKeys);
        updateOptionsKeyMap.put("field_detail_id", optionFieldKeys);
        int[] batchUpdateSuccess =
            secFieldOptionsRepo.batchUpdate(optionsList, updateOptionsKeyMap);
        List<BasicDynaBean> insertOptionsList = new ArrayList<>();
        int batchUpdateSuccessL = batchUpdateSuccess.length;
        for (int index = 0; index < batchUpdateSuccessL; index++) {
          if (batchUpdateSuccess[index] == 0) {
            insertOptionsList.add(optionsList.get(index));
          }
        }
        secFieldOptionsRepo.batchInsert(insertOptionsList);

        if (!markersList.isEmpty()) {
          secImageRepo.batchInsert(markersList);
        }

        if (parameter.getItemType().equals("CONS")) {
          insertFieldsList.addAll(updateFieldsList);
          obsvrService.saveObservations(insertFieldsList, optionsList, fieldDescList,
              (int) parameter.getId(), fields);
        }
        List<BasicDynaBean> savedImages = instaSectionImageService.getImages(imagesList);
        for (Map.Entry<Integer, Integer> entry : imagesMap.entrySet()) {
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
          boolean imageDeleted = true;
          for (BasicDynaBean imageBean : savedImages) {
            if (imageBean.get(image_id_key).equals(entry.getKey())) {
              imageDeleted = false;
            }
          }
          if (imageDeleted) {
            errMap.addError(image_id_key, "exception.instasection.image.deleted");
            Map fieldErrorMap = ((Map) errorMap.get(entry.getValue().toString()));
            if (fieldErrorMap == null) {
              fieldErrorMap = new HashMap<String, Object>();
              fieldErrorMap.put(entry.getValue().toString(),
                  new ValidationException(errMap).getErrors());
              errorMap.put(entry.getValue().toString(), fieldErrorMap);
            } else {
              fieldErrorMap.putAll(new ValidationException(errMap).getErrors());
            }
          }
        }
      } else {
        return response;
      }
    }

    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromCurrentForm(com.insta
   * .hms.core.clinical.forms.FormParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {

    Map<String, Object> responce =
        ConversionUtils.convertToStructuredMap(secValRepo.getSectionValues(parameter, sectionId),
            getStructure(), "section_detail_id", field_id_key);

    // Handling the empty sections
    if (responce.get("records") == null) {
      List<Map<String, Object>> records = new ArrayList<>();
      List<BasicDynaBean> sections = sdService.getAllRecords(parameter, this.sectionId);
      for (BasicDynaBean section : sections) {
        Map<String, Object> record = new HashMap<>();
        record.put("section_detail_id", section.get("section_detail_id"));
        record.put("finalized", section.get("finalized"));
        record.put("mod_time", section.get("mod_time"));
        record.put("revision_number", section.get("revision_number"));
        record.put("user_name", section.get("user_name"));
        record.put("fields", new ArrayList<>());
        records.add(record);
      }
      responce.put("records", records);
    }
    return responce;
  }

  /**
   * Gets the section.
   *
   * @param values the values
   * @return the section
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Object> getSection(List<BasicDynaBean> values) {
    Map map = secMasService.getSectionDefinition(sectionId);
    List<Map> fieldListMap = (List<Map>) map.get("fields");
    Integer sectionDetailId = null;
    List records = new ArrayList();
    Map section = new LinkedHashMap();
    section.put(section_id_key, map.get(section_id_key));
    section.put("records", records);
    if (values.isEmpty()) {
      section = new LinkedHashMap();
      section.put("section_detail_id", 0);
      section.put("finalized", "N");
      section.put("fields", fieldListMap);
      records.add(section);

      return section;
    }
    for (BasicDynaBean bean : values) {
      if (sectionDetailId == null
          || !sectionDetailId.equals((Integer) bean.get("section_detail_id"))) {
        sectionDetailId = (Integer) bean.get("section_detail_id");
        section = new LinkedHashMap();
        section.put("section_detail_id", sectionDetailId);
        section.put("finalized", bean.get("finalized"));
        section.put("fields", fieldListMap);
        records.add(section);
      }
      Integer fieldId = (Integer) bean.get(field_id_key);
      for (Map field : fieldListMap) {
        if (((Integer) field.get(field_id_key)).equals(fieldId)) {
          field.put("field_remarks", field.get("field_remarks"));
          field.put("date", field.get("date"));
          field.put("date_time", field.get("date_time"));
          field.put(image_id_key, field.get(image_id_key));
          field.put("field_detail_id", field.get("field_detail_id"));
          List<Map> options = (List<Map>) field.get(options_key);
          Integer optionId = (Integer) bean.get("option_id");
          if (optionId != null) {
            for (Map option : options) {
              if (((Integer) option.get("option_id")).equals(optionId)) {
                option.put("option_detail_id", bean.get("option_detail_id"));
                option.put("option_remarks", bean.get("option_remarks"));
                option.put(available_key, bean.get(available_key));
              }
            }
          }
          List markers = (List) field.get(marker_details_key);
          Integer markerId = (Integer) bean.get(marker_id_key);
          if (markerId != null && markerId.intValue() != 0) {
            Map markerMap = new LinkedHashMap();
            markerMap.put(marker_id_key, markerId);
            markerMap.put("marker_detail_id", bean.get("marker_detail_id"));
            markerMap.put("notes", bean.get("notes"));
            markerMap.put("coordinate_x", bean.get("coordinate_x"));
            markerMap.put("coordinate_y", bean.get("coordinate_y"));
            markers.add(markerMap);
          }
        }
      }
    }
    return section;
  }

  /**
   * Gets the structure.
   *
   * @return the structure
   */
  private List getStructure() {
    List<Object> structure = new ArrayList<Object>();
    structure.add(section_id_key);
    List<Object> secDetList = new ArrayList<Object>();
    secDetList.add("section_detail_id");
    secDetList.add("finalized");
    secDetList.add("revision_number");
    secDetList.add("user_name");
    secDetList.add("mod_time");
    structure.add(secDetList);
    List<Object> fieldsList = new ArrayList<Object>();

    fieldsList.add(field_id_key);
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
    fieldsList.add(available_key);
    fieldsList.add("date");
    fieldsList.add("date_time");
    fieldsList.add(image_id_key);
    fieldsList.add("field_remarks");
    fieldsList.add("field_detail_id");

    List<Object> optionsList = new ArrayList<Object>();
    optionsList.add("option_id");
    optionsList.add("option_remarks");
    optionsList.add("option_display_order");
    optionsList.add("option_value");
    optionsList.add("value_code");
    optionsList.add("option_phrase_category_id");
    optionsList.add("option_pattern_id");
    optionsList.add(available_key);
    optionsList.add("option_detail_id");
    Map<String, List<Object>> optionsMap = new LinkedHashMap<String, List<Object>>();
    optionsMap.put(options_key, optionsList);

    fieldsList.add(optionsMap);

    List markerDetailsList = new ArrayList();
    markerDetailsList.add("coordinate_x");
    markerDetailsList.add("coordinate_y");
    markerDetailsList.add(marker_id_key);
    markerDetailsList.add("notes");
    markerDetailsList.add("marker_detail_id");
    Map markerDetails = new LinkedHashMap();
    markerDetails.put(marker_details_key, markerDetailsList);
    fieldsList.add(markerDetails);
    
    Map<String, List<Object>> fieldsMap = new LinkedHashMap<String, List<Object>>();
    fieldsMap.put("fields", fieldsList);
    secDetList.add(fieldsMap);
    return structure;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromLastSavedForm(com.insta
   * .hms.core.clinical.forms.FormParameter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map params = new HashMap();
    params.put(section_id_key, sectionId);

    BasicDynaBean sectionBean = secMasService.findByPk(params);
    List<BasicDynaBean> list = null;
    if (sectionBean.get("linked_to").equals("patient")) {
      list = secValRepo.getPatientLevelSectionValues(parameter, sectionId);
    } else if (sectionBean.get("linked_to").equals("visit")) {
      list = secValRepo.getVisitLevelSectionValues(parameter, sectionId);
    } else if (sectionBean.get("linked_to").equals("order item")) {
      list = secValRepo.getOrderItemLevelSectionValues(parameter, sectionId);
    } else {
      list = secValRepo.getSectionValues(parameter, sectionId);
    }

    Map<String, Object> response = ConversionUtils.convertToStructuredMap(list, getStructure(),
        "section_detail_id", field_id_key);
    if (response.get("records") != null) {
      Integer recordsLength = ((List) response.get("records")).size();

      // To filter duplicate records of section where allow duplicate is false
      if (!(Boolean) sectionBean.get("allow_duplicate") && recordsLength > 1) {
        List<Map<String, Object>> previousSectionValuesList = (List) response.get("records");
        Map<String, Object> lastSavedSectionData = new HashMap<String, Object>();
        int maxSectionDetailId = 0;
        for (int i = 0; i < previousSectionValuesList.size(); i++) {
          if ((Integer) previousSectionValuesList.get(i).get(
              "section_detail_id") > maxSectionDetailId) {
            maxSectionDetailId = (Integer) previousSectionValuesList.get(i).get(
                "section_detail_id");
            lastSavedSectionData.clear();
            lastSavedSectionData.putAll(previousSectionValuesList.get(i));
          }
        }
        response.put("records", Arrays.asList(lastSavedSectionData));
        recordsLength = 1;
      }
      
      for (int i = 0; i < recordsLength; i++) {
        ((Map) ((List) response.get("records")).get(i)).put("section_detail_id", 0);
        ((Map) ((List) response.get("records")).get(i)).put("revision_number", null);
      }
    } else {
      // handling empty sections
      List<Integer> sectionIds = new ArrayList<>();
      sectionIds.add(this.sectionId);
      List<BasicDynaBean> carryFordwardSection =
          sdService.getCarryForwardSectionsBySectionIds(parameter, sectionIds);
      List<Map<String, Object>> records = new ArrayList<>();
      Long noOfSections = 1L;
      if (!carryFordwardSection.isEmpty()) {
        noOfSections = (Long) carryFordwardSection.get(0).get("count");
      }
      for (int i = 0; i < noOfSections; i++) {
        Map<String, Object> record = new HashMap<>();
        record.put("section_detail_id", 0);
        record.put("revision_number", null);
        record.put("finalized", "N");
        record.put("fields", new ArrayList<>());
        records.add(record);
      }
      response.put("records", records);
    }
    return response;
  }

  /**
   * Gets the triage section data.
   *
   * @param sectionItemId the section item id
   * @return the triage section data
   */
  public Map<String, Object> getTriageSectionData(Integer sectionItemId) {
    List<Object> structure = new ArrayList<>();
    List<Object> secDetList = new ArrayList<>();
    secDetList.add("section_id");
    secDetList.add("section_detail_id");
    structure.add(secDetList);

    List<Object> fieldsList = new ArrayList<>();
    fieldsList.add("field_id");
    fieldsList.add("field_name");
    fieldsList.add("field_type");
    fieldsList.add("field_remarks");
    fieldsList.add("date");
    fieldsList.add("date_time");
    fieldsList.add("image_id");
    fieldsList.add("date");
    fieldsList.add("date_time");
    Map<String, List<Object>> fieldsMap = new LinkedHashMap<>();
    fieldsMap.put("fields", fieldsList);
    secDetList.add(fieldsMap);

    Map<String, List<Object>> optionsMap = new LinkedHashMap<>();
    List<Object> optionsList = new ArrayList<>();
    optionsList.add("option_value");
    optionsList.add("option_remarks");
    optionsMap.put("options", optionsList);
    fieldsList.add(optionsMap);

    List<Object> markerDataList = new ArrayList<>();
    markerDataList.add("coordinate_x");
    markerDataList.add("coordinate_y");
    markerDataList.add("marker_id");
    markerDataList.add("notes");
    Map<String, List<Object>> markerData = new LinkedHashMap<>();
    markerData.put("marker_details", markerDataList);
    fieldsList.add(markerData);

    return ConversionUtils.convertToStructuredMap(secValRepo.getTriageSectionData(sectionItemId),
        structure, "section_detail_id", "field_id");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#deleteSection(java.lang.Integer,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    // TODO Auto-generated method stub
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#processTemplateData
   * (com.insta.hms.core.clinical
   * .forms.FormParameter, java.util.Map, java.util.Map, java.lang.Integer)
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void processTemplateData(FormParameter parameter, Map<String, Object> templateData,
      Map<String, Object> responseData, Integer formId) {
    Integer sectionDetailId =
        (Integer) ((List<Map<String, Object>>) responseData.get("records")).get(0).get(
            "section_detail_id");
    boolean dataStatus = sdService.getSectionDataStatus(parameter, sectionDetailId, sectionId);
    if (!dataStatus && templateData != null) {
      Map<String, Object> templateFieldDataMap =
          ConversionUtils.listMapToMapMap(
              (List<Map>) ((Map) ((List) templateData.get("records")).get(0)).get("fields"),
              "field_id");
      List<Map<String, Object>> fields =
          (List<Map<String, Object>>) ((List<Map<String, Object>>) responseData.get("records"))
              .get(0).get("fields");
      Integer fieldsL = fields.size();
      for (int i = 0; i < fieldsL; i++) {
        Map<String, Object> field = fields.get(i);
        Map<String, Object> templateFieldData =
            (Map<String, Object>) templateFieldDataMap.get(field.get("field_id").toString());
        if (templateFieldData != null) {
          field.put("date", templateFieldData.get("date"));
          field.put("date_time", templateFieldData.get("date_time"));
          field.put("image_id", templateFieldData.get("image_id"));
          field.put("field_remarks", templateFieldData.get("field_remarks"));
          field.put("marker_details", templateFieldData.get("marker_details"));

          Map<String, Object> templateOptionsDataMap =
              ConversionUtils.listMapToMapMap((List<Map>) templateFieldData.get("options"),
                  "option_id");
          List<Map<String, Object>> options = (List<Map<String, Object>>) field.get("options");
          Integer optionsL = options.size();
          if (!templateOptionsDataMap.isEmpty()) {
            for (int j = 0; j < optionsL; j++) {
              Map<String, Object> option = options.get(j);
              Map<String, Object> templateOptionData =
                  (Map<String, Object>) templateOptionsDataMap.get(option.get("option_id")
                      .toString());
              if (templateOptionData != null && "Y".equals(templateOptionData.get("available"))) {
                option.put("option_remarks", templateOptionData.get("option_remarks"));
                option.put("available", "Y");
              }
            }
          }
        }
      }
      responseData.put("isTemplateRecords", true);
    }
  }

  /**
   * Gets the cons insta section field values.
   *
   * @param consId the cons id
   * @return the cons insta section field values
   */
  public String getConsInstaSectionFieldValues(Integer consId) {
    return secValRepo.getConsInstaSectionFieldValues(consId);
  }

}
