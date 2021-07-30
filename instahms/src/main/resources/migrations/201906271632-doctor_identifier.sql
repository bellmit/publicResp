-- liquibase formatted sql
-- changeset goutham005:<new_column_doctor_identifier>

ALTER TABLE hl7_lab_interfaces ADD COLUMN doctor_identifier character(1) default 'I';
COMMENT ON COLUMN hl7_lab_interfaces.doctor_identifier IS 'I-Insta identifier (doctor id), D-doctor license number';
