package com.insta.hms.mdm.contracttypes;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class ContractTypeValidator extends MasterValidator {

  private static NotNullRule NOT_NULL_RULE = new NotNullRule();
  
  public ContractTypeValidator() {
    super();
    addDefaultRule(NOT_NULL_RULE, new String[] { "contract_type" });
  }
}
