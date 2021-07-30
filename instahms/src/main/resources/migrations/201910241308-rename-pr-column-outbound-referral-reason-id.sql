-- liquibase formatted sql
-- changeset rajendratalekar:rename-pr-column-outbound-referral-reason-id

ALTER TABLE patient_registration 
   DROP CONSTRAINT fk_patient_registration_outbound_referral_reason_id;

ALTER TABLE patient_registration RENAME COLUMN outbound_referral_reason_id TO reason_for_referral_id;
ALTER TABLE patient_registration 
   ADD CONSTRAINT fk_patient_registration_outbound_referral_reason_id
   FOREIGN KEY (reason_for_referral_id) REFERENCES reason_for_referral(id);
