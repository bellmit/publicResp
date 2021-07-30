-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-common-4-2

CREATE INDEX idx_receipt_usage_receiptid_entitytype ON receipt_usage(receipt_id, entity_type);
CREATE INDEX idx_receipts_mrno_isdeposit on receipts(mr_no,is_deposit);