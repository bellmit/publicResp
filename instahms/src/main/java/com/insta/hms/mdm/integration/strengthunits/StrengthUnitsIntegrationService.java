package com.insta.hms.mdm.integration.strengthunits;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.springframework.stereotype.Service;

@Service
public class StrengthUnitsIntegrationService extends BulkDataIntegrationService {

  public StrengthUnitsIntegrationService(StrengthUnitsIntegrationRepository repository,
      StrengthUnitsIntegrationValidator validator, StrengthUnitsBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
}
