package com.insta.hms.mdm.integration.stores;

import com.insta.hms.common.AutoIdGenerator;
import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ManufacturerIntegrationRepository extends BulkDataIntegrationRepository<String> {

  public ManufacturerIntegrationRepository() {
    super("manf_master", "manf_code", "integration_manf_id", "manf_name");
  }

  @Override
  public Object getNextId() {
    return AutoIdGenerator.getNewId(getKeyColumn(), getTable(), "Manufacturer");
  }
}
