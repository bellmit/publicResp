package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ConfidentialityValidator(queryParamNames = "pbmprescid", urlEntityName = "pbmPrescription")
public class PBMConfidentialityValidator implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(PBMConfidentialityValidator.class);

  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  @Override
  public List<String> getAssociatedMrNo(List<String> pbmPrescIds) {
    logger.debug("Checking confidentiality for pbmPrescIds:"
        + ArrayUtils.toString(pbmPrescIds));
    return pbmPrescriptionsService.getMrNosByPBMPrescriptionId(pbmPrescIds);
  }

  @Override
  public Boolean isValidParameter(String pbmPrescId) {
    return pbmPrescriptionsService.isPBMPrescIdValid(pbmPrescId);
  }
}
