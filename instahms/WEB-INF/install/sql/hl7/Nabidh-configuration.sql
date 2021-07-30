-- READ ME 
-- Update this file as per the comments and execute the commands in db.

-- Inserts NABIDH code system in table if not exists.
INSERT INTO code_systems (label, status) SELECT 'NABIDH' AS label,'A' AS status FROM code_systems WHERE NOT EXISTS (SELECT * FROM code_systems WHERE label = 'NABIDH') LIMIT 1;
INSERT INTO hie_events(event_id,event_name) VALUES(39,'ADD_PATIENT_TO_PCG');

-- To Configure ADT destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ADT host, path and request parameters respectively and also replace SYSTEM_CODE with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,uri,req_parameters,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Nabidh_ADT','https',cs.id,'I','developerstg.dha.gov.ae','api/HL7P1/ADT','{"app_id":"820d886c","app_key":"9dc0b2c235b16a1a47ca59c44bac5120"}','PRACTOEMR','PRACTOEMR','DHA','NABIDH','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Nabidh');

-- To Configure OMP and RDS destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination OMP and RDS host, path and request parameters respectively and also replace SYSTEM_CODE with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,uri,req_parameters,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Nabidh_OMP_RDS','https',cs.id,'I','developerstg.dha.gov.ae','api/HL7P1/ORM','{"app_id":"820d886c","app_key":"9dc0b2c235b16a1a47ca59c44bac5120"}','PRACTOEMR','PRACTOEMR','DHA','NABIDH','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Nabidh');

-- To Configure ORU destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ORU host, path and request parameters respectively and also replace SYSTEM_CODE with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,uri,req_parameters,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Nabidh_ORU','https',cs.id,'I','developerstg.dha.gov.ae','api/HL7P1/ORU','{"app_id":"820d886c","app_key":"9dc0b2c235b16a1a47ca59c44bac5120"}','PRACTOEMR','PRACTOEMR','DHA','NABIDH','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Nabidh');

-- To Configure VXU destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ORU host, path and request parameters respectively and also replace SYSTEM_CODE with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,uri,req_parameters,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Nabidh_VXU','https',cs.id,'I','developerstg.dha.gov.ae','api/HL7P1/VXU','{"app_id":"820d886c","app_key":"9dc0b2c235b16a1a47ca59c44bac5120"}','PRACTOEMR','PRACTOEMR','DHA','NABIDH','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Nabidh');

-- To Configure MDM destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ADT host, path and request parameters respectively and also replace SYSTEM_CODE with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,uri,req_parameters,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Nabidh_MDM','https',cs.id,'I','developerstg.dha.gov.ae','api/HL7P1/MDM','{"app_id":"820d886c","app_key":"9dc0b2c235b16a1a47ca59c44bac5120"}','PRACTOEMR','PRACTOEMR','DHA','NABIDH','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Nabidh');

UPDATE interface_config_master SET timeout_in_sec = 30, retry_max_count = 5, retry_for_days = 0, retry_interval_in_minutes = 1;

-- Map messages to events
-- Enter center_ids
CREATE OR REPLACE FUNCTION message_config() 
RETURNS VOID AS $BODY$
BEGIN
 DECLARE 
 -- Add center ids here replace 1,2 with center ids. Specify only those center id's for which you want to configure messages.
 centerIdArray INT[] := array[0];
 centerId int;
 BEGIN
  FOREACH centerId in ARRAY centerIdArray LOOP
   -- ADT - Pre_Registration Event
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 1,'ADT_A28','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - New_Patient_Reg - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','OP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A04','OP','2.5',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - New_Patient_Reg - IP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','IP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A01','IP','2.5',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - New_Patient_Reg - OSP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','OSP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
   
   -- ADT - Existing_Patient_Reg - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 3,'ADT_A04','OP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Existing_Patient_Reg - IP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 3,'ADT_A01','IP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Update_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 4,'ADT_A31','ALL','2.5',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
   
   -- ADT - Update_Visit - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 5,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Visit_Close - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 6,'ADT_A03','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Physical_Discharge - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 7,'ADT_A03','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Visit_Close_Having_No_Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 8,'ADT_A11','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Physical_Discharge_Having_No_Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 9,'ADT_A11','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - OP_IP_Convertion - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 10,'ADT_A06','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Readmit_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 11,'ADT_A13','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 12,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Diagnosis_Of_Inactive_Visit - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 13,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Allergies - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 14,'ADT_A08','OP','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Merge_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 15,'ADT_A39','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Update_Insurance_Details - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 16,'ADT_A31','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Surgery - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 20,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Vitals -ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 27,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

   -- ADT - Patient_Consent -ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 28,'ADT_A08','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
   
   -- OMP - Medicine_Prescription_Add - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 21,'OMP_O09','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_OMP_RDS';

   -- OMP - Medicine_Prescription_Update - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 22,'OMP_O09','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_OMP_RDS';

   -- OMP - Medicine_Prescription_Delete - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 23,'OMP_O09','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_OMP_RDS';

   -- RDS - Medicine_Prescription_Dispense - ALL
   --INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 24,'RDS_O13','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_OMP_RDS';

   -- ORU - Laboratory_Signoff - ALL
   --INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 25,'ORU_R01','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ORU';

   -- ORU - Radiology_Signoff - ALL
   --INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 26,'ORU_R01','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ORU';

   -- VXU - Patient_Vaccination_Add - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 29,'VXU_V04','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_VXU';

   -- VXU - Patient_Vaccination_Update - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 30,'VXU_V04','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_VXU';
   
   -- MDM - Cons_Form_Save - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 31,'MDM_T02','OP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';
   
   -- MDM - Cons_Form_Reopen_And_Save - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 32,'MDM_T04','OP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';

   -- MDM - Discharge_Form_Save - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 33,'MDM_T02','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';
  
   -- MDM - Discharge_Form_Reopen_And_Save - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 34,'MDM_T04','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';
   
   -- MDM - Ip_Form_Save - IP
