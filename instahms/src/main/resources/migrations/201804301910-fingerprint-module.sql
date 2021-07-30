-- liquibase formatted sql
-- changeset qwewrty1:Fingerprint-module-and-Nexus-changes

CREATE TABLE patient_fingerprints(
    id SERIAL PRIMARY KEY,
    mr_no VARCHAR(15) REFERENCES patient_details(mr_no),
    created_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    finger VARCHAR(20) NOT NULL,
    fp_data BYTEA NOT NULL,
    fp_thumbnail BYTEA NOT NULL
);
COMMENT ON COLUMN patient_fingerprints.id IS 'patient_fingerprints PRIMARY key';
COMMENT ON COLUMN patient_fingerprints.mr_no IS 'Patient MR_NO';
COMMENT ON COLUMN patient_fingerprints.finger IS 'Patient Finger Eg: Right_Thumb';
COMMENT ON COLUMN patient_fingerprints.fp_data IS 'Patient Finger impression in bytea datatype';
COMMENT ON COLUMN patient_fingerprints.fp_thumbnail IS 'Patient Finger thumbnail(84 X 84) in bytea datatype';


CREATE TABLE fp_log_purpose(
    purpose_id SERIAL PRIMARY KEY,
    purpose VARCHAR(100) UNIQUE,
    created_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    deleted_by VARCHAR(30) REFERENCES u_user(emp_username),
    deleted_at TIMESTAMP,
    status VARCHAR(1) DEFAULT 'A'
);

COMMENT ON COLUMN fp_log_purpose.purpose_id IS 'fp_log_purpose PRIMARY key';
COMMENT ON COLUMN fp_log_purpose.purpose IS 'Fingerprint verification purpose.';
COMMENT ON COLUMN fp_log_purpose.status IS 'status values are,  A for Active, I for Inactive';

CREATE SEQUENCE fp_log_id_sequence START 1;

CREATE TABLE fp_logs(
    log_id BIGINT PRIMARY KEY DEFAULT nextval('fp_log_id_sequence'),
    mr_no VARCHAR(15) REFERENCES patient_details(mr_no),
    patient_id VARCHAR(15) REFERENCES patient_registration(patient_id),
    purpose_id INTEGER REFERENCES fp_log_purpose(purpose_id) NOT NULL, 
    finger VARCHAR(20) NOT NULL, 
    authorized_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
    authorized_at TIMESTAMP DEFAULT now() NOT NULL
);

COMMENT ON COLUMN fp_logs.log_id IS 'fp_logs PRIMARY key';
COMMENT ON COLUMN fp_logs.purpose_id IS 'purpose_id from fp_log_purpose table.';
COMMENT ON COLUMN fp_logs.finger IS 'Patient Finger Eg: Right_Thumb';

INSERT INTO fp_log_purpose(purpose, created_by, updated_by, status) VALUES ('Fingerprint Registration', 'InstaAdmin', 'InstaAdmin', 'A'); 

INSERT INTO fp_log_purpose(purpose, created_by, updated_by, status) VALUES ('Fingerprint Edit', 'InstaAdmin', 'InstaAdmin', 'A'); 

INSERT INTO fp_log_purpose(purpose, created_by, updated_by, status) VALUES ('Fingerprint Delete', 'InstaAdmin', 'InstaAdmin', 'A'); 

INSERT INTO fp_log_purpose(purpose, created_by, updated_by, status) VALUES ('Other 1', 'InstaAdmin', 'InstaAdmin', 'I'); 

INSERT INTO fp_log_purpose(purpose, created_by, updated_by, status) VALUES ('Other 2', 'InstaAdmin', 'InstaAdmin', 'I');

ALTER TABLE generic_preferences ADD COLUMN fingerprint_dp_threshold INTEGER DEFAULT 3000;

ALTER TABLE u_user ADD COLUMN nexus_token character varying(100);