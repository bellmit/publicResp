package com.insta.hms.eservice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class EValidator.
 *
 * @param <T> the generic type
 */
public abstract class EValidator<T> {

  /**
   * The error map.
   */
  Map<String, String> errorMap = new HashMap<String, String>();

  /**
   * Validate.
   *
   * @param data the data
   * @return true, if successful
   */
  public abstract boolean validate(T data);

  /**
   * Gets the error map.
   *
   * @return the error map
   */
  public Map<String, String> getErrorMap() {
    return Collections.unmodifiableMap(errorMap);
  }

  /**
   * Validate non null.
   *
   * @param object    the object
   * @param key       the key
   * @param message   the message
   * @param msgParams the msg params
   * @return true, if successful
   */
  public boolean validateNonNull(Object object, String key, String message, Object[] msgParams) {
    if (null != object) {
      return true;
    } else {
      addError(key, message, msgParams);
      return false;
    }
  }
  
  /**
   * Validate non null.
   *
   * @param object    the object
   * @param key       the key
   * @param message   the message
   * @param msgParams the msg params
   * @param rights    the rights
   * @return true, if successful
   */
  public boolean validateNonNull(Object object, String key, String message, Object[] msgParams,
          boolean rights) {
    if (null != object) {
      return true;
    } else {
      addError(key, message, msgParams, rights);
      return false;
    }
  }

  /**
   * Validate not empty.
   *
   * @param object    the object
   * @param key       the key
   * @param message   the message
   * @param msgParams the msg params
   * @return true, if successful
   */
  public boolean validateNotEmpty(Object object, String key, String message, Object[] msgParams) {
    if (validateNonNull(object, key, message, msgParams)) {
      if (!object.toString().trim().equals("")) {
        return true;
      } else {
        addError(key, message, msgParams);
        return false;
      }
    }
    return false;
  }

  /**
   * Validate not empty.
   *
   * @param object    the object
   * @param key       the key
   * @param message   the message
   * @param msgParams the msg params
   * @param rights    the rights
   * @return true, if successful
   */
  public boolean validateNotEmpty(Object object, String key, String message, Object[] msgParams,
      boolean rights) {
    if (validateNonNull(object, key, message, msgParams, rights)) {
      if (!object.toString().trim().equals("")) {
        return true;
      } else {
        addError(key, message, msgParams, rights);
        return false;
      }
    }
    return false;
  }

  /**
   * Adds the error.
   *
   * @param key     the key
   * @param message the message
   */
  public void addError(String key, String message) {
    addError(key, message, new Object[] {});
  }

  /**
   * Adds the error.
   *
   * @param key          the key
   * @param message      the message
   * @param formatParams the format params
   */
  public void addError(String key, String message, Object[] formatParams) {
    if (null != key && null != message) {
      String formattedMessage = String.format(message, formatParams);
      errorMap.put(key, formattedMessage);
    }
  }

  /**
   * Adds the error.
   *
   * @param key          the key
   * @param message      the message
   * @param formatParams the format params
   * @param hasRights    the has rights
   */
  public void addError(String key, String message, Object[] formatParams, boolean hasRights) {
    if (null != key && null != message) {
      if (hasRights) {
        String formattedMessage = String.format(message + " <br/><a target=\'_blank\' "
            + "href=\"%s\">%s</a> <br/>",
            formatParams[1], formatParams[0]);
        errorMap.put(key, formattedMessage);
      } else {
        String formattedMessage = String.format(message + " <br/><b>%s</b><br/>", formatParams[0]);
        errorMap.put(key, formattedMessage);
      }
    }
  }
}
