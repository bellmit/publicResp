package com.insta.instaapi.common;

import flexjson.JSONSerializer;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class ApiResponse {

  /**
   * Send error as response.
   * 
   * @param response    Response object
   * @param responseMap Map containing error data to be sent in response.
   * @param ex          Runtime exception that occurred because if which this response is being
   *                    dispatched
   * @throws IOException IO Exception
   */
  public static void sendErrorResponse(HttpServletResponse response,
      Map<String, Object> responseMap, RuntimeException ex) throws IOException {
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");

    if (ex instanceof ApiException.InvalidRequestTokenException) {
      responseMap.put("return_code", ((ApiException.InvalidRequestTokenException) ex).getErrCode());
      responseMap.put("return_message",
          ((ApiException.InvalidRequestTokenException) ex).getErrMsg());
    } else if (ex instanceof ApiException.MandatoryFieldsMissingException) {
      responseMap.put("return_code",
          ((ApiException.MandatoryFieldsMissingException) ex).getErrCode());
      responseMap.put("return_message",
          ((ApiException.MandatoryFieldsMissingException) ex).getErrMsg());
    } else if (ex instanceof ApiException.InvalidScreenRightsException) {
      responseMap.put("return_code", ((ApiException.InvalidScreenRightsException) ex).getErrCode());
      responseMap.put("return_message",
          ((ApiException.InvalidScreenRightsException) ex).getErrMsg());
    } else if (ex instanceof ApiException.DataNotFoundException) {
      responseMap.put("return_code", ((ApiException.DataNotFoundException) ex).getErrCode());
      responseMap.put("return_message", ((ApiException.DataNotFoundException) ex).getErrMsg());
    } else if (ex instanceof ApiException.InvalidDataException) {
      responseMap.put("return_code", ((ApiException.InvalidDataException) ex).getErrCode());
      responseMap.put("return_message", ((ApiException.InvalidDataException) ex).getErrMsg());
    } else {

      responseMap.put("return_code", "1024");
      responseMap.put("return_message", "Unknown error message");
    }
    responseMap.put("exception_message", ex.getMessage());

    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
  }

  /**
   * Send error as response.
   * 
   * @param response    Response object
   * @param responseMap Map containing error data to be sent in response.
   * @param ex          Exception that occurred because if which this response is being dispatched
   * @throws IOException IO Exception
   */
  public static void sendErrorResponse(HttpServletResponse response,
      Map<String, Object> responseMap, Exception ex) throws IOException {
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    responseMap.put("return_code", "1024");
    responseMap.put("return_message", "Un known error message");
    responseMap.put("exception_message", ex.getMessage());
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
  }

  /**
   * Send success as response.
   * 
   * @param response    Response object
   * @param responseMap Map containing data to be sent in response.
   * @throws IOException IO Exception
   */
  public static void sendSuccessResponse(HttpServletResponse response,
      Map<String, Object> responseMap) throws IOException {
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    responseMap.put("return_code", "2001");
    responseMap.put("return_message", "Success");
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
  }
}
