-- liquibase formatted sql
-- changeset anupama.mr:status-message-for-accounting-export

ALTER TABLE accounting_export_journal ADD COLUMN status_message character varying(255);

