-- liquibase formatted sql
-- changeset shilpanr:Deprecating-episode-limits.

ALTER TABLE insurance_plan_main RENAME COLUMN limits_include_followup TO obsolete_limits_include_followup;

ALTER TABLE insurance_plan_main RENAME COLUMN op_episode_limit TO obsolete_op_episode_limit;

ALTER TABLE patient_insurance_plans RENAME COLUMN episode_limit TO obsolete_episode_limit;

ALTER TABLE patient_insurance_plans RENAME COLUMN episode_deductible TO obsolete_episode_deductible;

ALTER TABLE patient_insurance_plans RENAME COLUMN episode_copay_percentage TO obsolete_episode_copay_percentage;

ALTER TABLE patient_insurance_plans RENAME COLUMN episode_max_copay_percentage TO obsolete_episode_max_copay_percentage;
