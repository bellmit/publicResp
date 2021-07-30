-- liquibase formatted sql

-- changeset goutham005:hl7_lab_interface_changes

ALTER TABLE hl7_lab_interfaces ALTER COLUMN rcv_supporting_doc SET DEFAULT 'N';