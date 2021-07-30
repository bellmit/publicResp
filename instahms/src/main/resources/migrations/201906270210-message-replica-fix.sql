-- liquibase formatted sql
-- changeset sanjana.goyal:message-replica-fix

update message_types set message_to='${patient_email_id}' where message_type_id='email_patient_ward_bed_shift';
update message_types set message_to='${patient_email}' where message_type_id='email_patient_due_for_visit';

update message_types set message_subject=message_type_name where message_mode='EMAIL' and (message_subject='' or message_subject is null);
