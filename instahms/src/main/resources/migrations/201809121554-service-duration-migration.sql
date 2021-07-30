-- liquibase formatted sql
-- changeset akshaysuman:service_duration_change

ALTER TABLE services ADD COLUMN service_duration Integer ;

UPDATE services s SET service_duration = 
	(COALESCE(
	(select default_duration from scheduler_master sm where res_sch_type = 'SER' AND s.service_id = sm.res_sch_name),
	(select default_duration from scheduler_master sm where res_sch_type = 'SER' AND sm.res_sch_name = '*')
	));

ALTER TABLE services 
	ALTER COLUMN service_duration TYPE integer, 
	ALTER COLUMN service_duration SET NOT NULL;