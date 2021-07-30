-- liquibase formatted sql
-- changeset adeshatole:auto-generate-cust-item-code

ALTER TABLE generic_preferences ADD COLUMN force_generate_cust_item_code VARCHAR(1) NOT NULL DEFAULT 'N';
