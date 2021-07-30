-- liquibase formatted sql
-- changeset adityabhatia02:user-mrno-association-table

ALTER TABLE confidentiality_grp_master ADD UNIQUE (abbreviation);
ALTER TABLE confidentiality_grp_master ADD UNIQUE (name);
CREATE SEQUENCE user_mrno_association_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE user_mrno_association (
	id integer primary key DEFAULT nextval('user_mrno_association_seq'),
	emp_username character varying(30) not null,
	mr_no character varying(15) not null,
	FOREIGN KEY (emp_username) REFERENCES u_user(emp_username),
	FOREIGN KEY (mr_no) REFERENCES patient_details(mr_no)
);

CREATE TABLE user_mrno_association_audit_log (
	log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
	emp_username character varying(30) not null,
	mr_no character varying(15) not null,
	remarks character varying(2000) not null,
	granted_access_at timestamp without time zone not null,
	revoked_access_at timestamp without time zone not null
);

COMMENT ON TABLE user_mrno_association is '{ "type": "Master", "comment": "Association table between user and mr number" }';
COMMENT ON TABLE user_mrno_association_audit_log is '{ "type": "Txn", "comment": "Audit log for break the glass" }';
COMMENT ON sequence user_mrno_association_seq is '{ "type": "Master", "comment": "Sequence for association table between user and confidentiality groups" }';
