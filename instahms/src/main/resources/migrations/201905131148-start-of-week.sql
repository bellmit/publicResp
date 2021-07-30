-- liquibase formatted sql
-- changeset sanjana:added-start-of-week-column

alter table hospital_center_master add column start_of_week integer default -1;