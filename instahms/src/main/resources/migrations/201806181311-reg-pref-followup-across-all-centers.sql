-- liquibase formatted sql
-- changeset satishl2772:adding-registration-preference-followup-across-all-centers

ALTER TABLE registration_preferences ADD COLUMN followup_across_centers CHARACTER(1) NOT NULL DEFAULT 'Y';
