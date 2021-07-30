-- liquibase formatted sql
-- changeset dattuvs:adding-clinical-preference-for-notes-grouping failOnError:false
ALTER TABLE clinical_preferences ADD COLUMN notes_grouping_preference VARCHAR(5) DEFAULT 'NG';
