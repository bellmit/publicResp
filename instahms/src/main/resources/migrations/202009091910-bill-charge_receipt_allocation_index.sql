-- liquibase formatted sql
-- changeset allabakash:Bill-charge-receipt-allocation-index

CREATE INDEX idx_bill_charge_receipt_allocation_chargeid ON bill_charge_receipt_allocation(charge_id);

