-- liquibase formatted sql
-- changeset akshaysuman:consultation-type-0-migration.sql

update scheduler_appointments set res_sch_name = '-1',consultation_type_id = -1 
where res_sch_id = (select res_sch_id from scheduler_master where res_sch_type = 'CTY') and consultation_type_id = 0;