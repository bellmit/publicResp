
package com.insta.hms.common.preferences;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.ReferenceDataConverter;
import com.insta.hms.mdm.ResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * the controller for single-record table like generic preferences.
 *
 * @author aditya.b
 */
public class PreferencesController extends BaseController {

  /** The service. */
  private PreferencesService service;
  
  /** The router. */
  private ResponseRouter router;

  /** The converter. */
  @LazyAutowired
  private ReferenceDataConverter converter;

  /**
   * Instantiates a new preferences controller.
   *
   * @param service the service
   * @param router the router
   */
  public PreferencesController(PreferencesService service, ResponseRouter router) {
    this.service = service;
    this.router = router;
  }

  /**
   * Gets the service.
   *
   * @return the service
   */
  protected PreferencesService getService() {
    return service;
  }

  /**
   * Show.
   *
   * @param request the request
   * @param response the response
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/show", "" }, method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {

    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = service.getAllPreferences();
    Map<String, String[]> paramMap = request.getParameterMap();
    if (null != bean) {
      modelView.addObject("bean", bean.getMap());

    }
    modelView.addAllObjects(getReferenceData(paramMap));
    modelView.setViewName(router.route("show"));
    return modelView;
  }

  /**
   * Update.
   *
   * @param request the request
   * @param response the response
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirectAttributes) {

    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = request.getParameterMap();
    BasicDynaBean bean = mapToBean(params, super.getFiles(request));
    Integer ret = service.update(bean);
    if (ret != 0) {
      redirectAttributes.mergeAttributes(bean.getMap());
    }
    modelView.setViewName(router.route("update"));
    return modelView;
  }

  /**
   * Map to bean.
   *
   * @param params the params
   * @return the basic dyna bean
   */
  protected BasicDynaBean mapToBean(Map<String, String[]> params) {
    return mapToBean(params, null);
  }

  /**
   * Map to bean.
   *
   * @param params the params
   * @param fileMap the file map
   * @return the basic dyna bean
   */
  protected BasicDynaBean mapToBean(Map<String, String[]> params,
      Map<String, MultipartFile> fileMap) {
    return service.toBean(params, fileMap);
  }

  /**
   * Gets the reference lists.
   *
   * @param params the params
   * @return the reference lists
   */
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map<String, String[]> params) {
    return Collections.emptyMap();
  }

  /**
   * Gets the reference data.
   *
   * @param paramMap the param map
   * @return the reference data
   */
  protected Map<String, List<Map>> getReferenceData(Map<String, String[]> paramMap) {
    Map<String, List<BasicDynaBean>> lookupMaps = getReferenceLists(paramMap);
    return converter.convert(lookupMaps);
  }

}
