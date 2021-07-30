package com.insta.hms.common;

import com.fasterxml.jackson.databind.ser.FilterProvider;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class JsonView. This class is to override methods from MappingJackson2JsonView so that we can
 * choose what to serialize and what not to serialize
 * 
 * @author aditya
 * 
 */
public class JsonView extends MappingJackson2JsonView {

  /** The extract value from single key model. */
  private boolean extractValueFromSingleKeyModel = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.web.servlet.view.json.MappingJackson2JsonView#
   * filterModel(java.util.Map) added !entry.getKey().equals("regionsList") to the default
   * implementation
   */
  @Override
  protected Object filterModel(Map<String, Object> model) {
    Map<String, Object> result = new HashMap<String, Object>(model.size());
    Set<String> modelKeys = (!CollectionUtils.isEmpty(this.getModelKeys()) ? this.getModelKeys()
        : model.keySet());

    for (Map.Entry<String, Object> entry : model.entrySet()) {
      if (!(entry.getValue() instanceof BindingResult) && modelKeys.contains(entry.getKey())
          && !entry.getKey().equals(JsonView.class.getName())
          && !entry.getKey().equals(FilterProvider.class.getName())
          && !entry.getKey().equals("referenceData")) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return (this.extractValueFromSingleKeyModel && result.size() == 1
        ? result.values().iterator().next()
        : result);
  }
}
