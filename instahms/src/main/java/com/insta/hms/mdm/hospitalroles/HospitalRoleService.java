package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The Class HospitalRoleService.
 *
 * @author anil.n
 */

@Service
public class HospitalRoleService extends MasterService {
  
  /** The hospital role repository. */
  @LazyAutowired
  private HospitalRoleRepository hospitalRoleRepository;

  @LazyAutowired
  private HospitalRoleValidator hospitalRoleValidator;
  
  /** The hospital role repository. */
  @LazyAutowired
  private HospitalRoleOrderControlRepository hospitalRoleOrderControlRepository;
  
  /** The Hospital Role Prescription Types Repository. */
  @LazyAutowired
  private HospitalRolePrescriptionTypesRepository hospitalRolePrescriptionTypesRepository;

  /** The generic preferences service. */
  @LazyAutowired 
  private GenericPreferencesService genericPreferencesService;
  
  private static final String ACTION = "action";
  private static final String HOSPITAL_ROLE_CONTROL_ID = "hospital_role_control_id";
  private static final String HOSP_ROLE_ID = "hosp_role_id";
  private static final String HOSP_ROLE_NAME = "hosp_role_name";
  private static final String ROLE_ID = "role_id";
  private static final String MALAFFI_ROLE = "malaffi_role";
  private static final String SERVICE_GROUP_ID = "service_group_id";
  private static final String SERVICE_GROUP_NAME = "service_group_name";
  private static final String SERVICE_SUB_GROUP_ID = "service_sub_group_id";
  private static final String SERVICE_SUB_GROUP_NAME = "service_sub_group_name";
  private static final String STAR = "*";
  private static final String STATUS = "status";
  private static final String PRESCRIPTION_TYPES = "prescription_types";

  /**
   * Instantiates a new hospital role service.
   *
   * @param repo the repo
   * @param validator the validator
   * @param hospitalRoleOrderControlRepository the HospitalRoleOrderControlRepository
   */
  public HospitalRoleService(HospitalRoleRepository repo,HospitalRoleValidator validator,
      HospitalRoleOrderControlRepository hospitalRoleOrderControlRepository) {
    super(repo, validator);
  }

  /**
   * Find Hospital Roles by filter.
   *
   * @param params filter parameters
   * @return result map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {
    return hospitalRoleRepository.findByFilters(params);
  }

  /**
   * Gets the hospital users.
   *
   * @param hospRoleId the hosp role id
   * @param centerId the center id
   * @return the hospital users
   */
  public List<Map<String, Object>> getHospitalUsers(Integer hospRoleId, Integer centerId) {
    BasicDynaBean genBean = genericPreferencesService.getPreferences();
    int centersIncDefault = (Integer) genBean.get("max_centers_inc_default");
    return ConversionUtils.listBeanToListMap(hospitalRoleRepository
        .getHospitalUsers(hospRoleId, centerId, centersIncDefault));
  }

