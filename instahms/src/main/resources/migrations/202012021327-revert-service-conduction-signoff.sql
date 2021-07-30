-- liquibase formatted sql
-- changeset riyapoddar-13:Adding-revert-sevice-conduction-signoff-acls

insert into action_rights(action,role_id,rights) select 'revert_service_conduction', role_id ,'N' from u_role;
insert into action_rights(action,role_id,rights) select 'revert_service_signoff', role_id,'N' from u_role;