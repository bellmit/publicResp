-- liquibase formatted sql
-- changeset krishnasameerachanta:<add-consumption-validity-columns>

ALTER TABLE packages ADD consumption_validity_value integer;
ALTER TABLE packages ADD consumption_validity_unit character(1);

ALTER TABLE patient_customised_package_details ADD consumption_validity_value integer;
ALTER TABLE patient_customised_package_details ADD consumption_validity_unit character(1);
