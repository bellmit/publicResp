-- liquibase formatted sql
-- changeset anupama.mr:unique-constraint-changes-for-store-accountgroup-configuration

ALTER TABLE center_integration_details DROP CONSTRAINT center_integration_details_center_id_integration_id_key;
ALTER TABLE center_integration_details ADD CONSTRAINT cid_center_id_integration_id_store_code_key UNIQUE(integration_id, center_id, store_code);

