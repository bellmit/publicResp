-- liquibase formatted sql
-- changeset adeshatole:grn-integration-id

ALTER TABLE store_grn_main ADD COLUMN integration_grn_id character varying(100) UNIQUE;