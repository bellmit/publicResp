package com.insta.hms.common.session;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The Class SessionService to get attributes set in the session. ALWAYS autowire sessionService
 * NEVER directly autowire HmsSessionService or ApiSessionService
 * 
 * @author tanmay.k
 */

@Service("sessionService")
public interface SessionService {
  /**
   * Gets the session attributes.
   *
   * @return the session attributes
   */
  public Map<String, Object> getSessionAttributes();

  /**
   * Gets the session attributes.
   *
   * @param keys
   *          the keys
   * @return the session attributes
   */
  public Map<String, Object> getSessionAttributes(String[] keys);

  /**
   * Set a new session attributes.
   *
   * @param key
   *          the key
   * @param value
   *          for the corresponding key
   */
  public void setSessionAttribute(String key, String value);
}
