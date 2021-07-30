package com.insta.hms.mdm.integration.stores;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ManufacturerBulkDataIntegrationEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_manf_id" };

  private static final String[] FIELDS = { "manf_name", "manf_address", "manf_city", "manf_state",
      "manf_country", "manf_pin", "manf_phone1", "manf_phone2", "manf_fax", "manf_mailid",
      "manf_website", "status", "manf_mnemonic", "pharmacy", "inventory" };

  public ManufacturerBulkDataIntegrationEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
