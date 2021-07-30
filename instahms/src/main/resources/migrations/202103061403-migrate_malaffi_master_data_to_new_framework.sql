-- liquibase formatted sql
-- changeset javalkarvinay:migrate_malaffi_master_data_to_new_framework splitStatements:false
-- validCheckSum: ANY

-- Added race master for code sets.
INSERT INTO code_system_categories (id,label,status,table_name,entity_name,entity_id) VALUES (13,'Race','A','race_master','race_name','race_id'),(14,'Vital Parameters','A','vital_parameter_master','param_label','param_id');

-- Renaming mod_malaffi to mod_hie.
UPDATE modules_activated SET module_id = 'mod_hie' WHERE module_id = 'mod_malaffi';

-- HIE events.
INSERT INTO hie_events (event_id, event_name, event_description) VALUES (1,'Pre_Registration',''),(2,'New_Patient_Reg',''),(3,'Existing_Patient_Reg',''),(4,'Update_Patient',''),(5,'Update_Visit',''),(6,'Visit_Close',''),(7,'Physical_Discharge',''),(8,'Visit_Close_Having_No_Diagnosis',''),(9,'Physical_Discharge_Having_No_Diagnosis',''),(10,'OP_IP_Convertion',''),(11,'Readmit_Patient',''),(12,'Diagnosis',''),(13,'Diagnosis_Of_Inactive_Visit',''),(14,'Allergies',''),(15,'Merge_Patient',''),(16,'Update_Insurance_Details',''),(17,'Patient_Problem_Add',''),(18,'Patient_Problem_Update',''),(19,'Patient_Problem_Delete',''),(20,'Surgery',''),(21,'Medicine_Prescription_Add',''),(22,'Medicine_Prescription_Update',''),(23,'Medicine_Prescription_Delete',''),(24,'Medicine_Prescription_Dispense',''),(25,'Laboratory_Signoff',''),(26,'Radiology_Signoff','');

-- Migrates interface configurations to new table.
INSERT INTO interface_config_master (interface_name,interface_type,connection_type,code_system_id,status,destination_host,destination_port,timeout_in_sec,retry_max_count,retry_for_days,retry_interval_in_minutes,sending_facility,sending_application,receving_facility,receving_application,created_by,modified_by) SELECT interface_name,'hl7','socket',1,status,ip_address,port,timeout_in_sec,retry_max_count,retry_for_days,retry_interval_in_minutes,sending_facility,sending_application,receving_facility,'Rhapsody','_implementer','_implementer' from obsolete_interface_hl7 ih, obsolete_interface_details_hl7 idh WHERE idh.id IN (SELECT DISTINCT interface_details_id FROM obsolete_message_mapping_hl7 WHERE interface_id=ih.id LIMIT 1) AND ih.interface_name like 'Malaffi_%' ORDER BY ih.id;

-- Migrates evnt mappings.
CREATE OR REPLACE FUNCTION eventMappingFunction() 
RETURNS VOID AS $BODY$ 
DECLARE
 eventMappping RECORD;
