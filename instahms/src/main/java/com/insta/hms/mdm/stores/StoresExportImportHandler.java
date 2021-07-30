package com.insta.hms.mdm.stores;

import com.insta.hms.csvutils.CSVHandler;

import org.springframework.stereotype.Component;

/**
 * The Class StoresExportImportHandler.
 */
@Component
public final class StoresExportImportHandler extends CSVHandler {
  
  /** The Constant TABLE. */
  private static final String TABLE = "stores";
  
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

  // our field ref table ref table id field ref table
  /** The Constant MASTERS. */
  // name field
  private static final String[][] MASTERS = new String[][] {
      { "counter_id", "counters", "counter_id", "counter_no" },
      { "account_group", "account_group_master", "account_group_id", "account_group_name" },
      { "store_type_id", "store_type_master", "store_type_id", "store_type_name" },
      { "center_id", "hospital_center_master", "center_id", "center_name" },
      { "store_rate_plan_id", "store_rate_plans", "store_rate_plan_id", "store_rate_plan_name" }, };

  /**
   * Instantiates a new stores export import handler.
   */
  public StoresExportImportHandler() {
    super(TABLE, KEYS, FIELDS, MASTERS, null);
    setSequenceName("stores");
    setAlias("store_rate_plan_id", "store_tariff_name");
  }

}