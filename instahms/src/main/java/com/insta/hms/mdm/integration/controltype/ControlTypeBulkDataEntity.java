package com.insta.hms.mdm.integration.controltype;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ControlTypeBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_control_type_id" };
  private static final String[] FIELDS = new String[] { "control_type_id", "control_type_name" };

  public ControlTypeBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
