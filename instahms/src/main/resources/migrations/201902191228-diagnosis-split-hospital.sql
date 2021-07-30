-- liquibase formatted sql
-- changeset junaidahmed:diagnosis-split-for-hospital-claims

CREATE SEQUENCE hospital_claim_diagnosis_seq;

CREATE TABLE hospital_claim_diagnosis (
    visit_id character varying(15),
    id integer NOT NULL DEFAULT nextval('hospital_claim_diagnosis_seq'),
    description character varying,
    icd_code character varying(100),
    code_type character varying(50),
    diag_type character(1),
    username character varying(30),
    mod_time timestamp with time zone,
    remarks character varying(5000),
    year_of_onset integer,
    present_on_admission character varying(20),
    modified_by character varying(30) DEFAULT 'SYSTEM'
);

COMMENT ON sequence hospital_claim_diagnosis_seq is '{ "type": "Txn", "comment": "" }';
COMMENT ON table hospital_claim_diagnosis is '{ "type": "Txn", "comment": "Coder updated MRD diagnoses" }';
COMMENT ON COLUMN hospital_claim_diagnosis.modified_by is 'Will be set to SYSTEM when copied from mrd_diagnosis, updated with coders username when modified';

INSERT INTO hospital_claim_diagnosis(id, visit_id, description, icd_code, code_type, diag_type, username, mod_time, remarks, 
	modified_by, year_of_onset, present_on_admission)
SELECT id, visit_id, description, icd_code, code_type, diag_type, username, mod_time, remarks, 'SYSTEM', year_of_onset, present_on_admission 
FROM mrd_diagnosis 
WHERE mod_time between (CURRENT_TIMESTAMP - INTERVAL '3 month') and CURRENT_TIMESTAMP;
