-- liquibase formatted sql
-- changeset sandeep:receipt-audit-log-migration

INSERT INTO receipts_audit_log (log_id, user_name, mod_time, operation, field_name, old_value, new_value, receipt_id)
	SELECT  log_id, user_name, mod_time, operation, field_name, old_value, new_value, receipt_no AS receipt_id
	FROM bill_receipts_audit_log
	UNION ALL
	SELECT  log_id, user_name, mod_time, operation,	field_name, old_value, new_value, deposit_no AS receipt_id
	FROM deposits_audit_log;