package com.insta.hms.mdm.resourceoverride;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/** 
 * The Class ResourceOverrideController.
 */
@Controller
@RequestMapping(URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH)
public class ResourceOverrideController extends MasterController {

  /** The resource availability service. */
  @LazyAutowired ResourceAvailabilityService resourceAvailabilityService;

  /** The center service. */
  @LazyAutowired CenterService centerService;

  /** The resource override service. */
  @LazyAutowired ResourceOverrideService resourceOverrideService;
  
  
  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService prefService;

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(ResourceOverrideController.class);

  /** Default value for visit mode. */
  private static final String DEFAULT_VISIT_MODE = "I";

  /**
   * Instantiates a new resource override controller.
   *
   * @param service the service
   */
  public ResourceOverrideController(ResourceOverrideService service) {
    super(service, MasterResponseRouter.RESOURCE_AVAILABILITY_OVERRIDE_ROUTER);
  }

  /**
   * Gets the filter lookup lists.
   *
   * @param params the params
   * @return the filter lookup lists
   */
  @SuppressWarnings({"rawtypes"})
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((ResourceOverrideService) getService()).getListPageData(params);
  }

  /**
   * Gets the reference lists.
   *
   * @param params the params
   * @return the reference lists
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((ResourceOverrideService) getService()).getAddPageData(params);
  }
  
  /**
   * Show addbulk.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @RequestMapping(value = "/addbulk", method = RequestMethod.GET)
  public ModelAndView showAddbulk(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    Map params = req.getParameterMap();
    addReferenceData(getReferenceData(params), mav);
    addReferenceData(getReferenceBean(params), mav);
    mav.setViewName(URLRoute.SHOW_BULK_OVERRIDE);
    return mav;
  }

  /**
   * Show.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    ModelAndView mav = new ModelAndView();

    String resourceType = req.getParameter("res_sch_type");
    String resAvailId = req.getParameter("res_avail_id");
    String resourceName = req.getParameter("res_sch_name");
    String screenName = req.getParameter("_screen_name");
    mav.addObject("screenName", screenName);
    mav.addObject("referer", req.getParameter("_referer"));
    BasicDynaBean bean =
        resourceOverrideService.getResourceDetails(Integer.parseInt(resAvailId), resourceType);
    if (bean == null) {
      bean = resourceOverrideService.getResourceDetailsByDate(resourceType, resourceName);
    }
    Date recordFromDate = null;
    Date recordToDate = null;
    List<BasicDynaBean> recordsExistList = new ArrayList<BasicDynaBean>();
    if (resAvailId != null && !resAvailId.equals("")) {
      recordsExistList = resourceOverrideService.getResourcesList(resourceType, resourceName);
    }

    if (recordsExistList != null && !recordsExistList.isEmpty()) {
      recordFromDate =
          new Date(((java.sql.Date) recordsExistList.get(0).get("availability_date")).getTime());
      recordToDate =
          new Date(
              ((java.sql.Date)
                      recordsExistList.get(recordsExistList.size() - 1).get("availability_date"))
                  .getTime());
    }
    List<BasicDynaBean> resourceAvailtimingList =
        ResourceOverrideRepository.getResourceAvailtimingList(Integer.parseInt(resAvailId));
    mav.addObject("recordFromDate", recordFromDate);
    mav.addObject("recordToDate", recordToDate);
    mav.addObject("bean", bean);
    mav.addObject("resourceAvailtimingList", resourceAvailtimingList);
    mav.addObject(
        "allResourcesList",
        ConversionUtils.listBeanToListMap(resourceAvailabilityService.getAllResources()));
    mav.addObject(
        "centersJSON", ConversionUtils.listBeanToListMap(centerService.getAllCentersDetails()));

    int loggedCenterId = RequestContext.getCenterId();
    List schedulerdoctors =
        ConversionUtils.listBeanToListMap(
            resourceAvailabilityService.getResourceMasterList("DOC", loggedCenterId));
    mav.addObject("DoctorsJSON", schedulerdoctors);
    List<BasicDynaBean> resourceBean = resourceAvailabilityService.getCategoryDescription(false);
    if (resourceBean != null) {
      mav.addObject("categoryDescripton", resourceBean);
    }

    mav.setViewName(router.route("show"));

    return mav;
  }

  /**
   * Creates the.
   *
   * @param request the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect) {

    Map requestParams = request.getParameterMap();
    List<Map> overrideDetails = new ArrayList<Map>();

    String[] fromTime = (String[]) requestParams.get("from_time");
    String[] toTime = (String[]) requestParams.get("to_time");
    String[] status = (String[]) requestParams.get("availability_status");
    String[] remarks = (String[]) requestParams.get("remarks");
    String[]  centerId = (String[]) requestParams.get("center_id");
    String[] defaultValue = (String[]) requestParams.get("default_value");
    String[] rdelete = (String[]) requestParams.get("r_delete");
    String[] resType = (String[]) requestParams.get("res_sch_type");
    String[] loginCenterId = (String[]) requestParams.get("login_center_id");
    String[] visitMode = (String[]) requestParams.get("visit_mode");

    Integer centersIncDefault = (Integer) prefService.getAllPreferences()
        .get("max_centers_inc_default");
    
    for (int i = 0; i < fromTime.length; i++) {
      if (fromTime != null && !fromTime[i].equals("")) {
        Map availabilityDetail = new HashMap();
        availabilityDetail.put("start_time", fromTime[i]);
        availabilityDetail.put("end_time", toTime[i]);
        if (resType != null && resType[0].equalsIgnoreCase("DOC")) {
          if (centersIncDefault > 1) {
            if (centerId[i] != null && !centerId[i].equals("")) {
              availabilityDetail.put("center_id", 
                  Integer.parseInt(centerId[i]));
            } else {
              availabilityDetail.put("center_id",null);
            }
          } else {
            availabilityDetail.put("center_id",status[i]
                .equalsIgnoreCase("N") ? null : 0);
          }
          if (visitMode != null && visitMode[i] != null && !visitMode[i].isEmpty()) {
            availabilityDetail.put("visit_mode", visitMode[i]);
          } else {
            availabilityDetail.put("visit_mode", DEFAULT_VISIT_MODE);
          }
        } else {
          availabilityDetail.put("center_id",status[i]
              .equalsIgnoreCase("N") ? null 
                  : (loginCenterId != null 
                  ? Integer.parseInt(loginCenterId[0]) : 0));
        }
        
        availabilityDetail.put("availability_status", status[i]);
        availabilityDetail.put("remarks", remarks[i]);
        availabilityDetail.put("default_value", defaultValue[i]);
        availabilityDetail.put("r_delete", rdelete[i]);
        overrideDetails.add(availabilityDetail);
      }
    }

    //construct json data
    Map<String, Object> timingsMap = new HashMap<String, Object>();
    timingsMap.put("override_for", "D");
    timingsMap.put("start_date", ((String[]) requestParams.get("from_date"))[0]);
    timingsMap.put("end_date", ((String[]) requestParams.get("to_date"))[0]);
    timingsMap.put("resource_id", ((String[]) requestParams.get("res_sch_name"))[0]);
    timingsMap.put("resource_type", ((String[]) requestParams.get("res_sch_type"))[0]);
    timingsMap.put("r_delete", (String[]) requestParams.get("r_delete"));
    timingsMap.put("cancel_existing_appointments", "false");
    timingsMap.put("availability_details", overrideDetails);
    Map<String, Object> parameterMap = new HashMap<String, Object>(requestParams);
    parameterMap.put("timings", timingsMap);
    Map detailsMap = new HashMap();
    detailsMap = resourceOverrideService.saveBulkOverrideDetails(parameterMap).get(0);
    //detailsMap = resourceOverrideService.saveOverrideDetails(parameterMap, overrideDetails);

    boolean success = (Boolean) detailsMap.get("redirectStatus");
    BasicDynaBean bean = (BasicDynaBean) detailsMap.get("resourceBean");
    String warningMessage = (String) detailsMap.get("warningMessage");
    if (success) {
      redirect.addFlashAttribute("info", "Resource Availability details inserted successfully.");
      redirect.addAttribute("res_avail_id", bean.get("res_avail_id"));
      redirect.addAttribute("res_sch_type", bean.get("res_sch_type"));
      redirect.addAttribute("res_sch_name", request.getParameter("res_sch_name"));

      if (warningMessage != null) {
        redirect.addFlashAttribute("warning", warningMessage);
      } else {
        redirect.addFlashAttribute("info", "Resource Availability details inserted successfully.");
      }
      return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW);
    } else {
      redirect.addFlashAttribute(
          "error", "resource availability already exists within given date range...");
      redirect.addAttribute("res_avail_id", bean.get("res_avail_id"));
      redirect.addAttribute("res_sch_type", bean.get("res_sch_type"));
      redirect.addAttribute("res_sch_name", request.getParameter("res_sch_name"));
      ModelAndView mav = new ModelAndView();
      mav.setViewName("redirect:add");
      return mav;
    }
  }
  
  /**
   * Check overrides exist.
   *
   * @param request the request
   * @param resp the resp
   * @return the map
   */
  @RequestMapping(value = "/overridesexist", method = RequestMethod.GET)
  public Map<String, Boolean> checkOverridesExist(HttpServletRequest request,
      HttpServletResponse resp) {
    Map requestParams = request.getParameterMap();
    java.sql.Date startDate = null;
    java.sql.Date endDate = null;
    try {
      startDate = DateUtil.parseDate(((String[]) requestParams.get("from_date"))[0]);
      endDate = DateUtil.parseDate(((String[]) requestParams.get("to_date"))[0]);
    } catch (ParseException exp) {
      throw new ValidationException("exception.scheduler.override.invalid.dates");
    }
    String resIdString = ((String[]) requestParams.get("res_sch_name"))[0];
    String[] resIds = resIdString.split(",");
    boolean overridePresent = resourceOverrideService.checkOverrideExists(startDate, endDate,
        resIds);
    HashMap<String, Boolean> res = new HashMap<>();
    res.put("exists", overridePresent);
    return res;

  }
  
  /**
   * Creates the bulk.
   *
   * @param request the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/createbulk", method = RequestMethod.POST)
  public ModelAndView createBulk(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {
    Map requestParams = request.getParameterMap();
    resourceOverrideService.insertBulkOverrides(requestParams);
    redirect.addFlashAttribute("info", "Resource Availability details inserted successfully.");
    redirect.addAttribute("method", "list");
    redirect.addAttribute("res_sch_type", "");
    redirect.addAttribute("res_sch_name", "");
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH_REDIRECT);
    return mav;
  }

  /**
   * Update.
   *
   * @param request the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @SuppressWarnings({"rawtypes"})
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect) {
    Map detailsMap = new HashMap();

    Map params = request.getParameterMap();
    detailsMap = resourceOverrideService.updateResourceOverrideDetails(params);
    String warningMessage = (String) detailsMap.get("warningMessage");
    boolean success = (Boolean) detailsMap.get("successStatus");

    if (success) {
      redirect.addAttribute("res_avail_id", request.getParameter("res_avail_id"));
      redirect.addAttribute("res_sch_type", request.getParameter("res_sch_type"));
      redirect.addAttribute("res_sch_name", request.getParameter("res_sch_name"));
      if (warningMessage != null) {
        redirect.addFlashAttribute("warning", warningMessage);
      } else {
        redirect.addFlashAttribute("Resource Availability details updated successfully..");
      }
      return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW);
    } else {
      redirect.addFlashAttribute("error", "Failed to update Resource Availability details..");
    }
    redirect.addAttribute("res_avail_id", request.getParameter("res_avail_id"));
    redirect.addAttribute("res_sch_type", request.getParameter("res_sch_type"));
    redirect.addAttribute("res_sch_name", request.getParameter("res_sch_name"));
    return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_TO_SHOW);
  }

  /**
   * Delete selected rows.
   *
   * @param request the request
   * @param res the res
   * @param redirect the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/deleteSelectedRows", method = RequestMethod.GET)
  public ModelAndView deleteSelectedRows(
      HttpServletRequest request, HttpServletResponse res, RedirectAttributes redirect) {
    Map<String, String> recordsMap = new HashMap<String, String>();
    String responseContent = null;
    recordsMap.put("resAvailId", request.getParameter("res_avail_id"));
    recordsMap.put("res_sch_type", request.getParameter("res_sch_type"));
    try {
      responseContent = resourceOverrideService.deleteSelectedRows(recordsMap);
      res.setContentType("application/json");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      res.getWriter().write(responseContent);
      res.flushBuffer();
    } catch (Exception exception) {
      logger.debug("Exception while deleting the selected rows " + exception.getMessage());
    }
    return null;
  }

  /**
   * Show default timings.
   *
   * @param request the request
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @RequestMapping(value = "/showDefaultTimings", method = RequestMethod.GET)
  public ModelAndView showDefaultTimings(
      HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect) {
    Map requestMap = new HashMap();
    requestMap.put("resourceType", request.getParameter("res_sch_type"));
    requestMap.put("resourceName", request.getParameter("res_sch_name"));

    resourceOverrideService.showDefaultTimings(requestMap);
    BasicDynaBean bean = (BasicDynaBean) requestMap.get("bean");

    if (bean != null) {
      redirect.addAttribute("res_sch_name", bean.get("res_sch_name"));
      redirect.addAttribute("res_sch_type", bean.get("res_sch_type"));
      redirect.addAttribute("res_sch_id", bean.get("res_sch_id"));
      redirect.addAttribute("category_default_duration", "None");
    }
    return new ModelAndView(URLRoute.RESOURCE_AVAILABILITY_REDIRECT_DEF_TIMING);
  }

  /**
   * Delete resource override.
   *
   * @param request the request
   * @param res the res
   * @param redirect the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/deleteResourceAvailability", method = RequestMethod.GET)
  public ModelAndView deleteResourceOverride(
      HttpServletRequest request, HttpServletResponse res, RedirectAttributes redirect) {
    Map<String, Object> resourceMap = new HashMap<String, Object>();
    resourceMap.put("resourceType", request.getParameter("res_sch_type"));
    resourceMap.put("resAvailId", request.getParameter("res_avail_id"));
    resourceMap.put("resourceName", request.getParameter("res_sch_name"));
    try {
      resourceOverrideService.deleteResourceOverride(resourceMap);
    } catch (Exception exception) {
      logger.debug("Exception while deleting the specific row " + exception.getMessage());
    }
    redirect.addAttribute("method", "list");
    redirect.addAttribute("res_sch_type", request.getParameter("res_sch_type"));
    redirect.addAttribute("res_sch_name", request.getParameter("res_sch_name"));
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH_REDIRECT);
    return mav;
  }

  /**
   * Gets the resource avail date.
   *
   * @param request the request
   * @param res the res
   * @param redirect the redirect
   * @return the resource avail date
   */
  @RequestMapping(value = "/getResourceAvailDate", method = RequestMethod.GET)
  public ModelAndView getResourceAvailDate(
      HttpServletRequest request, HttpServletResponse res, RedirectAttributes redirect) {
    Integer resAvailId = null;
    Map<String, Object> resourceMap = new HashMap<String, Object>();
    String category = request.getParameter("resourceType");
    String resourceType = null;

    resourceMap.put("colDate", request.getParameter("colDate"));
    resourceMap.put("resourceName", request.getParameter("resourceName"));

    if (category.equals("DOC")) {
      resourceType = category;
    } else if (category.equals("DIA")) {
      resourceType = "EQID";
    } else if (category.equals("SNP")) {
      resourceType = "SRID";
    } else if (category.equals("OPE")) {
      resourceType = "THID";
    }
    resourceMap.put("resourceType", resourceType);
    try {
      resAvailId = resourceOverrideService.getResourceAvailDate(resourceMap);
    } catch (Exception exception) {
      logger.debug(
          "Exception while getting the resouce " + "avail date details " + exception.getMessage());
    }
    redirect.addAttribute("method", "list");
    redirect.addAttribute("res_sch_name", request.getParameter("resourceName"));
    redirect.addAttribute("res_sch_type", resourceType);
    redirect.addAttribute("_screenName", "schedulerScreen");
    String referrer = request.getHeader("referer");
    redirect.addAttribute("_referrer", referrer);
    if (resAvailId != 0) {
      redirect.addAttribute("_res_avail_id", resAvailId);
    }
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH_REDIRECT);
    return mav;
  }
}

