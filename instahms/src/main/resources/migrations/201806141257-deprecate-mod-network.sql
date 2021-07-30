-- liquibase formatted sql
-- changeset utkarshjindal:dropping-mod-network
DROP TABLE network_center_details CASCADE;
DROP SEQUENCE IF EXISTS network_center_details_seq;
DELETE FROM modules_activated WHERE module_id = 'mod_network';

