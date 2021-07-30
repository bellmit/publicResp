-- liquibase formatted sql
-- changeset vakul-practo:patient-allergies-drop-allergy-columns splitStatements:false
-- validCheckSum: ANY

ALTER TABLE patient_allergies RENAME column allergy to obsolete_allergy;
ALTER TABLE patient_allergies RENAME column allergy_type to obsolete_allergy_type;