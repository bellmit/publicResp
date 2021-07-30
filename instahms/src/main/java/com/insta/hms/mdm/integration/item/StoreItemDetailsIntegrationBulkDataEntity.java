package com.insta.hms.mdm.integration.item;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class StoreItemDetailsIntegrationBulkDataEntity extends CsVBulkDataEntity {

  private static final String[] KEYS = new String[] { "cust_item_code" };
  private static final String[] FIELDS = new String[] { "medicine_name", "medicine_short_name",
      "med_category_id", "item_barcode_id", "manf_name", "generic_name",
      "status", "max_cost_price", "package_type", "package_uom", "issue_units",
      "batch_no_applicable", "control_type_id", "value", "supplier_name", "invoice_details",
      "code_desc", "high_cost_consumable", "allow_zero_claim_amount", "prior_auth_required",
      "item_selling_price", "bin", "item_strength_units", "route_of_admin", "consumption_uom",
      "consumption_capacity", "item_form_id", "item_strength",
      "tax_type" };

  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("med_category_id", "store_category_master", "category_id",
          "integration_category_id"),
      new BulkDataMasterEntity("manf_name", "manf_master", "manf_code", "integration_manf_id"),
      new BulkDataMasterEntity("generic_name", "generic_name", "generic_code",
          "integration_generic_name_id"),
      new BulkDataMasterEntity("control_type_id", "store_item_controltype", "control_type_id",
          "integration_control_type_id"),
      new BulkDataMasterEntity("item_form_id", "item_form_master", "item_form_id",
          "integration_form_id"),
      };

  public StoreItemDetailsIntegrationBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }

}
