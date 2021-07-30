package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.lowagie.text.DocumentException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/registration")
public class RegistrationController extends BaseRestController {

  /** The Constant BAD_REQUEST. */
  public static final String BAD_REQUEST = "exception.bad.request";

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * pre Registration API.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/preregistration", method = RequestMethod.POST,
      consumes = "application/json")
  public ResponseEntity<Map<String, Object>> preregistration(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) throws Exception {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    Map<String, Object> map = null;
    try {
      registrationService.obtainLock();
      map = registrationService.preRegistration(requestBody);
    } finally {
      registrationService.releaseLock();
    }
    if (map.get("error_message") != null) {
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
  
  /**
   * Creates the visit.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the response entity
   * @throws Exception the exception
   */
  @RequestMapping(value = "/createVisit", method = RequestMethod.POST,
      consumes = "application/json")
  public ResponseEntity<Map<String, Object>> createVisit(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) throws Exception {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    Map<String, Object> map = null;
    try {
      registrationService.obtainLock();
      map = registrationService.apiCreateVisit(requestBody);
    } finally {
      registrationService.releaseLock();
    }
    if (map.get("error_message") != null) {
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

}
