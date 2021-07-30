-- liquibase formatted sql
-- changeset vakul-practo:virtual-table-changes splitStatements:false
-- validCheckSum: ANY


ALTER TABLE code_system_categories ADD COLUMN isvirtualtable VARCHAR(1) default 'N';

INSERT INTO code_system_categories (id, label, status, table_name, entity_name, entity_id, isVirtualTable)
VALUES (20, 'Language Codes', 'A', 'language_master', 'lang_disp_name', 'lang_id', 'Y');