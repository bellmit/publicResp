-- liquibase formatted sql
-- changeset goutham005:enable_new_consultation_bydefault

INSERT INTO modules_activated (module_id, activation_status) (SELECT 'mod_newcons', activation_status FROM modules_activated WHERE module_id = 'mod_op' 
AND NOT EXISTS (select 1 from modules_activated where module_id = 'mod_newcons'));