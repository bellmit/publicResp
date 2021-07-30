-- liquibase formatted sql
-- changeset goutham005:hl7_accession_number_changes.


ALTER TABLE hl7_lab_interfaces ADD COLUMN custom_field_1 CHARACTER VARYING(10);

ALTER TABLE tests_prescribed ADD COLUMN custom_field_1 CHARACTER VARYING(100);