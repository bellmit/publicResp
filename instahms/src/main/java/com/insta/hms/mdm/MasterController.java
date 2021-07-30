package com.insta.hms.mdm;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.CronJobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class MasterController which servers as a base class for masters.
 */
public abstract class MasterController extends BaseController implements MultipartController {

  /** The Constant MASTER_BASE_PATH. */
  public static final String MASTER_BASE_PATH = "/master";

  /** The Constant ADD_SUCCESS_MSG_SUFFIX. */
  public static final String ADD_SUCCESS_MSG_SUFFIX = "added successfully";

  /** The Constant EDIT_SUCCESS_MSG_SUFFIX. */
  public static final String EDIT_SUCCESS_MSG_SUFFIX = "updated successfully";

  /** The Constant ERROR_UNKNOWN_MSG. */
  public static final String ERROR_UNKNOWN_MSG = "An unknown error occured";

  /** The Constant INVALID_PARAM_MSG. */
  public static final String INVALID_PARAM_MSG = "Incorrectly formatted values provided";

  /** The Constant MISSING_PATIENT_RECORD_PREFIX. */
  public static final String MISSING_PATIENT_RECORD_PREFIX = " does not exist";

  /** The Constant ALLOWED_CRON_RETRY. */
  public static final String[] ALLOWED_CRON_RETRY = {"PACKAGE", "OPERATION", "DIAGNOSTIC"};
  
  /** The converter. */
  @Autowired
  private ReferenceDataConverter converter;

  @LazyAutowired
  private MessageUtil messageUtil;

  /** The service. */
  private MasterService service;

  /** The cron job service. */
  @LazyAutowired
  private CronJobService cronJobService;

  /** The router. */
  protected ResponseRouter router;

  /**
   * Instantiates a new master controller.
   *
   * @param service
   *          the service
   * @param router
   *          the router
   */
  public MasterController(MasterService service, ResponseRouter router) {
    this.service = service;
    this.router = router;
  }

  /**
   * Method invoked when search action is invoked on an entity.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @SuppressWarnings({ "rawtypes" })
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {

    ModelAndView modelView = new ModelAndView();
    Map paramMap = req.getParameterMap();
    PagedList pagedList = service.search(paramMap);
    modelView.addObject("pagedList", pagedList);
    addReferenceData(getFilterLookups(paramMap), modelView);
    modelView.setViewName(router.route("list"));
    return modelView;
  }

  /**
   * Adds the master entity.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    Map params = req.getParameterMap();
    addReferenceData(getReferenceData(params), mav);
    addReferenceData(getReferenceBean(params), mav);
    mav.setViewName(router.route("add"));
    return mav;
  }

  /**
   * Shows the master entity.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    Map params = req.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    BasicDynaBean bean = service.findByPk(params, true);
    modelView.addObject("bean", bean.getMap());
    addReferenceData(getReferenceData(params), modelView);
    addReferenceData(getReferenceBean(params), modelView);
    modelView.setViewName(router.route("show"));

    return modelView;
  }

  /**
   * Method called when add / edit action is invoked on an entity. Implements the most common use
   * case of not involving Action Forms
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {

    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);
    BasicDynaBean bean = mapToBean(params, fileMap);
    Integer ret = service.insert(bean);
    if (ret != 0) {
      attribs.mergeAttributes(getAttributesForRedirection(bean.getMap(), fileMap));
      String createdMessage = messageUtil.getMessage("flash.created.successfully", null);
      attribs.addFlashAttribute("info", createdMessage);
    }
    modelView.setViewName(router.route("create"));
    return modelView;
  }

  /**
   * Update.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);
    BasicDynaBean bean = mapToBean(params, fileMap);
    Integer ret = service.update(bean);
    if (ret != 0) {
      attribs.mergeAttributes(getAttributesForRedirection(bean.getMap(), fileMap));
      String updatedMessage = messageUtil.getMessage("flash.updated.successfully", null);
      attribs.addFlashAttribute("info", updatedMessage);
    }
    modelView.setViewName(router.route("update"));
    return modelView;
  }

  /**
   * Performs a soft delete if status field is present.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param hardDelete
   *          the hard delete
   * @return the model and view
   */
  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  protected ModelAndView delete(HttpServletRequest request, HttpServletResponse response,
      Boolean hardDelete, RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> params = request.getParameterMap();
    BasicDynaBean bean = mapToBean(params);
    Integer ret = service.delete(bean);

    if (ret != 0) {
      String deletedMessage = messageUtil.getMessage("flash.deleted.successfully", null);
      attribs.addFlashAttribute("info", deletedMessage);
    }

    modelView.setViewName(router.route("delete"));
    return modelView;
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
    String filterText = (null != parameters && parameters.containsKey("filterText"))
        ? parameters.get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = (null != filterText)
        ? service.autocomplete(filterText, parameters) : service.lookup(true);
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    modelMap.addAttribute("listSize", searchSet.size());
    return modelMap;
  }

