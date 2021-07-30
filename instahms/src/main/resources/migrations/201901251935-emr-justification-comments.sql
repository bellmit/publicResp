-- liquibase formatted sql
-- changeset goutham005:EMR-justification-comments

CREATE SEQUENCE emr_justification_comments_seq 
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE;

COMMENT ON sequence emr_justification_comments_seq is '{ "type": "Txn", "comment": "Sequence for emr justification comments" }';
	
CREATE TABLE emr_justification_comments (
	emr_access_comment_id integer DEFAULT nextval('emr_justification_comments_seq'),
	emp_username character varying(30),
	mr_no character varying(15),
	comments text,
	access_time timestamp without time zone default now(),

	CONSTRAINT emr_access_comment_id_pkey PRIMARY KEY (emr_access_comment_id)
);

COMMENT ON table emr_justification_comments is '{ "type": "Txn", "comment": "will capture comments while accessing emr records for the logged in user" }';

