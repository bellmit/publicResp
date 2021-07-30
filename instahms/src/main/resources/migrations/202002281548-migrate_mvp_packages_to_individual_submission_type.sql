-- liquibase formatted sql
-- changeset abhishekv31:convert-all-mvp-packages-to-submission-type-individual
UPDATE packages SET submission_batch_type='I' WHERE multi_visit_package;
