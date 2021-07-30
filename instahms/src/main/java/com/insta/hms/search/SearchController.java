package com.insta.hms.search;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class SearchController.
 *
 * @author aditya the controller for savedSearches
 */

@Controller
@RequestMapping(URLRoute.SAVED_SEARCHES_PATH)
public class SearchController extends BaseController {

  @Autowired
  private SearchService service;

  /**
   * List the saved searches.
   *
   * @param request the request
   * @return the model and view
   */
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest request) {

    Map<String, String[]> paramMap = request.getParameterMap();
    PagedList pagedList = service.getSavedSearches(paramMap);

    ModelAndView modelView = new ModelAndView();
    modelView.addObject("pagedList", pagedList);
    modelView.setViewName(URLRoute.SAVED_SEARCHES_LIST);
    return modelView;
  }

  /**
   * Show.
   *
   * @param request the request
   * @return the model and view
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest request) {
    String searchId = request.getParameter("search_id");
    BasicDynaBean bean = service.getSearch("search_id", searchId);
    ModelAndView modelView = new ModelAndView();
    if (bean != null) {
      modelView.addObject("bean", bean.getMap());
    } else {
      throw new EntityNotFoundException(
          new String[] { "exception.entity.not.found", "id", searchId });
    }
    modelView.setViewName(URLRoute.SAVED_SEARCHES_ADDSHOW);
    return modelView;
  }

  /**
   * Update.
   *
   * @param redirect the redirect
   * @param request the request
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(RedirectAttributes redirect, HttpServletRequest request)
      throws URISyntaxException {

    // TODO:Review the use of flash messages
    Map<String, String[]> params = request.getParameterMap();
    int success = service.updateSearch(params);
    String searchId = request.getParameter("search_id");
    if (success < 1) {
      throw new EntityNotFoundException(
          new String[] { "exception.entity.not.found", "id", searchId });
    }

    BasicDynaBean bean = service.getSearch("search_id", searchId);
    Map map = bean.getMap();
    redirect.mergeAttributes(map);
    redirect.mergeAttributes(UrlUtil.paramsToMap(SearchUtil.getSearchCriteria(params)));
    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
    flashMap.put("success", "updateSuccessful");
    return new ModelAndView(URLRoute.SAVED_SEARCHES_REDIRECT_TO_SHOW);
  }

  /**
   * Save search.
   *
   * @param request the request
   * @param redirect the redirect
   * @return the model and view
   * @throws URISyntaxException the URI syntax exception
   */
  @RequestMapping(value = "/saveSearch", method = RequestMethod.GET)
  public ModelAndView saveSearch(HttpServletRequest request, RedirectAttributes redirect) {
    // cannot test saved Search with json because saveSearch is GET
    HttpSession session = request.getSession(false);

    String userId = (String) session.getAttribute("userId");
    Map<String, String[]> parameters = request.getParameterMap();
    String queryParams = SearchUtil.getSearchCriteria(parameters);
    String actionId = parameters.get("_actionId")[0];
    BasicDynaBean bean = service.insertSearch(parameters, userId);

    if (bean != null) {

      String url = UrlUtil.buildURL(actionId, null, queryParams, null, null, false);
      redirect.addAttribute("_savedsearch", bean.get("search_name"));
      ModelAndView modelView = new ModelAndView();
      modelView.setViewName("redirect:/" + url);
      return modelView;
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.insert.failed",
          new String[] { "search" });
    }

  }

  /**
   * Gets the my search.
   *
   * @param request the request
   * @param redirect the redirect
   * @return the my search
   * @throws URISyntaxException the URI syntax exception
   */
  @RequestMapping(value = "/getMySearch", method = RequestMethod.GET)
  public ModelAndView getMySearch(HttpServletRequest request, RedirectAttributes redirect)
      throws URISyntaxException {
    Map<String, String[]> parameters = request.getParameterMap();
    String actionId = parameters.get("_actionId")[0];
    BasicDynaBean bean = service.getMySearch(parameters);
    if (bean == null) {
      throw new EntityNotFoundException(
          new String[] { "SavedSearch", "id", parameters.get("_mysearch")[0] });
    }
    String queryParams = (String) bean.get("query_params");
    String url = UrlUtil.buildURL(actionId, null, null, null, null, false);

    ModelAndView modelView = new ModelAndView();

    redirect.mergeAttributes(UrlUtil.paramsToMap(queryParams));
    redirect.addAttribute("_savedsearch", bean.get("search_name"));

    modelView.setViewName("redirect:/" + url);
    return modelView;
  }

  /**
   * Method to batch-close a set of consultations, given all the patient IDs that need to be closed.
   *
   * @param request the request
   * @param redirect the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/delete", method = RequestMethod.GET)
  public ModelAndView delete(HttpServletRequest request, RedirectAttributes redirect) {

    String[] deleteSearches = request.getParameterValues("_deleteSearch");

    // FlashMessages not showing up;have to fix
    if (service.batchDeleteSearches("search_id", deleteSearches)) {
      redirect.addFlashAttribute("success", "Deleted Successfully");
    } else {
      redirect.addFlashAttribute("error", "Failed to delete some searches");
    }

    ModelAndView modelView = new ModelAndView();
    String referrerView = UrlUtil.redirectToReferer(request);
    modelView.setViewName("redirect:" + referrerView);
    return modelView;
  }
}
