package com.insta.hms.mdm.integration.strengthunits;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StrengthUnitsIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public StrengthUnitsIntegrationRepository() {
    super("strength_units", "unit_id", "integration_strength_unit_id");
  }
  
}
