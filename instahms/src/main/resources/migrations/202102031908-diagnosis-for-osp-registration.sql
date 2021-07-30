-- liquibase formatted sql
-- changeset asif:adding_diagnosis_for_osp_registration

ALTER TABLE registration_preferences ADD COLUMN
diagnosis_for_osp_registration character(1) NOT NULL DEFAULT 'O';
