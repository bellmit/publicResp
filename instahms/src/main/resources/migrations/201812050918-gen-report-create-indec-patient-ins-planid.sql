-- liquibase formatted sql
-- changeset mohamedanees:<create-index-pat-ins-plan-details-planid>

CREATE INDEX patient_insurance_plan_details_planid ON patient_insurance_plan_details USING btree (patient_insurance_plans_id);