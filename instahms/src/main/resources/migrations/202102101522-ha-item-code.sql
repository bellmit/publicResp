-- liquibase formatted sql
-- changeset manasaparam:added-ha-item-code-in-fa-table

ALTER TABLE hms_accounting_info ADD COLUMN ha_item_code character varying(20);
ALTER TABLE hms_accounting_info ADD COLUMN ha_code_type character varying(20);