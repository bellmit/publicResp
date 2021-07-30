-- liquibase formatted sql
-- changeset vinaykumarjavalkar:pending-prescription-add-column

ALTER TABLE patient_pending_prescriptions ADD COLUMN visit_id CHARACTER VARYING(19);
