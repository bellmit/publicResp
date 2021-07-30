-- liquibase formatted sql
-- changeset javalkarvinay:38096_added_column_to_identify_revert_signoff

ALTER TABLE tests_prescribed ADD COLUMN signoff_reverted boolean default false;
