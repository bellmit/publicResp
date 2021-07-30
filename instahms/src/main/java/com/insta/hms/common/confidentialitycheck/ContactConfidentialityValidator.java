package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;

import java.util.Arrays;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "contactid" }, urlEntityName = { "contact" })
public class ContactConfidentialityValidator implements ConfidentialityInterface {

  @Override
  public List<String> getAssociatedMrNo(List<String> parameter) {
    return Arrays.asList("APPOINTMENT");
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return true;
  }
}
