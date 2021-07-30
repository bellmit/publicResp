-- liquibase formatted sql
-- changeset manjular:adding-new-column-apply_cp_validation_for_po failOnError:false

ALTER TABLE generic_preferences  ADD COLUMN apply_cp_validation_for_po CHARACTER DEFAULT 'A';
COMMENT ON COLUMN generic_preferences.apply_cp_validation_for_po IS 'A for Allow ,W for Warn ,B for Block';

