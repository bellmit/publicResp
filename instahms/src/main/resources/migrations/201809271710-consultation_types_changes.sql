-- liquibase formatted sql
-- changeset raeshmika:<adding-duration-to-consultation-type>

ALTER TABLE consultation_types ADD COLUMN duration INTEGER ;

ALTER TABLE consultation_types ADD COLUMN duration_unit character varying(10) DEFAULT 'MINUTES';
