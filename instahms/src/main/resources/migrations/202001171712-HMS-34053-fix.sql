-- liquibase formatted sql
-- changeset manjular:adding-is_customized_package-to-patient_customised_package_details

ALTER TABLE patient_customised_package_details ADD COLUMN is_customized_package  boolean DEFAULT false;

