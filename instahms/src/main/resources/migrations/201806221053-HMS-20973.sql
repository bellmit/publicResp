-- liquibase formatted sql
-- changeset SirishaRL:prescribing_doctor_dept_id_in_accounting_table failOnError:false
ALTER TABLE hms_accounting_info RENAME COLUMN custom_7 TO prescribing_doctor_dept_id;