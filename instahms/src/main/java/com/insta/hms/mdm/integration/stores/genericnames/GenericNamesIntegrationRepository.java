package com.insta.hms.mdm.integration.stores.genericnames;

import com.insta.hms.common.AutoIdGenerator;
import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class GenericNamesIntegrationRepository extends BulkDataIntegrationRepository<String> {

  public GenericNamesIntegrationRepository() {
    super("generic_name", "generic_code", "integration_generic_name_id");
  }

  @Override
  public Object getNextId() {
    return AutoIdGenerator.getSequenceId("generic_sequence", "GENERICNAME");
  }

}
