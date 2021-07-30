-- liquibase formatted sql
-- changeset yashwantkumar:index_on_table_vital_reading

CREATE INDEX vital_reading_param_id_idx ON vital_reading(param_id);
