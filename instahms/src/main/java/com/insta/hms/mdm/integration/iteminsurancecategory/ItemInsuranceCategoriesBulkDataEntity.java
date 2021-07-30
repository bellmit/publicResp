package com.insta.hms.mdm.integration.iteminsurancecategory;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ItemInsuranceCategoriesBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "integration_insurance_category_id" };
  private static final String[] FIELDS = new String[] { "insurance_category_name",
      "insurance_payable" };

  public ItemInsuranceCategoriesBulkDataEntity() {
    super(KEYS, FIELDS, null, null);
  }

}
