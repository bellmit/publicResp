-- liquibase formatted sql
-- changeset javalkarvinay:added_isDrug_column_to_store_category_master

ALTER TABLE store_category_master ADD COLUMN is_drug CHARACTER(1) DEFAULT 'N';
