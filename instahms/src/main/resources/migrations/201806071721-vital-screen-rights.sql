-- liquibase formatted sql

-- changeset krishna.t:screen-rights-corrected-for-vitals

insert into screen_rights select role_id, 'vital_measurements', rights from screen_rights where screen_id = 'vital_form' and role_id not in (
	select role_id from screen_rights where screen_id='vital_measurements'
);

insert into url_action_rights select role_id, 'vital_measurements', rights from url_action_rights where action_id = 'vital_form' and role_id not in (
	select role_id from url_action_rights where action_id='vital_measurement'
);

delete from screen_rights where screen_id='vital_form';
delete from url_action_rights where action_id='vital_form';

