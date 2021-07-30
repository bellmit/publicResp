package com.insta.hms.common;

import org.apache.commons.beanutils.BasicDynaBean;

public interface Validator {
  // public BasicDynaBean validateRequest(Map<String, String[]> params);
  public boolean validate(BasicDynaBean bean);
  // public boolean validateUpdate(BasicDynaBean bean);
  // public boolean validateInsert(BasicDynaBean bean);
}
