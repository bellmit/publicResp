-- liquibase formatted sql
-- changeset allabakash:received_message_job_status

ALTER TABLE received_message ALTER column job_status TYPE CHARACTER VARYING(50);