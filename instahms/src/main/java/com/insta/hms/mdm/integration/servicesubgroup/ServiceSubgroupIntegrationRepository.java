package com.insta.hms.mdm.integration.servicesubgroup;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ServiceSubgroupIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public ServiceSubgroupIntegrationRepository() {
    super("service_sub_groups", "service_sub_group_id", "integration_service_sub_group_id",
        "service_sub_group_name");
  }

}
