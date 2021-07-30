package com.insta.hms.mdm.centerpreferences;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class CenterPreferencesService.
 */
@Service
public class CenterPreferencesService extends MasterService {

  /** The practitionermapping service. */
  @LazyAutowired
  private PractitionerTypeMappingsService practitionermappingService;

  /**
   * Instantiates a new center preferences service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public CenterPreferencesService(CenterPreferencesRepository repository,
      CenterPreferencesValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the center preferences.
   *
   * @return the center preferences
   */
  public BasicDynaBean getCenterPreferences() {
    return getCenterPreferences(0);
  }

  /**
   * Gets the center preferences.
   *
   * @param centerId the center id
   * @return the center preferences
   */
  public BasicDynaBean getCenterPreferences(Integer centerId) {
    Map<String, Integer> params = new HashMap<String, Integer>();
    params.put("center_id", centerId);
    BasicDynaBean centerPrefBean = findByPk(params);
    if (centerPrefBean == null) {
      params.put("center_id", 0);
      centerPrefBean = findByPk(params);
    }
    return centerPrefBean;
  }

  /**
   * Gets the center city state country name.
   *
   * @param centerId the center id
   * @return the center city state country name
   */
  public BasicDynaBean getCenterCityStateCountryName(Integer centerId) {
    Map<String, Integer> params = new HashMap<String, Integer>();
    params.put("center_id", centerId);
    return findByPk(params, true);
  }

  /**
   * Insert or update center preferences.
   *
   * @param centerId the center id
   * @param centerPrefBean the center pref bean
   * @return the integer
   */
  public Integer insertOrUpdateCenterPreferences(Integer centerId, BasicDynaBean centerPrefBean) {
    Map<String, Integer> params = new HashMap<String, Integer>();
    params.put("center_id", centerId);
    if (findByPk(params) == null) {
      return insert(centerPrefBean);
    }
    return update(centerPrefBean);
  }

  /**
   * Gets the rate plan for non insured bills.
   *
   * @param centerId the center id
   * @return the rate plan for non insured bills
   */
  public String getRatePlanForNonInsuredBills(Integer centerId) {
    String ratePlan = null;
    BasicDynaBean centerPrefs = getCenterPreferences(centerId);
    ratePlan = (centerPrefs != null
        && centerPrefs.get("pref_rate_plan_for_non_insured_bill") != null
        && !centerPrefs.get("pref_rate_plan_for_non_insured_bill").equals(""))
            ? (String) centerPrefs.get("pref_rate_plan_for_non_insured_bill") : null;
    return ratePlan;
  }

  /**
   * Insert practitioner mapping.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> insertPractitionerMapping(Map<String, String[]> params) {

    Map<String, Object> resultMap = new HashMap<String, Object>();

    Integer centerId = Integer.parseInt(params.get("center_id")[0]);
    Map<String, String[]> mappingIdMap = new HashMap<String, String[]>();
    Map<String, String[]> visitTypeMap = new HashMap<String, String[]>();
    Map<String, String[]> practitionerIdMap = new HashMap<String, String[]>();
    Map<String, String[]> consultationIdMap = new HashMap<String, String[]>();

    for (Entry<String, String[]> entry : params.entrySet()) {
      String[] value = entry.getValue();
      String key = entry.getKey();
      if (key.contains("mapping_id")) {
        mappingIdMap.put(key, value);
      } else if (key.contains("practitioner_type")) {
        practitionerIdMap.put(key, value);
      } else if (key.contains("consultation_type")) {
        consultationIdMap.put(key, value);
      } else if (key.contains("visit_type")) {
        visitTypeMap.put(key, value);
      }
    }

    Map<String, BasicDynaBean> mappingBeanInsertMap = new HashMap<String, BasicDynaBean>();
    for (Entry<String, String[]> mappingId : mappingIdMap.entrySet()) {
      BasicDynaBean mappingBean = practitionermappingService.getBean();
      String key = mappingId.getKey();
      String id = key.substring("mapping_id".length(), key.length());

      if (!mappingIdMap.get("mapping_id" + id)[0].equals("-1")) {
        if (practitionerIdMap.get("practitioner_type" + id)[0].equals("")
            || consultationIdMap.get("consultation_type" + id)[0].equals("")
            || visitTypeMap.get("visit_type" + id)[0].equals("")) {

          resultMap.put("error", "Cannot Save empty values");
          return resultMap;

        } else if (!mappingIdMap.get("mapping_id" + id)[0].equals("")) {
          mappingBean.set("center_id", centerId);
          mappingBean.set("practitioner_id",
              Integer.parseInt(practitionerIdMap.get("practitioner_type" + id)[0]));
          mappingBean.set("consultation_type_id",
              Integer.parseInt(consultationIdMap.get("consultation_type" + id)[0]));
          mappingBean.set("visit_type", visitTypeMap.get("visit_type" + id)[0]);

          String tempId = centerId.toString() + practitionerIdMap.get("practitioner_type" + id)[0]
              + visitTypeMap.get("visit_type" + id)[0];
          if (!mappingBeanInsertMap.containsKey(tempId)) {
            mappingBeanInsertMap.put(tempId, mappingBean);
          }
        }
      }
    }

    for (Entry<String, String[]> mappingId : mappingIdMap.entrySet()) {
      BasicDynaBean mappingBean = practitionermappingService.getBean();
      String key = mappingId.getKey();
      String id = key.substring("mapping_id".length(), key.length());

      if (!mappingIdMap.get("mapping_id" + id)[0].equals("-1")) {
        if (practitionerIdMap.get("practitioner_type" + id)[0].equals("")
            || consultationIdMap.get("consultation_type" + id)[0].equals("")
            || visitTypeMap.get("visit_type" + id)[0].equals("")) {

          resultMap.put("error", "Cannot Save empty values");
          return resultMap;

        } else if (mappingIdMap.get("mapping_id" + id)[0].equals("")) {
          mappingBean.set("center_id", centerId);
          mappingBean.set("practitioner_id",
              Integer.parseInt(practitionerIdMap.get("practitioner_type" + id)[0]));
          mappingBean.set("consultation_type_id",
              Integer.parseInt(consultationIdMap.get("consultation_type" + id)[0]));
          mappingBean.set("visit_type", visitTypeMap.get("visit_type" + id)[0]);

          String tempId = centerId.toString() + practitionerIdMap.get("practitioner_type" + id)[0]
              + visitTypeMap.get("visit_type" + id)[0];
          if (!mappingBeanInsertMap.containsKey(tempId)) {
            mappingBeanInsertMap.put(tempId, mappingBean);
          }
        }
      }
    }

    ArrayList<Object> delKeys = new ArrayList<Object>();
    delKeys.add(centerId);
    int[] mappingDeleteRes = practitionermappingService.batchDelete("center_id", delKeys);
    resultMap.put("deleteRes", mappingDeleteRes);

    List<BasicDynaBean> mappingBeanInsertList = new ArrayList<BasicDynaBean>();
    mappingBeanInsertList.addAll(mappingBeanInsertMap.values());
    if (mappingBeanInsertList.size() != 0) {
      int[] mappingInsertRes = practitionermappingService.batchInsert(mappingBeanInsertList);
      resultMap.put("insertRes", mappingInsertRes);
    }

    return resultMap;
  }

}
