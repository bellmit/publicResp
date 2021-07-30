-- liquibase formatted sql
-- changeset javalkarvinay:marital_status_and_religion_prefence_for_hl7_message

ALTER TABLE registration_preferences ADD COLUMN marital_status_required CHARACTER(1) DEFAULT 'N';
ALTER TABLE registration_preferences ADD COLUMN religion_required CHARACTER(1) DEFAULT 'N';
