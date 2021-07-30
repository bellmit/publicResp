-- liquibase formatted sql
-- changeset yashwantkumar:add_notification_review_column_in_test_visit_reports

ALTER TABLE test_visit_reports ADD COLUMN reviewed_by character varying;
ALTER TABLE test_visit_reports ADD COLUMN review_remarks character varying;
ALTER TABLE test_visit_reports ADD COLUMN reviewed_date timestamp without time zone;
