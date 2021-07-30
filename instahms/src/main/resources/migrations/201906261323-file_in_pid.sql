-- liquibase formatted sql
-- changeset goutham005:<new_column_for_file_in_pid>

ALTER TABLE hl7_lab_interfaces ADD COLUMN file_in_pid character(1) default 'N';
COMMENT ON COLUMN hl7_lab_interfaces.file_in_pid IS 'file pointer read from obx or pid';
