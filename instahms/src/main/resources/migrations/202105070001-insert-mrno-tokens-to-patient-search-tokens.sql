-- liquibase formatted sql
-- changeset rajendratalekar:insert-mrno-tokens-to-patient-search-tokens

INSERT INTO patient_search_tokens (SELECT mr_no, 'patient_details', lower(mr_no), false FROM  patient_details);
INSERT INTO patient_search_tokens (SELECT mr_no, 'patient_details', lower(reverse(mr_no)), true FROM  patient_details);
INSERT INTO patient_search_tokens (SELECT mr_no, 'patient_details', lower(oldmrno), false FROM  patient_details where oldmrno is not null and oldmrno != '');
INSERT INTO patient_search_tokens (SELECT mr_no, 'patient_details', lower(reverse(oldmrno)), true FROM  patient_details where oldmrno is not null and oldmrno != '');
