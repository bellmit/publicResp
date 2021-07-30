package com.insta.hms.mdm.integration.storecategory;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreCategoryIntegrationService extends BulkDataIntegrationService {

  public StoreCategoryIntegrationService(StoreCategoryIntegrationRepository repository,
      StoreCategoryIntegrationValidator validator,
      StoreCategoryIntegrationBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

}
