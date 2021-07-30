package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.insurance.InsuranceClaimService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ConfidentialityValidator(queryParamNames = { "claimid" }, urlEntityName = "claim")
public class ClaimConfidentialityValidator implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(ClaimConfidentialityValidator.class);

  @LazyAutowired
  private BillClaimService billClaimService;
  
  @LazyAutowired
  private InsuranceClaimService insuranceClaimService;

  @Override
  public List<String> getAssociatedMrNo(List<String> claimList) {
    logger.debug("Checking confidentiality for claim ids :" + ArrayUtils.toString(claimList));
    return billClaimService.getMrNosByClaimId(claimList);
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return insuranceClaimService.isClaimIdExist(parameter);
  }
}
