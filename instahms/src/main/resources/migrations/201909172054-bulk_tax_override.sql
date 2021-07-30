-- liquibase formatted sql
-- changeset sirisharl:<no-of-tax-groupd-at-bill>

ALTER TABLE center_preferences ADD COLUMN no_of_tax_groups integer DEFAULT 0;
