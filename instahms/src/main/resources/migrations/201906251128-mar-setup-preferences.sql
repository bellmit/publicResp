-- liquibase formatted sql
-- changeset janakivg:mar-setup-changes-clinical-preferences

ALTER TABLE clinical_preferences ALTER COLUMN advance_setup_period SET DEFAULT 2;
UPDATE clinical_preferences SET advance_setup_period=2;
COMMENT ON COLUMN clinical_preferences.advance_setup_period IS 'value in days';

ALTER TABLE clinical_preferences ADD COLUMN maximum_medication_setup_days INTEGER DEFAULT 5;
COMMENT ON COLUMN clinical_preferences.maximum_medication_setup_days IS 'value in days';

ALTER TABLE patient_mar_setup ADD COLUMN serving_dosage character varying(100);
ALTER TABLE patient_mar_setup ADD COLUMN package_uom character varying(100);

ALTER TABLE recurrence_daily_master ADD COLUMN medication_type character varying(2) DEFAULT 'M';
INSERT INTO recurrence_daily_master (recurrence_daily_id,display_name,status,num_activities,mod_time,medication_type,display_order) values (-3,'Continuous','A',1,now(),'IV',-3);

INSERT INTO serving_frequency_master (name,occurrences,recurrence_daily_id,status) values ('Continuous',1,-3,'A');

ALTER TABLE patient_medicine_prescriptions ADD COLUMN stopped_user character varying(30);
ALTER TABLE patient_medicine_prescriptions ADD COLUMN stopped_reason character varying(2000);
ALTER TABLE patient_medicine_prescriptions ADD COLUMN stopped_date timestamp without time zone;
ALTER TABLE patient_medicine_prescriptions ADD COLUMN stopped_doctor_id character varying(15);
