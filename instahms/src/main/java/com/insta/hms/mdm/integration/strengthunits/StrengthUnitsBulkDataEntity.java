package com.insta.hms.mdm.integration.strengthunits;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class StrengthUnitsBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_strength_unit_id" };
  private static final String[] FIELDS = new String[] { "unit_id", "unit_name", "status" };

  public StrengthUnitsBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
