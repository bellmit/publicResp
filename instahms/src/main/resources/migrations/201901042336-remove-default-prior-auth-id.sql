-- liquibase formatted sql
-- changeset rajendratalekar:remove-default-prior-auth-id

ALTER TABLE patient_insurance_plans ALTER COLUMN prior_auth_id DROP DEFAULT;
UPDATE patient_insurance_plans set prior_auth_id = NULL where prior_auth_id = '0';