package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.mdm.ReferenceDataConverter;
import com.insta.hms.mdm.ResponseRouter;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class CoderClaimReviewController.
 */
@Controller("coderClaimReviewController")
@RequestMapping("/coderreviews")
public class CoderClaimReviewController extends BaseController {

  /** The coder claim review service. */
  CoderClaimReviewService coderClaimReviewServic;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The coder ticket details service. */
  @LazyAutowired
  CoderTicketDetailsService coderTicketDetailsService;

  /** The coder claim review repository. */
  @LazyAutowired
  CoderClaimReviewRepository coderClaimReviewRepository;

  /** The ticket comments service. */
  @LazyAutowired
  TicketCommentsService ticketCommentsService;

  /** The coder claim review service. */
  @LazyAutowired
  CoderClaimReviewService coderClaimReviewService;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The doctor consultation service. */
  @LazyAutowired
  DoctorConsultationService doctorConsultationService;

  /** The Constant URL_RIGHTS_MAP. */
  private static final String URL_RIGHTS_MAP = "urlRightsMap";
  
  /** The converter. */
  @Autowired
  private ReferenceDataConverter converter;
  /** The router. */
  protected ResponseRouter router;

  /**
   * Instantiates a new coder claim review controller.
   */
  public CoderClaimReviewController() {
    this.router = new CoderResponseRouter("CoderClaimReview");
  }

  /**
   * Gets the reference lists.
   *
   * @param params
   *          the params
   * @return the reference lists
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return coderClaimReviewService.getAddEditPageData(params);
  }

  /**
   * List.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp)
      throws ParseException {

    Map<String, Object> responseMap = coderClaimReviewService
        .getList(new HashMap(req.getParameterMap()));
    ModelAndView modelView = new ModelAndView();
    modelView.addAllObjects(responseMap);
    modelView.setViewName(router.route("list"));
    return modelView;
  }

  /**
   * Adds the review.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView mav = new ModelAndView();
    Map params = req.getParameterMap();
    addReferenceData(getReferenceData(params), mav);
    addReferenceData(getReferenceBean(params), mav);
    mav.setViewName(router.route("add"));
    return mav;
  }

  /** The Constant PATIENT_ID. */
  private static final String PATIENT_ID = "patient_id";
  
  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(CoderClaimReviewController.class);

