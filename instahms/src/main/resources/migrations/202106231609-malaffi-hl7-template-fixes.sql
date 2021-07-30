-- liquibase formatted sql
-- changeset javalkarvinay:malaffi-hl7-template-fixes splitStatements:false

CREATE OR REPLACE FUNCTION insertSegmentTemplate() 
RETURNS VOID AS $BODY$ 
DECLARE
 segmentRecord RECORD;
BEGIN
 FOR segmentRecord IN SELECT DISTINCT mmdh.segment,mmdh.version,icm.interface_id FROM message_mapping_details_hl7 mmdh JOIN interface_config_master icm ON (mmdh.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%') LOOP
  IF segmentRecord.segment ILIKE 'PR1' THEN
   INSERT INTO hl7_segment_template (segment_field_number,interface_id,version,created_by,modified_by) VALUES ('PR1_0',segmentRecord.interface_id,segmentRecord.version,'_implementer','_implementer');
  END IF;
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
select insertSegmentTemplate();
DROP function insertSegmentTemplate();

UPDATE hl7_segment_template hst SET field_template = '<#if code?? && code.malaffi?? && code.malaffi.department??><#if department_id?? && code.malaffi.department[department_id?string]??><#assign malaffiDeptCode = code.malaffi.department[department_id?string]><#elseif code.malaffi.department.default??><#assign malaffiDeptCode = code.malaffi.department.default></#if></#if><#if visit_type?? && visit_type = "O"><#if encounter_types_visit_name?? && encounter_types_visit_name = "Homecare">HC<#elseif malaffiDeptCode?? && malaffiDeptCode = 13?string>E<#else>${visit_type}</#if><#else>${visit_type}</#if>' FROM interface_config_master icm WHERE segment_field_number = 'PV1_2' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if allergy_type?? && allergy_type != "N"><#if allergy_type = "M"><#assign al1_2_1 = "DA"><#assign al1_2_2 = "Drug allergy"><#elseif allergy_type = "F"><#assign al1_2_1 = "FA"><#assign al1_2_2 = "Food allergy"><#elseif allergy_type = "O"><#assign al1_2_1 = "MA"><#assign al1_2_2 = "Miscellaneous allergy"></#if></#if><#if al1_2_1??>${al1_2_1}^<#if al1_2_2??>${al1_2_2}</#if>^<#if code_system_name??>${code_system_name}</#if></#if>' FROM interface_config_master icm WHERE segment_field_number = 'AL1_2' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if ins_co_code?? && (ins_co_code?starts_with("A") || ins_co_code?starts_with("E"))>true</#if>' FROM interface_config_master icm WHERE segment_field_number = 'IN1_0' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if operation_conducted_status?? && operation_conducted_status="C">true</#if>' FROM interface_config_master icm WHERE segment_field_number = 'PR1_0' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if tp_modified_by_doctor_id?? && tp_modified_by_doctor_id !="" && tp_modified_by_doc_last_name?? && tp_modified_by_doc_first_name??><#assign obr32_1 = tp_modified_by_doc_license_number><#assign obr32_2 = tp_modified_by_doc_last_name><#assign obr32_3 = tp_modified_by_doc_first_name><#elseif tp_modified_by_employee_id?? && tp_modified_by_employee_id != "" && tp_modified_by_user_last_name?? && tp_modified_by_user_first_name??><#assign obr32_1 = tp_modified_by_employee_id><#assign obr32_2 = tp_modified_by_user_last_name><#assign obr32_3 = tp_modified_by_user_first_name></#if><#if obr32_1??>${obr32_1}^${obr32_2}^${obr32_3}^^^^^^&<#if sending_facility??>${sending_facility}-DOHID</#if></#if>' FROM interface_config_master icm WHERE segment_field_number = 'OBR_32' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if tp_modified_by_doctor_id?? && tp_modified_by_doctor_id !="" && tp_modified_by_doc_last_name?? && tp_modified_by_doc_first_name??><#assign obx16_1 = tp_modified_by_doc_license_number><#assign obx16_2 = tp_modified_by_doc_last_name><#assign obx16_3 = tp_modified_by_doc_first_name><#elseif tp_modified_by_employee_id?? && tp_modified_by_employee_id != "" && tp_modified_by_user_last_name?? && tp_modified_by_user_first_name??><#assign obx16_1 = tp_modified_by_employee_id><#assign obx16_2 = tp_modified_by_user_last_name><#assign obx16_3 = tp_modified_by_user_first_name></#if><#if obx16_1??>${obx16_1}^${obx16_2}^${obx16_3}^^^^^^&<#if sending_facility??>${sending_facility}-DOHID</#if></#if>' FROM interface_config_master icm WHERE segment_field_number = 'OBX_16' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE message_mapping_details_hl7 SET repeat_segment = false WHERE message_type = 'ORU_R01' AND version='2.5.1' AND segment IN ('ORC','OBR');

UPDATE hl7_segment_template hst SET field_template = '<#if signoff_reverted?? && signoff_reverted>C<#else>F</#if>' FROM interface_config_master icm WHERE segment_field_number = 'OBR_25' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if presc_doc_license_number?? && presc_doc_first_name?? && presc_doc_last_name??><#assign docLicenseNumber = presc_doc_license_number><#assign docFirstName = presc_doc_first_name><#assign docLastName = presc_doc_last_name><#assign sFacility = sending_facility+"-DOHID"><#elseif visit_data?? && visit_data[0]?? && visit_data[0].op_type?? && visit_data[0].op_type = "O"><#assign docLicenseNumber = "SYSTEM"><#assign docFirstName = "SYSTEM"><#assign docLastName = "SYSTEM"><#assign sFacility = sending_facility></#if><#if docLicenseNumber??>${docLicenseNumber}^<#if docLastName??>${docLastName}</#if>^<#if docFirstName??>${docFirstName}</#if>^^^^^^&<#if sFacility??>${sFacility}</#if></#if>' FROM interface_config_master icm WHERE segment_field_number = 'ORC_12' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';

UPDATE hl7_segment_template hst SET field_template = '<#if presc_doc_license_number?? && presc_doc_first_name?? && presc_doc_last_name??><#assign docLicenseNumber = presc_doc_license_number><#assign docFirstName = presc_doc_first_name><#assign docLastName = presc_doc_last_name><#assign sFacility = sending_facility+"-DOHID"><#elseif visit_data?? && visit_data[0]?? && visit_data[0].op_type?? && visit_data[0].op_type = "O"><#assign docLicenseNumber = "SYSTEM"><#assign docFirstName = "SYSTEM"><#assign docLastName = "SYSTEM"><#assign sFacility = sending_facility></#if><#if docLicenseNumber??>${docLicenseNumber}^<#if docLastName??>${docLastName}</#if>^<#if docFirstName??>${docFirstName}</#if>^^^^^^&<#if sFacility??>${sFacility}</#if></#if>' FROM interface_config_master icm WHERE segment_field_number = 'OBR_16' AND hst.interface_id = icm.interface_id AND icm.interface_name like 'Malaffi_%';