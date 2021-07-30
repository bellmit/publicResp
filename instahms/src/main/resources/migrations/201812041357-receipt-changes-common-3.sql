-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes-common-3

CREATE INDEX receipt_usage_entity_id_idx ON receipt_usage(entity_id);
CREATE INDEX receipt_receipt_type_idx ON receipts(receipt_type);
