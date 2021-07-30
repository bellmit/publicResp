package com.insta.hms.mdm.integration.stores.genericnames;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenericNamesIntegrationService extends BulkDataIntegrationService {

  public GenericNamesIntegrationService(GenericNamesIntegrationRepository repository,
      GenericNamesIntegrationValidator validator, GenericNamesBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
}
