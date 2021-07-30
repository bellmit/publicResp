package com.insta.hms.mdm.integration.servicegroup;

import com.bob.hms.common.Constants;
import com.insta.hms.mdm.bulk.BulkDataIntegrationService;
import com.insta.hms.mdm.bulk.CsvRowContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class ServiceGroupIntegrationService extends BulkDataIntegrationService {

  public ServiceGroupIntegrationService(ServiceGroupIntegrationRepository repository,
      ServiceGroupIntegrationValidator validator, ServiceGroupBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  @Override
  protected boolean preUpdate(CsvRowContext csvRowContext) {
    BasicDynaBean bean = csvRowContext.getBean();
    bean.set("username", Constants.API_USERNAME);
    bean.set("mod_time", new Timestamp(new Date().getTime()));
    return true;
  }

}
