-- liquibase formatted sql
-- changeset pranays:add-clinical-events-for-save-and-finalise failOnError:false

--Consultation
INSERT INTO hie_events VALUES (31, 'Cons_Form_Save','A','');
INSERT INTO hie_events VALUES (32, 'Cons_Form_Reopen_And_Save','A','');
--Discharge Summary
INSERT INTO hie_events VALUES (33, 'Discharge_Form_Save','A','');
INSERT INTO hie_events VALUES (34, 'Discharge_Form_Reopen_And_Save','A','');
--Ip Emr
INSERT INTO hie_events VALUES (35, 'Ip_Form_Save','A','');
INSERT INTO hie_events VALUES (36, 'Ip_Form_Reopen_And_Save','A','');
--OT Record
INSERT INTO hie_events VALUES (37, 'Surgery_Form_Save','A','');
INSERT INTO hie_events VALUES (38, 'Surgery_Form_Reopen_And_Save','A','');
