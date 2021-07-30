-- liquibase formatted sql
-- changeset janakivg:store-category-master-prescribable

ALTER TABLE store_category_master ADD COLUMN prescribable boolean NOT NULL DEFAULT true;

