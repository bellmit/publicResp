-- liquibase formatted sql
-- changeset raj-nt:deprecating-mod_scan
DELETE FROM modules_activated WHERE module_id = 'mod_scan';
