-- liquibase formatted sql
-- changeset raeshmika:<adding-column-scheduleable-by-services-master>

ALTER TABLE services ADD COLUMN scheduleable_by char(1) DEFAULT 'A';