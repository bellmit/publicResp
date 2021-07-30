-- liquibase formatted sql
-- changeset shilpanr:Adding-patient_insurance_plans_id-column-in-patient-insurance-plan-details-table.

ALTER TABLE patient_insurance_plan_details ADD COLUMN patient_insurance_plans_id integer;

UPDATE patient_insurance_plan_details pipd SET patient_insurance_plans_id = pip.patient_insurance_plans_id  
FROM patient_insurance_plans pip 
WHERE pipd.visit_id = pip.patient_id AND pipd.plan_id = pip.plan_id;