-- liquibase formatted sql
-- changeset prashantbaisla:add-organization-column-to-hospital_center_master

ALTER TABLE hospital_center_master ADD COLUMN organization character varying(100);
