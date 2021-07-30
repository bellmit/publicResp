-- liquibase formatted sql
-- changeset tejakilaru:doctor-consultation-doctor-name-index

CREATE INDEX doctor_consultation_doctor_name_idx ON doctor_consultation(doctor_name);