  /**
   * Update.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @RequestMapping(value = "/retrychargeschedule", method = RequestMethod.POST)
  protected ModelAndView retryChargeSchedule(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes attribs) {
    ModelAndView modelView = new ModelAndView();
    String entity = req.getParameter("entity");
    String entityId = req.getParameter("entity_id");
    if (entity != null
        && entityId != null
        && (Arrays.asList(ALLOWED_CRON_RETRY)).contains(entity)) {
      cronJobService.retryMasterChargeScheduleJob(entity, entityId,
          (String) req.getSession(false).getAttribute("userid"));
      String updatedMessage = messageUtil.getMessage("flash.retry.successfully", null);
      attribs.addFlashAttribute("info", updatedMessage);
    }
    modelView.setViewName(router.route("retrychargeschedule"));
    return modelView;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MultipartController#view(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  @GetMapping("view")
  public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, Object> parameterMap = getParameterMap(request);
    BasicDynaBean bean = getService().findByPk(parameterMap);

    String fieldName = (String) parameterMap.get("field_name");
    response.setContentType(getContentType(parameterMap, bean));

    OutputStream responseStream = null;

    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy((InputStream) bean.get(fieldName), responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }

  /**
   * Gets the content type.
   *
   * @param parameterMap
   *          the parameter map
   * @param bean
   *          the bean
   * @return the content type
   */
  private String getContentType(Map<String, Object> parameterMap, BasicDynaBean bean) {
    if (parameterMap.containsKey("content_type")) {
      return (String) parameterMap.get("content_type");
    } else if (null != bean.get("content_type")) {
      return (String) bean.get("content_type");
    } else {
      return MediaType.IMAGE_JPEG_VALUE;
    }
  }

  /**
   * Gets the attributes for redirection.
   *
   * @param beanMap
   *          the bean map
   * @param fileMap
   *          the file map
   * @return the attributes for redirection
   */
  protected Map<String, Object> getAttributesForRedirection(Map beanMap,
      Map<String, MultipartFile> fileMap) {
    if (null == fileMap) {
      return beanMap;
    }

    Map<String, Object> beanMapWithoutMultipart = new HashMap<String, Object>(beanMap);
    for (String fieldName : fileMap.keySet()) {
      beanMapWithoutMultipart.remove(fieldName);
    }

    return beanMapWithoutMultipart;
  }

  /**
   * Map to bean.
   *
   * @param params
   *          the params
   * @return the basic dyna bean
   */
  protected BasicDynaBean mapToBean(Map<String, String[]> params) {
    return mapToBean(params, null);
  }

  /**
   * Map to bean.
   *
   * @param params
   *          the params
   * @param fileMap
   *          the file map
   * @return the basic dyna bean
   */
  protected BasicDynaBean mapToBean(Map<String, String[]> params,
      Map<String, MultipartFile> fileMap) {
    return service.toBean(params, fileMap);
  }


  /**
   * Gets the reference data.
   *
   * @param params the params
   * @return the reference data
   */
  protected Map<String, List<Map>> getReferenceData(Map params) {
    Map<String, List<BasicDynaBean>> lookupMaps = getReferenceLists(params);
    return converter.convert(lookupMaps);
  }

  /**
   * Gets the filter lookups.
   *
   * @param paramMap
   *          the param map
   * @return the filter lookups
   */
  protected Map<String, List<Map>> getFilterLookups(Map paramMap) {
    Map<String, List<BasicDynaBean>> lookupMaps = getFilterLookupLists(paramMap);
    return converter.convert(lookupMaps);
  }

  /**
   * Gets the service.
   *
   * @return the service
   */
  protected MasterService getService() {
    return service;
  }

  /**
   * Returns a map of record lists used in as data for auto-complete that are part of either a) the
   * search panel of the dash-board b) lookup / foreign key fields used in add / edit page
   * Each entry in the map corresponds to one field with the a) key being the name of the table
   * field for which auto-complete is required b) value being the list of records that need to show
   * up in the auto-complete.
   * The list is expected to contain dyna-beans corresponding to the entity record Conversion of
   * this list to any other form is not necessary. Such conversions are handled by the base class.
   * 
   *
   * @param params
   *          the params
   * @return the reference lists
   */
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return Collections.emptyMap();
  }

  /**
   * Gets the filter lookup lists.
   *
   * @param params
   *          the params
   * @return the filter lookup lists
   */
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return Collections.emptyMap();
  }

  /**
   * Gets the reference bean.Override in the child class to send reference beans.
   *
   * @param params
   *          the params
   * @return the reference bean
   */
  protected Map<String, Map> getReferenceBean(Map params) {
    return Collections.emptyMap(); // converter.convert(lookupMaps);
  }

  /**
   * Adds the reference data.
   *
   * @param referenceData
   *          the reference data
   * @param mav
   *          the mav
   */
  protected void addReferenceData(Map referenceData, ModelAndView mav) {
    addReferenceData(referenceData, mav, null);
  }

  /**
   * Adds the reference data.
   *
   * @param referenceData
   *          the reference data
   * @param mav
   *          the mav
   * @param aggKey
   *          the agg key
   */
  private void addReferenceData(Map referenceData, ModelAndView mav, String aggKey) {
    if (null != referenceData & referenceData.size() > 0) {
      if (null != aggKey) {
        mav.addObject(aggKey, referenceData);
      } else {
        mav.addAllObjects(referenceData);
      }
    }
    // This is a kludge right now.
    // We need to finalize on the approach for
    // generic preferences and then move this to
    // an appropriate place.

    Integer centerId = RequestContext.getCenterId();
    mav.addObject("userCenterId", centerId);

  }
}