BEGIN
 FOR eventMappping IN SELECT DISTINCT event_id FROM obsolete_message_mapping_hl7 ORDER BY event_id LOOP
  IF eventMappping.event_id = '1' THEN
   -- 1_IP_New_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 2 AS event_id, 'IP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '2' THEN
   -- 2_IP_Existing_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 3 AS event_id, 'IP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '3' THEN
   -- 3_Physical_Discharge
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 7 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '4' THEN
   -- 4_OP_New_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 2 AS event_id, 'OP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '5' THEN
   -- 5_OP_Existing_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 3 AS event_id, 'OP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '6' THEN
   -- 6_Pre_Registration
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 1 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '7' THEN
   -- 7_OP_IP_Conversion
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 10 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '8' THEN
   -- 8_Edit_Patient_Details
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 4 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '9' THEN
   -- 9_Edit_Visit_Details
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 5 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '10' THEN
   -- 10_ReAdmit_OP_Patient AND 12_ReAdmit_IP_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 11 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
 ELSIF eventMappping.event_id = '11' THEN
   -- 11_Merge_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 15 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '13' THEN
   -- 13_Patient_Problem_OP AND 14_Patient_Problem_IP
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT CASE WHEN mmh.message_type_id LIKE '%PC1%' THEN 17 WHEN mmh.message_type_id LIKE '%PC2%' THEN 18 WHEN mmh.message_type_id LIKE '%PC3%' THEN 19 END AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '15' THEN
   -- 15_Diagnosis_OP AND 19_Diagnosis_IP
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 12 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '16' THEN
   -- 16_Allergies_OP AND 20_Allergies_IP
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 14 AS event_id, 'OP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '17' THEN
   -- 17_Edit_Insurance_Details
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 16 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '18' THEN
   -- 18_Visit_Close
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 6 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '21' THEN
   -- 21_Diagnosis_Inactive_Visit
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 13 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '22' THEN
   -- 22_Allergies_Inactive_Visit (Deprecated as it was already in inactive state)
  ELSIF eventMappping.event_id = '23' THEN
   -- 23_Visit_Close_When_Diagnosis_Not_Available
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 8 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '24' THEN
   -- 24_Physical_Discharge_When_Diagnosis_Not_Available
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 9 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '25' THEN
   -- 25_Surgery
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 20 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '26' THEN
   -- 26_Medicine_Presc_Insert AND 32_Discharge_Medication_Insert
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 21 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '27' THEN
   -- 27_Medicine_Presc_Update AND 33_Discharge_Medication_Update
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 22 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '28' THEN
   -- 28_Medicine_Presc_Delete AND 34_Discharge_Medication_Delete
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 23 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '29' THEN
   -- 29_Medicine_Dispense AND 35_Medicine_Dispense_Discharge_Med
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 24 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '30' THEN
   -- 30_Laboratory_Test_SignedOff AND 31_Laboratory_Test_Amend
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 25 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '36' THEN
   -- 36_Radiology_Test_SignedOff AND 37_Radiology_Test_Amend
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 26 AS event_id, 'ALL', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', 0 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '38' THEN
   -- 38_OSP_New_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 2 AS event_id, 'OSP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  ELSIF eventMappping.event_id = '39' THEN
   -- 39_OSP_Existing_Patient
   INSERT INTO interface_event_mapping (event_id, visit_type, message_type, version, priority, center_id, status, interface_id, created_by, modified_by) SELECT 3 AS event_id, 'OSP', concat(substring(mmh.message_type_id,1,3),'_',substring(mmh.message_type_id,4,3)) AS message_type, '2.5.1', mmh.priority-1 AS priority, mmh.center_id, mmh.status, icm.interface_id, '_implementer', '_implementer' FROM obsolete_message_mapping_hl7 mmh LEFT JOIN obsolete_interface_hl7 ih ON (mmh.interface_id = ih.id) LEFT JOIN interface_config_master icm ON (icm.interface_name = ih.interface_name AND icm.destination_port = ih.port AND icm.destination_host = ih.ip_address) WHERE mmh.event_id = eventMappping.event_id;
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select eventMappingFunction();
DROP function eventMappingFunction();

-- Migrates message mappings.
CREATE OR REPLACE FUNCTION messageMappingFunction() 
RETURNS VOID AS $BODY$ 
DECLARE
 messageMappping RECORD;
