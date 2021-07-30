-- liquibase formatted sql
-- changeset yashwantkumar:optimized_lab_report_print
ALTER TABLE generic_preferences ADD COLUMN optimized_lab_report_print character varying(1) default 'N';