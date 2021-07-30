-- liquibase formatted sql
-- changeset sonam009:op forms migration to create follow up forms

SELECT * INTO form_component_op_cons FROM (
 SELECT fc.*, dept_id, center_id, fcca.status AS center_status, status_on_practo
 FROM form_components fc
 JOIN form_department_details fdc ON (fc.id = fdc.id)
 JOIN form_components_center_applicability fcca ON (fcca.form_components_id = fc.id)
 WHERE form_type='Form_CONS' AND form_name !='Consultation' AND istemplate=false
 )AS foo;
 
 SELECT * INTO form_component_op_follow_up_cons FROM (
 SELECT fc.*, dept_id, center_id, fcca.status AS center_status, status_on_practo
 FROM form_components fc
 JOIN form_department_details fdc ON (fc.id = fdc.id)
 JOIN form_components_center_applicability fcca ON (fcca.form_components_id = fc.id)
 WHERE form_type ='Form_OP_FOLLOW_UP_CONS' AND istemplate=false
 )AS foo;
 
 DELETE FROM form_component_op_cons op  WHERE exists (SELECT 1 FROM form_component_op_follow_up_cons fop 
 WHERE  fop.form_name=op.form_name OR (op.doctor_id=fop.doctor_id AND op.dept_id=fop.dept_id AND op.center_id=fop.center_id));
 
 SELECT * , nextval('form_components_seq') AS new_id INTO form_components_temp_id FROM ( 
	SELECT distinct sections, id, group_patient_sections, form_type, operation_id, form_name, status, doctor_id, dept_id FROM form_component_op_cons)AS foo;
	
UPDATE  form_component_op_cons fcoc SET id = fcti.new_id FROM form_components_temp_id fcti WHERE fcti.id=fcoc.id;

INSERT INTO form_components(sections, id, group_patient_sections, form_type, operation_id, form_name, status, doctor_id) 
(SELECT sections, new_id, group_patient_sections, 'Form_OP_FOLLOW_UP_CONS', operation_id, form_name, status,  doctor_id FROM form_components_temp_id);

INSERT INTO form_department_details(id, dept_id ) (SELECT new_id, dept_id FROM form_components_temp_id);

INSERT INTO form_components_center_applicability(form_components_center_id, form_components_id, center_id, status, status_on_practo ) 
(SELECT nextval('form_components_center_applicability_seq'), id, center_id, center_status,  status_on_practo FROM form_component_op_cons);

DROP TABLE form_component_op_follow_up_cons;
DROP TABLE form_component_op_cons;
DROP TABLE form_components_temp_id;
