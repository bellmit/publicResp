-- liquibase formatted sql
-- changeset pranays:<add-generic-form-id-to-auto-save-json-data>

ALTER TABLE auto_save_json_data ADD COLUMN generic_form_id integer;