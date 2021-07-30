package com.insta.hms.api.controllers;

import com.insta.hms.api.services.ApiReferralDoctorService;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/referraldoctors")
public class ApiReferralDoctorController extends BaseRestController {

  /**
   * The Constant BAD_REQUEST.
   */
  public static final String BAD_REQUEST = "exception.bad.request";

  /**
   * The referral doctor service.
   */
  @LazyAutowired
  private ApiReferralDoctorService service;

  /**
   * Get List of Referral Doctors.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getReferrals(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> map = new HashMap<>();
    map = service.getReferralsList(parameters);
    if (map.get("return_code").equals("1021")) {
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * Get Referral Doctor by referralId.
   *
   * @param referralId  the referralId
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/{referralId}", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getReferralById(@PathVariable String referralId,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, Object> map = new HashMap<>();
    map = service.getReferralById(referralId);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
}
