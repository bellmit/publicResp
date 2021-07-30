package com.insta.hms.eservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class EResponse.
 */
public abstract class EResponse {

  /**
   * The response code.
   */
  protected int responseCode;

  /**
   * The error message.
   */
  protected String errorMessage;

  /**
   * The content.
   */
  protected Object content;

  /**
   * The extra params.
   */
  protected Object[] extraParams;

  /**
   * Instantiates a new e response.
   *
   * @param responseCode the response code
   * @param errorMessage the error message
   * @param content      the content
   */
  public EResponse(int responseCode, String errorMessage, Object content) {
    super();
    this.responseCode = responseCode;
    this.errorMessage = errorMessage;
    this.content = content;
  }

  /**
   * Instantiates a new e response.
   *
   * @param responseCode the response code
   * @param errorMessage the error message
   * @param content      the content
   * @param extraParams  the extra params
   */
  public EResponse(int responseCode, String errorMessage, Object content, Object[] extraParams) {
    super();
    this.responseCode = responseCode;
    this.errorMessage = errorMessage;
    this.content = content;
    this.extraParams = extraParams;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the error report.
   *
   * @return the error report
   */
  public Object getErrorReport() {
    if (responseCode < 0) {
      return content;
    }
    return null;
  }

  /**
   * Gets the response code.
   *
   * @return the response code
   */
  public int getResponseCode() {
    return responseCode;
  }

  /**
   * Checks if is error.
   *
   * @return true, if is error
   */
  public boolean isError() {
    return responseCode < 0;
  }

  /**
   * Gets the result params.
   *
   * @return the result params
   */
  public Object[] getResultParams() {
    return extraParams;
  }

  /**
   * Gets the input stream.
   *
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public InputStream getInputStream() throws IOException {
    Object[] params = getResultParams();
    if (null != content && !String.valueOf(content).isEmpty()) {
      return getResultStream(content, params);
    }
    return null;
  }

  /**
   * Gets the result stream.
   *
   * @param content      the content
   * @param resultParams the result params
   * @return the result stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected InputStream getResultStream(Object content, Object[] resultParams) throws IOException {
    if (content instanceof InputStream) {
      return (InputStream) content;
    }
    if (content instanceof byte[]) {
      return toStream((byte[]) content);
    }
    if (content instanceof String) {
      return toStream((String) content);
    }
    return null;
  }

  /**
   * To stream.
   *
   * @param bytes the bytes
   * @return the input stream
   */
  private static InputStream toStream(byte[] bytes) {
    return new ByteArrayInputStream(bytes);
  }

  /**
   * To stream.
   *
   * @param result the result
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static InputStream toStream(String result) throws IOException {
    return new ByteArrayInputStream(result.getBytes("UTF-8"));
  }
}
