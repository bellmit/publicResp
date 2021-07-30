-- liquibase formatted sql
-- changeset javalkarvinay:added_new_events

INSERT INTO events_hl7 VALUES (19,'Diagnosis_IP'),(20,'Allergies_IP'),(21,'Diagnosis_Inactive_Visit'),(22,'Allergies_Inactive_Visit');
UPDATE events_hl7 SET event='Diagnosis_OP' WHERE id=15;
UPDATE events_hl7 SET event='Allergies_OP' WHERE id=16;
