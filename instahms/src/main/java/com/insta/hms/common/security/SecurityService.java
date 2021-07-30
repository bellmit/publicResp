package com.insta.hms.common.security;

import com.insta.hms.common.DbStatsRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.doctors.DoctorService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SecurityService which provides the service facade for accumulation of all access
 * control attributes.
 * 
 * @author tanmay.k
 */
@Service("securityService")
public class SecurityService {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The security repository. */
  @LazyAutowired
  private SecurityRepository securityRepository;

  @LazyAutowired
  private DbStatsRepository dbStatsRepository;

  @LazyAutowired
  private DoctorService doctorService;

  /** The Constant _DEFAULT_SECURITY_ATTRIBUTES. */
  private static final String[] _DEFAULT_SECURITY_ATTRIBUTES = new String[] { "menuAvlblMap",
      "multiStoreAccess", "groupAvlblMap", "actionRightsMap", "urlRightsMap" };

  /**
   * Gets the security attributes.
   *
   * @return the security attributes
   */
  public Map<String, Object> getSecurityAttributes() {
    Map<String, Object> securityAttributes = getSecurityAttributesFromSession();

    /*
     * TODO - Aberration/Hack done for allowing access to all action rights to Admin users since
     * action rights are not stored in the database but hardcoded in Role.jsp. Ideally, the way
     * would be to extract them out to the database and hit it or the set of all actionRights.
     */
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer roleId = (Integer) sessionAttributes.get("roleId");
    Map<String, Object> actionRightMap =
        (Map<String, Object>) securityAttributes.get("actionRightsMap");
    if ((roleId.equals(1) || roleId.equals(2))
        && (null == actionRightMap || actionRightMap.isEmpty())) {
      Map<String, Boolean> actionRightsMap = new HashMap<>();
      actionRightsMap.put("all_rights", true);
      securityAttributes.put("actionRightsMap", actionRightsMap);
    }

    String docId = (String) sessionAttributes.get("doctorId");
    if (docId != null) {
      securityAttributes.put("doctor_centers_list", doctorService.getDoctorCentersList(docId));
    }
    securityAttributes.put("modules_activated", getActivatedModules());
    securityAttributes.put("large_dataset_flags", dbStatsRepository.getLargeDatasetMap());
    return securityAttributes;
  }

  /**
   * Gets the activated modules.
   *
   * @return the activated modules
   */
  public List<String> getActivatedModules() {
    List<String> activatedModules = new ArrayList<>();
    for (BasicDynaBean module : securityRepository.getActivatedModules()) {
      activatedModules.add((String) module.get("module_id"));
    }
    return activatedModules;
  }

  /**
   * Gets the security attributes from session.
   *
   * @return the security attributes from session
   */
  public Map<String, Object> getSecurityAttributesFromSession() {
    return sessionService.getSessionAttributes(_DEFAULT_SECURITY_ATTRIBUTES);
  }

  /**
   * Checks if user is an administrator.
   * 
   * @return true if administrator
   */
  public boolean isAdministrator() {
    Integer roleId = (Integer) sessionService.getSessionAttributes().get("roleId");
    return roleId == 1 || roleId == 2;
  }
}
