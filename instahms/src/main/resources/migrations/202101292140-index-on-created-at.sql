-- liquibase formatted sql
-- changeset manasaparam:creating-index-on-created-at

CREATE INDEX hai_created_at_idx ON hms_accounting_info USING btree (created_at);