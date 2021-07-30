-- liquibase formatted sql
-- changeset manasaparam:adding-visit-mode-column-in-scheduler_appointments

ALTER TABLE scheduler_appointments ADD COLUMN visit_mode character varying(1);

update scheduler_appointments set visit_mode ='I';