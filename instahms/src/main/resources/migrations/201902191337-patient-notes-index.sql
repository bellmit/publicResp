-- liquibase formatted sql
-- changeset janakivg:patient_notes_patient_id_idx

CREATE INDEX notes_patient_id_idx ON patient_notes USING btree (patient_id);
