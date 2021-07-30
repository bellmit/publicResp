-- liquibase formatted sql
-- changeset raeshmika:<patient_photo_mandatory-preference>

ALTER TABLE registration_preferences ADD COLUMN patient_photo_mandatory character varying(1) default 'N';