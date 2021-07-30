package com.insta.hms.mdm.resourceavailability;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ResourceAvailabilityController.
 */
@Controller
@RequestMapping(URLRoute.RESOURCE_AVAILABILITY_PATH)
public class ResourceAvailabilityController extends MasterController {
  
  /** The category service. */
  @LazyAutowired
  private ResourceAvailabilityService categoryService;
  
  /** The center service. */
  @LazyAutowired
  private CenterService centerService;
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ResourceAvailabilityController.class);

  /**
   * Instantiates a new resource availability controller.
   *
   * @param service the service
   */
  public ResourceAvailabilityController(ResourceAvailabilityService service) {
    super(service, MasterResponseRouter.RESOURCE_AVAILABILITY_ROUTER);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((ResourceAvailabilityService) getService()).getListPageData(params);
  }

  @Override
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    Map params = req.getParameterMap();
    mav.addObject("centersJSON",
        ConversionUtils.listBeanToListMap(centerService.getAllCentersDetails()));

    if (req.getParameter("res_sch_type") != null && !req.getParameter("res_sch_type").equals("")) {

      String resourceType = req.getParameter("res_sch_type");
      mav.addObject("res_sch_type", resourceType);
      String category = null;

      int resourceSchId = 0;

      categoryService.setAttributes("", resourceType, resourceSchId, mav);
      if (resourceType != null && !resourceType.equals("")) {
        if (resourceType.equals("DOC")) {
          category = "DOC";
        } else if (resourceType.equals("SER") || resourceType.equals("SRID")) {
          category = "SNP";
        } else if (resourceType.equals("TST") || resourceType.equals("EQID")) {
          category = "DIA";
        } else if (resourceType.equals("THID") || resourceType.equals("SUR")) {
          category = "OPE";
        } else {
          category = "BED";
        }
      }
      mav.addObject("category", category);

    }
    boolean primaryCategory = false;
    if (req.getParameter("resource_type") != null
        && req.getParameter("resource_type").toString().equals("primary")) {
      primaryCategory = true;
    }
    List<BasicDynaBean> listBean = categoryService.getCategoryDescription(primaryCategory);
    if (listBean != null) {
      mav.addObject("categoryDescripton", listBean);
    }
    mav.setViewName(router.route("add"));
    return mav;
  }

  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String category = null;
    String categoryDuration = null;
    int resSchId = Integer.parseInt(req.getParameter("res_sch_id"));
    String resourceType = req.getParameter("res_sch_type");
    BasicDynaBean bean = categoryService.getCategoryDetails(resSchId);
    if (bean != null) {
      category = (String) bean.get("res_sch_category");
    }
    if (req.getParameter("category_default_duration") != null) {
      categoryDuration = req.getParameter("category_default_duration");
    }
    List<BasicDynaBean> listBean = categoryService.getCategoryDescription(false);
    categoryService.setAttributes("", resourceType, resSchId, mav);
    if (listBean != null) {
      mav.addObject("categoryDescripton", listBean);
    }
    mav.addObject("category", bean.get("res_sch_category"));
    mav.addObject("resource_type", bean.get("res_sch_type"));
    mav.addObject("centersJSON",
        ConversionUtils.listBeanToListMap(centerService.getAllCentersDetails()));
    mav.addObject("bean", bean);
    mav.addObject("category_default_duration", bean.get("default_duration"));
    mav.setViewName(router.route("show"));
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {
    Map messageMap = new HashMap();
    Map params = request.getParameterMap();

    messageMap = categoryService.saveCategoryDetails(params);

    redirect.addAttribute("res_sch_id", messageMap.get("res_sch_id"));
    redirect.addAttribute("res_sch_type", request.getParameter("res_sch_type"));
    redirect.addAttribute("res_sch_name", request.getParameter("schedulerName"));
    redirect.addAttribute("category_default_duration",
        Integer.parseInt(request.getParameter("category_default_duration")));
    if (messageMap.get("warningMessage") != null) {
      redirect.addFlashAttribute("info", messageMap.get("warningMessage"));
    }
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) throws ValidationException {
    Map messageMap = new HashMap();
    Map params = request.getParameterMap();

    messageMap = categoryService.saveCategoryDetails(params);

    redirect.addAttribute("res_sch_id", messageMap.get("res_sch_id"));
    redirect.addAttribute("res_sch_type", request.getParameter("res_sch_type"));
    redirect.addAttribute("res_sch_name", (request.getParameter("schedulerName") == null || request
        .getParameter("schedulerName").equals("")) ? "*" : request.getParameter("schedulerName"));
    redirect.addAttribute("category_default_duration",
        request.getParameter("category_default_duration"));
    if (messageMap.get("warning_message") != null) {
      redirect.addFlashAttribute("info", messageMap.get("warning_message"));
    } else if (messageMap.get("book_enable_warn_message") != null) {
      redirect.addFlashAttribute("info", messageMap.get("book_enable_warn_message"));
    }
    return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW);
  }

  /**
   * Gets the resource timings by duration.
   *
   * @param request the request
   * @param response the response
   * @return the resource timings by duration
   */
  @RequestMapping(value = "/getResourceTimingsByDuration", method = RequestMethod.POST)
  public ModelAndView getResourceTimingsByDuration(HttpServletRequest request,
      HttpServletResponse response) {
    String responseContent = null;
    Map params = request.getParameterMap();
    try {
      responseContent = categoryService.getResourceTimingsByDuration(params);
      response.setContentType("application/json");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      response.getWriter().write(responseContent);
      response.flushBuffer();
    } catch (IOException exception) {
      logger.debug("Failed to parse the date details" + exception.getMessage());
    }
    return null;
  }

  /**
   * Gets the scheduler center.
   *
   * @param request the request
   * @param response the response
   * @return the scheduler center
   */
  @RequestMapping(value = "/getSchedulerCenter", method = RequestMethod.POST)
  public ModelAndView getSchedulerCenter(HttpServletRequest request, HttpServletResponse response) {
    Map params = request.getParameterMap();
    String schedulerCenterLists = null;
    try {
      schedulerCenterLists = categoryService.getSchedulerCenter(params);
      response.setContentType("application/x-json");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      response.getWriter().write(schedulerCenterLists);
      response.flushBuffer();
    } catch (IOException exception) {
      logger.debug("Failed to detch the scheduled center details" + exception.getMessage());
    }
    return null;
  }

}
