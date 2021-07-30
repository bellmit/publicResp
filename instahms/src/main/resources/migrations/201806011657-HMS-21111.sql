-- liquibase formatted sql
-- changeset SirishaRL:missing_db_11.12.1_release_script 
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:false SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE  table_name='scheduler_appointments' AND column_name='cond_doc_id') 

alter table scheduler_appointments add column cond_doc_id varchar(30);