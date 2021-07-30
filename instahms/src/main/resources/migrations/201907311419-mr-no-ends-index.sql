-- liquibase formatted sql
-- changeset sanjana.goyal:mr-no-ends-index

drop index mr_no_ends_with_index;
CREATE INDEX mr_no_ends_with_index ON patient_details USING btree (reverse(mr_no) varchar_pattern_ops);
