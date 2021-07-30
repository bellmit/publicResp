/**
 * 
 */

package com.insta.hms.core.patient.inpatientlist;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class InPatientSearchController.
 *
 * @author anup vishwas
 */

@RestController
@RequestMapping(URLRoute.IN_PATIENT_INDEX_URL)
public class InPatientSearchController extends BaseRestController {

  /** The in patient search service. */
  @LazyAutowired
  InPatientSearchService inPatientSearchService;

  /**
   * List.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the map
   * @throws ParseException
   *           the parse exception
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
  public Map<String, Object> list(HttpServletRequest req, HttpServletResponse resp)
      throws ParseException, UnsupportedEncodingException {

    return inPatientSearchService.getPatientsList(req.getParameterMap());
  }

  /**
   * Gets the filter data.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the filter data
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/getFilterData" }, method = RequestMethod.GET)
  public Map<String, Object> getFilterData(HttpServletRequest req, HttpServletResponse resp) {

    return inPatientSearchService.getFilterData();
  }

  /**
   * Gets the advance filter.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the advance filter
   * @throws UnsupportedEncodingException
   *           the unsupported encoding exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/advanceFilterList" }, method = RequestMethod.GET)
  public Map<String, Object> getAdvanceFilter(HttpServletRequest req, HttpServletResponse res)
      throws UnsupportedEncodingException, ParseException {

    return inPatientSearchService.getPatientsAdvanced(req.getParameterMap());
  }
}
