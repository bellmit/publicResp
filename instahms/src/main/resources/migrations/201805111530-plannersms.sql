-- liquibase formatted sql
--changeset akshaySuman:plannerSms

INSERT INTO message_events (event_id, event_name, event_description) VALUES ('appointment_planner', 'Appointment Planner', 'Event used for triggering the planner SMS');

INSERT INTO message_types (message_type_id, message_type_name, message_type_description, 
message_body, event_id, message_mode, status, message_group, editability) VALUES 
('sms_appointment_planner','Appointment Planner','This message is sent when a plan is booked',
'[#assign appointments_date_list = appointment_date_list?split(r"\s*,\s*", "r") /][#assign appointments_time_list = appointment_time_list?split(r"\s*,\s*", "r") /][#assign complaint_types_list = complaint_type_list?split(r"\s*,\s*", "r") /][#assign n = appointments_date_list?size /] <!--[#if patient_name!=""]--> Dear ${patient_name!" "}, <!--[/#if]--> ${n?string("0")} <!--[#if n?string("0")=="1"]--> appointment <!--[/#if]--> <!--[#if n?string("0")!="1"]--> appointments <!--[/#if]--> have been planned for you. [#list 1..n as i] ${i?string("0")}. Slot : ${appointments_date_list[i-1]} ${appointments_time_list[i-1]} <!--[#if complaint_types_list[i-1]!="N/A"]-->for ${complaint_types_list[i-1]} <!--[/#if]--> [/#list] at ${hospital_name} ${center_name!" "} .<!--[#if center_contact_phone!=""]--> For any queries please contact ${center_contact_phone!" "}. <!--[/#if]-->',
'appointment_planner','SMS','I','general', 
CASE WHEN (select true from modules_activated WHERE module_id='mod_practo_sms' and activation_status='Y') 
THEN 'I' ELSE 'A' END);
