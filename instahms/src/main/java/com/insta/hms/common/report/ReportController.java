package com.insta.hms.common.report;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(UrlRoute.REPORTS)
public class ReportController extends BaseRestController {
  @LazyAutowired
  ReportService reportService;

  /**
   * Gets filter values.
   *
   * @param fieldName  the field name
   * @param reportName the report name
   * @param provider   the provider
   * @param isCustom   the is custom
   * @param limit      the limit
   * @return the filter values
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @GetMapping("/getFilterValues")
  public ResponseEntity getFilterValues(@RequestParam(name = "fieldName") String fieldName,
      @RequestParam(name = "reportName") String reportName,
      @RequestParam(name = "reportProvider", required = false) String provider,
      @RequestParam(name = "isCustom", required = false) Boolean isCustom,
      @RequestParam(name = "limit", required = false) Integer limit) throws Exception {
    return new ResponseEntity(
        reportService.getFilterValues(reportName, provider, fieldName, isCustom, limit),
        HttpStatus.OK);
  }


}
