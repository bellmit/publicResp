-- liquibase formatted sql
-- changeSET Allabakash:review-type-center-role-pre-migration.sql
-- validCheckSum: ANY

--update codification_message_type_role table role_id with role_id = 1, if role_id is null;
UPDATE codification_message_type_role SET role_id = 1 WHERE role_id IS NULL;
 
