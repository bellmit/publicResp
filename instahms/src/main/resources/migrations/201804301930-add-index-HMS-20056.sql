-- liquibase formatted sql
-- changeset goutham005:added-index-on-test_details.test_id.

CREATE INDEX test_details_test_id_idx ON test_details(test_id);