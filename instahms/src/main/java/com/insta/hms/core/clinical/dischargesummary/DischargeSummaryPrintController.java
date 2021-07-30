package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author anup vishwas.
 *
 */

@Controller
@RequestMapping(URLRoute.DISCHARGE_SUMMARY_PRINT_URL)
public class DischargeSummaryPrintController extends BaseRestController {
  static Logger logger = LoggerFactory.getLogger(DischargeSummaryPrintController.class);

  @LazyAutowired
  private DischargeSummaryPrintService disPrintService;

  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getAllPrinterDefinition", method = RequestMethod.GET)
  public Map<String, Object> getPrinters(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) throws SQLException {

    return disPrintService.getAllPrinterDefinition();
  }

  /**
   * print.
   * 
   * @param request the request
   * @param response the response
   * @return the maodel and view
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/printDischargeSummary", method = RequestMethod.GET)
  public ModelAndView print(HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<String, Object>();
    disPrintService.printDischargeSummary(params, requestMap, response);

    return new ModelAndView().addAllObjects(requestMap);
  }

}
