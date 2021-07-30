package com.insta.hms.common.confidentialitycheck;

import java.util.List;

public interface ConfidentialityInterface {
  
  public List<String> getAssociatedMrNo(List<String> parameter);

  public Boolean isValidParameter(String parameter);
}
