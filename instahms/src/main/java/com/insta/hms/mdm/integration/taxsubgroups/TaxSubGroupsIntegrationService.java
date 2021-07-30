package com.insta.hms.mdm.integration.taxsubgroups;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.bulk.BulkDataIntegrationService;
import com.insta.hms.mdm.integration.taxgroups.TaxGroupsIntegrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaxSubGroupsIntegrationService extends BulkDataIntegrationService {

  @LazyAutowired
  private TaxGroupsIntegrationService taxGroupIntegrationService;

  public TaxSubGroupsIntegrationService(TaxSubGroupsIntegrationRepository repository,
      TaxSubGroupsIntegrationValidator validator, TaxSubgroupsIntegrationBulkDataEntity csvEntity) {
    super(repository, validator, csvEntity);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    Map<String, List<BasicDynaBean>> masterData = new HashMap<>();
    masterData.put("item_group_id", taxGroupIntegrationService.lookup(false));
    return masterData;
  }

  private static final String TAX_SUBGROUP_DETAILS = "select isg.item_subgroup_id,"
      + " item_subgroup_name, item_group_id, integration_subgroup_id,"
      + " tax_rate, validity_start, validity_end from item_sub_groups isg left join"
      + " item_sub_groups_tax_details isgtd on (isg.item_subgroup_id = isgtd.item_subgroup_id)"
      + " where isg.integration_subgroup_id = ?";

  public BasicDynaBean getTaxSubgroupsDetails(String integrationSubgroupId) {
    return DatabaseHelper.queryToDynaBean(TAX_SUBGROUP_DETAILS, integrationSubgroupId);
  }
}
