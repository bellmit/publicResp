package com.insta.hms.mdm.integration.servicegroup;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ServiceGroupBulkDataEntity extends CsVBulkDataEntity {
  //TODO move all integration classes to independent package
  private static final String[] KEYS = new String[] { "integration_service_group_id" };

  private static String[] FIELDS = new String[] { "status", "service_group_name", "display_order",
      "username", "mod_time", "service_group_code" };

  public ServiceGroupBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
