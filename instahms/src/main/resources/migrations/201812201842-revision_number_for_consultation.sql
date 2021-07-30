-- liquibase formatted sql
-- changeset sonam009:revision-number-for-consultation-and-triage

ALTER TABLE doctor_consultation ADD COLUMN cons_revision_number numeric; 
ALTER TABLE doctor_consultation ADD COLUMN triage_revision_number numeric;
UPDATE doctor_consultation SET cons_revision_number =  (SELECT EXTRACT(epoch FROM current_timestamp))
  WHERE consultation_mod_time IS NOT null;

UPDATE doctor_consultation SET triage_revision_number =  (SELECT EXTRACT(epoch FROM current_timestamp))
  WHERE triage_mod_time IS NOT null;

ALTER TABLE doctor_consultation DROP COLUMN triage_mod_time;   