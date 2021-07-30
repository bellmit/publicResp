-- liquibase formatted sql
-- changeset rajrajeshwarsinghrathore:added_encounter_end_date_and_encounter_end_time_to_patient_registration

alter table patient_registration
    add encounter_end_date date default null;

alter table patient_registration
    add encounter_end_time time default null;
