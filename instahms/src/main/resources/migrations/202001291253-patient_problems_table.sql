-- liquibase formatted sql
-- changeset javalkarvinay:patient_problems_table

CREATE SEQUENCE patient_problem_list_seq START 1;
COMMENT ON SEQUENCE patient_problem_list_seq IS '{ "type": "Txn", "comment": "Holds sequence for patient problem list id" }';

CREATE TABLE patient_problem_list (
    ppl_id INTEGER DEFAULT nextval('patient_problem_list_seq'),
    mr_no VARCHAR(15) REFERENCES patient_details(mr_no),
    section_detail_id INTEGER,
    patient_problem_id INTEGER REFERENCES mrd_codes_master(mrd_code_id),
    ext_identified BOOLEAN NOT NULL,
    identified_by VARCHAR(20),
    ext_identified_by VARCHAR(35),
    onset DATE DEFAULT now(),
    problem_note VARCHAR,
    status CHARACTER(1) NOT NULL,
    recorded_date TIMESTAMP DEFAULT now(),
    recorded_by VARCHAR(20),
    created_by VARCHAR(30),
    created_at TIMESTAMP DEFAULT now(),
    modified_by VARCHAR(30),
    modified_at TIMESTAMP DEFAULT now(),
    deleted_by VARCHAR(30),
    deleted_at TIMESTAMP,
    PRIMARY KEY (ppl_id)
);
COMMENT ON TABLE patient_problem_list IS '{ "type": "Txn", "comment": "Holds patient problem list" }';

CREATE SEQUENCE patient_problem_list_details_seq START 1;
COMMENT ON SEQUENCE patient_problem_list_details_seq IS '{ "type": "Txn", "comment": "Holds sequence for patient problem list details id" }';

CREATE TABLE patient_problem_list_details (
    ppld_id INTEGER DEFAULT nextval('patient_problem_list_details_seq'),
    ppl_id INTEGER REFERENCES patient_problem_list(ppl_id),
    visit_id VARCHAR(15) REFERENCES patient_registration(patient_id),
    problem_status VARCHAR(1) NOT NULL,
    last_status_date DATE,
    created_by VARCHAR(30),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (ppld_id)
);
COMMENT ON TABLE patient_problem_list_details IS '{ "type": "Txn", "comment": "Holds patient problem list details " }';