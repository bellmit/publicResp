-- liquibase formatted sql
-- changeset rajendratalekar:insert-name-tokens-to-patient-search-tokens

INSERT INTO patient_search_tokens (SELECT pd.mr_no,'patient_details', s.token, false FROM  patient_details pd, unnest(string_to_array(regexp_replace(btrim(lower(concat_ws(' ', pd.patient_name, pd.middle_name, pd.last_name))), '(\s|\t)+', ' ','g'), ' ')) s(token));
INSERT INTO patient_search_tokens (SELECT cd.contact_id,'contact_details', s.token, false FROM  contact_details cd, unnest(string_to_array(regexp_replace(btrim(lower(concat_ws(' ', cd.patient_name, cd.middle_name, cd.last_name))), '(\s|\t)+', ' ','g'), ' ')) s(token));
