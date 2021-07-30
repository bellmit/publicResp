-- liquibase formatted sql
-- changeset utkarshjindal:deprecating-mod_roster
DELETE FROM modules_activated WHERE module_id = 'mod_roster';
DROP TABLE IF EXISTS shift_users CASCADE;
DROP SEQUENCE IF EXISTS shift_users_seq;
DROP TABLE IF EXISTS shift_master CASCADE;
DROP SEQUENCE IF EXISTS shift_master_seq;
DROP TABLE IF EXISTS roster_resource_type_master CASCADE;
DROP SEQUENCE IF EXISTS roster_resource_type_master_seq;
DROP TABLE IF EXISTS roster_resource_master CASCADE;
DROP SEQUENCE IF EXISTS roster_resource_master_seq;

