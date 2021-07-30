-- liquibase formatted sql
-- changeset javalkarvinay:IES-2210-added-missing-columns-to-audit-table

ALTER TABLE patient_other_prescriptions_audit ADD COLUMN obsolete_start_date DATE;
ALTER TABLE patient_other_prescriptions_audit ADD COLUMN obsolete_end_date DATE;

