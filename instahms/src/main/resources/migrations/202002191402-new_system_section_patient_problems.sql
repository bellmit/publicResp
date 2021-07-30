-- liquibase formatted sql
-- changeset javalkarvinay:new_system_section_patient_problems

insert into system_generated_sections (section_id,section_name,section_mandatory,op,ip,surgery,service,triage,initial_assessment,generic_form,op_follow_up_consult_form,display_name,edd_expression_value)
values (-21,'Problem List (Sys)',false,'Y','Y','N','N','N','N','N','N','Problem List',null);

-- Patient Problem OP Event for HL7 message
insert into events_hl7 (id,event) values (13,'Patient_Problem_OP'),(14,'Patient_Problem_IP');
