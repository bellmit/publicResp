-- liquibase formatted sql
-- changeset utkarshjindal:deprecate-mod-insta_lite-and-patient_self_appointment
DELETE FROM modules_activated WHERE module_id = 'mod_insta_lite';
DELETE FROM modules_activated WHERE module_id = 'mod_patient_self_appointment';
