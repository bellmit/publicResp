-- liquibase formatted sql
-- changeset sonam009:added-discharge-medication-sys-section

INSERT INTO system_generated_sections (section_id, section_name, op, ip, service, display_name) VALUES ('-22', 'Discharge Medication (Sys)', 'Y', 'Y', 'N', 'Discharge Medication');
 
INSERT INTO insta_section_rights (role_id, section_id) (SELECT ur.role_id, -22 FROM u_role ur join screen_rights sr using (role_id) WHERE ur.role_id != 1 AND ur.role_id != 2 AND sr.screen_id='discharge_medication');
 
UPDATE form_components set sections=REPLACE(REPLACE(sections, '-22,', ''), ',-22', '') || ',-22' where form_type = 'Form_IP' AND istemplate=false ;

ALTER TABLE patient_medicine_prescriptions ADD COLUMN is_discharge_medication boolean default false;
ALTER TABLE patient_other_medicine_prescriptions ADD is_discharge_medication boolean default false;
ALTER TABLE patient_other_prescriptions ADD is_discharge_medication boolean default false;
