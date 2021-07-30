package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.integration.item.StoreItemDetailIntegrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/storeitem")
public class APIStoresItemController extends BaseRestController {

  @LazyAutowired
  private StoreItemDetailIntegrationService storeItemDetailsIntegrationService;

  private static final String BAD_REQUEST = "exception.bad.request";
  private static final String HTTP_STATUS = "http-status";

  /**
   * item create end point.
   * @param request the request
   * @param response the response
   * @param requestBody the requestBody
   * @return ResponseEntity
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> create(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    Map<String, Object> responseData = null;
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    requestBody.addAttribute("endPoint", "API");
    responseData = storeItemDetailsIntegrationService.createItem(requestBody);
    HttpStatus status = (HttpStatus) responseData.get(HTTP_STATUS);
    responseData.remove(HTTP_STATUS);
    return new ResponseEntity<>(responseData, status);
  }

  /**
   * item update end point.
   * @param request the request
   * @param response the response
   * @param requestBody the requestBody
   * @return ResponseEntity
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> update(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    Map<String, Object> responseData = null;
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    requestBody.addAttribute("endPoint", "API");
    responseData = storeItemDetailsIntegrationService.updateItem(requestBody);
    HttpStatus status = (HttpStatus) responseData.get(HTTP_STATUS);
    responseData.remove(HTTP_STATUS);
    return new ResponseEntity<>(responseData, status);
  }
  
}
