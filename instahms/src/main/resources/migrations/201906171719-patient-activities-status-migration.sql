-- liquibase formatted sql
-- changeset tejakilaru:patient-activities-status-migration

UPDATE patient_activities SET activity_status='S' WHERE activity_status='P' and iv_status is null and prescription_type='M';
