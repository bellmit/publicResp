-- liquibase formatted sql
-- changeset junzy:year-of-onset-fk-idx.sql

ALTER TABLE mrd_codes_details ADD CONSTRAINT mrd_codes_details_mrd_cdoe_id_fk FOREIGN KEY (mrd_code_id) REFERENCES mrd_codes_master (mrd_code_id);

CREATE INDEX mrd_codes_details_mrd_code_id_idx ON mrd_codes_details USING btree (mrd_code_id);