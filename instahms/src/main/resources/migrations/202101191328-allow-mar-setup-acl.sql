-- liquibase formatted sql
-- changeset riyapoddar-13:Adding-allow-mar-setup-acl

insert into action_rights(action,role_id,rights) select 'allow_mar_setup', role_id ,'A' from u_role;