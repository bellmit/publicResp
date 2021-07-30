-- liquibase formatted sql
-- changeset javalkarvinay:added_vital_event

INSERT INTO hie_events (event_id,event_name,status) VALUES (27,'Vitals','A'),(28,'Patient_Consent','A'),(29,'Patient_Vaccination_Add','A'),(30,'Patient_Vaccination_Update','A');
