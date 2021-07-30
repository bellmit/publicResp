-- READ ME 
-- Update this file as per the comments and execute the commands in db.

-- Inserts MALAFFI code system in table if not exists.
INSERT INTO code_systems (label, status) SELECT 'MALAFFI' AS label,'A' AS status FROM code_systems WHERE NOT EXISTS (SELECT * FROM code_systems WHERE label = 'MALAFFI') LIMIT 1;

-- To Configure ADT destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ADT host, port and request parameters respectively and also replace PRACTOEMR with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,destination_port,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Malaffi_ADT','socket',cs.id,'I','127.0.0.1',8003,'PRACTOEMR','PRACTOEMR','ADHIE','Rhapsody','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Malaffi');

-- To Configure PPR destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination PPR host, port and request parameters respectively and also replace PRACTOEMR with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,destination_port,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Malaffi_PPR','socket',cs.id,'I','127.0.0.1',8003,'PRACTOEMR','PRACTOEMR','ADHIE','Rhapsody','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Malaffi');

-- To Configure OMP and RDS destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination OMP and RDS host, port and request parameters respectively and also replace PRACTOEMR with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,destination_port,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Malaffi_OMP_RDS','socket',cs.id,'I','127.0.0.1',8003,'PRACTOEMR','PRACTOEMR','ADHIE','Rhapsody','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Malaffi');

-- To Configure ORU destination host, uri, req parameters replace localhost, app, APP_ID, APP_KEY with destination ORU host, port and request parameters respectively and also replace PRACTOEMR with nabidh provided system code.
INSERT INTO interface_config_master (interface_type,interface_name,connection_type,code_system_id,status,destination_host,destination_port,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) (SELECT 'hl7','Malaffi_ORU','socket',cs.id,'I','127.0.0.1',8003,'PRACTOEMR','PRACTOEMR','ADHIE','Rhapsody','_implementer','_implementer' FROM code_systems cs WHERE label ILIKE 'Malaffi');

UPDATE interface_config_master SET timeout_in_sec = 120, retry_max_count = 5, retry_for_days = 0, retry_interval_in_minutes = 1;

