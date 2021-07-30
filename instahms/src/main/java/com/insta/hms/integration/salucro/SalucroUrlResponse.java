package com.insta.hms.integration.salucro;

import java.util.Map;

/**
 * The Class SalucroUrlResponse.
 * POJO representation of response received from Salucro.
 * This includes only the fields that we require
 */
public class SalucroUrlResponse {

  /** The result. */
  Map<String, Object> result;

  /** The payload. */
  Map<String, Object> payload;

  /** The error. */
  Map<String, Object> error;

  /**
   * Gets the error.
   *
   * @return the error
   */
  public Map<String, Object> getError() {
    return error;
  }

  /**
   * Sets the error.
   *
   * @param error the error
   */
  public void setError(Map<String, Object> error) {
    this.error = error;
  }

  /**
   * Gets the result.
   *
   * @return the result
   */
  public Map<String, Object> getResult() {
    return result;
  }

  /**
   * Sets the result.
   *
   * @param result the result
   */
  public void setResult(Map<String, Object> result) {
    this.result = result;
  }

  /**
   * Gets the payload.
   *
   * @return the payload
   */
  public Map<String, Object> getPayload() {
    return payload;
  }

  /**
   * Sets the payload.
   *
   * @param payload the payload
   */
  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "SalucroUrlResponse [result=" + result + ","
         + " payload=" + payload + ", error=" + error + "]";
  } 
}