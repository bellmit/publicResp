package com.insta.hms.mdm.integration.servicegroup;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ServiceGroupIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  /**
   * Instantiates a new service group integration repository.
   */
  public ServiceGroupIntegrationRepository() {
    super("service_groups", "service_group_id", "integration_service_group_id",
        "service_group_name",
        new String[] { "service_group_id", "integration_service_group_id", "service_group_name" });
  }
}
