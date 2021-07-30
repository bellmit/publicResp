-- liquibase formatted sql
-- changeset sreenivasayashwanth:operation-duration

ALTER TABLE operation_master ADD COLUMN operation_duration Integer ;

UPDATE operation_master o SET operation_duration = 
	(COALESCE(
	(select default_duration from scheduler_master sm where res_sch_type = 'SUR' AND o.op_id = sm.res_sch_name),
	(select default_duration from scheduler_master sm where res_sch_type = 'SUR' AND sm.res_sch_name = '*')
	));

ALTER TABLE operation_master 
	ALTER COLUMN operation_duration TYPE integer, 
	ALTER COLUMN operation_duration SET NOT NULL;