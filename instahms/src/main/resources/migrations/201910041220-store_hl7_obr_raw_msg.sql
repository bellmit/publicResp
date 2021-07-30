-- liquibase formatted sql
-- changeset vinaykumarjavalkar:stores hl7 OBR raw message in test_visit_reports table

ALTER TABLE test_visit_reports ADD COLUMN hl7_obr_segment TEXT;