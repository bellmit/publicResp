-- liquibase formatted sql
-- changeset pranays:<add-lower-index-on-doc-name-referal-name>

CREATE INDEX idx_lower_doctor_name ON doctors(LOWER(doctor_name));

CREATE INDEX idx_lower_referal_name ON referral(LOWER(referal_name));
