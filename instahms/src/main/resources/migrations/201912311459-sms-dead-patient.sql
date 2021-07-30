-- liquibase formatted sql
-- changeset manasaparam:<contact_preference migration for dead patients>

update contact_preferences set receive_communication='N' where mr_no in (select distinct mr_no from patient_registration where  discharge_type_id=3);
insert into contact_preferences(mr_no,receive_communication,lang_code) (select distinct mr_no,'N','en' from patient_registration where discharge_type_id =3 and mr_no not in (select mr_no from contact_preferences));