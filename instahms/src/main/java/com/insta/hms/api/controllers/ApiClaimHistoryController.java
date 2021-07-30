package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.insurance.claimhistory.ClaimHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ApiClaimHistoryController.
 */
@Controller
@RequestMapping("/api/claims")
public class ApiClaimHistoryController extends BaseRestController {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ApiClaimHistoryController.class);

  /** The claim hist service. */
  @LazyAutowired
  private ClaimHistoryService claimHistService;

  /**
   * Gets the claim history.
   *
   * @param request the request
   * @param response the response
   * @param fromSubmissionDate the from submission date
   * @param toSubmissionDate the to submission date
   * @return the claim history
   * @throws ParseException the parse exception
   */
  @GetMapping(value = "/history")
  public ResponseEntity<Map<String, Object>> getClaimHistory(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(value = "submission_date_from") String fromSubmissionDate,
      @RequestParam(value = "submission_date_to") String toSubmissionDate) throws ParseException {
    Map<String, Object> responseBody = claimHistService.getBillLevelClaimHistory(
        fromSubmissionDate, toSubmissionDate);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

}
