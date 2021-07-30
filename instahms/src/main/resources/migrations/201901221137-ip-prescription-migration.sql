-- liquibase formatted sql
-- changeset tejakilaru:ip-prescription-migration

ALTER TABLE patient_prescription ADD COLUMN doctor_id character varying(15);
ALTER TABLE patient_prescription ADD COLUMN prior_med character(1) default 'N';
ALTER TABLE patient_prescription ADD COLUMN freq_type character(1);
ALTER TABLE patient_prescription ADD COLUMN recurrence_daily_id integer;
ALTER TABLE patient_prescription ADD COLUMN repeat_interval integer;
ALTER TABLE patient_prescription ADD COLUMN start_datetime timestamp without time zone;
ALTER TABLE patient_prescription ADD COLUMN end_datetime timestamp without time zone;
ALTER TABLE patient_prescription ADD COLUMN no_of_occurrences integer;
ALTER TABLE patient_prescription ADD COLUMN end_on_discontinue character(1) default 'N';
ALTER TABLE patient_prescription ADD COLUMN discontinued character(1) default 'N';
ALTER TABLE patient_prescription ADD COLUMN repeat_interval_units character(1) default 'M';
ALTER TABLE patient_prescription ADD COLUMN adm_request_id integer;
