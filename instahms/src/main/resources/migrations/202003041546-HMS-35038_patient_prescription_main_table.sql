-- liquibase formatted sql
-- changeset javalkarvinay:HMS-35038_patient_prescription_main_table

CREATE SEQUENCE patient_prescriptions_main_seq START 1;
COMMENT ON SEQUENCE patient_prescriptions_main_seq IS '{ "type": "Txn", "comment": "Holds sequence for patient prescription main id" }';

CREATE TABLE patient_prescriptions_main (
    doc_presc_id INTEGER DEFAULT nextval('patient_prescriptions_main_seq'),
    consultation_id INTEGER,
    visit_id VARCHAR(15) REFERENCES patient_registration(patient_id),
    prescribing_doc_id VARCHAR(20),
    created_by VARCHAR(30),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (doc_presc_id)
);
COMMENT ON TABLE patient_prescriptions_main IS '{ "type": "Txn", "comment": "Holds patient prescriptions main" }';

ALTER TABLE patient_prescription ADD COLUMN doc_presc_id INTEGER REFERENCES patient_prescriptions_main(doc_presc_id);

ALTER TABLE patient_prescription ADD COLUMN created_by VARCHAR(30),
                                 ADD COLUMN created_at TIMESTAMP DEFAULT now(),
                                 ADD COLUMN modified_by VARCHAR(30),
                                 ADD COLUMN modified_at TIMESTAMP; 
UPDATE patient_prescription SET created_at=prescribed_date;