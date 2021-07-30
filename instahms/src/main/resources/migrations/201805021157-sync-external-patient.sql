-- liquibase formatted sql

-- changeset allabakash:sync-external-patient

-- Sync_external_patient_module

INSERT INTO modules_activated values('mod_sync_external_patient', 'Y');

CREATE TABLE prepopulate_visit_info (mr_no CHARACTER VARYING(19) REFERENCES patient_details(mr_no), visit_values text);

CREATE TABLE public_api_ip_whitelist (ip_start TEXT NOT NULL, ip_end TEXT , UNIQUE (ip_start, ip_end));

