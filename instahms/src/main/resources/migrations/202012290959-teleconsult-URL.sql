-- liquibase formatted sql
-- changeset manasaparam:adding-teleconsultURL

ALTER TABLE scheduler_appointments ADD COLUMN teleconsult_URL character varying(260);