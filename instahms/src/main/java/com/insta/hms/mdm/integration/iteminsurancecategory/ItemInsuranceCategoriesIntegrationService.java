package com.insta.hms.mdm.integration.iteminsurancecategory;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemInsuranceCategoriesIntegrationService extends BulkDataIntegrationService {

  public ItemInsuranceCategoriesIntegrationService(
      ItemInsuranceCategoriesIntegrationRepository repository,
      ItemInsuranceCategoriesIntegrationValidator validator,
      ItemInsuranceCategoriesBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

}
