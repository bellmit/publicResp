-- liquibase formatted sql
-- changeset akshaysuman:plan_code_search

ALTER TABLE registration_preferences ADD COLUMN plan_code_search varchar(1) default 'N' NOT NULL;