-- liquibase formatted sql
-- changeset adityabhatia02:remove-null-constraint-from-usermrnoassoication-audit-logs

ALTER TABLE user_mrno_association_audit_log ALTER COLUMN granted_access_at DROP not null;
ALTER TABLE user_mrno_association_audit_log ALTER COLUMN revoked_access_at DROP not null;