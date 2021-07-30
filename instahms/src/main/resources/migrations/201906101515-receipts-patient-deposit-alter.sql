-- liquibase formatted sql
-- changeset allabakash:receipt-patient-deposit-alter

ALTER TABLE patient_deposits RENAME TO patient_deposits_obsolete;