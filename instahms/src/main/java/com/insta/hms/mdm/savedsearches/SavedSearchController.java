package com.insta.hms.mdm.savedsearches;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class SavedSearchController.
 *
 * @author krishnat
 */
@RestController
@RequestMapping(URLRoute.SAVED_SEARCHES_PATH)
public class SavedSearchController extends BaseRestController {

  /** The saved search service. */
  @LazyAutowired SavedSearchService savedSearchService;

  /**
   * Gets the saved searches.
   *
   * @param req the req
   * @param resp the resp
   * @return the saved searches
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getSearches"}, method = RequestMethod.GET)
  public List<Map<String, Object>> getSavedSearches(
      HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
    String flowId = req.getParameter("flow_id");

    return savedSearchService.getSavedSearches(flowId);
  }

  /**
   * Gets the saved search.
   *
   * @param req the req
   * @param resp the resp
   * @return the saved search
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/show"}, method = RequestMethod.GET)
  public Map<String, Object> getSavedSearch(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    String searchId = req.getParameter("search_id");
    int id = 0;
    if (searchId == null || !searchId.equals("")) {
      id = Integer.parseInt(searchId);
    }
    Map<String, Object> map =
        id == 0 ? null : savedSearchService.getSavedSearch(Integer.parseInt(searchId));
    if (map == null) {
      throw new EntityNotFoundException(
          new String[] {"exception.entity.not.found", "id", searchId});
    } else {
      return map;
    }
  }

  /**
   * Update.
   *
   * @param redirect the redirect
   * @param request the request
   * @return the map
   * @throws URISyntaxException the URI syntax exception
   * @throws NumberFormatException the number format exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @RequestMapping(value = "/updateSearch", method = RequestMethod.POST)
  public Map<String, Object> update(RedirectAttributes redirect, HttpServletRequest request)
      throws URISyntaxException, NumberFormatException, UnsupportedEncodingException {

    Map<String, String[]> params = request.getParameterMap();
    int success = savedSearchService.updateSearch(params);
    String searchId = request.getParameter("search_id");
    if (success < 1) {
      throw new EntityNotFoundException(
          new String[] {"exception.entity.not.found", "id", searchId});
    }

    return savedSearchService.getSavedSearch(Integer.parseInt(searchId));
  }

  /**
   * Save search.
   *
   * @param request the request
   * @return the map
   * @throws URISyntaxException the URI syntax exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @RequestMapping(value = "/saveSearch", method = RequestMethod.POST)
  public Map<String, Object> saveSearch(HttpServletRequest request)
      throws URISyntaxException, UnsupportedEncodingException {
    HttpSession session = request.getSession(false);

    String userId = (String) session.getAttribute("userId");
    Map<String, String[]> parameters = request.getParameterMap();
    BasicDynaBean bean = savedSearchService.insertSearch(parameters, userId);

    if (bean != null) {
      SearchParameters filter = savedSearchService.getInstance((String) bean.get("flow_id"));
      return savedSearchService.getSavedSearch(bean, filter);
    } else {
      throw new HMSException(
          HttpStatus.BAD_REQUEST, "exception.insert.failed", new String[] {"Filter"});
    }
  }

  /**
   * Delete search.
   *
   * @param req the req
   * @param res the res
   * @return the map
   * @throws URISyntaxException the URI syntax exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  @RequestMapping(value = "/deleteSearch", method = RequestMethod.POST)
  public Map<String, Object> deleteSearch(HttpServletRequest req, HttpServletResponse res)
      throws URISyntaxException, UnsupportedEncodingException {
    String searchId = req.getParameter("search_id");
    int id = 0;
    if (searchId == null || !searchId.equals("")) {
      id = Integer.parseInt(searchId);
    }

    if (id == 0 || savedSearchService.deleteSearch(id) < 1) {
      throw new EntityNotFoundException(
          new String[] {"exception.entity.not.found", "id", searchId});
    } else {
      res.setStatus(HttpServletResponse.SC_ACCEPTED);
      return new HashMap<String, Object>();
    }
  }
}
