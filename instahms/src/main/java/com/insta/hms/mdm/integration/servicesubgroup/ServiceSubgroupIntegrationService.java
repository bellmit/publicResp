package com.insta.hms.mdm.integration.servicesubgroup;

import com.bob.hms.common.Constants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.bulk.BulkDataIntegrationService;
import com.insta.hms.mdm.bulk.CsvRowContext;
import com.insta.hms.mdm.integration.servicegroup.ServiceGroupIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceSubgroupIntegrationService extends BulkDataIntegrationService {

  @LazyAutowired
  ServiceGroupIntegrationService serviceGroupIntegrationService;

  public ServiceSubgroupIntegrationService(ServiceSubgroupIntegrationRepository repository,
      ServiceSubgroupIntegrationValidator validator, ServiceSubgroupBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    Map<String, List<BasicDynaBean>> masterData = new HashMap<>();
    masterData.put("service_group_id", serviceGroupIntegrationService.lookup(false));
    return masterData;
  }

  @Override
  protected boolean preUpdate(CsvRowContext csvRowContext) {
    BasicDynaBean bean = csvRowContext.getBean();
    bean.set("username", Constants.API_USERNAME);
    bean.set("mod_time", new Timestamp(new Date().getTime()));
    return true;
  }

}
