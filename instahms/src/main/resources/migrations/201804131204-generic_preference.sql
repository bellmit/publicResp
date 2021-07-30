-- liquibase formatted sql
-- changeset kchetan:add-edit-insurance-applicable

ALTER TABLE generic_preferences ADD COLUMN add_edit_insurance_applicable character varying(1) default 'A';
