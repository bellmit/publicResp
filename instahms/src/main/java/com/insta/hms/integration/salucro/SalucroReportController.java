package com.insta.hms.integration.salucro;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SalucroController.
 */
@RestController
@RequestMapping("/salucroreports")
public class SalucroReportController extends BaseRestController {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(SalucroReportController.class);

  /** The salucro util. */
  @LazyAutowired
  private SalucroUtil salucroUtil;

  /** The service. */
  @LazyAutowired
  private SalucroService service;

  /**
   * Gets the salucro configurations index page.
   *
   * @return the salucro configurations index page
   */
  @IgnoreConfidentialFilters
  @GetMapping("/index")
  public ModelAndView getSalucroReportsIndexPage() {
    return renderFlowUi(
        "Salucro", "salucroIntegration", "withFlow", "salucroFlow", "salucroreports", false);
  }


  @IgnoreConfidentialFilters
  @GetMapping("/fetch")
  public Map<String, String> fetchReportUrl() throws Exception {
    return service.getSalucroReportDetails();
  }

}