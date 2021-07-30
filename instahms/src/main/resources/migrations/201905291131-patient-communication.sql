-- liquibase formatted sql
-- changeset sanjana.goyal:patient-communication

alter table contact_preferences add column promotional_consent character varying(1) default 'N';

CREATE SEQUENCE patient_communication_preferences_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

create table patient_communication_preferences (preference_id numeric default nextval('patient_communication_preferences_seq'), mr_no character varying(15), communication_type character(1), message_group_name character varying(100), PRIMARY KEY (preference_id), FOREIGN KEY (mr_no) REFERENCES patient_details(mr_no));

COMMENT ON table patient_communication_preferences is '{ "type": "Txn", "comment": "Table to store which mr_no wants to receive which selected message_type via whih mode" }';

comment on column patient_communication_preferences.communication_type is 'S-sms, E-email, N-nobe, B-both';

insert into patient_communication_preferences (mr_no,communication_type, message_group_name ) select mr_no, 'N', 'Vaccine Reminder' from patient_details where sms_for_vaccination='N';

insert into message_category (message_category_name,status) values ('Registration','A');
insert into message_category (message_category_name,status) values ('Billing','A');
insert into message_category (message_category_name,status) values ('Appointments','A');
insert into message_category (message_category_name,status) values ('Diagnostics','A');
insert into message_category (message_category_name,status) values ('Discharge','A');
insert into message_category (message_category_name,status) values ('Inventory','A');
insert into message_category (message_category_name,status) values ('Vaccination','A');
insert into message_category (message_category_name,status) values ('Ward','A');
insert into message_category (message_category_name,status) values ('Doctor','A');
insert into message_category (message_category_name,status) values ('Management','A');
insert into message_category (message_category_name,status) values ('Promotional','A');
insert into message_category (message_category_name,status) values ('Custom','A');
insert into message_category (message_category_name,status) values ('Custom Promotional','A');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Registration') where message_type_id in ('sms_revise_patient_on_patient_admitted', 'sms_patient_on_patient_admitted', 'sms_edit_patient_access', 'sms_family_on_ip_admission', 'sms_patient_on_op_admission', 'sms_family_on_op_admission', 'sms_patient_on_op_revisit', 'sms_patient_on_ip_revisit', 'sms_family_on_ip_revisit', 'sms_patient_on_ip_admission', 'sms_family_on_op_revisit', 'sms_feedback_reminder', 'sms_enable_mobile_access', 'email_enable_mobile_access');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Billing') where message_type_id in ('notification_bill_cancellation', 'sms_bill_payment_received', 'email_op_bn_cash_bill', 'email_manual_op_bn_cash_bill', 'sms_patient_due_for_visit', 'sms_deposit_paid', 'sms_bill_refund');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Appointments') where message_type_id in ('sms_appointment_cancellation', 'sms_appointment_confirmation', 'sms_appointment_details_change', 'sms_appointment_reschedule', 'sms_followup_reminder', 'sms_appointment_reminder', 'sms_next_day_appointment_reminder', 'sms_dynamic_appointment_reminder', 'sms_appointment_booked');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Diagnostics') where message_type_id in ('email_diag_report','sms_report_ready');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Discharge') where message_type_id in ('sms_patient_on_discharge', 'sms_nok_on_patient_discharge');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Inventory') where message_type_id in ('email_purches_oder_report');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Vaccination') where message_type_id in ('sms_vaccine_reminder');
update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Ward') where message_type_id in ('sms_patient_ward_bed_shift', 'sms_next_of_kin_ward_bed_shift');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Doctor') where message_type_id in ('sms_patient_admitted', 'sms_doctor_appointments', 'sms_to_doctor_on_pat_discharge', 'sms_doctor_ward_bed_shift', 'sms_appointment_confirmation_for_doctor', 'message_for_lab_critical_val', 'sms_op_patient_admitted');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Management') where message_type_id in ('sms_daily_collection', 'sms_discount_given','email_on_coder_review_update' , 'sms_advance_paid');

update message_types set category_id =(select message_category_id from message_category where message_category_name= 'Custom') where category_id is null;

alter table message_types add column recipient_category character varying(50);

update message_types set recipient_category='Patient' where category_id in (select message_category_id from message_category where message_category_name in ('Registration','Billing', 'Appointments', 'Diagnostics', 'Discharge','Vaccination', 'Ward', 'Custom' ));

update message_types set recipient_category='Doctor' where category_id in (select message_category_id from message_category where message_category_name in ('Doctor'));

update message_types set recipient_category='Management' where category_id in (select message_category_id from message_category where message_category_name in ('Management'));

update message_types set recipient_category='Inventory' where category_id in (select message_category_id from message_category where message_category_name in ('Inventory'));

update message_types set recipient_category='Practo' where category_id in (select message_category_id from message_category where message_category_name in ('Practo Share'));

alter table message_types add column message_group_name character varying(100);

update message_types set message_group_name = message_type_name;

update message_types set message_group_name = 'Enable Mobile Access' where message_type_id in ('email_enable_mobile_access','sms_enable_mobile_access');

insert into message_types (message_type_id,message_type_name,message_type_description,message_sender,message_to,message_subject, 
message_body,message_cc,message_bcc,event_id, message_mode,status,category_id,message_group,editability,message_footer,confidential,message_group_name,
recipient_category) select (case when message_type_id ilike 'sms_%' then REPLACE(message_type_id, 'sms', 'email') else  REPLACE(message_type_id, 'email', 'sms') end), 
message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id,
case when message_mode='SMS' then 'EMAIL' else 'SMS' end, status, category_id, message_group, editability, message_footer, confidential,
message_group_name, recipient_category from message_types where message_mode in ('SMS','EMAIL') and recipient_category='Patient' AND category_id in 
(select message_category_id from message_category where message_category_name in ('Registration','Billing', 'Appointments', 'Diagnostics', 'Discharge',
'Vaccination', 'Ward') and message_type_id not in ('email_enable_mobile_access','sms_enable_mobile_access', 'sms_revise_patient_on_patient_admitted',
'sms_patient_on_patient_admitted','sms_family_on_ip_admission','sms_family_on_op_admission', 'sms_family_on_ip_revisit','email_diag_report',
'sms_family_on_op_revisit', 'email_manual_op_bn_cash_bill', 'sms_next_of_kin_ward_bed_shift', 'sms_nok_on_patient_discharge', 'email_on_coder_review_update' , 'sms_appointment_planner'));

update message_types set message_type_name = CONCAT(message_type_name,'(SMS)') where message_mode='SMS' and recipient_category='Patient' AND category_id in (select message_category_id from message_category where message_category_name in ('Registration','Billing', 'Appointments', 'Diagnostics', 'Discharge',
'Vaccination', 'Ward'));

update message_types set message_type_name = CONCAT(message_type_name,'(EMAIL)') where message_mode='EMAIL' and recipient_category='Patient' AND category_id in (select message_category_id from message_category where message_category_name in ('Registration','Billing', 'Appointments', 'Diagnostics', 'Discharge', 'Vaccination', 'Ward'));

COMMENT ON sequence patient_communication_preferences_seq is '{ "type": "Txn", "comment": "patient communication preferences sequence" }';
