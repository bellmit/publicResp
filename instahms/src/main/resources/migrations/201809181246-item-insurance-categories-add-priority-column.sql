-- liquibase formatted sql
-- changeset anandpatel:adding-column-priority-in-item_insurance_categories-table
ALTER TABLE item_insurance_categories ADD COLUMN priority integer;
