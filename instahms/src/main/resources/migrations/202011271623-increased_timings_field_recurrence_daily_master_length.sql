-- liquibase formatted sql
-- changeset riyapoddar-13:increased_timings_field_recurrency_serving_freequency_length

ALTER TABLE recurrence_daily_master ALTER COLUMN timings TYPE varchar(255);
ALTER TABLE serving_frequency_master ALTER COLUMN timings TYPE varchar(255);
