-- liquibase formatted sql
-- changeset utkarshjindal:depracte-mod_linen
DELETE FROM modules_activated WHERE module_id = 'mod_linen';
DROP TABLE IF EXISTS bed_linen_current_list CASCADE;
DROP SEQUENCE IF EXISTS bed_linen_current_list_seq;