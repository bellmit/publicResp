-- liquibase formatted sql
-- changeset pranays:<add-test-docid-col-in-tests-prescribed-table>

ALTER TABLE tests_prescribed ADD COLUMN test_doc_id integer;

ALTER TABLE test_documents
ADD COLUMN reviewed_by character varying,
ADD COLUMN review_remarks character varying,
ADD COLUMN reviewed_date timestamp without time zone;