-- liquibase formatted sql
-- changeset tejakilaru:new_clinical_preferences

CREATE TABLE clinical_preferences (
	op_prescription_format CHARACTER(1) NOT NULL DEFAULT 'B',
	ip_prescription_format CHARACTER(1) NOT NULL DEFAULT 'B',
	allow_prescription_format_override CHARACTER(1) NOT NULL DEFAULT 'N',
	op_prescription_validity CHARACTER(1) NOT NULL DEFAULT 'N',
	op_prescription_validity_period INTEGER,
	op_consultation_auto_closure CHARACTER(1) NOT NULL DEFAULT 'N',
	op_consultation_auto_closure_period INTEGER
);

INSERT INTO clinical_preferences (op_prescription_validity_period, op_consultation_auto_closure_period) VALUES (1,1);
