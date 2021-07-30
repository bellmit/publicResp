package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.core.diagnostics.DiagnosticsService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The Class ReportIdConfidentialityValidator. Confidentiality validator for Laboratory and
 * Radiology report id
 */
@ConfidentialityValidator(queryParamNames = { "reportid", "revertsignoff" }, urlEntityName = {
    "reportid" })
public class ReportIdConfidentialityValidator implements ConfidentialityInterface {

  /** The diagnostics service. */
  @Autowired
  private DiagnosticsService diagnosticsService;

  @Override
  public List<String> getAssociatedMrNo(List<String> reportId) {
    return diagnosticsService.getAssociatedMrNoForReportId(reportId);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return diagnosticsService.isReportIdValid(parameter);
  }

}
