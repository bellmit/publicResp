package com.insta.hms.common.http;

import java.util.Map;

public class HttpMessage {

  private HttpResponseHandler responseHandler;
  private int timeout;
  private String httpMethodType;
  private String apiURL;
  private String successResponse;
  private String paramData;
  private Map<String, String> headerMap;

  public HttpResponseHandler getResponseHandler() {
    return responseHandler;
  }

  public void setResponseHandler(HttpResponseHandler responseHandler) {
    this.responseHandler = responseHandler;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getHttpMethodType() {
    return httpMethodType;
  }

  public void setHttpMethodType(String httpMethodType) {
    this.httpMethodType = httpMethodType;
  }

  public String getApiURL() {
    return apiURL;
  }

  public void setApiURL(String apiURL) {
    this.apiURL = apiURL;
  }

  public String getParamData() {
    return paramData;
  }

  public void setParamData(String paramData) {
    this.paramData = paramData;
  }

  public Map<String, String> getHeaderMap() {
    return headerMap;
  }

  public void setHeaderMap(Map<String, String> headerMap) {
    this.headerMap = headerMap;
  }

  public String getSuccessResponse() {
    return successResponse;
  }

  public void setSuccessResponse(String successResponse) {
    this.successResponse = successResponse;
  }
}
