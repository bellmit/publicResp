package com.insta.hms.mdm.bulk;

import com.insta.hms.common.GenericRepository;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.integration.CsvImportable;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkDataIntegrationService extends BulkDataService implements CsvImportable {

  Logger logger = LoggerFactory.getLogger(BulkDataIntegrationService.class);
  private static final Map<String, List<BasicDynaBean>> EMPTY_MASTER_DATA = Collections.emptyMap();

  public BulkDataIntegrationService(BulkDataIntegrationRepository<?> repository,
      MasterValidator validator, CsVBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return EMPTY_MASTER_DATA;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer update(BasicDynaBean bean) {
    Map<String, Object> keys = new HashMap<String, Object>();
    validator.validateUpdate(bean);
    String keyColumn = repository.getKeyColumn();
    String integrationKeyColumn = null;
    if (repository instanceof BulkDataIntegrationRepository) {
      integrationKeyColumn = ((BulkDataIntegrationRepository<?>) repository)
          .getIntegrationKeyColumn();
    }
    if (integrationKeyColumn != null) {
      keys.put(integrationKeyColumn, bean.get(integrationKeyColumn));
    }
    BasicDynaBean existingBean = findByIntegrationId(bean.get(integrationKeyColumn));
    if (existingBean != null) {
      bean.set(keyColumn, existingBean.get(keyColumn));
    }
    return repository.update(bean, keys);
  }

  @Override
  public String importCsv(InputStreamReader csvStreamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException {
    return importData(csvStreamReader, feedback);

  }

  /**
   * Find by integration id.
   *
   * @param integrationId
   *          the integration id
   * @return the bean
   */
  public BasicDynaBean findByIntegrationId(Object integrationId) {
    MasterRepository<?> repository = getRepository();

    if (repository instanceof BulkDataIntegrationRepository) {
      return ((BulkDataIntegrationRepository<?>) repository).findByIntegrationId(integrationId);
    }
    return null;
  }

}
