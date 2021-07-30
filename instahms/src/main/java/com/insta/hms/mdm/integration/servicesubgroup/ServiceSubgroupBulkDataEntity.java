package com.insta.hms.mdm.integration.servicesubgroup;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ServiceSubgroupBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_service_sub_group_id" };
  private static final String[] FIELDS = new String[] { "integration_service_sub_group_id",
      "service_sub_group_id", "status", "service_group_id", "service_sub_group_name",
      "display_order", "username", "mod_time", "service_sub_group_code", "account_head_id",
      "eligible_to_earn_points", "eligible_to_redeem_points", "redemption_cap_percent" };

  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("service_group_id", "service_groups", "service_group_id",
          "integration_service_group_id"), };

  public ServiceSubgroupBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }

}