  /**
   * Save hospital Roles.
   *
   * @param requestBody request params
   */
  @Transactional(rollbackFor = Exception.class)
  public LinkedHashMap<String, Object> save(ModelMap requestBody) {
    hospitalRoleValidator.validateMalaffiRole(requestBody);
    hospitalRoleValidator.validateOrderControlItemsAndSubGroups(requestBody);
    String hospRoleName = (String) requestBody.get(HOSP_ROLE_NAME);
    String malaffiRole = (String) requestBody.get(MALAFFI_ROLE);
    String hospStatus = (String) requestBody.get(STATUS);

    Map<String, Object> hospParamMap = new HashMap<String, Object>();
    hospParamMap.put(HOSP_ROLE_NAME, hospRoleName);
    hospParamMap.put(MALAFFI_ROLE, malaffiRole);
    hospParamMap.put(STATUS, hospStatus);
    Integer hospRoleId = (Integer) requestBody.get(HOSP_ROLE_ID);
    String hospAction = (String) requestBody.get(ACTION);
    BasicDynaBean hospitalRole = null;
    if (hospRoleId != null) {
      Map<String, Integer> fetchHosp = new HashMap<String, Integer>();
      fetchHosp.put(HOSP_ROLE_ID, hospRoleId);
      hospitalRole = hospitalRoleRepository.findByPk(fetchHosp);
      if ("U".equals(hospAction)) {
        ConversionUtils.copyToDynaBean(hospParamMap, hospitalRole);
        update(hospitalRole);
      }
    } else if ("N".equals(hospAction)) {
      hospitalRole = hospitalRoleRepository.getBean();
      ConversionUtils.copyToDynaBean(hospParamMap, hospitalRole);
      insert(hospitalRole);
    }
    if (hospitalRole == null) {
      throw new ValidationException(
           "patient.hospitalroles.addshow.norole.exeception");
    }
    hospRoleId = (Integer) hospitalRole.get(HOSP_ROLE_ID);
    
    // Save the prescription types for the pending prescription dashboard.
    List<String> prescriptionTypes = (List<String>) requestBody.get(PRESCRIPTION_TYPES);
    savePrescriptionTypes(hospRoleId, prescriptionTypes);
        
    List<Map<String, Object>> newServiceSubGroups = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> deletedServiceSubGroups = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> orderControlItems = 
            (List<Map<String, Object>>) requestBody.get("order_control_items");
    List<Map<String, Object>> orderControlSubGroups = 
            (List<Map<String, Object>>) requestBody.get("order_control_sub_groups");
    for (Map<String, Object> serviceGroups : orderControlSubGroups) {
      Integer serviceGroupId = (Integer) serviceGroups.get(SERVICE_GROUP_ID);
      String serviceGroupAction = (String) serviceGroups.get(ACTION);
      List<Map<String, Object>> serviceSubGroups = (List<Map<String, Object>>) 
          serviceGroups.get("service_sub_groups");
      
      
      boolean noServiceSubGroups = true;
      for (Map<String, Object> subGroup : serviceSubGroups) {
        if (StringUtils.isEmpty((String) subGroup.get(ACTION))
            || subGroup.get(ACTION).equals("N")) {
          noServiceSubGroups = false;
          break;
        }
      }
      
      if (noServiceSubGroups && !"D".equals(serviceGroupAction)) {
        Map<String, Object> allSubGroup = new HashMap<>();
        allSubGroup.put(SERVICE_SUB_GROUP_ID, -9);
        allSubGroup.put(ACTION, "N");
        serviceSubGroups.add(allSubGroup);
      }
      
      for (Map<String, Object> subGroup : serviceSubGroups) {
        Integer subGroupId = (Integer) subGroup.get(SERVICE_SUB_GROUP_ID);
        subGroup.put("item_id", STAR);
        subGroup.put("entity", STAR);
        subGroup.put(SERVICE_GROUP_ID, serviceGroupId);
        subGroup.put(SERVICE_SUB_GROUP_ID, subGroupId);
        Integer hospitalContId = (Integer) subGroup.get(HOSPITAL_ROLE_CONTROL_ID);
        if (null != hospitalContId) {
          subGroup.put(HOSPITAL_ROLE_CONTROL_ID, hospitalContId);  
        }
        subGroup.put(ROLE_ID, hospRoleId);
        String subGroupAction = (String) subGroup.get(ACTION);
        if ("D".equals(serviceGroupAction) && hospitalContId != null) {
          deletedServiceSubGroups.add(subGroup);
          continue;
        }
        if ("N".equals(subGroupAction) && hospitalContId == null) {
          newServiceSubGroups.add(subGroup); 
        } else if ("D".equals(subGroupAction) && hospitalContId != null) {
          deletedServiceSubGroups.add(subGroup);
        }
      }
      
    }
    for (Map<String, Object> orderItem : orderControlItems) {
      orderItem.put(ROLE_ID, hospRoleId);
      Integer hospitalContId = (Integer) orderItem.get(HOSPITAL_ROLE_CONTROL_ID);
      String itemAction = (String) orderItem.get(ACTION);
      if ("N".equals(itemAction) && hospitalContId == null) {
        newServiceSubGroups.add(orderItem); 
      } else if ("D".equals(itemAction) && hospitalContId != null) {
        deletedServiceSubGroups.add(orderItem); 
      }
    }
    if (deletedServiceSubGroups.size() > 0) {
      for (Map<String, Object> deleteServiceSubGroup : deletedServiceSubGroups) {
        Integer hospitalContId = (Integer) deleteServiceSubGroup.get(HOSPITAL_ROLE_CONTROL_ID);
        hospitalRoleOrderControlRepository.delete(HOSPITAL_ROLE_CONTROL_ID, hospitalContId);
      }
    }
    if (newServiceSubGroups.size() > 0) {
      for (Map<String, Object> newServiceSubGroup : newServiceSubGroups) {
        BasicDynaBean newControlRole =
            hospitalRoleOrderControlRepository.getBean();
        ConversionUtils.copyToDynaBean(newServiceSubGroup, newControlRole);
        hospitalRoleOrderControlRepository.insert(newControlRole);
      }
    }
    return show(hospRoleId);
  }

