-- liquibase formatted sql
-- changeset akshaysuman:test_duration_change

ALTER TABLE diagnostics ADD COLUMN test_duration Integer ;

UPDATE diagnostics d SET test_duration = 
	(COALESCE(
	(select default_duration from scheduler_master sm where res_sch_type = 'TST' AND d.test_id = sm.res_sch_name),
	(select default_duration from scheduler_master sm where res_sch_type = 'TST' AND sm.res_sch_name = '*')
	));

ALTER TABLE diagnostics 
	ALTER COLUMN test_duration TYPE integer, 
	ALTER COLUMN test_duration SET NOT NULL;