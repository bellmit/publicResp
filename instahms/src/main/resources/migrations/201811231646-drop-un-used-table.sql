-- liquibase formatted sql
-- changeset yashwantkumar:drop-unused-tables
drop table test_units;
drop table service_units;
drop table treatment_form_entries;
drop table treatment_followups;
drop table patient_treatment;
drop table test_payment_master cascade;
drop table service_payment_master cascade;
drop table donor_blood_test_results cascade;
drop table fixed_asset_master_bkp cascade;
drop table assert_complaint_master_bkp cascade;
