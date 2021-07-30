-- liquibase formatted sql
-- changeset riyapoddar-13:Adding-services-conducted-end-date

ALTER TABLE services_prescribed ADD COLUMN conducted_end_date timestamp without time zone;