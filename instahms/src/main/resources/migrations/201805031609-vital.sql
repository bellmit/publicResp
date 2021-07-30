-- liquibase formatted sql
-- changeset {suryakant.t}:{commit-inserting screen rights and action rights for existing role}

insert into screen_rights (select role_id, 'vital_measurements', rights from screen_rights where screen_id='op_out_patient');

insert into url_action_rights (select role_id, 'vital_measurement', rights from url_action_rights where action_id in ('op_out_patient'));

