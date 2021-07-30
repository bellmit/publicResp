-- liquibase formatted sql
-- changeset junzy:migration-for-haad-year-of-onset.sql

CREATE TABLE mrd_codes_details (mrd_code_id INTEGER, health_authority VARCHAR, is_year_of_onset_mandatory BOOLEAN,
            PRIMARY KEY (mrd_code_id, health_authority));