-- Map messages to events
-- Enter center_ids
CREATE OR REPLACE FUNCTION message_config() 
RETURNS VOID AS $BODY$
BEGIN
 DECLARE 
 -- Add center ids here replace 0 with center ids. Specify only those center id's for which you want to configure messages.
 centerIdArray INT[] := array[0];
 centerId int;
 BEGIN
  FOREACH centerId in ARRAY centerIdArray LOOP
   -- ADT - Pre_Registration Event
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 1,'ADT_A28','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - New_Patient_Reg - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','OP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A04','OP','2.5.1',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - New_Patient_Reg - IP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','IP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A01','IP','2.5.1',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - New_Patient_Reg - OSP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 2,'ADT_A28','OSP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   
   -- ADT - Existing_Patient_Reg - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 3,'ADT_A04','OP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Existing_Patient_Reg - IP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 3,'ADT_A01','IP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Update_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 4,'ADT_A08','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 4,'ADT_A31','ALL','2.5.1',1,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   
   -- ADT - Update_Visit - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 5,'ADT_A11','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Visit_Close - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 6,'ADT_A03','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Physical_Discharge - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 7,'ADT_A03','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Visit_Close_Having_No_Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 8,'ADT_A11','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Physical_Discharge_Having_No_Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 9,'ADT_A11','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - OP_IP_Convertion - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 10,'ADT_A06','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Readmit_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 11,'ADT_A13','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Diagnosis - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 12,'ADT_A08','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Diagnosis_Of_Inactive_Visit - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 13,'ADT_A31','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Allergies - OP
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 14,'ADT_A31','OP','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Merge_Patient - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 15,'ADT_A40','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Update_Insurance_Details - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 16,'ADT_A31','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';

   -- ADT - Surgery - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 20,'ADT_A08','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ADT';
   
   -- PPR - Patient_Problem_Add - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 17,'PPR_PC1','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_PPR';

   -- PPR - Patient_Problem_Update - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 18,'PPR_PC2','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_PPR';

   -- PPR - Patient_Problem_Delete - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 19,'PPR_PC3','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_PPR';
      
   -- OMP - Medicine_Prescription_Add - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 21,'OMP_O09','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_OMP_RDS';

   -- OMP - Medicine_Prescription_Update - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 22,'OMP_O09','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_OMP_RDS';

   -- OMP - Medicine_Prescription_Delete - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 23,'OMP_O09','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_OMP_RDS';

   -- RDS - Medicine_Prescription_Dispense - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 24,'RDS_O13','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_OMP_RDS';

   -- ORU - Laboratory_Signoff - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 25,'ORU_R01','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ORU';

   -- ORU - Radiology_Signoff - ALL
   INSERT INTO interface_event_mapping (event_id, message_type, visit_type, version, priority, center_id, interface_id, status, created_by, modified_by) SELECT 26,'ORU_R01','ALL','2.5.1',0,centerId,icm.interface_id,'A','_implementer','_implementer' FROM interface_config_master icm WHERE icm.interface_name = 'Malaffi_ORU';
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
 FOR messageMappping IN SELECT DISTINCT iem.message_type,iem.version,iem.interface_id FROM interface_event_mapping iem JOIN interface_config_master icm ON (iem.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%') LOOP
  IF messageMappping.message_type = 'ADT_A01' OR messageMappping.message_type = 'ADT_A04' OR messageMappping.message_type = 'ADT_A28' THEN
   -- ADT_A01/ADT_A04/ADT_A28 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A02' OR messageMappping.message_type = 'ADT_A38' THEN
   -- ADT_A02/ADT_A38 ('MSH', 'EVN', 'PID', 'PV1')
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
   -- ADT_A08 ('MSH', 'EVN', 'PID', 'PV1', 'PV2', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV2',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'DG1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PR1',messageMappping.version,8,true,'A',messageMappping.interface_id,'_implementer','_implementer');
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
  ELSIF messageMappping.message_type = 'ADT_A31' THEN
   -- ADT_A31 ('MSH','EVN','PID','AL1','IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'AL1',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'IN1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'ADT_A40' THEN
   -- ADT_A40 ('MSH','EVN','PID', 'MRG')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MRG',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'PPR_PC1' OR messageMappping.message_type = 'PPR_PC2' OR messageMappping.message_type = 'PPR_PC3' THEN
   -- PPR_PC1/PPR_PC2/PPR_PPC3 ('MSH','PID','PV1','PRB','NTE','ROL')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PRB',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'NTE',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ROL',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type = 'OMP_O09' THEN
   -- OMP_O09 ('MSH','PID','PV1','ORC','RXO','RXR')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXO',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES (messageMappping.message_type,'RXR',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
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
 FOR segmentRecord IN SELECT DISTINCT mmdh.segment,mmdh.version,icm.interface_id FROM message_mapping_details_hl7 mmdh JOIN interface_config_master icm ON (mmdh.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%') LOOP
  IF segmentRecord.segment ILIKE 'MSH' THEN
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
  ELSIF segmentRecord.segment ILIKE 'EVN' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('EVN_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'PID' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_17',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_28',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_29',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PID_30',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'PV1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_19',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_36',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_37',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_44',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV1_45',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'AL1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('AL1_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'IN1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('IN1_49',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'PV2' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PV2_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'DG1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('DG1_18',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'MRG' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('MRG_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'PR1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_8',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'PRB' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_14',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PRB_16',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'NTE' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('NTE_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'ROL' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ROL_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ROL_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ROL_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'ORC' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_9',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('ORC_15',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'OBR' THEN
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
  ELSIF segmentRecord.segment ILIKE 'OBX' THEN
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
  ELSIF segmentRecord.segment ILIKE 'RXD' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_6',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXD_10',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'RXO' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_2',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_5',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_7',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_11',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXO_12',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  ELSIF segmentRecord.segment ILIKE 'RXR' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select insertSegmentTemplate();
DROP function insertSegmentTemplate();

INSERT INTO interface_event_rule_master (rule_name,status,include,exclude,created_by,modified_by) VALUES ('Malaffi Skip OSP Visit','A','{}','{"visit_data" : {"op_type":"O"}}','_implementer','_implementer');
UPDATE interface_event_mapping SET rule_id = ierm.rule_id FROM interface_event_rule_master ierm, interface_config_master icm WHERE ierm.rule_name = 'Malaffi Rule' AND icm.interface_name like 'Malaffi_%' AND interface_event_mapping.interface_id = icm.interface_id AND (ierm.event_id != 2 AND ierm.visit_type != 'OSP');

\ir 'malaffi_template.sql'
\ir 'malaffi_code_sets.sql'