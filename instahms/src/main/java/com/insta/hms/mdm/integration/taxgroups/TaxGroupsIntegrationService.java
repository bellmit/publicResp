package com.insta.hms.mdm.integration.taxgroups;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;
import com.insta.hms.mdm.bulk.CsvRowContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class TaxGroupsIntegrationService extends BulkDataIntegrationService {

  public TaxGroupsIntegrationService(TaxGroupsIntegrationRepository repository,
      TaxGroupsIntegrationValidator validator, TaxGroupsIntegrationBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
  
  @Override
  protected boolean preUpdate(CsvRowContext  csvRowContext) {
    BasicDynaBean bean = csvRowContext.getBean();
    bean.set("item_group_type_id", "TAX");
    return super.preUpdate(csvRowContext);
  }
}
