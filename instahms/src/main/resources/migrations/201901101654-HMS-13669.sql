-- liquibase formatted sql
-- changeset sirisharl:adding_po_round_off_column
ALTER TABLE generic_preferences  ADD COLUMN po_round_off CHARACTER DEFAULT 'A';
COMMENT ON COLUMN generic_preferences.po_round_off IS 'A for Automatic ,M for Manual';
