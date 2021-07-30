-- liquibase formatted sql
-- changeset goutham005:<send-center-id>

ALTER TABLE hl7_lab_interfaces ADD COLUMN send_center_id character(1) default 'N';
COMMENT ON COLUMN hl7_lab_interfaces.send_center_id IS 'preference to send center id in OBR.18 and OBR.21 for radiology tests';
