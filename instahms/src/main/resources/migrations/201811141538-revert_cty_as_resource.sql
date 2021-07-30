-- liquibase formatted sql
-- changeset akshaysuman:removing_cty_from_scheduler_master

update scheduler_appointments set res_sch_id = 1 
where res_sch_id = (select res_sch_id from scheduler_master where res_sch_type = 'CTY');
delete from scheduler_master where res_sch_type = 'CTY';
