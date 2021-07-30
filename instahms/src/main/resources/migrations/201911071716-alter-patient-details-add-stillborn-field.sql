-- liquibase formatted sql
-- changeset rajendratalekar:alter-patient-details-add-stillborn-field

ALTER TABLE patient_details add column stillborn varchar(1);
