package com.insta.hms.mdm.integration.storecategory;

import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class StoreCategoryIntegrationBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_category_id" };
  private static final String[] FIELDS = new String[] { "category", "identification", "issue_type",
      "billable", "status", "claimable", "expiry_date_val", "retailable", "discount",
      "purchases_cat_vat_account_prefix", "purchases_cat_cst_account_prefix",
      "sales_cat_vat_account_prefix", "asset_tracking" };

  public StoreCategoryIntegrationBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
