-- liquibase formatted sql
-- changeset tejakilaru:<prescribale_flag_in_diagnostics>

ALTER TABLE diagnostics ADD COLUMN is_prescribable boolean default true;