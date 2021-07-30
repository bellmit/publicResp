-- liquibase formatted sql
-- changeset javalkarvinay:default_column_for_code_sets

ALTER TABLE code_sets ADD COLUMN is_default boolean;
ALTER TABLE code_sets ADD CONSTRAINT unique_default UNIQUE (code_system_category_id,code_system_id,is_default);
