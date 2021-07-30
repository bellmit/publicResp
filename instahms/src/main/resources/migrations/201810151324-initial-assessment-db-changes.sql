-- liquibase formatted sql
-- changeset goutham005:initial-assessment-db-changes

ALTER TABLE doctor_consultation ADD COLUMN ia_start_datetime timestamp without time zone;
ALTER TABLE doctor_consultation ADD COLUMN ia_end_datetime timestamp without time zone;
ALTER TABLE doctor_consultation ADD COLUMN ia_mod_time timestamp without time zone;