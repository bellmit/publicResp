-- liquibase formatted sql
-- changeset abhishekv31:change-prior-auth-comments-column-type
ALTER TABLE preauth_request_approval_details ALTER COLUMN approval_comments TYPE varchar(10000);
ALTER TABLE preauth_prescription_request ALTER COLUMN approval_comments TYPE varchar(10000);
