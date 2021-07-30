-- liquibase formatted sql
-- changeset sanjana.goyal:added-client-id-in-appointment-source-master

alter table appointment_source_master add column client_id INTEGER ;