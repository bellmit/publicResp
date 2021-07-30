-- liquibase formatted sql
-- changeset pranays:add-reopened-flags-and-migrate-ot-forms-to-patient_form_details

-- Form level reopen status column
-- Form_Gen, Form_OT
ALTER TABLE patient_form_details ADD COLUMN is_reopened boolean DEFAULT false;

-- Form_IP, Discharge Summary
ALTER TABLE patient_registration
    ADD COLUMN ipemr_reopened boolean DEFAULT false,
    ADD COLUMN discharge_summary_reopened boolean DEFAULT false;

-- Form_CONS
ALTER TABLE doctor_consultation ADD COLUMN cons_reopened boolean DEFAULT false;

