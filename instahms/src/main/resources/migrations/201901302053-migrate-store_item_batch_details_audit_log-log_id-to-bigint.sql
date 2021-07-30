-- liquibase formatted sql
-- changeset prashantbaisla:migrate-store_item_batch_details_audit_log-log_id-to-bigint
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:integer SELECT data_type FROM information_schema.columns WHERE column_name = 'log_id' and table_name = 'store_item_batch_details_audit_log' and table_schema = current_schema();

ALTER TABLE store_item_batch_details_audit_log RENAME TO store_item_batch_details_audit_log_old;
CREATE TABLE store_item_batch_details_audit_log (LIKE store_item_batch_details_audit_log_old INCLUDING ALL);
ALTER TABLE store_item_batch_details_audit_log ALTER COLUMN log_id TYPE bigint;
