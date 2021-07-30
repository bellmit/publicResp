-- liquibase formatted sql
-- changeset allabakash:idx-consolidated-fa-report
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:f select count(*) > 0 from pg_indexes where schemaname = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and indexname='idx_billcharge_posteddate'

 CREATE INDEX idx_billcharge_posteddate ON bill_charge(DATE(posted_date AT TIME ZONE 'UTC'));
 CREATE INDEX idx_patient_registration_patientid_optype ON patient_registration(patient_id, op_type);
 