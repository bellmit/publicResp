package com.insta.hms.core.clinical.order.master;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Map;

public interface OrgDetailsRepository {
  public BasicDynaBean getCodeDetails(String itemId, String orgId);
}
