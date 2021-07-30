-- liquibase formatted sql
-- changeset suhas-c-v:special-chars-list-filter-which-replace-comma

UPDATE password_rule
SET specail_char_list = REGEXP_REPLACE(specail_char_list,',','','g');