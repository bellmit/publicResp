-- liquibase formatted sql
-- changeset abhishekv31:change-prior-auth-approval-result-15-chars
ALTER TABLE preauth_request_approval_details alter column approval_result type varchar(15);
ALTER TABLE preauth_prescription_request alter column approval_result type varchar(15);

