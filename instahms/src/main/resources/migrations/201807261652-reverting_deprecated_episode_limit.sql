-- liquibase formatted sql
-- changeset shilpanr:Reverting-deprecated-episode-limits.

ALTER TABLE insurance_plan_main RENAME COLUMN obsolete_limits_include_followup TO limits_include_followup;

ALTER TABLE insurance_plan_main RENAME COLUMN obsolete_op_episode_limit TO op_episode_limit;

ALTER TABLE patient_insurance_plans RENAME COLUMN obsolete_episode_limit TO episode_limit;

ALTER TABLE patient_insurance_plans RENAME COLUMN obsolete_episode_deductible TO episode_deductible;

ALTER TABLE patient_insurance_plans RENAME COLUMN obsolete_episode_copay_percentage TO episode_copay_percentage;

ALTER TABLE patient_insurance_plans RENAME COLUMN obsolete_episode_max_copay_percentage TO episode_max_copay_percentage;