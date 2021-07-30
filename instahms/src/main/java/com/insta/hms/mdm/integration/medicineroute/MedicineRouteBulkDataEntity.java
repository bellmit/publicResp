package com.insta.hms.mdm.integration.medicineroute;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class MedicineRouteBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_medicine_route_id" };
  private static final String[] FIELDS = new String[] { "route_id", "route_name", "status",
      "route_code" };

  public MedicineRouteBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
