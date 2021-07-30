-- liquibase formatted sql
-- changeset rajendratalekar:drop-unused-column-organization-center-master
ALTER TABLE hospital_center_master DROP COLUMN organization;