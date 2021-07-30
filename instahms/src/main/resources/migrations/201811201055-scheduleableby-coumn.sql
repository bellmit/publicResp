-- liquibase formatted sql
-- changeset sanjana:adding-scheduleable-by-column-in-doctors

alter table doctors add column scheduleable_by char(1) default 'A';
update doctors set scheduleable_by='N' where schedule='f';