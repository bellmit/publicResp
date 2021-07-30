package com.insta.hms.integration.configuration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class InterfaceEventMappingRepository extends MasterRepository<Integer> {

  public InterfaceEventMappingRepository() {
    super("interface_event_mapping", "event_mapping_id");
  }

  private static final String GET_INCLUDE_EXCLUDE_RULE = "SELECT ierm.include, ierm.exclude"
      + " FROM interface_event_mapping iem JOIN interface_event_rule_master ierm"
      + " ON (iem.rule_id = ierm.rule_id AND ierm.status = 'A')"
      + " WHERE iem.event_mapping_id = ?";

  /**
   * Gets Include and exclude rule map.
   * 
   * @param eventMappingId the event map id
   * @return dyna bean
   */
  public BasicDynaBean getIncludeExcludeRule(int eventMappingId) {
    return DatabaseHelper.queryToDynaBean(GET_INCLUDE_EXCLUDE_RULE, new Object[] {eventMappingId});
  }
}
