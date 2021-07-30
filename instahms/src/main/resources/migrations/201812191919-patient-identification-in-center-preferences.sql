-- liquibase formatted sql
-- changeset sanjana:moving-patient-identification-to-center-preferences

alter table center_preferences add column patient_identification character varying(5);
update center_preferences set patient_identification=(select patient_identification from registration_preferences);
update center_preferences set patient_identification='N' where patient_identification not in ('G','GP');
alter table registration_preferences drop column obsolete_govt_id_pattern ;
alter table registration_preferences rename column patient_identification to obsolete_patient_identification;

comment on column govt_identifier_master.remarks is 'Used as description in UI';
comment on column govt_identifier_master.identifier_type is 'Used as default value in UI';
