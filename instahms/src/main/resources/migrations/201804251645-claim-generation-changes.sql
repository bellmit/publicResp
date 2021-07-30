-- liquibase formatted sql
-- changeset deepakpracto:claim-generation-changes.sql splitStatements:false

-- === update new column for already existing row for e-claim generation changes ==== 

update insurance_submission_batch set processing_status ='C' where status ='S';
