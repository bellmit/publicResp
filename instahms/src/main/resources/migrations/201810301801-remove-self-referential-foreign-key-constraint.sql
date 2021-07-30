-- liquibase formatted sql
-- changeset prashantbaisla:remove-self-referential-foreign-key-constraint
ALTER TABLE scheduler_appointments DROP CONSTRAINT appointment_id_fkey;