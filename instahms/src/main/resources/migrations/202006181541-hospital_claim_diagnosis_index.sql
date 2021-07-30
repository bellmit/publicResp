-- liquibase formatted sql
-- changeset manjular:index_on_hospital_claim_diagnosis_column_id failOnError:false

CREATE INDEX hospital_claim_diagnosis_id_idx ON hospital_claim_diagnosis(id);

