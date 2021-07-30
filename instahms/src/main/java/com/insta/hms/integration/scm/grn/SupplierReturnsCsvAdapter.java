package com.insta.hms.integration.scm.grn;

import com.insta.hms.integration.scm.AbstractCsvAdapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SupplierReturnsCsvAdapter extends AbstractCsvAdapter {

  public SupplierReturnsCsvAdapter(SupplierDebitNotesCsvContext context) {
    super(context);
  }

  @Override
  public Map<String, Object> mapToJobData(Map<String, Object> map) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put(SupplierDebitNotesCsvContext.FIELD_TRANSACTION_TYPE, getTransactionType());
    jobData.put(SupplierDebitNotesCsvContext.FIELD_STORE_NAME, map.get("dept_name"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_SUPPLIER_NAME, map.get("supplier_name"));
    jobData
        .put(SupplierDebitNotesCsvContext.FIELD_CUST_SUPPLIER_CODE, map.get("cust_supplier_code"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_DEBIT_NOTE_DATETIME,
        map.get("debit_note_datetime"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_DEBIT_NOTE_NO, map.get("debit_note_no"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_UOM, map.get("grn_qty_unit"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_RETURN_REASON, map.get("return_type"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_REMARKS, map.get("remarks"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_ITEM_ID, map.get("cust_item_code"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_ITEM_BATCH_NO, map.get("batch_no"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_QUANTITY, map.get("qty"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_BONUS_QUANTITY, map.get("bonus_qty"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_COST, map.get("cost_price"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_EXPIRY_DATE, map.get("exp_date"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_TAX_AMOUNT, map.get("tax"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_ITEM_DISCOUNT, map.get("itemdiscount"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_BILL_DISCOUNT, map.get("dbt_discount"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_OTHER_CHARGES, map.get("other_charges"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_OTHER_CHARGES_REMARKS,
        map.get("other_charges_remarks"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_ROUNDOFF, map.get("round_off"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_USERNAME, map.get("user_name"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_MRP, map.get("mrp"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_TAX_SUB_GROUPS, map.get("tax_subgroups"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_TAX_PERCENTAGE, map.get("tax_rate"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_CONSIGNMENT_NO, map.get("consignment_no"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_CONSIGNMENT_COMPANY, map.get("company_name"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_RETURN_DETAIL_NO, map.get("return_detail_no"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_CONSIGNMENT_DATE, map.get("consignment_date"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_OTHER_REASON, map.get("other_reason"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_GRN_NO, map.get("grn_no"));
    jobData.put(SupplierDebitNotesCsvContext.FIELD_RECEIVED_DEBIT_AMOUNT,
        map.get("received_debit_amt"));


    return jobData;
  }

  @Override
  protected String getTransactionType() {
    return "STOCK_RETURNS";
  }
}
