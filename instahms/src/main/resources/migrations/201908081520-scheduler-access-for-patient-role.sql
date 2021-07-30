-- liquibase formatted sql
-- changeset sanjana.goyal:fix-scheduler-access-for-patient-role

insert into url_action_rights select 5,'doc_scheduler_available_slots','A' where not exists (select role_id from url_action_rights where role_id=5 AND action_id='doc_scheduler_available_slots');
insert into url_action_rights select 5,'cat_resource_scheduler','A' where not exists (select role_id from url_action_rights where role_id=5 AND action_id='cat_resource_scheduler');
insert into url_action_rights select 5,'res_availability','A' where not exists (select role_id from url_action_rights where role_id=5 AND action_id='res_availability');
insert into url_action_rights select 5,'mas_doctors','A' where not exists (select role_id from url_action_rights where role_id=5 AND action_id='mas_doctors');
insert into url_action_rights select 5,'visit_emr_screen','A' where not exists (select role_id from url_action_rights where role_id=5 AND action_id='visit_emr_screen');
