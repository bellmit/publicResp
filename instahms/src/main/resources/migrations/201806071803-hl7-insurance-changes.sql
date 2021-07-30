-- liquibase formatted sql
-- changeset goutham005:hl7-insurance-changes.

ALTER TABLE hl7_lab_interfaces ADD COLUMN send_insurance_segments CHARACTER(1) DEFAULT 'N';

ALTER TABLE insurance_company_master ADD COLUMN interface_code CHARACTER VARYING(100);

ALTER TABLE insurance_plan_main ADD COLUMN interface_code CHARACTER VARYING(100);