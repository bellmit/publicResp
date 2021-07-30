package com.insta.hms.integration.scm.grn;

import com.insta.hms.integration.scm.CsvContext;

import org.springframework.stereotype.Component;

@Component
public class SupplierDebitNotesCsvContext extends CsvContext {

  private static String ENTITY_NAME = "supplier_return";

  public static final String FIELD_TRANSACTION_TYPE = "transaction_type";
  public static final String FIELD_STORE_NAME = "store_name";
  public static final String FIELD_SUPPLIER_NAME = "supplier_name";
  public static final String FIELD_CUST_SUPPLIER_CODE = "cust_supplier_code";
  public static final String FIELD_DEBIT_NOTE_DATETIME = "debit_note_datetime";
  public static final String FIELD_DEBIT_NOTE_NO = "debit_note_no";
  public static final String FIELD_UOM = "srn_quantity_unit";
  public static final String FIELD_RETURN_REASON = "return_type";
  public static final String FIELD_REMARKS = "remarks";
  public static final String FIELD_ITEM_ID = "item_id";
  public static final String FIELD_ITEM_BATCH_NO = "item_batch_no";
  public static final String FIELD_QUANTITY = "quantity";
  public static final String FIELD_BONUS_QUANTITY = "bonus_quantity";
  public static final String FIELD_MRP = "mrp";
  public static final String FIELD_COST = "cost";
  public static final String FIELD_EXPIRY_DATE = "expiry_date";
  public static final String FIELD_TAX_SUB_GROUPS = "tax_sub_groups";
  public static final String FIELD_TAX_PERCENTAGE = "tax_percentage";
  public static final String FIELD_TAX_AMOUNT = "tax_amount";
  public static final String FIELD_ITEM_DISCOUNT = "item_discount";
  public static final String FIELD_BILL_DISCOUNT = "bill_discount";
  public static final String FIELD_OTHER_CHARGES = "other_charges";
  public static final String FIELD_OTHER_CHARGES_REMARKS = "other_charges_remarks";
  public static final String FIELD_GRN_NO = "grn_no";
  public static final String FIELD_CONSIGNMENT_NO = "consignment_no";
  public static final String FIELD_CONSIGNMENT_DATE = "consignment_date";
  public static final String FIELD_CONSIGNMENT_COMPANY = "consignment_company";
  public static final String FIELD_RETURN_DETAIL_NO = "return_detail_no";
  public static final String FIELD_ROUNDOFF = "roundoff";
  public static final String FIELD_OTHER_REASON = "other_reason";
  public static final String FIELD_RECEIVED_DEBIT_AMOUNT = "received_debit_amount";
  public static final String FIELD_USERNAME = "username";


  private static final String[] COLUMN_SEQUENCE = {
      FIELD_TRANSACTION_TYPE,
      FIELD_STORE_NAME,
      FIELD_SUPPLIER_NAME,
      FIELD_CUST_SUPPLIER_CODE,
      FIELD_DEBIT_NOTE_DATETIME,
      FIELD_DEBIT_NOTE_NO,
      FIELD_UOM,
      FIELD_RETURN_REASON,
      FIELD_REMARKS,
      FIELD_ITEM_ID,
      FIELD_ITEM_BATCH_NO,
      FIELD_QUANTITY,
      FIELD_BONUS_QUANTITY,
      FIELD_MRP,
      FIELD_COST,
      FIELD_EXPIRY_DATE,
      FIELD_TAX_SUB_GROUPS,
      FIELD_TAX_PERCENTAGE,
      FIELD_TAX_AMOUNT,
      FIELD_ITEM_DISCOUNT,
      FIELD_BILL_DISCOUNT,
      FIELD_OTHER_CHARGES,
      FIELD_OTHER_CHARGES_REMARKS,
      FIELD_GRN_NO,
      FIELD_CONSIGNMENT_NO,
      FIELD_CONSIGNMENT_DATE,
      FIELD_CONSIGNMENT_COMPANY,
      FIELD_RETURN_DETAIL_NO,
      FIELD_ROUNDOFF,
      FIELD_OTHER_REASON,
      FIELD_RECEIVED_DEBIT_AMOUNT,
      FIELD_USERNAME
  };

  @Override
  public String[] getColumns() {
    return COLUMN_SEQUENCE;
  }

  @Override
  public String getEntityName() {
    return ENTITY_NAME;
  }
}
