package com.insta.hms.stores;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class StoresItemBatchDescProvider implements AuditLogDescProvider {

  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("item_batch_id", "Item Batch ID", false);
    desc.addField("medicine_id", "Medicine", false);
    desc.addField("batch_no", "Batch No", false);
    desc.addField("exp_dt", "Expiry Date");
    desc.addField("mrp", "MRP");

    desc.addFieldValue("medicine_id", "store_item_details", "medicine_id", "medicine_name");
    return desc;
  }
}
