-- liquibase formatted sql
-- changeset harishm18:patient-package-content-charges

CREATE SEQUENCE patient_package_content_charge_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE patient_package_content_charges (
	patient_package_content_charge_id INTEGER DEFAULT nextval('patient_package_content_charge_id_seq'::regclass) NOT NULL PRIMARY KEY,
	patient_package_content_id INTEGER,
    org_id character varying(15)  NOT NULL,
    bed_type character varying(50) NOT NULL,
    charge numeric(15, 2) NOT NULL,
    discount numeric(15, 2) NOT NULL,
    modified_by character varying(50),
    modified_at timestamp without time zone DEFAULT NULL,
    created_by character varying(50),
    created_at timestamp without time zone DEFAULT NOW()
);

ALTER TABLE ONLY patient_package_content_charges
    ADD CONSTRAINT fk_pat_pak_cont_id FOREIGN KEY (patient_package_content_id) REFERENCES patient_package_contents(patient_package_content_id);

COMMENT ON TABLE patient_package_content_charges IS 
	'{ "type": "Txn", "comment": "Contains details of patient package content charges." }';
COMMENT ON SEQUENCE patient_package_content_charge_id_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID of the patient package content charges." }';

CREATE SEQUENCE patient_customised_package_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE patient_customised_package_details (
	patient_customised_package_detail_id INTEGER DEFAULT nextval('patient_customised_package_detail_id_seq'::regclass) NOT NULL PRIMARY KEY,
	patient_package_id INTEGER,
    package_name character varying(100)  NOT NULL,
    package_code character varying(50) DEFAULT NULL,
    amount numeric(15, 2) NOT NULL,
    discount numeric(15, 2) NOT NULL,
    package_terms_conditions character varying(4000) DEFAULT NULL,
    pre_requisites character varying(4000) DEFAULT NULL,
    remarks character varying(4000) DEFAULT NULL,
    multi_visit_package boolean DEFAULT false,
    modified_by character varying(50),
    modified_at timestamp without time zone DEFAULT NULL,
    created_by character varying(50),
    created_at timestamp without time zone DEFAULT NOW()
);

ALTER TABLE ONLY patient_customised_package_details
    ADD CONSTRAINT fk_pat_cust_pak_id FOREIGN KEY (patient_package_id) REFERENCES patient_packages(pat_package_id);

ALTER TABLE patient_package_contents ADD COLUMN submission_type character(1) NOT NULL default 'P';

COMMENT ON TABLE patient_customised_package_details IS 
	'{ "type": "Txn", "comment": "Contains details of patient customised package details." }';
COMMENT ON SEQUENCE patient_customised_package_detail_id_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID of the patient customised package details." }';
