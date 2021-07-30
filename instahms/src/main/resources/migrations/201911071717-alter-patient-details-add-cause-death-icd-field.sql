-- liquibase formatted sql
-- changeset rajendratalekar:alter-patient-details-add-cause-of-death-icd-field

ALTER TABLE patient_details add column cause_of_death_icdcode varchar(20);
