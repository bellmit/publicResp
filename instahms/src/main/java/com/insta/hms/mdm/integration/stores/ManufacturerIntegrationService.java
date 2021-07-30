package com.insta.hms.mdm.integration.stores;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.springframework.stereotype.Service;

@Service
public class ManufacturerIntegrationService extends BulkDataIntegrationService {

  public ManufacturerIntegrationService(ManufacturerIntegrationRepository repository,
      ManufacturerIntegrationValidator validator, ManufacturerBulkDataIntegrationEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

}
