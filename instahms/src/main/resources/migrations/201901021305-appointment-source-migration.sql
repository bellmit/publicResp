-- liquibase formatted sql
-- changeset sanjana:appointment-source-id-migration

insert into appointment_source_master values (-1,'Widget','A','N','3','N');
update scheduler_appointments set app_source_id = -1 where app_source_id in (select appointment_source_id from appointment_source_master where appointment_source_id != -1 AND appointment_source_name ='Widget');
delete from appointment_source_master where appointment_source_id != -1 AND appointment_source_name ='Widget';