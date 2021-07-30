-- liquibase formatted sql
-- changeset adityabhatia02:registration-preferences-unidentified-patient

ALTER TABLE registration_preferences ADD COLUMN unidentified_patient_first_name varchar(100);
ALTER TABLE registration_preferences ADD COLUMN unidentified_patient_last_name varchar(100);
ALTER TABLE registration_preferences ADD COLUMN emergency_patient_department_id varchar(10);
ALTER TABLE center_preferences ADD COLUMN emergency_patient_category_id integer;

ALTER TABLE ONLY registration_preferences 
	ADD CONSTRAINT registration_preferences_dept_id_fk 
		FOREIGN KEY (emergency_patient_department_id) REFERENCES department(dept_id);

ALTER TABLE ONLY center_preferences
	ADD CONSTRAINT center_preferences_patient_category_id_fk
		FOREIGN KEY (emergency_patient_category_id) REFERENCES patient_category_master(category_id);