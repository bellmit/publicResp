package com.insta.hms.api.controllers;

import com.insta.hms.api.services.AccountingHeadsListService;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class AccountingHeadsListController.
 */
@RequestMapping("/api")
public class AccountingHeadsListController extends BaseRestController {

  /** The accounting heads list service. */
  @Autowired
  private AccountingHeadsListService accountingHeadsListService;

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(AccountingHeadsListController.class);

  /** The Constant CONSTANTHEADS. */
  private static final List<String> CONSTANTHEADS = Collections.unmodifiableList(Arrays.asList(
      "Counter Receipts", "Write Off A/C", "Reward Points A/C",
      "General Deposit Liability Account", "Package Deposit Liability Account",
      "IP Deposit Liability Account", "Payment Due A/C", "Inventory A/C", "Discounts A/C",
      "Roundoff A/C", "Sponsor Adjustment A/C", "Internal Consumption A/C", "Tax Liability A/C",
      "Tax Paid A/C", "Bank A/C'", "Payment Due A/C"));

  /**
   * Gets the accounting heads names.
   *
   * @param source
   *          the source
   * @param request
   *          the request
   * @return the accounting heads names
   */
  @GetMapping(value = "/accountingheads/{headssource}")
  @SuppressWarnings("unchecked")
  public ResponseEntity<List> getAccountingHeadsNames(@PathVariable("headssource") String source,
      HttpServletRequest request) {
    Map<String, String[]> requestParamMap = request.getParameterMap();

    List<Map<String, String>> accountHeadsList = new ArrayList<>();
    if (StringUtils.isEmpty(source)) {
      // return empty JSON
      return new ResponseEntity<List>(accountHeadsList, HttpStatus.OK);
    }

    if ("system_defaults".equals(source)) {
      for (String constantHead : CONSTANTHEADS) {
        Map<String, String> sysDefaults = new HashMap<>();
        sysDefaults.put("account_head_name", constantHead);
        accountHeadsList.add(sysDefaults);
      }
    } else {
      accountHeadsList = ConversionUtils.listBeanToListMap(accountingHeadsListService
          .getAccountingHeadsNames(source, requestParamMap));
    }
    return new ResponseEntity<List>(accountHeadsList, HttpStatus.OK);
  }

}
