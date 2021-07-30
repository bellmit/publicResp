package com.insta.hms.common.preferences.userpreferences;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.insta.hms.common.JsonStringToMap;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.PreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class UserPreferencesService.
 */
@Service("userPreferencesService")
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class UserPreferencesService extends PreferencesService {

  /** The user preferences repository. */
  @LazyAutowired
  private UserPreferencesRepository userPreferencesRepository;

  /** The json string to map. */
  @LazyAutowired
  private JsonStringToMap jsonStringToMap;

  /**
   * Instantiates a new user preferences service.
   *
   * @param userPreferencesRepository the user preferences repository
   * @param userPreferencesValidator the user preferences validator
   */
  public UserPreferencesService(UserPreferencesRepository userPreferencesRepository,
      UserPreferencesValidator userPreferencesValidator) {
    super(userPreferencesRepository, userPreferencesValidator);
    this.userPreferencesRepository = userPreferencesRepository;
  }

  /**
   * Insert pref.
   *
   * @param paramsMap the params map
   * @return the map
   */
  public Map insertPref(Map paramsMap) {

    Set<String> moduleIds = paramsMap.keySet();
    Map resultMap = new HashMap();

    for (String module : moduleIds) {
      Map prefs = (Map) paramsMap.get(module);
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String json = "";
      try {
        json = ow.writeValueAsString(prefs);
      } catch (JsonProcessingException exception) {
        exception.printStackTrace();
      }
      Map filterMap = new HashMap();
      filterMap.put("username", RequestContext.getUserName());
      filterMap.put("module_id", module);

      BasicDynaBean prefBean = userPreferencesRepository.findByKey(filterMap);
      if (prefBean == null) {
        // insert
        prefBean = userPreferencesRepository.getBean();
        prefBean.set("applicable_preferences", json);
        prefBean.set("username", RequestContext.getUserName());
        prefBean.set("module_id", module);
        prefBean.set("created_time", DateUtil.getCurrentTimestamp());
        userPreferencesRepository.insert(prefBean);
      } else {
        if (!prefBean.get("applicable_preferences").equals(json)) {
          // update
          prefBean.set("applicable_preferences", json);
          prefBean.set("mod_time", DateUtil.getCurrentTimestamp());
          Map keyMap = new HashMap();
          keyMap.put("preference_id", prefBean.get("preference_id"));
          userPreferencesRepository.update(prefBean, keyMap);
        }
      }
      resultMap.put(module, prefBean.getMap());
    }
    return resultMap;
  }

  /**
   * Gets the pref.
   *
   * @param moduleId the module id
   * @return the pref
   */
  public Map getPref(String moduleId) {

    Map filterMap = new HashMap();
    filterMap.put("username", RequestContext.getUserName());
    if (moduleId != null && !moduleId.equals("")) {
      filterMap.put("module_id", moduleId);
    }
    List<BasicDynaBean> beanList = userPreferencesRepository.findByCriteria(filterMap);
    Map userPrefsMap = new HashMap();
    for (BasicDynaBean bean : beanList) {
      userPrefsMap.put(bean.get("module_id"),
          jsonStringToMap.convert((String) bean.get("applicable_preferences")));
    }
    return userPrefsMap;
  }

}
