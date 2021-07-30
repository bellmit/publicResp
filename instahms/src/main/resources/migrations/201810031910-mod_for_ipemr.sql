-- liquibase formatted sql
-- changeset sonam009:enable_new_ipemr_bydefault

INSERT INTO modules_activated (module_id, activation_status) (SELECT 'mod_ipemr', activation_status FROM modules_activated WHERE module_id = 'mod_ipservices' 
AND NOT EXISTS (select 1 from modules_activated where module_id = 'mod_ipemr'));