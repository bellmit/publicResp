-- liquibase formatted sql
-- changeset vinaykumarjavalkar:hl7_preference_to_receive_preliminary_report

ALTER TABLE hl7_lab_interfaces ADD COLUMN receive_preliminary_report VARCHAR(1) default 'N';
ALTER TABLE hl7_lab_interfaces ADD COLUMN allow_multiple_addendums VARCHAR(1) default 'N';
ALTER TABLE test_visit_reports ADD COLUMN result_status VARCHAR(1);
ALTER TABLE test_documents ADD COLUMN result_status VARCHAR(1); 