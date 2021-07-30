-- liquibase formatted sql
-- changeset manjular:migrate-mrno_pattern_transaction_type

UPDATE hosp_id_patterns SET transaction_type='MRN' WHERE transaction_type IS NULL AND pattern_id IN(SELECT DISTINCT code FROM patient_category_master);
