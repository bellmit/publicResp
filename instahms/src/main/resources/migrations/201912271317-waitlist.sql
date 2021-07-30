-- liquibase formatted sql
-- changeset sanjana.goyal:waitlist

alter table scheduler_appointments add column waitlist integer;