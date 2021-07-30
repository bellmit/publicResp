-- liquibase formatted sql
-- changeset utkarshjindal:deprecate-mod_transportation

DROP TABLE IF EXISTS emergency_call_log CASCADE;
DROP SEQUENCE IF EXISTS emergency_call_log_seq;
DROP TABLE IF EXISTS ambulance_usage CASCADE;
DROP SEQUENCE IF EXISTS ambulance_usage_seq;
DELETE FROM modules_activated WHERE module_id = 'mod_transportation'
