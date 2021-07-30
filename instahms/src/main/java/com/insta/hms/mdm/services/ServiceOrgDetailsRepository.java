package com.insta.hms.mdm.services;

import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.order.master.OrgDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ServiceOrgDetailsRepository extends GenericRepository implements OrgDetailsRepository {

  /**
   * Instantiates a new {@link ServiceOrgDetailsRepository}.
   *
   */
  public ServiceOrgDetailsRepository() {
    super("service_org_details");
  }

  @Override
  public BasicDynaBean getCodeDetails(String itemId, String orgId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("service_id", itemId);
    filterMap.put("org_id", orgId);
    return findByKey(filterMap);
  }
}
