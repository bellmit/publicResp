-- liquibase formatted sql
-- changeset goutham005:enable_new_consultation_links_for_migrated_users


INSERT INTO screen_rights (SELECT role_id,'new_cons', rights FROM screen_rights  WHERE screen_id='op_prescribe' 
	AND role_id NOT IN (SELECT role_id FROM screen_rights WHERE screen_id = 'new_cons'));

INSERT INTO url_action_rights (SELECT role_id,'new_cons', rights FROM url_action_rights  WHERE action_id='op_prescribe'
	AND role_id NOT IN (SELECT role_id FROM url_action_rights WHERE action_id = 'new_cons'));


INSERT INTO screen_rights (SELECT role_id,'new_followup_cons', rights FROM screen_rights  WHERE screen_id='op_prescribe' 
	AND role_id NOT IN (SELECT role_id FROM screen_rights WHERE screen_id = 'new_followup_cons'));

INSERT INTO url_action_rights (SELECT role_id,'new_followup_cons', rights FROM url_action_rights  WHERE action_id='op_prescribe'
	AND role_id NOT IN (SELECT role_id FROM url_action_rights WHERE action_id = 'new_followup_cons'));


INSERT INTO screen_rights (SELECT role_id,'new_triage', rights FROM screen_rights  WHERE screen_id='triage_form' 
	AND role_id NOT IN (SELECT role_id FROM screen_rights WHERE screen_id = 'new_triage'));

INSERT INTO url_action_rights (SELECT role_id,'new_triage', rights FROM url_action_rights  WHERE action_id='triage_form'
	AND role_id NOT IN (SELECT role_id FROM url_action_rights WHERE action_id = 'new_triage'));
	
