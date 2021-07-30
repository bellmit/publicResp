package com.insta.hms.stores;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class PODescProvider implements AuditLogDescProvider {

  public PODescProvider() {
  }

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    Map poStatusMap = new LinkedHashMap(); // PO status is a set static values
    poStatusMap.put("X", "Cancel");
    poStatusMap.put("C", "Closed");
    poStatusMap.put("FC", "Force Closed");
    poStatusMap.put("O", "Open");
    poStatusMap.put("AO", "Amended Open");
    poStatusMap.put("V", "Validated");
    poStatusMap.put("AV", "Amended Validated");
    poStatusMap.put("A", "Approved");
    poStatusMap.put("AA", "Amended Approved");

    Map mrpTypeMap = new LinkedHashMap();
    mrpTypeMap.put("I", "Inclusion Of Tax");
    mrpTypeMap.put("E", "Exclusion Of Tax");

    Map discountTypeMap = new LinkedHashMap();
    discountTypeMap.put("A", "Amount");
    discountTypeMap.put("P", "Percentage");

    Map poQtyUnitMap = new LinkedHashMap();
    poQtyUnitMap.put("P", "Package UOM");
    poQtyUnitMap.put("I", "Unit UOM");

    // Set the display names for all the audited fields. This follows from the fields that were
    // specified in the trigger
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("po_no", "PO No");
    desc.addField("po_date", "PO Date");
    desc.addField("qut_date", "Quotation Date");
    desc.addField("qut_no", "Quotation No");
    desc.addField("supplier_id", "Supplier Name");
    desc.addField("reference", "Reference");
    desc.addField("vat_rate", "Tax Rate");
    desc.addField("po_total", "PO Total");
    desc.addField("status", "Status");
    desc.addField("supplier_terms", "Supplier Terms");
    desc.addField("hospital_terms", "Hospital Terms");
    desc.addField("actual_po_date", "Actual PO Date");
    desc.addField("user_id", "User Name");
    desc.addField("vat_type", "Tax Type");
    desc.addField("mrp_type", "MRP Type");
    desc.addField("store_id", "Store Name");
    desc.addField("approved_by", "Approved By");
    desc.addField("approved_time", "Approved Time");
    desc.addField("approver_remarks", "Approver Remarks");
    desc.addField("closure_reasons", "Closure Reasons");
    desc.addField("credit_period", "Credit Period");
    desc.addField("delivery_date", "Delivery Date");
    desc.addField("round_off", "Round Off");
    desc.addField("discount_type", "Discount Type");
    desc.addField("discount_per", "Discount Per");
    desc.addField("discount", "Discount");
    desc.addField("po_qty_unit", "PO Qty Unit");
    desc.addField("remarks", "Remarks");
    desc.addField("dept_id", "Dept Name");
    desc.addField("delivery_instructions", "Delivery Instructions");
    desc.addField("enq_no", "Enquire No");
    desc.addField("enq_date", "Enquire Date");
    desc.addField("validated_by", "Validated By");
    desc.addField("validated_time", "Validated Time");
    desc.addField("validator_remarks", "Validator Remarks");
    desc.addField("po_alloted_to", "PO Alloted To");
    desc.addField("amended_reason", "Amended Reason");
    desc.addField("amendment_time", "Amendment Time");
    desc.addField("amendment_validated_time", "Amendment Validated Time");
    desc.addField("amendment_approved_time", "Amendment Approved Time");
    desc.addField("amended_by", "Amended By");
    desc.addField("amendment_validated_by", "Amendment Validated By");
    desc.addField("amendment_approved_by", "Amendment Approved By");
    desc.addField("amendment_validator_remarks", "Amendment Validator Remarks");
    desc.addField("amendment_approver_remarks", "Amendment Approver Remarks");
    desc.addField("cancelled_by", "Cancelled By");
    desc.addField("quotation_file_name", "Uploaded File Name");
    desc.addField("transportation_charges", "Transportation Charges");

    desc.addFieldValue("status", poStatusMap);
    desc.addFieldValue("mrp_type", mrpTypeMap);
    desc.addFieldValue("discount_type", discountTypeMap);
    desc.addFieldValue("po_qty_unit", poQtyUnitMap);

    desc.addFieldValue("supplier_id", "supplier_master", "supplier_code", "supplier_name");
    desc.addFieldValue("store_id", "stores", "dept_id", "dept_name");
    desc.addFieldValue("dept_id", "department", "dept_id", "dept_name");

    return desc;
  }

}