--   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 35,'MDM_T02','IP','2.5.1',0,7,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';
   
   -- MDM - Ip_Form_Reopen_And_Save - IP
--   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 36,'MDM_T04','IP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';

   -- MDM - Surgery_Form_Save - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 37,'MDM_T02','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';

   -- MDM - Surgery_Form_Reopen_And_Save - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 38,'MDM_T04','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';
   
   -- ADD PATIENT TO PCG -- ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 39,'ADT_A31','ALL','2.5',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';
  END LOOP;
 END;
END;
$BODY$
LANGUAGE 'plpgsql';

select message_config();
DROP function message_config();

CREATE OR REPLACE FUNCTION messageMappingFunction() 
RETURNS VOID AS $BODY$ 
DECLARE
 messageMappping RECORD;
BEGIN
 FOR messageMappping IN SELECT DISTINCT iem.message_type,iem.version,iem.interface_id FROM interface_event_mapping iem JOIN interface_config_master icm ON (iem.interface_id = icm.interface_id AND icm.interface_name like 'Nabidh_%') LOOP
  IF messageMappping.message_type = 'ADT_A01' OR messageMappping.message_type = 'ADT_A04' THEN
   -- ADT_A01/ADT_A04 ('MSH','EVN','PID','PV1','AL1','IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A02'  OR messageMappping.message_type = 'ADT_A38' THEN
   -- ADT_A02/ADT_A38 ('MSH','EVN','PID','PV1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A03' THEN
   -- ADT_A03 ('MSH', 'EVN', 'PID', 'PV1', 'PV2', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV2',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PR1',messageMappping.version,8,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A05' THEN
   -- ADT_A05 ('MSH', 'EVN', 'PID', 'PV1', 'AL1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A06' THEN
   -- ADT_A06 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'DG1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A08' THEN
   -- ADT_A08 ('MSH', 'EVN', 'PID', 'PV1', 'PV2', 'AL1', 'DG1', 'IN1', 'PR1', 'OBX', 'ZSC')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV2',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PR1',messageMappping.version,8,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'OBX',messageMappping.version,9,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ZSC',messageMappping.version,10,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A11' THEN
   -- ADT_A11 ('MSH', 'EVN', 'PID', 'PV1', 'DG1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A13' THEN
   -- ADT_A13 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PR1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A28' OR messageMappping.message_type = 'ADT_A31' THEN
   -- ADT_A28/ADT_A31 ('MSH','EVN','PID','PV1',AL1','IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A39' THEN
   -- ADT_A39 ('MSH','EVN','PID', 'MRG')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MRG',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'OMP_O09' THEN
   -- OMP_O09 ('MSH','PID','PV1','ORC','RXO')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXO',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'RDS_O13' THEN
   -- RDS_O13 ('MSH','PID','PV1','ORC','RXD','RXR')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXD',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXR',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ORU_R01' THEN
   -- ORU_R01 ('MSH','PID','PV1','ORC','OBR','OBX','NTE')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ORC',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'OBR',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'OBX',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'NTE',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'VXU_V04' THEN
   -- VXU_V04 ('MSH','PID','PV1','ORC','RXA', RXR)
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXA',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXR',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'MDM_T02' OR messageMappping.message_type = 'MDM_T04' THEN
   -- MDM_T02/MDM_T04 ('MSH', 'EVN', 'PID', 'PV1', 'TXA', 'OBX')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'TXA',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'OBX',messageMappping.version,5,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select messageMappingFunction();
DROP function messageMappingFunction();

CREATE OR REPLACE FUNCTION insertSegmentTemplate() 
RETURNS VOID AS $BODY$ 
DECLARE
 segmentRecord RECORD;
BEGIN
 FOR segmentRecord IN SELECT DISTINCT mmdh.segment,mmdh.version,mmdh.interface_id,icm.interface_name FROM message_mapping_details_hl7 mmdh JOIN interface_config_master icm ON (mmdh.interface_id = icm.interface_id AND icm.interface_name like 'Nabidh_%') LOOP
  IF segmentRecord.segment = 'MSH' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MSH_18',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'EVN' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'PID' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_13',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_17',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_21',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_28',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_29',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_30',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_31',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_33',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_34',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'PV1' THEN
  INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_36',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_37',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_44',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_45',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'AL1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'IN1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_13',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_36',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'PV2' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV2_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'DG1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_18',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_21',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'MRG' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MRG_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MRG_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'PR1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'NTE' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'ORC' AND (segmentRecord.interface_name = 'Nabidh_OMP_RDS' OR segmentRecord.interface_name = 'Nabidh_ORU') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_28',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_29',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'ORC' AND (segmentRecord.interface_name = 'Nabidh_VXU') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_28',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_29',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'OBR' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_22',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_24',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_25',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBR_32',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'OBX' AND segmentRecord.interface_name = 'Nabidh_ORU' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_23',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'OBX' AND segmentRecord.interface_name = 'Nabidh_ADT' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_23',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'RXD' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'RXO' AND (segmentRecord.interface_name = 'Nabidh_OMP_RDS') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_13',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'RXA' AND (segmentRecord.interface_name = 'Nabidh_VXU') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_13',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_20',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXA_22',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'RXR' AND (segmentRecord.interface_name = 'Nabidh_OMP_RDS') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'RXR' AND (segmentRecord.interface_name = 'Nabidh_VXU') THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'ZSC' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ZSC_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ZSC_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ZSC_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ZSC_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ZSC_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'TXA' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_13',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_17',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_21',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('TXA_22',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment = 'OBX' AND segmentRecord.interface_name = 'Nabidh_MDM' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('OBX_23',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select insertSegmentTemplate();
DROP function insertSegmentTemplate();

INSERT INTO interface_event_rule_master (rule_name,status,include,exclude,created_by,modified_by) VALUES ('Nabidh Rule','A','{}','{"patient_data" : {"patient_group" : "1,2,3,4"}}','_implementer','_implementer');
UPDATE interface_event_mapping SET rule_id = ierm.rule_id FROM interface_event_rule_master ierm, interface_config_master icm WHERE ierm.rule_name = 'Nabidh Rule' AND icm.interface_name like 'Nabidh_%' AND interface_event_mapping.interface_id = icm.interface_id;
DELETE from message_mapping_details_hl7 where segment='AL1' and message_type not in ('ADT_A08','ADT_A31');
DELETE from message_mapping_details_hl7 where segment='DG1' and message_type not in ('ADT_A08','ADT_A31');
--Adhoc updates
UPDATE interface_event_mapping SET rule_id = null WHERE message_type like 'ADT_A31' and event_id in (4,5);
UPDATE interface_event_mapping SET status = 'I' WHERE event_id in (27,14);

-- Create event called "Data_Backload" 
INSERT INTO hie_events(event_id,event_name) VALUES(0,'Data_Backload');

-- Map message types to this event
INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, rule_id, interface_id, created_by, modified_by) 
SELECT 0,'ADT_A08','ALL','2.5',0,7,1,icm.interface_id,'InstaAdmin','InstaAdmin' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ADT';

INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, rule_id, interface_id, created_by, modified_by) 
SELECT 0,'OMP_O09','ALL','2.5',0,7,1,icm.interface_id,'InstaAdmin','InstaAdmin' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_OMP_RDS';

INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, rule_id, interface_id, created_by, modified_by) 
SELECT 0,'ORU_R01','ALL','2.5',0,7,1,icm.interface_id,'InstaAdmin','InstaAdmin' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_ORU';

--INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, rule_id, interface_id, created_by, modified_by) 
--SELECT 0,'MDM_T02','ALL','2.5',0,7,1,icm.interface_id,'InstaAdmin','InstaAdmin' FROM interface_config_master icm WHERE icm.interface_name = 'Nabidh_MDM';

update interface_config_master set do_escaping = 'N' where interface_name like 'Nabidh_%';

\ir 'nabidh_template.sql'
\ir 'nabidh_code_sets.sql'
