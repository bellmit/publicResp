-- liquibase formatted sql
-- changeset goutham005:<to-exclude-oru-status>

ALTER TABLE hl7_lab_interfaces ADD COLUMN exclude_oru_status character varying(200);
COMMENT ON COLUMN hl7_lab_interfaces.exclude_oru_status IS 'It is to exclude status (ORC.1) from ORU, should be comma separated values';
