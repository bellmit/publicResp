-- liquibase formatted sql
-- changeset pranays:obsolete-partogram-module

DELETE FROM modules_activated WHERE module_id = 'mod_partogram';

