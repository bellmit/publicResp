package com.insta.hms.mdm.integration.itemforms;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.springframework.stereotype.Service;

@Service
public class ItemFormIntegrationService extends BulkDataIntegrationService {

  public ItemFormIntegrationService(ItemFormIntegrationRepository repository,
      ItemFormIntegrationValidator validator, ItemFormBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
}
