package com.insta.hms.api.controllers;

import com.insta.hms.api.services.ApiInsurancePlanService;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/insuranceplans")
public class ApiInsurancePlanController extends BaseRestController {

  /** The Rate Plan API Service. */
  @LazyAutowired
  private ApiInsurancePlanService service;

  /**
   * Get List of Insurance Plan.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getInsurancePlan(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> map = service.getInsurancePlans(parameters);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * Get Insurance Plan by planId.
   *
   * @param planId  the plan id
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/planid/{planId}", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getInsurancePlanByPlanId(@PathVariable String planId ,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> map = service.getInsurancePlanByPlanId(planId,parameters);
    if (map.get("return_code").equals("1021")) {
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * Get Insurance Plan by planCode.
   *
   * @param planCode  the plan code
   * @param request  the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/plancode/{planCode}", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getInsurancePlanByPlanCode(
      @PathVariable String planCode,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> map = service.getInsurancePlanByPlanCode(planCode,parameters);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
}
