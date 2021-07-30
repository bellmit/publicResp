-- liquibase formatted sql
-- changeSET Allabakash:review-type-center-role-pre-migration-0
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:true select EXISTS (select 1 from pg_indexes where schemaname = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and indexname = 'review_type_center_role_message_type_id_center_id_key')

--Drop unique key constraint on review_type_center_role
ALTER TABLE review_type_center_role DROP constraint review_type_center_role_message_type_id_center_id_key;
