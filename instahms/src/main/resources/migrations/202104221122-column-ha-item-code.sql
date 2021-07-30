-- liquibase formatted sql
-- changeset manasaparam:ha-item-code-column-size

ALTER TABLE hms_accounting_info alter column ha_item_code type character varying(600);