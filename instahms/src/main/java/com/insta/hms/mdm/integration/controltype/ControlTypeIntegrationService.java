package com.insta.hms.mdm.integration.controltype;

import com.insta.hms.mdm.bulk.BulkDataIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ControlTypeIntegrationService extends BulkDataIntegrationService {

  public ControlTypeIntegrationService(ControlTypeIntegrationRepository repository,
      ControlTypeIntegrationValidator validator, ControlTypeBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }
}
