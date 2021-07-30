-- liquibase formatted sql
-- changeset pallavia08:bill-signature-column-in-bill-table-fairview-enhancement

ALTER TABLE bill ADD COLUMN bill_signature TEXT;
