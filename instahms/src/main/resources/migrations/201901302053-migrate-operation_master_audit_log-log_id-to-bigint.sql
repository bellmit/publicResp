-- liquibase formatted sql
-- changeset prashantbaisla:migrate-operation_master_audit_log-log_id-to-bigint
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:integer SELECT data_type FROM information_schema.columns WHERE column_name = 'log_id' and table_name = 'operation_master_audit_log' and table_schema = current_schema();

ALTER TABLE operation_master_audit_log RENAME TO operation_master_audit_log_old;
CREATE TABLE operation_master_audit_log (LIKE operation_master_audit_log_old INCLUDING ALL);
ALTER TABLE operation_master_audit_log ALTER COLUMN log_id TYPE bigint;
