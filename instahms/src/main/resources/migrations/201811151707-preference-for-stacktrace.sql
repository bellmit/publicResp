-- liquibase formatted sql
-- changeset adityabhatia02:preference-for-stacktrace

ALTER TABLE generic_preferences ADD COLUMN show_stacktrace BOOLEAN NOT NULL default true;