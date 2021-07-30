package com.insta.hms.core.patient.registration;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The Class RegistrationCustomFieldsService.
 */
@Service
public class RegistrationCustomFieldsService extends MasterService {
  
  private static final String VALUES = "values";

  /**
   * Instantiates a new registration custom fields service.
   *
   * @param repo
   *          the repo
   * @param vali
   *          the vali
   */
  public RegistrationCustomFieldsService(RegistrationCustomFieldsRepository repo,
      RegistrationCustomFieldsValidator vali) {
    super(repo, vali);
  }

  /**
   * Gets the active reg custom fields and values.
   *
   * @return the active reg custom fields and values
   */
  public List<BasicDynaBean> getActiveRegCustomFieldsAndValues() {
    return ((RegistrationCustomFieldsRepository) this.getRepository())
        .getActiveRegCustomFieldsAndValues();
  }

  /**
   * List all custom fields.
   *
   * @param applicableTo
   *          the applicable to
   * @return the list
   */
  public List<BasicDynaBean> listAllCustomFields(String applicableTo) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("status", "A");
    filterMap.put("applicable_to", applicableTo);
    return ((RegistrationCustomFieldsRepository) this.getRepository()).listAll(null, filterMap,
        "display_order");
  }
  
  /**
   * Get custom field values.
   *
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getCustomFieldValues(String type) {
    List<BasicDynaBean> regCustomFieldsList = ((RegistrationCustomFieldsRepository) this
        .getRepository()).getRegCustomFieldsAndValues(type);
    Map<String, Object> customFieldsMap = new HashMap<>();

    for (BasicDynaBean customFieldBean : regCustomFieldsList) {
      Map<String, Object> customFieldMap = new HashMap<>(customFieldBean.getMap());
      if ((String) customFieldBean.get(VALUES) != null) {
        customFieldMap.put(VALUES,
            ((String) customFieldBean.get(VALUES)).split(Pattern.quote("^^")));
      } else {
        customFieldMap.put(VALUES, Collections.emptyList());
      }
      customFieldsMap.put((String) customFieldBean.get("name"), customFieldMap);

    }
    return customFieldsMap;
  }

}
