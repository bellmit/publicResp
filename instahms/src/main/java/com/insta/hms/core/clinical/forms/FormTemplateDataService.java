package com.insta.hms.core.clinical.forms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.JsonStringToMap;
import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FormTemplateDataService.
 *
 * @author teja
 */
@Service
public class FormTemplateDataService extends BusinessService {

  /** The form data repository. */
  @LazyAutowired
  private FormTemplateDataRepository formDataRepository;

  /** The json string to map. */
  @LazyAutowired
  private JsonStringToMap jsonStringToMap;

  /** The form vital parameters repository. */
  @LazyAutowired
  private FormVitalParametersRepository formVitalParametersRepository;

  /**
   * Insert.
   *
   * @param data the data
   * @param formId the form id
   * @return true, if successful
   * @throws JsonProcessingException the json processing exception
   */
  public boolean insert(Map<String, Object> data, Integer formId) throws JsonProcessingException {
    BasicDynaBean bean = formDataRepository.getBean();
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = ow.writeValueAsString(data);
    bean.set("data", json);
    bean.set("form_id", formId);
    bean.set("section_id", data.get("section_id"));
    return formDataRepository.insert(bean) == 1;
  }

  /**
   * Gets the data.
   *
   * @param formId the form id
   * @param sectionId the section id
   * @return the data
   */
  public Map<String, Object> getdata(Integer formId, Integer sectionId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("form_id", formId);
    filterMap.put("section_id", sectionId);
    BasicDynaBean bean = formDataRepository.findByKey(filterMap);
    if (bean == null) {
      return null;
    }
    return jsonStringToMap.convert((String) bean.get("data"));
  }

  /**
   * Save template vitals.
   *
   * @param vitalParamIds the vital param ids
   * @param formId the form id
   * @return true, if successful
   */
  public boolean saveTemplateVitals(List<Integer> vitalParamIds, Integer formId) {
    List<BasicDynaBean> beans = new ArrayList<>();
    for (int paramId : vitalParamIds) {
      BasicDynaBean bean = formVitalParametersRepository.getBean();
      bean.set("vital_param_id", paramId);
      bean.set("form_id", formId);
      beans.add(bean);
    }
    return formVitalParametersRepository.isBatchSuccess(formVitalParametersRepository
        .batchInsert(beans));
  }

  /**
   * Gets the template vitals.
   *
   * @param formId the form id
   * @return the template vitals
   */
  public List<Integer> getTemplateVitals(Integer formId) {
    List<BasicDynaBean> vitalsBeans =
        formVitalParametersRepository.listAll(null, "form_id", formId);
    List<Integer> vitalIds = new ArrayList<>();
    if (vitalsBeans != null) {
      for (BasicDynaBean bean : vitalsBeans) {
        vitalIds.add((Integer) bean.get("vital_param_id"));
      }
    }
    return vitalIds;
  }

}
