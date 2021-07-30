-- liquibase formatted sql
-- changeset rajendratalekar:center-code-begins-with-index

create index center_code_begins_with_idx on hospital_center_master(center_code varchar_pattern_ops);
