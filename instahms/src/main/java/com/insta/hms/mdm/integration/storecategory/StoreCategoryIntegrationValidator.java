package com.insta.hms.mdm.integration.storecategory;

import com.insta.hms.mdm.integration.IntegrationValidator;

import org.springframework.stereotype.Component;

@Component
public class StoreCategoryIntegrationValidator extends IntegrationValidator {

  private static final String[] MANDATORY_FIELDS = new String[] { "integration_category_id",
      "category", "identification", "issue_type", "billable", "status", "claimable",
      "expiry_date_val", "retailable", "discount", "purchases_cat_vat_account_prefix",
      "purchases_cat_cst_account_prefix", "sales_cat_vat_account_prefix", "asset_tracking" };

  public StoreCategoryIntegrationValidator(StoreCategoryIntegrationRepository repository) {
    super(repository);
  }

  @Override
  protected String[] getMandatoryFields() {
    return MANDATORY_FIELDS;
  }

  @Override
  protected String[] getNonUpdatableFields() {
    return null;
  }

}
