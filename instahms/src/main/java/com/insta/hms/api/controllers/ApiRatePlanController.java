package com.insta.hms.api.controllers;

import com.insta.hms.api.services.ApiRatePlanService;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/rateplan")
public class ApiRatePlanController extends BaseRestController {


  /**
  * The Constant BAD_REQUEST.
  */
  public static final String BAD_REQUEST = "exception.bad.request";

  /** The Rate Plan API Service. */
  @LazyAutowired
  private ApiRatePlanService ratePlanService;

  /**
   * Get List of Rate Plan.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> search(HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> map = new HashMap<>();
    map = ratePlanService.getRatePlans(parameters);
    if (map.get("return_code").equals("1021")) {
      return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
     * Get rateplan by rateplanId.
     *
     * @param ratePlanId  the ratePlanId
     * @param request  the request
     * @param response the response
     * @return the response entity
   */
  @RequestMapping(value = "/{ratePlanId}", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getRatePlanById(@PathVariable String ratePlanId,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    Map<String, Object> map = new HashMap<>();
    map = ratePlanService.getRatePlanById(ratePlanId);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
}

