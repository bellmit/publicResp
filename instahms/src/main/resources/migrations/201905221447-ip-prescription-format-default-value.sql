-- liquibase formatted sql
-- changeset janakivg:ip_prescription_format-default-value-change

ALTER TABLE clinical_preferences ALTER COLUMN ip_prescription_format SET DEFAULT 'A'::bpchar;
UPDATE clinical_preferences set ip_prescription_format = 'A';
