package com.insta.hms.mdm.integration.packageuom;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PackageUomIntegrationService extends BulkDataIntegrationService {

  public PackageUomIntegrationService(PackageUomIntegrationRepository repository,
      PackageUomIntegrationValidator validator, PackageUomIntegrationEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return getRepository().findByKey(filterMap);
  }
}
