-- liquibase formatted sql
-- changeset raj-nt:drop-mod-fp-service-and-fp-authurl

DELETE FROM patient_header_preferences WHERE field_name='verify_finger_print';
ALTER TABLE patient_registration DROP COLUMN verify_finger_print;
ALTER TABLE doctor_consultation DROP COLUMN finger_print_verified;
ALTER TABLE tests_prescribed DROP COLUMN finger_print_verified;
DELETE FROM modules_activated WHERE module_id='mod_fpservice';
DROP TABLE IF EXISTS user_location;
DROP SEQUENCE IF EXISTS user_location_seq;
DROP TABLE IF EXISTS patient_finger_prints;
DROP SEQUENCE IF EXISTS patient_finger_prints_seq_id;
ALTER TABLE generic_preferences DROP COLUMN fp_authentication_url;
