-- liquibase formatted sql
-- changeset javalkarvinay:doctor_master_first_last_name

ALTER TABLE doctors ADD COLUMN doc_salutation_id VARCHAR(10);
ALTER TABLE doctors ADD COLUMN doc_first_name VARCHAR(25);
ALTER TABLE doctors ADD COLUMN doc_middle_name VARCHAR(25);
ALTER TABLE doctors ADD COLUMN doc_last_name VARCHAR(25);