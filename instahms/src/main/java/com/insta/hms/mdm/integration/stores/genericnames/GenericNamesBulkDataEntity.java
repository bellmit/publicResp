package com.insta.hms.mdm.integration.stores.genericnames;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class GenericNamesBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_generic_name_id" };
  private static final String[] FIELDS = new String[] { "generic_name", "status" };

  public GenericNamesBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
