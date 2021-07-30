package com.insta.hms.mdm.integration.controltype;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ControlTypeIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public ControlTypeIntegrationRepository() {
    super("store_item_controltype", "control_type_id", "integration_control_type_id");
  }
}
