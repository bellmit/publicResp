-- liquibase formatted sql
-- changeset anupama-practo:slida_configuration_moved_to_center_preferences
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 select count(column_name) from information_schema.columns where table_catalog = current_database() and table_schema = current_schema() and table_name = 'center_preferences' and column_name = 'slida_url'

ALTER TABLE center_preferences ADD COLUMN slida_protocol CHARACTER VARYING(16);

ALTER TABLE center_preferences ADD COLUMN slida_url CHARACTER VARYING(255);

ALTER TABLE generic_preferences RENAME slida_mailslot_path TO obsolete_slida_mailslot_path;


