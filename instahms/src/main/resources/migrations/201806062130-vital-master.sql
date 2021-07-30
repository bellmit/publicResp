-- liquibase formatted sql
-- changeset yaskumar:vital_parameter_master

ALTER TABLE vital_parameter_master ADD COLUMN created_by character varying(30); 
ALTER TABLE vital_parameter_master ADD COLUMN modified_by character varying(30);
ALTER TABLE vital_parameter_master ADD COLUMN modified_at timestamp without time zone;

ALTER TABLE vital_reference_range_master ADD COLUMN created_by character varying(30); 
ALTER TABLE vital_reference_range_master ADD COLUMN modified_by character varying(30);
ALTER TABLE vital_reference_range_master ADD COLUMN modified_at timestamp without time zone;
ALTER TABLE vitals_default_details ADD COLUMN visit_type character varying(1);


insert into screen_rights select role_id, 'new_mas_vitalparameters', rights from screen_rights where screen_id = 'mas_vitalparameters';

insert into url_action_rights select role_id, 'new_mas_vitalparameters', rights from url_action_rights where action_id = 'mas_vitalparameters';

INSERT INTO vitals_default_details (vital_default_id,param_id,center_id,dept_id,mandatory,visit_type)
SELECT nextval('vitals_default_details_seq'), vpm.param_id,hcm.center_id,d.dept_id, 'N','I' from department d,vital_parameter_master vpm,hospital_center_master hcm where vpm.param_container='V' AND (vpm.visit_type is null OR vpm.visit_type = 'I') AND (coalesce(d.dept_type_id,'') = '' OR d.dept_type_id != 'NOCL')
AND (CASE WHEN (select max_centers_inc_default from generic_preferences) > 1 THEN hcm.center_id !=0
ELSE true END);

INSERT INTO vitals_default_details (vital_default_id,param_id,center_id,dept_id,mandatory,visit_type)
SELECT nextval('vitals_default_details_seq'), vpm.param_id,hcm.center_id,d.dept_id, 'N','O' from department d,vital_parameter_master vpm,hospital_center_master hcm where vpm.param_container='V' AND (vpm.visit_type is null OR vpm.visit_type = 'O') AND (coalesce(d.dept_type_id,'') = '' OR d.dept_type_id != 'NOCL')
AND (CASE WHEN (select max_centers_inc_default from generic_preferences) > 1 THEN hcm.center_id !=0
ELSE true END);

CREATE INDEX param_id_idx ON vitals_default_details(param_id);
