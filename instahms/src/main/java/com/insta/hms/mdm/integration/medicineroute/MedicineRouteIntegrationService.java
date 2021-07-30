package com.insta.hms.mdm.integration.medicineroute;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.springframework.stereotype.Service;

@Service
public class MedicineRouteIntegrationService extends BulkDataIntegrationService {

  public MedicineRouteIntegrationService(MedicineRouteIntegrationRepository repository,
      MedicineRouteIntegrationValidator validator, MedicineRouteBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
}
