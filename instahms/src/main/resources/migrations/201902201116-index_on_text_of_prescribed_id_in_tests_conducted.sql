-- liquibase formatted sql
-- changeset abhishekv31:index-on-text-of-presribed-id-on-tests-conducted failOnError:false
CREATE INDEX tc_prescribed_id_varchar_index ON tests_conducted(text(prescribed_id));
