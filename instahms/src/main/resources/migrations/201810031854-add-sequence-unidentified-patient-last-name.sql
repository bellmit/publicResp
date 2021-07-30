-- liquibase formatted sql
-- changeset adityabhatia02:add-sequence-unidentified-patient-last-name

CREATE SEQUENCE unidentified_patient_seq 
				START WITH 1
				INCREMENT BY 1;

COMMENT ON sequence unidentified_patient_seq is '{ "type": "Txn", "comment": "Sequence for unidentified patient name suffix" }';
