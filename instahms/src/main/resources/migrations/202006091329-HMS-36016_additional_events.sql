-- liquibase formatted sql
-- changeset javalkarvinay:HMS-36016_additional_events

INSERT INTO events_hl7 VALUES (23,'Visit_Close_When_Diagnosis_Not_Available');
INSERT INTO events_hl7 VALUES (24,'Physical_Discharge_When_Diagnosis_Not_Available');
INSERT INTO events_hl7 VALUES (25,'Surgery');

ALTER TABLE message_mapping_HL7 ADD COLUMN status CHARACTER(1) DEFAULT 'A';