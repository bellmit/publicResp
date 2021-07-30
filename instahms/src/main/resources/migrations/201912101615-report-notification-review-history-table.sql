-- liquibase formatted sql
-- changeset pranays:<create-review-history-table-for-lab-reports>

CREATE SEQUENCE report_review_history_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE report_review_history(
	id INTEGER PRIMARY KEY DEFAULT nextval('report_review_history_seq'::regClass),
	entity_id INTEGER,
	remarks VARCHAR,
	reviewed_by VARCHAR(50),
	reviewed_date timestamp without time zone DEFAULT now() NOT NULL,
	is_test_doc VARCHAR(1) DEFAULT 'N'
);


COMMENT ON table report_review_history is '{ "type": "Txn", "comment": "Lab/Rad Report Review History" }';

COMMENT ON sequence report_review_history_seq is '{ "type": "Txn", "comment": "Entity(test-doc or report) sequence in Review History" }';
