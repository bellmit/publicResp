-- liquibase formatted sql
-- changeset rajendratalekar:insert-phone-and-govid-tokens-to-patient-search-tokens

INSERT INTO patient_search_tokens (SELECT mr_no,'patient_details', lower(government_identifier), false FROM  patient_details where government_identifier is not null and government_identifier != '');
INSERT INTO patient_search_tokens (SELECT mr_no,'patient_details', patient_phone, false FROM  patient_details where patient_phone is not null and patient_phone !='');
INSERT INTO patient_search_tokens (SELECT mr_no,'patient_details', replace(patient_phone, patient_phone_country_code, ''), false FROM  patient_details where patient_phone_country_code is not null or patient_phone_country_code != '');
INSERT INTO patient_search_tokens (SELECT contact_id,'contact_details', patient_contact, false FROM  contact_details  where  patient_contact is not null and patient_contact !='');
