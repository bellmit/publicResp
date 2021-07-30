-- liquibase formatted sql
-- changeset janakivg:alter-item_excluded_from_doctor-to-character-varying

ALTER TABLE patient_prescription ALTER item_excluded_from_doctor TYPE character varying(3) USING CASE item_excluded_from_doctor WHEN true THEN 'Y'  WHEN false  THEN 'N' END;


