-- liquibase formatted sql
-- changeset yaminipagaria:adding-and-updating-documented-date-in-patient-notes

ALTER TABLE patient_notes ADD COLUMN documented_date date, ADD COLUMN documented_time time without time zone;
update patient_notes set documented_date = date(created_time), documented_time = cast(created_time::timestamp as time);