  /**
   * Show list of reviews.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse resp) {

    Map params = req.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (!coderClaimReviewService
        .isAuthorized(Integer.parseInt(((String[]) params.get("id"))[0]))) {
      // 403 Not allowed,
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      modelView.setViewName("/pages/AccessControlForward");
      return modelView;
    }
    Map<String, Object> urlRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { URL_RIGHTS_MAP })
        .get(URL_RIGHTS_MAP);
    BasicDynaBean bean = coderClaimReviewService.findByPk(params);
    modelView.addObject("bean", bean.getMap());
    modelView.addObject("urlRightsMap", urlRightsMap);
    modelView.addAllObjects(
        converter.convert(coderClaimReviewService.getAddEditPageData(params)));
    modelView.addObject("activityMap",
        coderClaimReviewService.getActivityMap(params));
    Integer centerId = RequestContext.getCenterId();
    modelView.addObject("userCenterId", centerId);
    modelView.setViewName(router.route("show"));

    return modelView;
  }

  /**
   * Creates the review.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  protected ModelAndView create(HttpServletRequest req,
      HttpServletResponse resp, RedirectAttributes attribs) {
    Map<String, Map<String, Object>> requestMap = new HashMap();
    Map<String, Object> reqMap = ConversionUtils.flatten(req.getParameterMap());
    requestMap.put("requestObject", reqMap);
    Map<String, String> responseMap = null;
    try {
      responseMap = coderClaimReviewService.createReview(requestMap);
    } catch (Exception exp) {
      log.debug("Failed to create reviews. " + exp.getMessage());
    }

    ModelAndView mav = new ModelAndView();

    String createdMessage = messageUtil.getMessage("flash.created.successfully",
        null);
    attribs.addFlashAttribute("info", createdMessage);
    if (responseMap != null && !responseMap.isEmpty()) {
      attribs.addAttribute("id", responseMap.get("ticket_id"));
      attribs.addAttribute(PATIENT_ID, responseMap.get(PATIENT_ID));
    }
    mav.setViewName(router.route("create"));
    return mav;
  }

  /**
   * Update Review.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ModelAndView update(HttpServletRequest req,
      HttpServletResponse resp, RedirectAttributes attribs) {
    Map<String, String> responseMap = coderClaimReviewService
        .updateReview(req.getParameterMap());

    if (responseMap.get("updateStatus").equals("ticketUpdated")) {
      String updatedMessage = messageUtil
          .getMessage("flash.updated.successfully", null);
      attribs.addFlashAttribute("message", updatedMessage);
    } else if (responseMap.get("updateStatus").equals("commentInserted")) {
      String updatedMessage = messageUtil
          .getMessage("flash.comment.successfully", null);
      attribs.addFlashAttribute("message", updatedMessage);
    }
    attribs.addAttribute("id", responseMap.get("ticket_id"));
    attribs.addAttribute(PATIENT_ID, responseMap.get(PATIENT_ID));
    ModelAndView mav = new ModelAndView();
    mav.setViewName(router.route("update"));
    return mav;
  }

  /**
   * Open reopenconsultation.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the string
   */
  @RequestMapping(value = "/openReopenConsultation", method = RequestMethod.GET)
  protected String openReopenconsultation(HttpServletRequest req,
      HttpServletResponse resp, RedirectAttributes attribs) {
    Map<String, String> responseMap = doctorConsultationService
        .openReopenConsultation(req.getParameterMap());
    if (responseMap.get("status").equals("reopen")) {
      String reopenMessage = messageUtil.getMessage(
          "patient.outpatientlist.consult.details.reopen.success", null);
      attribs.addFlashAttribute("info", reopenMessage);
    }
    String consultationUrl = "/outpatient/OpPrescribeAction.do?_method=list"
        + "&consultation_id=" + req.getParameter("consultation_id")
        + "&patient_id=" + req.getParameter("patient_id") + "&doctor_id="
        + req.getParameter("doctor_id") + "&mr_no=" + req.getParameter("mr_no")
        + "&";
    return "redirect:" + consultationUrl;

  }
  
  /**
   * ajax function checkCodificationStatus.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param attribs
   *          the attribs
   * @return the string
   */
  @GetMapping(value = "/checkCodificationStatus")
  protected Map<String,String> checkCodificationStatus(HttpServletRequest req,
      HttpServletResponse resp, RedirectAttributes attribs) {
    String patientId = req.getParameter("patient_id");
    Map<String, String> map = new HashMap<>();
    map.put("status", coderClaimReviewService.getCodificationStatus(patientId));
    return map;
    
    
  }

  /**
   * Gets the reference data.
   *
   * @param paramMap
   *          the param map
   * @return the reference data
   */
  protected Map<String, List<Map>> getReferenceData(
      Map<String, String[]> paramMap) {
    Map<String, List<BasicDynaBean>> lookupMaps = getReferenceLists(paramMap);
    return converter.convert(lookupMaps);
  }

  /**
   * Gets the reference bean.Override in the child class to send reference
   * beans.
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
  private void addReferenceData(Map referenceData, ModelAndView mav,
      String aggKey) {
    if (null != referenceData && referenceData.size() > 0) {
      if (null != aggKey) {
        mav.addObject(aggKey, referenceData);
      } else {
        mav.addAllObjects(referenceData);
      }
    }

    Integer centerId = RequestContext.getCenterId();
    mav.addObject("userCenterId", centerId);

  }

}
