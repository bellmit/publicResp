package com.insta.hms.integration.insurance.submission;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.URLRoute;
import com.insta.hms.mdm.ResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SelfPaySubmissionController. ToDO: Rename the end points to
 * something more appropriate.
 */

@RestController("selfPaySubmissionController")
@RequestMapping(URLRoute.SELF_PAY_CLAIM_SUBMISSION)
public class SelfPaySubmissionController extends BaseRestController {

  /** The self pay service. */
  @LazyAutowired
  SelfPaySubmissionService selfPayService;

  /** The self pay service. */
  @LazyAutowired
  PersonRegisterSubmissionService personRegisterService;

  /** The router. */
  protected ResponseRouter router;

  /**
   * Shows the list of selfpay batches.
   *
   * @param request the request
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "", "/list" }, method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest request) {
    Map<String, String[]> param = request.getParameterMap();

    Map<String, String[]> parameters = new HashMap<>();
    parameters.putAll(param);
    ModelAndView modelView = new ModelAndView();

    int userCenterId = (Integer) request.getSession(false).getAttribute("centerId");

    parameters.put("center_id", new String[] { String.valueOf(userCenterId) });
    parameters.put("center_id@type", new String[] { "integer" });
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      PagedList beanList = selfPayService.listSubmission(parameters);
      modelView.addObject("pagedList", beanList);
    } else {
      PagedList beanList = personRegisterService.listSubmission(parameters);
      modelView.addObject("pagedList", beanList);
    }
    List<BasicDynaBean> accGrpAndCenterList = selfPayService.accountgrpAndCenterView(userCenterId);
    modelView.addObject("accountGrpAndCenterList", accGrpAndCenterList);
    modelView.setViewName(URLRoute.SELF_PAY_CLAIM_SUBMISSION_LIST);
    return modelView;
  }

  /**
   * Redirects to the list page with the errors as a flash message.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/downloadError" }, method = RequestMethod.GET)
  public ModelAndView exportError(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) throws IOException {

    Map<String, String[]> parameters = request.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      Integer selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);
      Map errorsMap = selfPayService.getErrors(selfpayBatchId);
      redirect.addFlashAttribute("info", errorsMap);
    } else {
      String personRegisterBatchId = parameters.get("batch_id")[0];
      Map errorsMap = personRegisterService.getErrors(personRegisterBatchId);
      redirect.addFlashAttribute("info", errorsMap);
    }

    redirect.addFlashAttribute("isEncoded", "false");
    // set the default filters to show only open batches
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * Downloads the XML to local filesystem from the application server.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/downloadXML" }, method = RequestMethod.GET)
  public ModelAndView downloadXml(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) throws IOException {
    Map<String, String[]> parameters = request.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      String msg = selfPayService.downloadXml(parameters, response);
      redirect.addFlashAttribute("info", msg);
    } else {
      String msg = personRegisterService.downloadXml(parameters, response);
      redirect.addFlashAttribute("info", msg);
    }
    // set the default filters to show only open batches
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * Upload claim.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/uploadClaim" }, method = RequestMethod.GET)
  public ModelAndView uploadClaim(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) throws IOException {
    Map<String, String[]> parameters = request.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      String msg = selfPayService.uploadClaim(parameters, response);
      redirect.addFlashAttribute("info", msg);
    } else {
      String msg = personRegisterService.uploadClaim(parameters, response);
      redirect.addFlashAttribute("info", msg);
    }
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * Mark the given selfpay batch as sent.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/markAsSent" }, method = RequestMethod.GET)
  public ModelAndView markAsSent(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) {

    Map<String, String[]> parameters = request.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      String msg = selfPayService.markAsSent(parameters);
      redirect.addFlashAttribute("info", msg);
    } else {
      String msg = personRegisterService.markAsSent(parameters);
      redirect.addFlashAttribute("info", msg);
    }
    // set the default filters to show only open batches
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * Sets the redirect to list page. Also sets the page filters to
   * selfpay_status=open and sortby created_at
   *
   * @param redirect      the redirect
   * @param modelView     the model view
   * @param selfpayStatus the selfpay status you want to filter by
   */
  private void setRedirectToList(RedirectAttributes redirect, ModelAndView modelView,
      String selfpayStatus, String submissionType) {
    redirect.addAttribute("status", selfpayStatus);
    redirect.addAttribute("sortReverse", "true");
    redirect.addAttribute("sortOrder", "created_at");
    redirect.addAttribute("submissionType", submissionType);
    modelView.setViewName("redirect:list");
  }

  /**
   * Deletes the selfpay batch.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/delete" }, method = RequestMethod.GET)
  public ModelAndView delete(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) {

    Map<String, String[]> parameters = request.getParameterMap();
    ModelAndView modelView = new ModelAndView();
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      String msg = selfPayService.delete(parameters);
      redirect.addFlashAttribute("info", msg);
    } else {
      String msg = personRegisterService.delete(parameters);
      redirect.addFlashAttribute("info", msg);
    }
    
    // set the default filters to show only open batches
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * The page that lists the filters to create a submission batch.
   *
   * @param request  the request
   * @param res      the res
   * @param redirect the redirect
   * @return the model and view
   * @throws SQLException the SQL exception
   */
  // create submission batch.
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/create", method = RequestMethod.GET)
  public ModelAndView add(HttpServletRequest request, HttpServletResponse res,
      RedirectAttributes redirect) throws SQLException {
    ModelAndView modelView = new ModelAndView();
    Map<String, Object> response = selfPayService.create();
    modelView.addAllObjects(response);
    modelView.setViewName(URLRoute.SELF_PAY_CLAIM_SUBMISSION_CREATE);
    return modelView;
  }

  /**
   * Triggers the selfpay XML generation job.
   *
   * @param request  the request
   * @param res      the res
   * @param redirect the redirect
   * @return the model and view
   * @throws Exception the exception
   */
  // this starts a claim xml generation job
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/generate" }, method = RequestMethod.GET)
  public ModelAndView generateXml(HttpServletRequest request, HttpServletResponse res,
      RedirectAttributes redirect) throws Exception {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> parameters = request.getParameterMap();
    String info = "";
    if (parameters.get("submissionType")[0].equalsIgnoreCase("SP")) {
      Integer selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);
      info = selfPayService.processSelfpayJob(selfpayBatchId, request.getContextPath());
    } else {
      String personRegisterBatchId = parameters.get("batch_id")[0];
      info = personRegisterService.processPersonRegisterJob(personRegisterBatchId,
          request.getContextPath());
    }
    redirect.addFlashAttribute("info", info);
    // set the default filters to show only open batches
    setRedirectToList(redirect, modelView, "O", parameters.get("submissionType")[0]);
    return modelView;
  }

  /**
   * Creates the submission batch.
   *
   * @param request  the request
   * @param res      the res
   * @param redirect the redirect
   * @return the model and view
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  @RequestMapping(value = "/createSubmission", method = RequestMethod.POST)
  public ModelAndView createSubmission(HttpServletRequest request, HttpServletResponse res,
      RedirectAttributes redirect) throws SQLException, ParseException {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, Object> response = new HashMap<>();
    if (parameters.get("submission_type")[0].equalsIgnoreCase("SP")) {
      response = selfPayService.createSubmission(parameters);
    } else {
      response = personRegisterService.createSubmission(parameters);
    }
    redirect.addAllAttributes(response);
    ModelAndView modelView = new ModelAndView();
    redirect.addFlashAttribute("info", response.get("info"));
    modelView.setViewName("redirect:create");
    return modelView;
  }
}
