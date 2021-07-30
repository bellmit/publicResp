-- liquibase formatted sql
-- changeset goutham005:initial-assessment-screen-rights

INSERT INTO screen_rights (SELECT role_id,'new_initial_assessment', rights FROM screen_rights  WHERE screen_id='initial_assessment' 
	AND role_id NOT IN (SELECT role_id FROM screen_rights WHERE screen_id = 'new_initial_assessment'));

INSERT INTO url_action_rights (SELECT role_id,'new_initial_assessment', rights FROM url_action_rights  WHERE action_id='initial_assessment'
	AND role_id NOT IN (SELECT role_id FROM url_action_rights WHERE action_id = 'new_initial_assessment'));
	