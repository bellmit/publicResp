-- liquibase formatted sql
-- changeset akshaysuman:add_edit_contact_action_rights

insert into action_rights (role_id,action,rights)  
(select role_id, 'add_edit_contact', rights from action_rights where action = 'allow_new_registration');