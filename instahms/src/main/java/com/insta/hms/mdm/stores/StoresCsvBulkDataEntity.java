package com.insta.hms.mdm.stores;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

/**
 * The Class StoresCsvBulkDataEntity.
 * 
 * @author tanmay.k
 */
@Component("storesCsvEntity")
public class StoresCsvBulkDataEntity extends CsVBulkDataEntity {

  /** The Constant KEYS. */
  private static final String[] KEYS = new String[] { "dept_id" };

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] { "dept_name", "counter_id", "status",
      "pharmacy_tin_no", "pharmacy_drug_license_no", "account_group", "store_type_id",
      "is_super_store", "is_sterile_store", "sale_unit", "center_id", "allowed_raise_bill",
      "is_sales_store", "auto_fill_indents", "auto_fill_prescriptions",
      "purchases_store_vat_account_prefix", "purchases_store_cst_account_prefix",
      "sales_store_vat_account_prefix", "store_rate_plan_id", "use_batch_mrp",
      "auto_po_generation_frequency_in_days", "allow_auto_po_generation",
      "auto_cancel_po_frequency_in_days", "allow_auto_cancel_po" };

  /**
   * The Constant MASTERS with fields referencedField, referencedTable, referencedTablePK and
   * referencedTableNameField.
   */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("counter_id", "counters", "counter_id", "counter_no"),
      new BulkDataMasterEntity("account_group", "account_group_master", "account_group_id",
          "account_group_name"),
      new BulkDataMasterEntity("store_type_id", "store_type_master", "store_type_id",
          "store_type_name"),
      new BulkDataMasterEntity("center_id", "hospital_center_master", "center_id", "center_name"),
      new BulkDataMasterEntity("store_rate_plan_id", "store_rate_plans", "store_rate_plan_id",
          "store_rate_plan_name") };

  /**
   * Instantiates a new stores CSV bulk data entity.
   */
  public StoresCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
    super.setAlias("store_rate_plan_id", "store_tariff_name");
  }

}