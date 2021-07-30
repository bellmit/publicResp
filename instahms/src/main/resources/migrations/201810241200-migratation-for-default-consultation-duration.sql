-- liquibase formatted sql
-- changeset raeshmika:<migrating-all-doctors-default-duration-as-consultation-duration>

UPDATE consultation_types SET duration = sm.default_duration
FROM (SELECT default_duration FROM scheduler_master WHERE res_sch_category='DOC' AND dept='*' AND res_sch_name='*' AND res_sch_type='DOC') sm
WHERE duration IS NULL;