package com.insta.hms.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ValidationErrorMap.
 *
 * @author aditya
 *     to be used when making a validation object(with
 *     localization);Contains a Map of the key(the field associated with the
 *     validation check) and a map made up of exception.properties key and a
 *     list of associated params. The inner map is used to localize the
 *     messages.
 */
public class ValidationErrorMap {

  // the key of this map is the exception associated with a field; and the
  /**
   * The error map.
   */
  // value is the map that is used for localization of message
  private Map<String, Map<String, List<String>>> errorMap;

  /**
   * Instantiates a new validation error map.
   */
  public ValidationErrorMap() {
    this.errorMap = new HashMap<String, Map<String, List<String>>>();
  }

  /**
   * Instantiates a new validation error map.
   *
   * @param errorMap the error map
   */
  public ValidationErrorMap(Map<String, Map<String, List<String>>> errorMap) {
    setErrorMap(errorMap);
  }

  /**
   * Adds the error.
   *
   * @param key        the key
   * @param messageKey the message key
   */
  public void addError(String key, String messageKey) {
    addError(key, messageKey, null);
  }

  /**
   * Adds the error.
   *
   * @param key           the key
   * @param messageKey    the message key
   * @param messageParams the message params
   */
  public void addError(String key, String messageKey, List<String> messageParams) {
    Map<String, List<String>> messageMap = null;
    if (this.errorMap.get(key) == null) {
      messageMap = new HashMap<String, List<String>>();
    } else {
      messageMap = this.errorMap.get(key);
    }
    messageMap.put(messageKey, messageParams);
    this.errorMap.put(key, messageMap);
  }

  /**
   * Gets the error map.
   *
   * @return the error map
   */
  public HashMap<String, Map<String, List<String>>> getErrorMap() {
    return (HashMap<String, Map<String, List<String>>>) this.errorMap;
  }

  /**
   * Sets the error map.
   *
   * @param errorMap the error map
   */
  public void setErrorMap(Map<String, Map<String, List<String>>> errorMap) {
    this.errorMap = errorMap;
  }
}
