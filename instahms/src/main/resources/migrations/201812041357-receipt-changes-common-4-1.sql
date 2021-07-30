-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-common-4-1
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from pg_indexes where schemaname = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and indexname='receipt_usage_entity_type_idx'

DROP INDEX receipt_usage_entity_type_idx;

