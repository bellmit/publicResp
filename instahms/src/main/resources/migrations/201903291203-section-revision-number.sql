-- liquibase formatted sql
-- changeset sonam009:section-revision-number
ALTER TABLE patient_section_details ADD COLUMN revision_number numeric; 