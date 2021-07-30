-- liquibase formatted sql
-- changeset javalkarvinay:increased_doc_name_field_length

ALTER TABLE doctors ALTER COLUMN doctor_name TYPE varchar(250);
ALTER TABLE doctors ALTER COLUMN doc_first_name TYPE varchar(50);
ALTER TABLE doctors ALTER COLUMN doc_middle_name TYPE varchar(100);
ALTER TABLE doctors ALTER COLUMN doc_last_name TYPE varchar(50);
