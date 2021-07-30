-- liquibase formatted sql
-- changeset allabakash:bill-charge-receipt-alloc-idx-billreceiptid failOnError:false

CREATE index idx_bill_charge_receipt_allocation_billreceiptid ON bill_charge_receipt_allocation(bill_receipt_id);