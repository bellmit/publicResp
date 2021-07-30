-- liquibase formatted sql
-- changeset yaminipagaria:status-column-in-vitals-for-tracking-delete

alter table vital_reading add column status CHAR(1) NOT NULL DEFAULT 'A';
alter table visit_vitals add column status CHAR(1) NOT NULL DEFAULT 'A';