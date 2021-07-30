-- liquibase formatted sql
-- changeset tejakilaru:triage-mod-time

ALTER TABLE doctor_consultation ADD COLUMN triage_mod_time timestamp without time zone;
