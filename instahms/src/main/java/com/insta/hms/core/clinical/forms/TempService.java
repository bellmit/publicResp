package com.insta.hms.core.clinical.forms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.insta.hms.common.JsonStringToMap;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TempService.
 *
 * @author krishnat
 */
@Service
public class TempService {

  /** The temp repo. */
  @LazyAutowired
  TempRepository tempRepo;

  /** The json string to map. */
  @LazyAutowired
  JsonStringToMap jsonStringToMap;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;


  /**
   * Save section.
   *
   * @param params the params
   * @param formbean the formbean
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  public String saveSection(Map<String, Object> params, BasicDynaBean formbean)
      throws JsonProcessingException {
    int sectionId = 0;
    int displayOrder = 0;
    String username = (String) sessionService.getSessionAttributes().get("userId");
    String autoSaveSectionId = null;

    for (Map.Entry<String, Object> map : params.entrySet()) {

      if (map.getKey().equals("section_id")) {
        sectionId = (Integer) map.getValue();
      }
      if (map.getKey().equals("display_order")) {
        displayOrder = (Integer) map.getValue();
      }
      if (map.getKey().equals("auto_save_section_id")) {
        autoSaveSectionId = (String) map.getValue();
      }
    }

    if (sectionId != 0) {
      String formFieldName = (String) formbean.get("form_field_name");

      BasicDynaBean bean = tempRepo.getBean();
      bean.set("mr_no", formbean.get("mr_no"));
      bean.set("patient_id", formbean.get("patient_id"));
      bean.set("section_item_id", formbean.get(formFieldName));
      bean.set("item_type", formbean.get("item_type"));
      bean.set("form_type", formbean.get("form_type"));
      bean.set("user_name", username);
      bean.set("section_id", sectionId);

      Map<String, Object> data = new HashMap<>(params);
      data.remove("sections");
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String json = ow.writeValueAsString(params);

      bean.set("json_data", json);


      if (autoSaveSectionId == null || autoSaveSectionId.equals("")) {
        autoSaveSectionId = username + "_" + sectionId + "_" + new java.util.Date().getTime();
        bean.set("auto_save_section_id", autoSaveSectionId);
        tempRepo.insert(bean);

      } else {
        Map<String, Object> keys = new HashMap<>();
        keys.put("auto_save_section_id", autoSaveSectionId);
        tempRepo.update(bean, keys);
      }
      return autoSaveSectionId;
    }
    return null;
  }


  /**
   * Save section.
   *
   * @param params the params
   * @param parameters the parameters
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  public String saveSection(Map<String, Object> params, FormParameter parameters)
      throws JsonProcessingException {
    int sectionId = 0;
    int displayOrder = 0;
    String username = (String) sessionService.getSessionAttributes().get("userId");
    String autoSaveSectionId = null;

    for (Map.Entry<String, Object> map : params.entrySet()) {

      if (map.getKey().equals("section_id")) {
        sectionId = (Integer) map.getValue();
      }
      if (map.getKey().equals("display_order")) {
        displayOrder = (Integer) map.getValue();
      }
      if (map.getKey().equals("auto_save_section_id")) {
        autoSaveSectionId = (String) map.getValue();
      }
    }

    if (sectionId != 0) {
      BasicDynaBean bean = tempRepo.getBean();
      bean.set("mr_no", parameters.getMrNo());
      bean.set("patient_id", parameters.getPatientId());
      bean.set(parameters.getFormFieldName(), parameters.getId());
      bean.set("item_type", parameters.getItemType());
      bean.set("form_type", parameters.getFormType());
      bean.set("user_name", username);
      bean.set("section_id", sectionId);

      Map<String, Object> data = new HashMap<>(params);
      data.remove("sections");
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String json = ow.writeValueAsString(params);

      bean.set("json_data", json);


      if (autoSaveSectionId == null || autoSaveSectionId.equals("")) {
        autoSaveSectionId = username + "_" + sectionId + "_" + new java.util.Date().getTime();
        bean.set("auto_save_section_id", autoSaveSectionId);
        tempRepo.insert(bean);

      } else {
        Map<String, Object> keys = new HashMap<>();
        keys.put("auto_save_section_id", autoSaveSectionId);
        tempRepo.update(bean, keys);
      }
      return autoSaveSectionId;
    }
    return null;
  }

  /**
   * Gets the sections.
   *
   * @param formbean the formbean
   * @return the sections
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getSections(BasicDynaBean formbean) {
    List<BasicDynaBean> list =
        tempRepo
            .getSections(formbean, (String) sessionService.getSessionAttributes().get("userId"));
    List<Map<String, Object>> convertedList = new ArrayList<>();

    for (BasicDynaBean bean : list) {
      Map<String, Object> map = new HashMap<>(bean.getMap());
      map.put("data", jsonStringToMap.convert((String) bean.get("json_data")));
      convertedList.add(map);
    }
    return convertedList;
  }

  /**
   * Gets the sections.
   *
   * @param parameters the parameters
   * @return the sections
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getSections(FormParameter parameters) {
    List<BasicDynaBean> list =
        tempRepo.getSections(parameters,
            (String) sessionService.getSessionAttributes().get("userId"));
    List<Map<String, Object>> convertedList = new ArrayList<>();

    for (BasicDynaBean bean : list) {
      Map<String, Object> map = new HashMap<>(bean.getMap());
      map.put("data", jsonStringToMap.convert((String) bean.get("json_data")));
      convertedList.add(map);
    }
    return convertedList;
  }

  /**
   * Delete sections.
   *
   * @param parameters the parameters
   * @return the integer
   */
  public Integer deleteSections(FormParameter parameters) {
    Map<String, Object> deleteKeysMap = new HashMap<>();
    deleteKeysMap.put(parameters.getFormFieldName(), parameters.getId());
    deleteKeysMap.put("form_type", parameters.getFormType());
    deleteKeysMap.put("user_name", (String) sessionService.getSessionAttributes().get("userId"));
    return tempRepo.delete(deleteKeysMap);
  }

  /**
   * Delete sections.
   *
   * @param formbean the formbean
   * @return the integer
   */
  public Integer deleteSections(BasicDynaBean formbean) {
    Map<String, Object> deleteKeysMap = new HashMap<>();
    String formFieldName = (String) formbean.get("form_field_name");
    deleteKeysMap.put("section_item_id", formbean.get(formFieldName));
    deleteKeysMap.put("form_type", formbean.get("form_type"));
    deleteKeysMap.put("user_name", (String) sessionService.getSessionAttributes().get("userId"));
    return tempRepo.delete(deleteKeysMap);
  }
}
