package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;

import java.util.Arrays;
import java.util.List;

/**
 * The Class IncomingVisitConfidentialityValidator.
 */
@ConfidentialityValidator(queryParamNames = { "" }, urlEntityName = { "incomingVisitId" })
public class IncomingVisitConfidentialityValidator implements ConfidentialityInterface {

  @Override
  public List<String> getAssociatedMrNo(List<String> parameter) {
    return Arrays.asList("ISR");
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return true;
  }

}
