-- liquibase formatted sql
-- changeset rajendratalekar:bill-charge-receipt-allocation-refund-check-index failOnError:false

CREATE INDEX bill_charge_receipt_allocation_refund_check_index ON bill_charge_receipt_allocation(refund_reference_id,activity);
