-- liquibase formatted sql
-- changeset rajendratalekar:alter-patient-registration-add-transfer-reason-field

ALTER TABLE patient_registration add column outbound_referral_reason_id integer;
ALTER TABLE patient_registration 
   ADD CONSTRAINT fk_patient_registration_outbound_referral_reason_id
   FOREIGN KEY (outbound_referral_reason_id) REFERENCES outbound_referral_reason_master(id);
