-- liquibase formatted sql
-- changeset abhishekv31:changed_username_column_type_to_varchar30 failOnError:false
ALTER TABLE pbm_prescription ALTER COLUMN pbm_finalized_by TYPE varchar(30), ALTER COLUMN erx_request_by TYPE varchar(30);
