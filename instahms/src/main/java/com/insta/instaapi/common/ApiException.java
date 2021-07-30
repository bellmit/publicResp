package com.insta.instaapi.common;

public class ApiException {

  public static class InvalidRequestTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidRequestTokenException(String msg) {
      super(msg);
    }

    // This message is written based on previous written API. Needs to be evaluate and modified

    private static final String ERROR_MSG = "invalid request token,please login again";
    private static final String ERROR_CODE = "1001";

    public String getErrCode() {
      return ERROR_CODE;
    }

    public String getErrMsg() {
      return ERROR_MSG;
    }

  }

  public static class MandatoryFieldsMissingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MandatoryFieldsMissingException(String ex) {
      super(ex);
    }

    // This message is written based on previous written API. Needs to be evaluate and modified

    private static final String ERROR_MSG = "Mandatory fields are not supplied";
    private static final String ERROR_CODE = "1002";

    public String getErrCode() {
      return ERROR_CODE;
    }

    public String getErrMsg() {
      return ERROR_MSG;
    }
  }

  public static class InvalidScreenRightsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidScreenRightsException(String msg) {
      super(msg);
    }

    // This message is written based on previous written API. Needs to be evaluate and modified

    private static final String ERROR_MSG = "Permission Denied. Please check with Administrator";
    private static final String ERROR_CODE = "1003";

    public String getErrCode() {
      return ERROR_CODE;
    }

    public String getErrMsg() {
      return ERROR_MSG;
    }
  }

  public static class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidDataException(String msg) {
      super(msg);
    }

    // This message is written based on previous written API. Needs to be evaluate and modified

    private static final String ERROR_MSG = "Given values are invalid";
    private static final String ERROR_CODE = "1023";

    public String getErrCode() {
      return ERROR_CODE;
    }

    public String getErrMsg() {
      return ERROR_MSG;
    }

  }

  public static class DataNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataNotFoundException(String msg) {
      super(msg);
    }

    // This message is written based on previous written API. Needs to be evaluate and modified

    private static final String ERROR_MSG = "Requested field data is not available in DB";
    private static final String ERROR_CODE = "1021";

    public String getErrCode() {
      return ERROR_CODE;
    }

    public String getErrMsg() {
      return ERROR_MSG;
    }
  }

}
