package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.common.StringUtil;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class HospitalRoleValidator.
 *
 * @author anil.n
 */
@Component
public class HospitalRoleValidator extends MasterValidator {

  /** The valid malaffi roles. */
  private static final Set<String> VALID_MALAFFI_ROLES = initializeValidMalaffiRoles();

  private static Set<String> initializeValidMalaffiRoles() {
    Set<String> validMalaffiRoles = new HashSet<>();
    validMalaffiRoles.add("P");
    validMalaffiRoles.add("S");
    validMalaffiRoles.add("T");
    validMalaffiRoles.add("F");
    validMalaffiRoles.add("");
    return Collections.unmodifiableSet(validMalaffiRoles);
  }

  /**
   * Validate malaffi role.
   *
   * @param requestBody the request body
   */
  public void validateMalaffiRole(ModelMap requestBody) {
    String malaffiRole = (String) requestBody.get("malaffi_role");
    if (!VALID_MALAFFI_ROLES.contains(malaffiRole)) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("malaffi_role", "exception.invalid.value",
          Arrays.asList(StringUtil.prettyName("malaffi_role")));
      throw new ValidationException(errorMap);
    }
  }

  /**
   * Save hospital Roles.
   *
   * @param requestBody request params
   */
  public void validateOrderControlItemsAndSubGroups(ModelMap requestBody) {

    List<Map<String, Object>> orderControlItems = 
          (List<Map<String, Object>>) requestBody.get("order_control_items");
    List<Map<String, Object>> orderControlSubGroups = 
            (List<Map<String, Object>>) requestBody.get("order_control_sub_groups");
    // Checking service sub groups
    Map<Integer, Object> existingServicesMap = new HashMap<Integer, Object>();
    for (Map<String, Object> serviceGroups : orderControlSubGroups) {
      Integer serviceGroupId = (Integer) serviceGroups.get("service_group_id");
      if ("D".equals(serviceGroups.get("action"))) {
        continue;
      }
      List<Map<String, Object>> serviceSubGroups = (List<Map<String, Object>>) 
          serviceGroups.get("service_sub_groups");
      if (serviceSubGroups.size() > 0) {
        List<Integer> existingSubService = new ArrayList<Integer>();
        for (Map<String, Object> subGroup : serviceSubGroups) {
          if ("D".equals(subGroup.get("action"))) {
            continue;
          }
          Integer subGroupId = (Integer) subGroup.get("service_sub_group_id");
          existingServicesMap.put(serviceGroupId, subGroupId);
          if (existingSubService.contains(subGroupId)) {
            throw new ValidationException(
                "patient.hospitalroles.save.ordercontrolsuborder.exception");
          }
          existingSubService.add(subGroupId);
        }
      }
    }
    Map<Integer, Object> existingItemsMap = new HashMap<Integer, Object>();
    for (Map<String, Object> orderItem : orderControlItems) {
      Integer subGroupId = (Integer) orderItem.get("service_sub_group_id");
      Integer serviceId = (Integer) orderItem.get("service_group_id");
      if ("D".equals(orderItem.get("action"))) {
        continue;
      }
      if (existingServicesMap.get(serviceId) != null
            && (existingServicesMap.get(serviceId) == subGroupId 
            || existingServicesMap.get(serviceId).equals(-9))) {
        throw new ValidationException("patient.hospitalroles.save.ordercontrolitem.exception");
      }
      Object itemId = orderItem.get("item_id");
      if (existingItemsMap.get(subGroupId) != null
              && itemId.equals(existingItemsMap.get(subGroupId))) {
        throw new ValidationException("patient.hospitalroles.save.ordercontrolitem.exception");
      }
      existingItemsMap.put(subGroupId, itemId);
    }
  }
}
