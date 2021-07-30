package com.insta.hms.mdm.regions;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class RegionController.
 */
@Controller
@RequestMapping(URLRoute.REGION_MASTER)
public class RegionController extends BaseController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RegionController.class);

  /** The region service. */
  @LazyAutowired
  private RegionService regionService;

  /**
   * List.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @return the model and view
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    Map<String, String[]> parameters = request.getParameterMap();
    List<BasicDynaBean> bean = regionService.getAllRegions();
    List<BasicDynaBean> regionsList = ConversionUtils.listBeanToListMap(bean);
    Map<String, Object> referenceMap = new HashMap<>();
    referenceMap.put("regionsList", regionsList);
    mav.addObject("referenceData", referenceMap);

    PagedList pagedList = regionService.getRegionMasterDetails(parameters);
    List list = ConversionUtils.listBeanToListMap(pagedList.getDtoList());
    pagedList.setDtoList(list);
    mav.addObject("pagedList", pagedList);
    mav.setViewName(URLRoute.REGION_MASTER_LIST);
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /**
   * Adds the.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    List<BasicDynaBean> regionsList = regionService.getAllRegions();
    mav.addObject("referenceData", regionsList);
    mav.setViewName(URLRoute.REGION_MASTER_ADD);
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /**
   * Creates the.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {
    Map<String, String[]> parameters = request.getParameterMap();
    BasicDynaBean regionBean = regionService.insertRegion(parameters);
    Map regionMap = regionBean.getMap();
    redirect.mergeAttributes(regionMap);
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.REGION_MASTER_REDIRECT_TO_SHOW);
  }

  /**
   * Show.
   *
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String regionId = req.getParameter("region_id");
    BasicDynaBean bean = regionService.getRegion("region_id", regionId);

    List<BasicDynaBean> regionsList = regionService.getAllRegions();
    mav.addObject("referenceData", regionsList);

    if (bean == null) {
      throw new EntityNotFoundException(new String[] { "Region", "id", regionId });
    }
    mav.addObject("bean", bean.getMap());
    mav.setViewName(URLRoute.REGION_MASTER_SHOW);
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /**
   * Update.
   *
   * @param request
   *          the request
   * @param redirect
   *          the redirect
   * @return the model and view
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest request, RedirectAttributes redirect) {

    Map<String, String[]> parameters = request.getParameterMap();

    int success = regionService.updateRegion(parameters);
    String regionId = parameters.get("region_id")[0];
    if (success < 1) {
      throw new EntityNotFoundException(new String[] { "Region", "id", regionId });
    }

    BasicDynaBean regionBean = regionService.getRegion("region_id", regionId);

    Map regionMap = regionBean.getMap();
    redirect.mergeAttributes(regionMap);
    return new ModelAndView(URLRoute.REGION_MASTER_REDIRECT_TO_SHOW);
  }

  /**
   * Lookup.
   *
   * @param request
   *          the request
   * @return the model map
   */
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  public ModelMap lookup(HttpServletRequest request) {

    Map<String, String[]> parameters = request.getParameterMap();
    List<String> filterSet = regionService.lookupRegionName(parameters);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", filterSet);
    modelMap.addAttribute("listSize", filterSet.size());
    return modelMap;
  }
}
