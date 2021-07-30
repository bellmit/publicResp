package com.insta.hms.core.scheduler.resourcelist;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.scheduler.ResourceCategory;
import com.insta.hms.core.scheduler.URLRoute;
import com.insta.hms.mdm.departments.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerResourceSearchController.
 */
@RestController
@RequestMapping(URLRoute.RESOURCE_INDEX_URL)
public class SchedulerResourceSearchController extends BaseRestController {

  /** The scheduler resource search service. */
  @LazyAutowired
  SchedulerResourceSearchService schedulerResourceSearchService;

  /** The department service. */
  @LazyAutowired
  private DepartmentService departmentService;

  /**
   * List.
   *
   * @param req the req
   * @param resp the resp
   * @return the map
   * @throws ParseException the parse exception
   * @throws NumberFormatException the number format exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
  public Map<String, Object> list(HttpServletRequest req, HttpServletResponse resp)
      throws ParseException, NumberFormatException, UnsupportedEncodingException {

    return schedulerResourceSearchService.getSecondaryResourcesList(req.getParameterMap(), false);
  }
  
  /**
   * Gets the default resources.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the default resources
   * @throws ParseException
   *           the parse exception
   * @throws NumberFormatException
   *           the number format exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/getDefaultResources" }, method = RequestMethod.GET)
  public Map<String, Object> getDefaultResources(HttpServletRequest req, 
      HttpServletResponse resp) {

    return schedulerResourceSearchService
        .getDefaultAdditionalResources(req.getParameterMap());
  }

  /**
   * Advance search.
   *
   * @param req the req
   * @param resp the resp
   * @return the map
   * @throws ParseException the parse exception
   * @throws NumberFormatException the number format exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/advanceList" }, method = RequestMethod.GET)
  public Map<String, Object> advanceSearch(HttpServletRequest req, HttpServletResponse resp)
      throws ParseException, NumberFormatException, UnsupportedEncodingException {
    return schedulerResourceSearchService.getSecondaryResourcesList(req.getParameterMap(), true);
    // return patientSearchService.getPatientsAdvanced(req.getParameterMap());
  }

  /**
   * Gets the metadata.
   *
   * @param request the request
   * @param response the response
   * @return the metadata
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/metadata" }, method = RequestMethod.GET)
  public Map<String, Object> getMetadata(HttpServletRequest request, HttpServletResponse response) {
    return schedulerResourceSearchService.getMetadata(request.getParameterMap(),
        (String) request.getAttribute("language"));
  }

  /**
   * Gets the filter data.
   *
   * @param req the req
   * @param resp the resp
   * @return the filter data
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/getFilterData" }, method = RequestMethod.GET)
  public Map<String, Object> getFilterData(HttpServletRequest req, HttpServletResponse resp) {
    return schedulerResourceSearchService.getFilterData();
  }

  /**
   * Do lookup.
   *
   * @param params the params
   * @return the map
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/lookup" }, method = RequestMethod.GET)
  public Map<String, Object> doLookup(@RequestParam MultiValueMap<String, String> params) {
    return schedulerResourceSearchService.lookup(params);
  }

  /**
   * Gets the sponsor info for last active visit.
   *
   * @param request the request
   * @param response the response
   * @return the sponsor info for last active visit
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/getSponsorInfoForLastActiveVisit" }, method = RequestMethod.GET)
  public Map<String, Object> getSponsorInfoForLastActiveVisit(HttpServletRequest request,
      HttpServletResponse response) {
    return schedulerResourceSearchService.getSponsorInfoForLastActiveVisit(request
        .getParameterMap());
  }

  /**
   * Gets the additional resources by resource type.
   *
   * @param request the request
   * @param response the response
   * @return the additional resources by resource type
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/resourceAvailabilities" }, method = RequestMethod.GET)
  public Map<String, Object> getAdditionalResourcesByResourceType(HttpServletRequest request,
      HttpServletResponse response) {
    return schedulerResourceSearchService.getAdditionalResourcesByResourceType(request
        .getParameterMap());
  }

  /**
   * Gets the complaint types by name.
   *
   * @param request the request
   * @param response the response
   * @return the complaint types by name
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings("unchecked")
  @RequestMapping(value = { "/complainttype/autocomplete" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getComplaintTypesByName(HttpServletRequest request,
      HttpServletResponse response) {
    return new ResponseEntity<Map<String, Object>>(
        schedulerResourceSearchService.getComplaintTypes(request.getParameterMap()), HttpStatus.OK);
  }

  /**
   * Gets the available resources.
   *
   * @param request the request
   * @param response the response
   * @return the available resources
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = { "/getAvailableResources" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getAvailableResources(HttpServletRequest request,
      HttpServletResponse response) {
    return new ResponseEntity<Map<String, Object>>(
        schedulerResourceSearchService.getAvailableResources(request.getParameterMap()),
        HttpStatus.OK);
  }

  /**
   * Gets the departments.
   *
   * @param request the request
   * @param response the response
   * @return the departments
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/departments" }, method = RequestMethod.GET)
  public Map<String, Object> getDepartments(HttpServletRequest request, 
      HttpServletResponse response) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("departments", departmentService.getAllDepartmentsData(true));
    return dataMap;
  }

  /**
   * Gets the previous appt details.
   *
   * @param request the request
   * @param response the response
   * @return the previous appt details
   */
  @RequestMapping(value = "/previousappt", method = RequestMethod.GET)
  public Map<String, Object> getPreviousApptDetails(HttpServletRequest request,
      HttpServletResponse response) {
    return schedulerResourceSearchService.getPreviousAppt(request.getParameterMap());
  }
  
  /**
   * Gets the additional resources types.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the additional resources types
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getAdditionalResoucesTypes", method = RequestMethod.GET)
  public Map<String, Object> getAdditionalResourcesTypes(HttpServletRequest request,
      HttpServletResponse response) {
    Map resultMap = new HashMap();
    Map<String, String[]> params = request.getParameterMap();
    resultMap.put("additional_resource_types", schedulerResourceSearchService
        .getAdditionalResourcesTypes(params.get("res_sch_category") != null ? params
            .get("res_sch_category")[0] : ResourceCategory.DOC.name()));
    return resultMap;
  }
}
