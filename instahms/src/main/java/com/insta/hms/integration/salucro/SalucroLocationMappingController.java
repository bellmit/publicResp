package com.insta.hms.integration.salucro;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SalucroController.
 */
@RestController
@RequestMapping(SalucroConstants.SALUCRO_LOCATION)
public class SalucroLocationMappingController extends BaseRestController {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(SalucroLocationMappingController.class);

  /** The salucro util. */
  @LazyAutowired
  private SalucroUtil salucroUtil;

  /** The service. */
  @LazyAutowired
  private SalucroService service;

  /**
   * Gets the salucro configurations index page.
   *
   * @return the salucro configurations index page
   */
  @IgnoreConfidentialFilters
  @GetMapping(SalucroConstants.INDEX)
  public ModelAndView getSalucroLocationMappingIndexPage() {
    return renderMasterUi("Master", "integrationMasters");
  }

  /**
   * Fetch Locations.
   *
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/locations")
  public ResponseEntity<Map<String, Object>> fetchLocations() throws Exception {
    Map<String, Object> responseMap = null;
    responseMap = service.fetchSalucroDetails();
    if ( responseMap == null || responseMap.isEmpty() ) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return new ResponseEntity<>(responseMap,
        HttpStatus.OK);
  }

  /**
   * Creates the salucro role to user mapping.
   *
   * @param requestBody the request body
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = "/create")
  public  Map<String, Object> createSalucroLocationToCounterMapping(
      @RequestBody ModelMap requestBody) {
    return service.insertCounterLocationMappingDetails(requestBody);
  }

  /**
   * Update salucro role to user mapping.
   *
   * @param requestBody the request body
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = "/update")
  public  Map<String, Object> updateSalucroLocationToCounterMapping(
       @RequestBody ModelMap requestBody) {
    return service.updateCounterLocationMappingDetails(requestBody);
  }

  /**
   * Delete salucro location to counter mapping.
   *
   * @param requestBody the request body
   * @return the map
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = "/delete")
  public  Map<String, Object> deleteSalucroLocationToCounterMapping(
       @RequestBody ModelMap requestBody) {
    return service.deleteLocationToCounterMapping(requestBody);
  }

  /**
   * List by center.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/list")
  public ResponseEntity<Map<String, Object>> listByCounter(
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map paramMap = request.getParameterMap();
    PagedList pagedList = service.searchCounter(
        paramMap, ConversionUtils.getListingParameter(paramMap));
    if ( pagedList == null ) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }
}