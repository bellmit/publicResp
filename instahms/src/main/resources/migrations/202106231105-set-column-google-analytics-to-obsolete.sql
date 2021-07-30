-- liquibase formatted sql
-- changeset raeshmika:set-column-google-analytics-to-obsolete

ALTER TABLE generic_preferences rename column google_analytics_key to obsolete_google_analytics_key;