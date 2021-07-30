-- liquibase formatted sql
-- changeset javalkarvinay:added_hl7_presc_events

INSERT INTO events_hl7 (id,event) values (26,'Medicine_Presc_Insert'),
                                         (27,'Medicine_Presc_Update'),
                                         (28,'Medicine_Presc_Delete'),
                                         (29,'Medicine_Dispense'),
                                         (30,'Test_SignedOff'),
                                         (31,'Test_Amend'),
                                         (32,'Discharge_Medication_Insert'),
                                         (33,'Discharge_Medication_Update'),
                                         (34,'Discharge_Medication_Delete'),
                                         (35,'Medicine_Dispense_Discharge_Med');

INSERT INTO code_system_categories(id, label) VALUES (10, 'medicine_route');