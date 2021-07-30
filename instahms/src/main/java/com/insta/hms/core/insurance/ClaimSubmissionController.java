package com.insta.hms.core.insurance;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.insurance.claimhistory.ClaimHistoryService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.extension.billing.AccountingService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.map.SingletonMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ClaimSubmissionController.
 */
@Controller("claimSubmissionController")
@RequestMapping(URLRoute.CLAIM_SUBMISSION)
public class ClaimSubmissionController extends BaseController {

  /** The claim history service. */
  @LazyAutowired
  private ClaimHistoryService claimHistoryService;

  /** The claim sub service. */
  @LazyAutowired
  private ClaimSubmissionService claimSubService;

  /** The ins sub batch service. */
  @LazyAutowired
  private InsuranceSubmissionBatchService insSubBatchService;

  @LazyAutowired
  AccountingService accountingService;

  /**
   * Claim sent.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @param redirect the redirect
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/markClaimAsSent")
  public ModelAndView markClaimAsSent(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) throws IOException {

    ModelAndView modelView = new ModelAndView();
    String submissionBatchID = request.getParameter("submission_batch_id");
    if (submissionBatchID != null && !submissionBatchID.isEmpty()) {
      claimSubService.markClaimAsSent(submissionBatchID);
      claimHistoryService.createClaimHistory(submissionBatchID);
    }

    redirect.addAttribute("submission_batch_id", submissionBatchID);
    modelView.setViewName("redirect:getaddoreditbatch");
    return modelView;
  }

  /**
   * Gets the addor edit batch ref screen.
   *
   * @param request  the request
   * @param mmap     the mmap
   * @param response the response
   * @return the addor edit batch ref screen
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getaddoreditbatch")
  public ModelAndView getAddorEditBatchRefScreen(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {
    String submissionBatchID = request.getParameter("submission_batch_id");

    ModelAndView modelView = new ModelAndView();

    if (submissionBatchID.isEmpty()) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }

    BasicDynaBean subBatch = insSubBatchService.findSubmissionBatch(submissionBatchID);
    if (subBatch == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }

    modelView.addObject("batch", subBatch);
    modelView.setViewName(URLRoute.ADD_EDIT_BATCH);
    return modelView;
  }

  /**
   * Addoredit batch ref.
   *
   * @param request  the request
   * @param redirect the redirect
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = "/addeditbatchref")
  public ModelAndView addoreditBatchRef(HttpServletRequest request, RedirectAttributes redirect) {

    String submissionBatchID = request.getParameter("submission_batch_id");
    String referenceNumber = request.getParameter("reference_number");

    BasicDynaBean subBatchBean = insSubBatchService.findByKey(submissionBatchID);
    subBatchBean.set("reference_number", referenceNumber);

    insSubBatchService.update(subBatchBean, submissionBatchID);

    ModelAndView modelView = new ModelAndView();

    redirect.addAttribute("submission_batch_id", submissionBatchID);
    modelView.setViewName("redirect:addoreditbatch");

    return modelView;

  }


  /**
   * Gets account groups.
   *
   * @param req the req
   * @return the account groups
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getaccountgroups")
  public Map<String, List> getAccountGroups(HttpServletRequest req) {
    return Collections.singletonMap("result", ConversionUtils
        .listBeanToListMap(claimSubService.getAccountGroupAndCenterType(req.getParameter("query"),
            (Integer) req.getSession(false).getAttribute("centerId"))));
  }

}
