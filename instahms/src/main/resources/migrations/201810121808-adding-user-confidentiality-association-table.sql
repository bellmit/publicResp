-- liquibase formatted sql
-- changeset adityabhatia02:adding-user-confidentiality-group-tables

CREATE TABLE confidentiality_grp_master (
	id integer primary key,
	name varchar(100),
	status character(1) DEFAULT 'A' NOT NULL,
	break_the_glass character(1) DEFAULT 'Y' NOT NULL
);

CREATE TABLE user_confidentiality_association (
	id integer primary key,
	emp_username character varying(30),
	confidentiality_grp_id integer,
	FOREIGN KEY (emp_username) REFERENCES u_user(emp_username),
	FOREIGN KEY (confidentiality_grp_id) REFERENCES confidentiality_grp_master(id),
	status character(1) DEFAULT 'A' NOT NULL
);

INSERT INTO confidentiality_grp_master VALUES ('0', 'General Patient', 'A');
ALTER TABLE patient_details ADD COLUMN patient_group integer default '0' NOT NULL;
ALTER TABLE ONLY patient_details ADD CONSTRAINT patient_confidentiality_group_fk FOREIGN KEY (patient_group) references confidentiality_grp_master(id);