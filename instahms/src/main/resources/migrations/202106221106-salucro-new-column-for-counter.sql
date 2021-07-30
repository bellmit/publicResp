-- liquibase formatted sql
-- changeset pallavia08:salucro-new-column-for-counter
ALTER TABLE salucro_role_mapping ADD COLUMN counter_id character varying(20);