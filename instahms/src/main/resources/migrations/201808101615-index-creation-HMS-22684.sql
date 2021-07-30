-- liquibase formatted sql
-- changeset mancini2802:created-index-for-tests-prescribed



CREATE INDEX tests_prescribed_package_ref_is_null_idx ON tests_prescribed USING btree (package_ref NULLS FIRST);
CREATE INDEX tests_prescribed_pres_doctor_idx ON tests_prescribed USING btree (pres_doctor);
CREATE INDEX tests_prescribed_sflag_idx ON tests_prescribed USING btree (sflag);