package com.insta.hms.integration.salucro;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SalucroController.
 */
@RestController
@RequestMapping("/salucrotransactions")
public class SalucroTransactionController extends BaseRestController {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(SalucroTransactionController.class);

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
  public ModelAndView getSalucroTransactionsIndexPage() {
    return renderFlowUi(
        "Salucro", "salucroIntegration", "withFlow", "salucroFlow", "salucrotransactions", false);
  }

  /**
   * Fetch transaction url.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @GetMapping("/fetch")
  public ResponseEntity<Map<String, Object>> fetchTransactionUrl(
          HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, String> responseMap = new HashMap<String, String>();
    responseMap = service.getSalucroTransactionDetails(request);
    return new ResponseEntity<Map<String, Object>>(
          new HashMap<String,Object>(responseMap), HttpStatus.OK);

  }

  /**
   * Fetch Transactions Based on Filter.
   *
   * @return the response entity
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/fetchtransactions")
  public ResponseEntity<Map<String, Object>> fetchTransactionDateFilterDetails(
        HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map paramMap = request.getParameterMap();
    PagedList pagedList = service.fetchTransaction(
        paramMap, ConversionUtils.getListingParameter(paramMap));
    if ( pagedList == null ) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }
}
