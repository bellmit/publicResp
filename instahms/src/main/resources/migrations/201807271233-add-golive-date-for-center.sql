-- liquibase formatted sql
-- changeset raj-nt:create-schema-meta-table failOnError:false

ALTER TABLE hospital_center_master ADD COLUMN golive_date date;
