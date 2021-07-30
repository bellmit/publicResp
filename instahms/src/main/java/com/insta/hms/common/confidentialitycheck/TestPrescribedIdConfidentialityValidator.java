package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.core.diagnostics.DiagnosticsService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The Class TestPrescribedIdConfidentialityValidator. This class validate confidentiality for
 * Laboratory and Radiology prescription id
 */
@ConfidentialityValidator(queryParamNames = { "prescribedid" }, urlEntityName = { "prescribedid" })
public class TestPrescribedIdConfidentialityValidator implements ConfidentialityInterface {

  /** The diagnostics service. */
  @Autowired
  private DiagnosticsService diagnosticsService;

  @Override
  public List<String> getAssociatedMrNo(List<String> prescribedId) {
    return diagnosticsService.getAssociatedMrNoForTestsPrescribedId(prescribedId);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return diagnosticsService.isTestsPrescriptionIdValid(parameter);
  }

}
