-- liquibase formatted sql
-- changeset dattuvs:<commit-message-describing-this-database-change>

ALTER TABLE patient_iv_administered_details ADD COLUMN id SERIAL PRIMARY KEY;
COMMENT ON SEQUENCE patient_iv_administered_details_id_seq IS '{ "type": "Txn", "comment": "Holds sequence for patient iv administered details" }';