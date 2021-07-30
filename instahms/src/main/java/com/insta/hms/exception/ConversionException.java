package com.insta.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ConversionException.
 *
 * @author aditya
 *     ConversionException is meant to be used to show the
 *     errorFields object created in @ConversionUtils while converting from
 *     parameters to dynabean.
 */
public class ConversionException extends HMSException {

  /**
   * Eclipse generated serialVersionUID.
   */
  private static final long serialVersionUID = -4722324385770093927L;

  /**
   * The Constant defaultStatus.
   */
  private static final HttpStatus defaultStatus = HttpStatus.BAD_REQUEST;

  /**
   * The Constant defaultKey.
   */
  private static final String defaultKey = "exception.conversion";

  /**
   * The fields.
   */
  /*
   * Maintains Conversion fields; gets the fields where there was an error while
   * converting in conversionUtils. No need to localize as field names cannot be
   * localized.
   */
  private List<String> fields = new ArrayList<String>();

  /**
   * Instantiates a new conversion exception.
   *
   * @param messageKey the message key
   * @param params     the params
   */
  public ConversionException(String messageKey, String[] params) {
    super(defaultStatus, messageKey, params);
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param fields the fields
   */
  public ConversionException(List<String> fields) {
    this();
    this.fields.addAll(fields);
  }

  /**
   * Gets the fields.
   *
   * @return the fields
   */
  public List<String> getFields() {
    return this.fields;
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param params the params
   */
  public ConversionException(String[] params) {
    this(defaultKey, params);
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param messageKey the message key
   */
  public ConversionException(String messageKey) {
    this(messageKey, null);
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param cause the cause
   */
  public ConversionException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new conversion exception.
   */
  public ConversionException() {
    this(defaultKey, null);
  }

}