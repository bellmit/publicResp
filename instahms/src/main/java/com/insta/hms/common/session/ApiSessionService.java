package com.insta.hms.common.session;

import com.bob.hms.common.RequestContext;
import com.bob.hms.common.RightsHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.role.RoleMasterRepository;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * This class is not meant to be autowired. Refer to SessionService
 */
@Service
public class ApiSessionService implements SessionService {

  @LazyAutowired
  private CenterService centerService;

  @LazyAutowired
  private UserService userService;

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private PatientDetailsRepository patientDetailsRepository;

  @Autowired
  private RoleMasterRepository roleMasterRepository;

  /** The Constant _DEFAULT_SESSION_ATTRIBUTES. */
  private static final String[] _DEFAULT_SESSION_ATTRIBUTES = new String[] { "userId",
      "sesHospitalId", "centerId", "roleId", "apiUser" };

  /**
   * Gets the session attributes.
   *
   * @return the session attributes
   */
  @Override
  public Map<String, Object> getSessionAttributes() {
    return getSessionAttributes(_DEFAULT_SESSION_ATTRIBUTES);
  }

  /**
   * Gets the session attributes.
   *
   * @param keys the keys
   * @return the session attributes
   */
  @Override
  public Map<String, Object> getSessionAttributes(String[] keys) {
    Map<String, Object> sessionPairs = new HashMap<>();
    BasicDynaBean useBean = userService.getLoggedUser();
    if (useBean != null) {
      // Clinician centric login - u_user credentials
      Integer roleId = Integer.valueOf(((Number) useBean.get("role_id")).intValue());
      String username = (String) useBean.get("emp_username");
      Integer centerId = (Integer) useBean.get("center_id");
      return getSessionPairs(keys, roleId, username, centerId);
    }
    
    if (RequestContext.getHttpRequest() != null) {
      // Patient centric login - session based
      HttpSession session = RequestContext.getHttpRequest().getSession(false);
      for (String key : keys) {
        Object value = session.getAttribute(key);
        if (null != value) {
          sessionPairs.put(key, value);
        }
      }
      return sessionPairs;
    }
    
    String username = RequestContext.getUserName();
    if (username != null) {
      useBean = patientDetailsRepository.findByKey("mr_no", username);
      if (useBean != null) {
        // Clinician centric login - patient_details(mobile access) credentials
        Integer roleId = ((BigDecimal) roleMasterRepository.findByKey("role_name", "Patient")
            .get("role_id")).intValue();
        return getSessionPairs(keys, roleId, username, 0);
      }
    }

    return sessionPairs;
  }

  private Map<String, Object> getSessionPairs(String[] keys, Integer roleId, String username,
      Integer centerId) {
    Map<String, Object> sessionPairs = new HashMap<>();
    for (String key : keys) {
      Object value = null;
      if (key.equals("userId")) {
        value = username;
      } else if (key.equals("sesHospitalId")) {
        value = RequestContext.getSchema();
      } else if (key.equals("centerId")) {
        value = centerId;
      } else if (key.equals("roleId")) {
        value = roleId;
      } else if (key.equals("urlRightsMap")) {
        Map actionUrlMap = (Map) servletContext.getAttribute("actionUrlMap");
        List privilegedActions = (List) servletContext.getAttribute("privilegedActions");
        value = RightsHelper.getUrlRightsMap(roleId, privilegedActions, actionUrlMap);
      } else if (key.equals("apiUser")) {
        value = "Y";
      }
      if (null != value) {
        sessionPairs.put(key, value);
      }
    }
    return sessionPairs;

  }

  @Override
  public void setSessionAttribute(String key, String value) {
    // TODO Auto-generated method stub

  }
}
