package com.insta.hms.common.validation;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The Class ActionRightsACLRule.
 */
@Component
public class ActionRightsACLRule {

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /**
   * Check if user has access rights to edit the given fields.
   *
   * @param newBean
   *          the new bean
   * @param existingBean
   *          the existing bean
   * @param fields
   *          the fields
   * @param actionRight
   *          the action right
   * @param violations
   *          the violations
   * @return true, if successful
   */
  @SuppressWarnings("unchecked")
  public boolean apply(BasicDynaBean newBean, BasicDynaBean existingBean, List<String> fields,
      String actionRight, List<String> violations) {
    if (existingBean == null || newBean == null) {
      return false;
    }

    boolean success = true;
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if ("N".equals(actionRightsMap.get(actionRight))) {
      for (String field : fields) {
        Object existingValue = existingBean.get(field);
        Object newValue = newBean.get(field);
        // to check both the values are not equal.
        if (!(((existingValue == null || "".equals(existingValue))
            && (newValue == null || "".equals(newValue)))
            || (existingValue != null && existingValue.equals(newValue)))) {
          success = false;
          violations.add(field);
        }
      }
    }
    return success;
  }

}
