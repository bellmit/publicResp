package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author anup vishwas.
 *
 */

@Controller
@RequestMapping(URLRoute.DISCHARGE_SUMMARY_URL)
public class DischargeSummaryController extends BaseController {
  static Logger logger = LoggerFactory.getLogger(DischargeSummaryController.class);

  @LazyAutowired
  private DischargeSummaryService dischargeSummaryService;

  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getDischargeSummaryIndexPage() {
    return renderFlowUi("Discharge Summary", "ipFlow", "withFlow", "ipFlow", "dischargesummary",
        true);
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getAllVisits"}, method = RequestMethod.GET)
  public Map<String, Object> getAllVisits(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) throws SQLException {

    return dischargeSummaryService.getAllVisitDetails();
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getTemplates"}, method = RequestMethod.GET)
  public Map<String, Object> getTemplates(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    return dischargeSummaryService.getAllTemplates();
  }

  // in progress
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getTemplateContent"}, method = RequestMethod.GET)
  public Map<String, Object> getTemplateContent(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response)
      throws NumberFormatException, IOException, TemplateException, SQLException {

    return dischargeSummaryService.getTemplateContent();
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getDischargeValueItems"}, method = RequestMethod.GET)
  public Map<String, Object> getDischargeValueItems(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    return dischargeSummaryService.getDischargeValueItems();
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getSelectedItemDetails"}, method = RequestMethod.GET)
  public Map<String, Object> getSelectedItemDetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    return dischargeSummaryService.getSelectedItemDetails();
  }

  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/processSelectedTokens"}, method = RequestMethod.GET)
  public Map<String, Object> processSelectedTokens(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) throws SQLException, IOException, TemplateException {

    return dischargeSummaryService.processSelectedTokens();
  }

  /**
   * Save and sign off.
   * 
   * @param request the http request
   * @param response the http response
   * @param requestBody the model map
   * @return map
   * @throws SQLException the exception
   * @throws ParseException the exception
   */
  @RequestMapping(value = {"/saveAndSignoff"}, method = RequestMethod.POST,
      consumes = "application/json")
  public Map<String, Object> saveAndSignoff(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody)
      throws SQLException, ParseException {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = dischargeSummaryService.saveAndSignoff(requestBody);

    return map;
  }

  /**
   * Delete discharge summary.
   * 
   * @param request the http request
   * @param response the http response
   * @param requestBody the model map
   * @return the map
   */
  @RequestMapping(value = {"/deleteDischargeSummary"}, method = RequestMethod.POST)
  public Map<String, Object> deleteDischargeSummary(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = dischargeSummaryService.deleteDischargeSummary(requestBody);
    return map;

  }

  /**
   * Revert finalize.
   * 
   * @param request the http request
   * @param response the http response
   * @param requestBody the model map
   * @return map
   */
  @RequestMapping(value = {"revertFinalized"}, method = RequestMethod.POST)
  public Map<String, Object> revertFinalized(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = dischargeSummaryService.revertFinalizedDischargeSummary(requestBody);
    return map;
  }

  /**
   * Get generic image list.
   * 
   * @param request the http request
   * @param mmap the model map
   * @param response the http response
   * @return the map
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/getGenericImages"}, method = RequestMethod.GET)
  public Map<String, Object> getGenericImageList(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    Map<String, Object> map = dischargeSummaryService.getGenericImageList();
    return map;
  }

}