  /**
   * Gets the hospital roles with services.
   *
   * @param roleId the hosp role id
   * @return the hospital users
   */
  public LinkedHashMap<String, Object> show(Object roleId) {
    Map<String, Integer> params = new HashMap<String, Integer>();
    params.put(HOSP_ROLE_ID, Integer.valueOf(roleId.toString()));
    BasicDynaBean hospitalRole = hospitalRoleRepository.findByPk(params);
    if (null == hospitalRole) {
      throw new ValidationException(
        "js.topnav.menu.hospital.admin.masters.hospital.roles.master.exception"
      );
    }
    LinkedHashMap<String, Object> showData = new LinkedHashMap<String, Object>();
    showData.putAll(hospitalRole.getMap());
    if (showData.get(MALAFFI_ROLE) == null) {
      showData.put(MALAFFI_ROLE, "");
    }
    List<BasicDynaBean> serviceSubGroups = hospitalRoleOrderControlRepository
        .getServiceSubGroupsByRoleId(Integer.valueOf(roleId.toString()));
    Map<String,List<Map<String, Object>>> serviceSubGroupsMap =
            new HashMap<String,List<Map<String, Object>>>();
    for (BasicDynaBean service : serviceSubGroups) {
      Map<String, Object> serviceSubgroupMap = new HashMap<String, Object>();
      serviceSubgroupMap.put(SERVICE_SUB_GROUP_ID,
            service.get(SERVICE_SUB_GROUP_ID));
      serviceSubgroupMap.put(SERVICE_SUB_GROUP_NAME,
            service.get(SERVICE_SUB_GROUP_NAME));
      serviceSubgroupMap.put(HOSPITAL_ROLE_CONTROL_ID,
            service.get(HOSPITAL_ROLE_CONTROL_ID));
      String serviceGroupId = Integer.toString((int)service.get(SERVICE_GROUP_ID));
      if (serviceSubGroupsMap.containsKey(serviceGroupId)) {
        List<Map<String, Object>> serviceSubGroupsVal = serviceSubGroupsMap.get(serviceGroupId);
        serviceSubGroupsVal.add(serviceSubgroupMap);
      } else {
        List<Map<String, Object>> serviceSubGroupsVal =
             new ArrayList<Map<String, Object>>();
        serviceSubGroupsVal.add(serviceSubgroupMap);
        serviceSubGroupsMap.put(serviceGroupId, serviceSubGroupsVal);
      }
    }
    List<Map<String, Object>> resultServiceSubGroups = new ArrayList<Map<String, Object>>();
    List<String> addedServiceGroups = new ArrayList<String>();
    for (BasicDynaBean resultService: serviceSubGroups) {
      String serviceGroupId = Integer.toString((int)resultService.get(SERVICE_GROUP_ID));
      if (addedServiceGroups.contains(serviceGroupId)) {
        continue;
      }
      addedServiceGroups.add(serviceGroupId);
      Map<String, Object> resultServiceSubGroupsMap = new HashMap<String, Object>();
      resultServiceSubGroupsMap.put(SERVICE_GROUP_ID, resultService.get(SERVICE_GROUP_ID));
      resultServiceSubGroupsMap.put(SERVICE_GROUP_NAME, resultService.get(SERVICE_GROUP_NAME));
      if (serviceSubGroupsMap.containsKey(serviceGroupId)) {
        resultServiceSubGroupsMap.put("service_sub_groups",
            serviceSubGroupsMap.get(serviceGroupId));
      }
      resultServiceSubGroups.add(resultServiceSubGroupsMap);
    }
    List<BasicDynaBean> subGroupItems = hospitalRoleOrderControlRepository
        .getServiceSubGroupItemsByRoleId(Integer.valueOf(roleId.toString()));
    showData.put("order_control_sub_groups", resultServiceSubGroups);
    showData.put("order_control_items",
        ConversionUtils.listBeanToListMap(subGroupItems)
    );
    List<String> rxTypes = hospitalRolePrescriptionTypesRepository
        .getPrescriptionTypes(Integer.valueOf(roleId.toString()));
    showData.put(PRESCRIPTION_TYPES, rxTypes);
    return showData;
  }

  /**
   * Gets the hospital role ids.
   *
   * @param userName the user name
   * @return the hospital role ids
   */
  public List<Integer> getHospitalRoleIds(String userName) {
    List<BasicDynaBean> hospitalRoleIds = hospitalRoleRepository
        .getHospitalRoleIds(userName);
    List<Integer> hospitalRoleIdsList = new ArrayList<>();
    for (BasicDynaBean hospitalRole : hospitalRoleIds) {
      hospitalRoleIdsList.add((Integer) hospitalRole.get(HOSP_ROLE_ID));
    }
    return hospitalRoleIdsList;
  }

  public List<BasicDynaBean> getMalaffiRoleMappedHospitalRoles() {
    return hospitalRoleRepository.getMalaffiRoleMappedHospitalRoles();
  }

  public List<BasicDynaBean> getOrderControlRules(List userId) {
    return hospitalRoleOrderControlRepository.getOrderControlRules(userId);
  }
  
  public List<BasicDynaBean> getSelectedHospitalRoles(List<Integer> roleIds) {
    return hospitalRoleRepository.getSelectedHospitalRoles(roleIds);
  }

  private void savePrescriptionTypes(int hospRoleId, List<String> rxTypes) {
    hospitalRolePrescriptionTypesRepository.deleteMappedPrescriptionTypes(hospRoleId);
    hospitalRolePrescriptionTypesRepository.insert(hospRoleId, rxTypes);
  }
}
