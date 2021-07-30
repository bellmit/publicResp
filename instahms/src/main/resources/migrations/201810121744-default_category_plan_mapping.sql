-- liquibase formatted sql
-- changeset shilpanr:mapping-default-insurance-category-to-all-existing-insurance-plans
INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_type,category_payable)
(SELECT plan_id,0,'o','N' FROM insurance_plan_main);

INSERT INTO insurance_plan_details (plan_id, insurance_category_id, patient_type,category_payable)
(SELECT plan_id,0,'i','N' FROM insurance_plan_main);
