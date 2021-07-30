-- liquibase formatted sql
-- changeset shilpanr:case-rate-details-in-patient-registration-and-in-insurance-plan-table

ALTER TABLE insurance_plan_main ADD COLUMN limit_type character varying(1) DEFAULT 'C';

ALTER TABLE insurance_plan_main ADD COLUMN case_rate_count integer DEFAULT 0;

ALTER TABLE patient_registration ADD COLUMN primary_case_rate_id integer DEFAULT NULL;

ALTER TABLE patient_registration ADD COLUMN secondary_case_rate_id integer DEFAULT NULL;