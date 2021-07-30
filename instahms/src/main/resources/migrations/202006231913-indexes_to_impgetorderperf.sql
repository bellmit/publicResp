-- liquibase formatted sql
-- changeset shilpanr:indexes-to-improve-getorder-performance failOnError:false

CREATE INDEX scheduler_appointments_visit_id_idx ON scheduler_appointments(visit_id);

CREATE INDEX scheduler_appointments_patient_presc_id_idx ON scheduler_appointments(patient_presc_id);

CREATE INDEX preauth_prescription_preauth_cons_id_idx on preauth_prescription(preauth_cons_id);
