-- liquibase formatted sql
-- changeset utkarshjindal:dropping-table-used-for-mod_accumed
DROP TABLE IF EXISTS insurance_change_log CASCADE;
DROP SEQUENCE IF EXISTS insurance_change_log_seq;
DELETE FROM modules_activated WHERE module_id = 'mod_accumed';
