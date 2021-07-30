-- liquibase formatted sql
-- changeset vinaykumarjavalkar:new column to differentiate system and user log in pend prescription.

ALTER TABLE pending_prescription_details ADD COLUMN system_log boolean default false;