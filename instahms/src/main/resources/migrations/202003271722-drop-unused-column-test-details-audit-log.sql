-- liquibase formatted sql
-- changeset rajendratalekar:drop-unused-column-test-detials-audit-log
ALTER TABLE test_details_audit_log DROP COLUMN id;