-- liquibase formatted sql
-- changeset tejakilaru:medicine-prescription-changes

ALTER TABLE patient_medicine_prescriptions ADD COLUMN prescription_format character(1) not null default 'A';
ALTER TABLE patient_medicine_prescriptions ADD COLUMN expiry_date timestamp without time zone;

ALTER TABLE patient_other_medicine_prescriptions ADD COLUMN prescription_format character(1) not null default 'A';
ALTER TABLE patient_other_medicine_prescriptions ADD COLUMN expiry_date timestamp without time zone;

ALTER TABLE doctor_medicine_favourites ADD COLUMN prescription_format character(1) not null default 'A';
ALTER TABLE doctor_other_medicine_favourites ADD COLUMN prescription_format character(1) not null default 'A';
