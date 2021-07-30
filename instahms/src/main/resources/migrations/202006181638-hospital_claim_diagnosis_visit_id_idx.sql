-- liquibase formatted sql
-- changeset manjular:index_on_hospital_claim_diagnosis_column_visit_id failOnError:false

CREATE INDEX hospital_claim_diagnosis_visit_id_idx ON hospital_claim_diagnosis(visit_id);

