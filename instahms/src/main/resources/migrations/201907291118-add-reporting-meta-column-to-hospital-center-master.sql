-- liquibase formatted sql
-- changeset raj-nt:add-reporting-meta-column-to-hospital-center-master

ALTER TABLE hospital_center_master ADD COLUMN reporting_meta TEXT NOT NULL DEFAULT '{}';
