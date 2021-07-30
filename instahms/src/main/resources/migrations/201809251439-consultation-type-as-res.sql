-- liquibase formatted sql
-- changeset akshaysuman:consultation-type-changes-for-scheduler

insert into scheduler_master 
(res_sch_id,res_sch_category,dept,res_sch_name,description,height_in_px,default_duration,status,res_sch_type)
values 
(nextval('res_sch_id_seq'),'DOC','*','*','for all consultations',15,15,'A','CTY');

update scheduler_appointments set res_sch_id = (select res_sch_id from scheduler_master where res_sch_type = 'CTY') 
where res_sch_id = 1;

update scheduler_appointments set res_sch_name = coalesce(
consultation_type_id,'-1'
) where res_sch_id = (select res_sch_id from scheduler_master where res_sch_type = 'CTY');
