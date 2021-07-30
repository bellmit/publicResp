-- liquibase formatted sql
-- changeset akshaySuman:creatingIndexOnOldmrnoForPatientRegistration

DROP INDEX IF EXISTS oldmrno_idx;
CREATE INDEX oldmrno_idx ON patient_details ((lower(oldmrno)));