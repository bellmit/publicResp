-- liquibase formatted sql
-- changeset adeshatole:op-visit-type-rules-masters


CREATE SEQUENCE op_visit_type_rule_master_seq;

CREATE TABLE op_visit_type_rule_master (
	rule_id INTEGER PRIMARY KEY DEFAULT nextval('op_visit_type_rule_master_seq'),
	rule_name CHARACTER VARYING UNIQUE NOT NULL,
	op_main_visit_limit INTEGER NOT NULL,
	ip_main_visit_limit INTEGER NOT NULL,
	post_limit_visit CHARACTER(1) NOT NULL,
	status character (1) NOT NULL DEFAULT 'A'
);

CREATE SEQUENCE op_visit_type_rule_details_seq;

CREATE TABLE op_visit_type_rule_details (
	rule_details_id INTEGER PRIMARY KEY DEFAULT nextval('op_visit_type_rule_details_seq'),
	rule_id INTEGER REFERENCES op_visit_type_rule_master (rule_id),
	valid_from INTEGER NOT NULL,
	valid_to INTEGER NOT NULL,
	op_visit_type CHARACTER(1) NOT NULL,
    prev_main_visit_type CHARACTER(1) NOT NULL
);

CREATE SEQUENCE op_visit_type_rule_applicability_seq;

CREATE TABLE op_visit_type_rule_applicability (
	rule_applicability_id INTEGER PRIMARY KEY DEFAULT nextval('op_visit_type_rule_applicability_seq'),
    center_id INTEGER NOT NULL,
    tpa_id CHARACTER VARYING(15) NOT NULL,
    dept_id CHARACTER VARYING(10) NOT NULL,
    doctor_id CHARACTER VARYING(20) NOT NULL, 
	rule_id INTEGER REFERENCES op_visit_type_rule_master (rule_id),
    UNIQUE(center_id, tpa_id, dept_id, doctor_id, rule_id)
);
