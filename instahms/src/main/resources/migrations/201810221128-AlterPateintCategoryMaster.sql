-- liquibase formatted sql
-- changeset utkarshjindal:altering-patient-category-master

ALTER TABLE patient_category_master ALTER COLUMN category_name TYPE character varying(100);
