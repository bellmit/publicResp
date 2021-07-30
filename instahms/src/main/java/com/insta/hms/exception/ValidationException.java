package com.insta.hms.exception;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The Class ValidationException.
 *
 * @author aditya
 */
public class ValidationException extends HMSException {

  /**
   * Eclipse generated serialVerisonId.
   */
  private static final long serialVersionUID = 5503781916915228921L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.validation.failed";

  /**
   * The errors.
   */
  /*
   * Maintains validation errors;the type is actually String,List<String>. But to
   * override this map in @NestableValidationException, the type is String,Object.
   */
  private Map<String, List<String>> errors = new HashMap<String, List<String>>();

  /**
   * Spring MessageUtil class for localization.
   */
  private static final MessageUtil messageUtil =
      ApplicationContextProvider.getBean(MessageUtil.class);

  /**
   * Instantiates a new validation exception.
   */
  public ValidationException() {
    this(defaultKey, null);
  }

  /**
   * Instantiates a new validation exception.
   *
   * @param cause the cause
   */
  public ValidationException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new validation exception.
   *
   * @param singleValueErrors the single value errors
   */
  public ValidationException(HashMap<String, String> singleValueErrors) {
    this();
    for (Map.Entry<String, String> errorsEntry : singleValueErrors.entrySet()) {
      addError(errorsEntry);
    }
  }

  /**
   * Instantiates a new validation exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public ValidationException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new validation exception.
   *
   * @param params the params
   */
  public ValidationException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new validation exception.
   *
   * @param messageKey the message key
   */
  public ValidationException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * This constructor is for when the whole errors object has been constructed by
   * the Validator and localization is to be done.
   *
   * @param validationErrorMap the validation error map
   */
  public ValidationException(ValidationErrorMap validationErrorMap) {
    this();
    Map<String, Map<String, List<String>>> errorKeyMap = validationErrorMap.getErrorMap();
    // messageMap contains message key and list of params
    Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
    Map<String, List<String>> validationErrors = this.errors;
    // iterating over the errorKeyMap
    for (Map.Entry<String, Map<String, List<String>>> entry : errorKeyMap.entrySet()) {
      // get messageMap for each key
      messageMap = entry.getValue();
      // localize and add to the validation errors object
      for (Map.Entry<String, List<String>> messageEntry : messageMap.entrySet()) {
        // params list to array of params
        List<String> paramList = messageEntry.getValue();
        String[] params = null;
        if (null != paramList) {
          params = paramList.toArray(new String[0]);
        }
        String message = messageEntry.getKey();
        message = messageUtil.getMessage(messageEntry.getKey(), params);

        if (validationErrors.get(entry.getKey()) != null) {
          validationErrors.get(entry.getKey()).add(message);
        } else {
          validationErrors.put(entry.getKey(), new ArrayList<String>(Arrays.asList(message)));
        }
      }
    }
  }

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  public Map<String, List<String>> getErrors() {
    return this.errors;
  }

  /**
   * Gets the formatted errors to display in a flash map. Used in generic
   * controller advice
   *
   * @return the formatted errors
   */
  public String getFormattedErrors() {

    Collection<List<String>> errors = getErrors().values();
    if (null == errors || errors.isEmpty()) {
      return null;
    }
    List<String> errorMessages = new ArrayList<String>();
    for (List<String> errorList : errors) {
      if (errorList == null || errorList.isEmpty()) {
        continue;
      }
      errorMessages.add(StringUtils.collectionToDelimitedString(errorList, "<br/>"));
    }
    return errorMessages.isEmpty() ? null : StringUtils.collectionToDelimitedString(errorMessages,
    "<br/>");
  }

  /**
   * Adds the error.
   *
   * @param error the error
   */
  public void addError(Map.Entry<String, String> error) {
    addError(error, null);
  }

  /**
   * Adds the error.
   *
   * @param error  the error
   * @param params the params
   */
  public void addError(Map.Entry<String, String> error, String[] params) {
    // hacked my way around to get map.entry object
    String key = error.getKey();
    List<String> value = Arrays.asList(error.getValue());
    HashMap<String, List<String>> errorMap = new HashMap<String, List<String>>();
    errorMap.put(key, value);
    // get the first entry in the map
    addListError(errorMap.entrySet().iterator().next(), params);
  }

  /**
   * Adds the list error.
   *
   * @param error the error
   */
  public void addListError(Map.Entry<String, List<String>> error) {
    addListError(error, null);
  }

  /**
   * Adds the list error.
   *
   * @param error  the error
   * @param params the params
   */
  public void addListError(Map.Entry<String, List<String>> error, String[] params) {
    List<String> listValue = error.getValue();
    // attempt to localize
    ListIterator<String> iterator = listValue.listIterator();
    while (iterator.hasNext()) {
      String value = iterator.next();
      iterator.set(messageUtil.getMessage(value, params));
    }
    this.errors.put(error.getKey(), listValue);
  }
}
