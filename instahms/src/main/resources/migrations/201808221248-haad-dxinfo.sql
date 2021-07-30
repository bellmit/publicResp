-- liquibase formatted sql
-- changeset junzy:adds-columns-for-new-haad-regulations-for-dxinfo

ALTER TABLE mrd_diagnosis ADD COLUMN present_on_admission VARCHAR;
ALTER TABLE mrd_diagnosis ADD COLUMN year_of_onset INTEGER;