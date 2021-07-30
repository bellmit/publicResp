-- liquibase formatted sql
-- changeset javalkarvinay:added_timeout_name_status_to_interface_config

ALTER TABLE interface_hl7 ADD COLUMN interface_name VARCHAR(100);
ALTER TABLE interface_hl7 ADD COLUMN status CHARACTER(1) DEFAULT 'A';
ALTER TABLE interface_hl7 ADD COLUMN timeout_in_sec INT DEFAULT 5;
ALTER TABLE interface_hl7 ADD COLUMN retry_max_count INT DEFAULT 100;
ALTER TABLE interface_hl7 ADD COLUMN retry_for_days INT DEFAULT 0;
ALTER TABLE interface_hl7 ADD COLUMN retry_interval_in_minutes INT DEFAULT 30;

ALTER TABLE message_mapping_hl7 DROP COLUMN retry_on_fail;