-- liquibase formatted sql
-- changeset sanjana.goyal:emrview-api-acess-to-portal-access-users

insert into url_action_rights (role_id, action_id, rights) select role_id,'visit_emr_screen', 'A' from u_role where portal_id='P' AND (not exists (select * from url_action_rights where role_id=(select role_id from u_role where portal_id='P' limit 1) AND action_id= 'visit_emr_screen')) limit 1 ; 