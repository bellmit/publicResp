-- liquibase formatted sql
-- changeset goutham005:<new-column-for-orm-status-to-exclude>

ALTER TABLE hl7_lab_interfaces ADD COLUMN exclude_orm_status character varying(200);
COMMENT ON COLUMN hl7_lab_interfaces.exclude_orm_status IS 'It is to exclude status (ORC.1) from ORM, should be comma separated values';
