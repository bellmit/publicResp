package com.insta.hms.integration.hl7.v2;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.integration.configuration.InterfaceEventMappingRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class Hl7Validator {

  static Logger log = LoggerFactory.getLogger(Hl7Validator.class);

  @LazyAutowired
  private Hl7Repository repository;

  @LazyAutowired
  private InterfaceEventMappingRepository interfaceEventMappingRepository;

  /**
   * Validate Msg Generation.
   * 
   * @param visitApplicability the visit type
   * @param visitId the visit id
   * @return boolean value
   */
  public boolean validateMsgApplicability(String visitApplicability, String visitId) {
    BasicDynaBean bean = repository.getPatientDataToValidate(visitId);
    if ((!StringUtils.isEmpty(visitId) && !"ALL".equals(visitApplicability))
        && (("OP".equals(visitApplicability) && !"o".equals(bean.get("visit_type")))
            || ("IP".equals(visitApplicability) && !"i".equals(bean.get("visit_type")))
            || ("OSP".equals(visitApplicability) && !"O".equals(bean.get("op_type"))))) {
      return false;
    }
    return true;
  }

  /**
   * Checks the data against the rule to include or exclude, returns true if message needs to be
   * included, returns false if message needs to be excluded.
   * 
   * @param dataMap the data of message
   * @return boolean value
   */
  public boolean checkIncludeExcludeRule(Map<String, Object> dataMap) {
    BasicDynaBean bean = interfaceEventMappingRepository
        .getIncludeExcludeRule((int) dataMap.get("event_mapping_id"));
    // checks for exclude rule, if message is decided to be excluded,
    // then no need to check for include rule.
    if (null != bean && null != bean.get("exclude")
        && !checkRule(JsonUtility.toObjectMap((String) bean.get("exclude")), dataMap, false)) {
      return false;
    }
    if (null != bean && null != bean.get("include")) {
      return checkRule(JsonUtility.toObjectMap((String) bean.get("include")), dataMap, true);
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private boolean checkRule(Map<String, Object> ruleMap, Map<String, Object> dataMap,
      boolean isInclude) {
    boolean ruleStatus = true;
    for (String ruleMapKey : ruleMap.keySet()) {
      for (String ruleDataKey : ((Map<String, Object>) ruleMap.get(ruleMapKey)).keySet()) {
        if (null != dataMap.get(ruleMapKey)) {
          if (dataMap.get(ruleMapKey) instanceof Map) {
            ruleStatus &= validateRuleValueWithData((Map<String, Object>) ruleMap.get(ruleMapKey),
                (Map<String, Object>) dataMap.get(ruleMapKey), ruleDataKey, isInclude);
          } else if (dataMap.get(ruleMapKey) instanceof List) {
            for (Map<String, Object> map : (List<Map<String, Object>>) dataMap.get(ruleMapKey)) {
              ruleStatus &= validateRuleValueWithData((Map<String, Object>) ruleMap.get(ruleMapKey),
                  map, ruleDataKey, isInclude);
            }
          } else {
            log.info("dataMap is not instance of List nor Map, so rule cannot be checked");
          }
        }
      }
    }
    return ruleStatus;
  }

  private boolean validateRuleValueWithData(Map<String, Object> rmap, Map<String, Object> dmap,
      String ruleDataKey, boolean isInclude) {
    String ruleValue = String.valueOf(rmap.get(ruleDataKey));
    String dataValue = String.valueOf(dmap.get(ruleDataKey));
    List<String> ruleValueList = new ArrayList<String>(Arrays.asList(ruleValue.split(",")));
    return isInclude ? ruleValueList.contains(dataValue) : !ruleValueList.contains(dataValue);
  }
}
