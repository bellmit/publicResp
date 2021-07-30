package com.insta.hms.stores;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

public class StockTakeAuditViewDescProvider extends MultiAuditLogDescProvider {

  private static final String[] STOCK_TAKE_AUDIT_VIEW_TABLES = new String[] {
      "physical_stock_take_audit_log", "physical_stock_take_detail_audit_log" };

  public StockTakeAuditViewDescProvider() {
    super(STOCK_TAKE_AUDIT_VIEW_TABLES);
  }

  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = super.getAuditLogDesc(tableName);
    // Create all the static lookup maps
    Map statusMap = new HashMap(); // Bill status is a set static values
    statusMap.put("I", "Initiated");
    statusMap.put("C", "Completed");
    statusMap.put("X", "Cancelled");
    statusMap.put("R", "Reconciled");
    statusMap.put("A", "Approved");
    statusMap.put("P", "In Progress");

    // First add all the key fields
    desc.addField("stock_take_id", "Stock Take Number", false);
    desc.addField("stock_take_detail_id", "Stock Take Item", false);
    desc.addField("item_batch_id", "Batch No", false);
    desc.addFieldValue("item_batch_id", "store_item_batch_details",
        "item_batch_id", "batch_no");
        // TODO : We need a way to get medicine name for display

    // =================== STOCK TAKE DESCRIPTION ==========================
    desc.addField("stock_take_initiated_by", "Initiated By", true);
    desc.addField("stock_take_initiated_datetime", "Initiated Date / Time",
        true);
    desc.addField("stock_take_completed_by", "Completed By", true);
    desc.addField("stock_take_completed_datetime", "Completion Date / Time",
        true);
    desc.addField("stock_take_reconciled_by", "Reconciled By", true);
    desc.addField("stock_take_reconciled_datetime",
        "Reconciliation Date / Time", true);
    desc.addField("stock_take_approved_by", "Approved By", true);
    desc.addField("stock_take_approved_datetime", "Approval Date / Time", true);
    desc.addField("stock_take_cancelled_by", "Cancelled By", true);
    desc.addField("stock_take_cancelled_datetime",
        "Cancellation Date / Time", true);
    desc.addField("stock_take_status", "Status");
    desc.addFieldValue("stock_take_status", statusMap);

    // Add all lookup values from a db table
    // desc.addFieldValue("bill_discount_auth", "discount_authorizer",
    // "disc_auth_id", "disc_auth_name");

    // ====================STOCK TAKE DETAIL DESCRIPTION=====================

    // Set the display names for all the audited fields. This follows from
    // the fields that were specified in the trigger

    desc.addField("stock_take_detail_physical_stock_qty", "Physical Stock Qty",
        false);
    desc.addField("stock_take_detail_system_stock_qty", "System Stock Qty",
        false);
    desc.addField("stock_take_detail_recorded_datetime", "Recorded Date / Time",
        true);
    desc.addField("stock_take_detail_stock_adjustment_reason_id",
        "Remarks", true);
    desc.addField("stock_take_detail_item_batch_id", "Batch No.", true);
    desc.addField("stock_take_detail_stock_take_detail_id", "Stock Take Item", false, true);
    // Now set the lookup value mapping wherever necessary
    desc.addFieldValue("stock_take_detail_stock_adjustment_reason_id",
        "stock_adjustment_reason_master", "adjustment_reason_id",
        "adjustment_reason");
    desc.addFieldValue("stock_take_detail_item_batch_id",
        "store_item_batch_details", "item_batch_id", "batch_no");

    return desc;
  }
}

