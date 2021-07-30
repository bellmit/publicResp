-- liquibase formatted sql
-- changeset pranays:update-varchar-limit-finalized-user-in-patient-seciton-details

ALTER TABLE patient_section_details ALTER COLUMN finalized_user TYPE character varying(30);