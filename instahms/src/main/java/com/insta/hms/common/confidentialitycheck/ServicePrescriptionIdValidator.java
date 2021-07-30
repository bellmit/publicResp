package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.core.diagnostics.DiagnosticsService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The Class ServicePrescriptionIdValidator. This class validate confidentiality for Services
 * prescription id
 */
@ConfidentialityValidator(queryParamNames = { "prescriptionid" }, urlEntityName = {
    "prescriptionid" })
public class ServicePrescriptionIdValidator implements ConfidentialityInterface {

  /** The diagnostics service. */
  @Autowired
  private DiagnosticsService diagnosticsService;

  @Override
  public List<String> getAssociatedMrNo(List<String> prescribedId) {
    return diagnosticsService.getAssociatedMrNoForServicePrescribedId(prescribedId);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return diagnosticsService.isServicePrescribedIdValid(parameter);
  }
}
