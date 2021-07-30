-- liquibase formatted sql
-- changeset sanjana.goyal:schedulable-by-for-packages

Alter table packages add column scheduleable_by character(1) default 'S' not null;

comment on column packages.scheduleable_by IS 'S-hms staff only, A - All , 
this column is used to decide whether to show the package on patient portal or not';