BEGIN
 FOR messageMappping IN SELECT DISTINCT message_type,version,interface_id FROM interface_event_mapping LOOP
  IF messageMappping.message_type ILIKE 'ADT_A01' THEN
   -- ADT_A01 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A01','IN1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A02' THEN
   -- ADT_A02 ('MSH', 'EVN', 'PID', 'PV1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A02','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A02','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A02','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A02','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A03' THEN
   -- ADT_A03 ('MSH', 'EVN', 'PID', 'PV1', 'PV2', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','PV2',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','AL1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','DG1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','IN1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A03','PR1',messageMappping.version,8,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A04' THEN
   -- ADT_A04 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A04','IN1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A05' THEN
   -- ADT_A05 ('MSH', 'EVN', 'PID', 'PV1', 'AL1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A05','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A05','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A05','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A05','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A05','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A06' THEN
   -- ADT_A06 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'DG1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A06','DG1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A08' THEN
   -- ADT_A08 ('MSH', 'EVN', 'PID', 'PV1', 'PV2', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','PV2',messageMappping.version,4,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','AL1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','DG1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','IN1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A08','PR1',messageMappping.version,8,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A11' THEN
   -- ADT_A11 ('MSH', 'EVN', 'PID', 'PV1', 'DG1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A11','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A11','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A11','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A11','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A11','DG1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A13' THEN
   -- ADT_A13 ('MSH', 'EVN', 'PID', 'PV1', 'AL1', 'DG1', 'IN1', 'PR1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','DG1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','IN1',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A13','PR1',messageMappping.version,7,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A28' THEN
   -- ADT_A28 ('MSH','EVN','PID','PV1','AL1','IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','AL1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A28','IN1',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A31' THEN
   -- ADT_A31 ('MSH','EVN','PID','AL1','IN1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A31','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A31','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A31','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A31','AL1',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A31','IN1',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A38' THEN
   -- ADT_A38 ('MSH','EVN','PID','PV1')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A38','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A38','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A38','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A38','PV1',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ADT_A40' THEN
   -- ADT_A40 ('MSH','EVN','PID', 'MRG')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A40','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A40','EVN',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A40','PID',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ADT_A40','MRG',messageMappping.version,3,false,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'PPR_PC1' THEN
   -- PPR_PC1 ('MSH','PID','PV1','PRB','NTE','ROL')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','PRB',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','NTE',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC1','ROL',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'PPR_PC2' THEN
   -- PPR_PC2 ('MSH','PID','PV1','PRB','NTE','ROL')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','PRB',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','NTE',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC2','ROL',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'PPR_PC3' THEN
   -- PPR_PC3 ('MSH','PID','PV1','PRB','NTE','ROL')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','PRB',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','NTE',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('PPR_PC3','ROL',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'OMP_O09' THEN
   -- OMP_O09 ('MSH','PID','PV1','ORC','RXO','RXR')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','RXO',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('OMP_O09','RXR',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'RDS_O13' THEN
   -- RDS_O13 ('MSH','PID','PV1','ORC','RXD','RXR')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','RXD',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('RDS_O13','RXR',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  ELSIF messageMappping.message_type ILIKE 'ORU_R01' THEN
   -- ORU_R01 ('MSH','PID','PV1','ORC','OBR','OBX','NTE')
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','MSH',messageMappping.version,0,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','PID',messageMappping.version,1,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','PV1',messageMappping.version,2,false,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','ORC',messageMappping.version,3,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','OBR',messageMappping.version,4,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','OBX',messageMappping.version,5,true,'A',messageMappping.interface_id,'_implementer','_implementer');
   INSERT INTO message_mapping_details_hl7 (message_type,segment,version,seg_order,repeat_segment,status,interface_id,created_by,modified_by) VALUES ('ORU_R01','NTE',messageMappping.version,6,true,'A',messageMappping.interface_id,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select messageMappingFunction();
DROP function messageMappingFunction();

-- Inserts segment template records.
CREATE OR REPLACE FUNCTION insertSegmentTemplate() 
RETURNS VOID AS $BODY$ 
DECLARE
 segmentRecord RECORD;
BEGIN
 FOR segmentRecord IN SELECT DISTINCT segment, version, interface_id FROM message_mapping_details_hl7 WHERE version = '2.5.1' LOOP
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
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_1',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_3',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('RXR_4',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select insertSegmentTemplate();
DROP function insertSegmentTemplate();