-- liquibase formatted sql
-- changeset allabakash:pending-prescriptions

CREATE SEQUENCE pending_prescription_declined_reasons_seq START 1;
COMMENT ON SEQUENCE pending_prescription_declined_reasons_seq IS '{ "type": "Master", "comment": "Holds sequence for declined_reason_id" }';

CREATE TABLE pending_prescription_declined_reasons (
  declined_reason_id BIGINT DEFAULT nextval('pending_prescription_declined_reasons_seq'),
  created_by VARCHAR(30) NOT NULL, 
  created_at TIMESTAMP DEFAULT now(),
  modified_by VARCHAR(30),
  modified_at TIMESTAMP DEFAULT now(),
  reason CHARACTER VARYING(350),
  status VARCHAR(1),
  PRIMARY KEY(declined_reason_id)
);
COMMENT ON TABLE pending_prescription_declined_reasons IS '{ "type": "Master", "comment": "Holds restrictions against receipts" }';



CREATE SEQUENCE patient_pending_prescriptions_seq START 1;
COMMENT ON SEQUENCE patient_pending_prescriptions_seq IS '{ "type": "Txn", "comment": "Holds sequence for pending prescription id" }';


CREATE TABLE patient_pending_prescriptions (
   pat_pending_presc_id BIGINT DEFAULT nextval('patient_pending_prescriptions_seq'),
   patient_presc_id VARCHAR(35) NOT NULL,
   presc_item_id VARCHAR(50) NOT NULL,
   presc_item_type VARCHAR(10) NOT NULL,
   presc_item_qty INTEGER,
   prescribed_date TIMESTAMP DEFAULT now(),
   start_datetime TIMESTAMP,
   prescribed_by VARCHAR(20),
   status VARCHAR(10),
   declined_by VARCHAR(30),
   declined_at TIMESTAMP,
   declined_reason_id BIGINT REFERENCES pending_prescription_declined_reasons(declined_reason_id),
   modified_by VARCHAR(30),
   modified_at TIMESTAMP DEFAULT now(),
   assigned_to_role_id INTEGER,
   assigned_to_user_id CHARACTER VARYING(35),
   PRIMARY KEY(pat_pending_presc_id)
);
COMMENT ON TABLE patient_pending_prescriptions IS '{ "type": "Txn", "comment": "Holds pending prescription workflow table" }';

CREATE SEQUENCE pending_prescription_details_seq START 1;
COMMENT ON SEQUENCE pending_prescription_details_seq IS '{ "type": "Txn", "comment": "Holds sequence for pending prescription id" }';

CREATE TABLE pending_prescription_details (
  pending_prescription_details_id BIGINT DEFAULT nextval('pending_prescription_details_seq'),
  modified_by VARCHAR(30),
  modified_at TIMESTAMP DEFAULT now(),
  assigned_to_user_id CHARACTER VARYING(35),
  assigned_to_role_id INTEGER,
  pending_prescription_id BIGINT REFERENCES patient_pending_prescriptions(pat_pending_presc_id),
  remark text,
  PRIMARY KEY(pending_prescription_details_id)
);
COMMENT ON TABLE pending_prescription_details IS '{ "type": "Txn", "comment": "Holds pending prescription details table" }';



