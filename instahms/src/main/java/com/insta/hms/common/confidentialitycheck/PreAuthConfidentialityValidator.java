package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ConfidentialityValidator(queryParamNames = "preauthprescid", urlEntityName = "preauth_presc_id")
public class PreAuthConfidentialityValidator implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(PreAuthConfidentialityValidator.class);

  @LazyAutowired
  private EAuthPrescriptionService eauthPrescriptionService;

  @Override
  public List<String> getAssociatedMrNo(List<String> preAuthPrescIds) {
    logger.debug("Checking confidentiality for preAuthPrescIds:"
        + ArrayUtils.toString(preAuthPrescIds));

    return eauthPrescriptionService.getMrNosByEAuthPrescId(preAuthPrescIds);
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return eauthPrescriptionService.isPreAuthPrescIdValid(parameter);
  }
}
