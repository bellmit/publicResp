-- liquibase formatted sql
-- changeset yaminipagaria:adding-admission-discharge-prescription-notes-in-doctor_consultation-and-patient_registration

alter table patient_registration add column discharge_prescription_notes text;
alter table doctor_consultation add column discharge_prescription_notes text;