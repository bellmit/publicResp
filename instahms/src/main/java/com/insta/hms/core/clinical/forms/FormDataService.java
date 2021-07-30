package com.insta.hms.core.clinical.forms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.JsonStringToMap;
import com.insta.hms.common.annotations.LazyAutowired;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The Class FormDataService.
 */
@Service
public class FormDataService extends BusinessService {

  /** The form data repository. */
  @LazyAutowired
  private FormDataRepository formDataRepository;

  /** The json string to map. */
  @LazyAutowired
  private JsonStringToMap jsonStringToMap;

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
    bean.set("form_data", json);
    bean.set("form_id", formId);
    return formDataRepository.insert(bean) == 1;
  }

  /**
   * Gets the data.
   *
   * @param formId the form id
   * @return the data
   */
  public Map<String, Object> getdata(Integer formId) {
    BasicDynaBean bean = formDataRepository.findByKey("form_id", formId);
    if (bean == null) {
      return null;
    }
    return jsonStringToMap.convert((String) bean.get("form_data"));
  }

}
