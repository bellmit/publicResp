-- liquibase formatted sql
-- changeset sanjana:scheduler-access-right-addonsadmin

INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'doc_scheduler_available_slots','A';