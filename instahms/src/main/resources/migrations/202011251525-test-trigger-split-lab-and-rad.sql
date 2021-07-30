-- liquibase formatted sql
-- changeset javalkarvinay:test-trigger-split-lab-and-rad

UPDATE events_hl7 SET event='Laboratory_Test_SignedOff' WHERE id=30;
UPDATE events_hl7 SET event='Laboratory_Test_Amend' WHERE id=31;
INSERT INTO events_hl7 (id,event) values (36,'Radiology_Test_SignedOff');
INSERT INTO events_hl7 (id,event) values (37,'Radiology_Test_Amend');

