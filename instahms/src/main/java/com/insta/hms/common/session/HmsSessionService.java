package com.insta.hms.common.session;

import com.bob.hms.common.RequestContext;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * This class is not meant to be autowired. Refer to SessionService
 */
@Service
public class HmsSessionService implements SessionService {
  /** The Constant _DEFAULT_SESSION_ATTRIBUTES. */
  private static final String[] _DEFAULT_SESSION_ATTRIBUTES =
      new String[] { "userId", "sesHospitalId", "centerId", "roleId", "roleName", "centerName",
          "billingcounterName", "billingcounterId", "loginCenterHealthAuthority", "doctorId",
          "nexus_token", "login_handle", "user_accessible_patient_groups", "hospital_role_ids",
          "loggedInRoleId", "loggedInCenterId", "timeZone", "pharmacyCounterName",
          "pharmacyCounterId" };

  /** The Constant aliases. */
  private static final Map<String, String> aliases = new HashMap<>();

  static {
    aliases.put("billingcounterName", "billing_counter");
    aliases.put("billingcounterId", "billing_counter_id");
    aliases.put("pharmacyCounterName", "pharmacy_counter");
    aliases.put("pharmacyCounterId", "pharmacy_counter_id");
  }

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
   * @param keys
   *          the keys
   * @return the session attributes
   */
  @Override
  public Map<String, Object> getSessionAttributes(String[] keys) {
    HttpSession session = RequestContext.getHttpRequest().getSession(false);
    Map<String, Object> sessionPairs = new HashMap<>();

    for (String key : keys) {
      String fieldName = aliases.containsKey(key) ? aliases.get(key) : key;
      Object value = session.getAttribute(key);

      if (null != value) {
        sessionPairs.put(fieldName, value);
      }
    }
    return sessionPairs;
  }

  /**
   * Set a new session attributes.
   *
   * @param key
   *          the key
   * @param value
   *          for the corresponding key
   */
  @Override
  public void setSessionAttribute(String key, String value) {
    HttpSession session = RequestContext.getHttpRequest().getSession(false);
    session.setAttribute(key, value);
  }
}
