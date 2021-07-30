-- liquibase formatted sql
-- changeset junzy:adds-transaction-validity-dates-columns

ALTER TABLE preauth_request_approval_details ADD COLUMN end_date TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE preauth_request_approval_details ADD COLUMN start_date TIMESTAMP WITHOUT TIME ZONE;
