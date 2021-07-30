-- liquibase formatted sql
-- changeset shilpanr:Adding-discontinue-column-in-patient-packages-table

ALTER TABLE patient_packages ADD COLUMN is_discontinued boolean;
ALTER TABLE patient_packages ADD COLUMN discontinue_remark character varying(4000);

UPDATE patient_packages pp SET is_discontinued = pcpd.is_discontinued 
FROM patient_customised_package_details pcpd WHERE pp.pat_package_id = pcpd.patient_package_id;

UPDATE patient_packages pp SET discontinue_remark = pcpd.discontinue_remark 
FROM patient_customised_package_details pcpd WHERE pp.pat_package_id = pcpd.patient_package_id;

ALTER TABLE patient_customised_package_details DROP COLUMN is_discontinued;
ALTER TABLE patient_customised_package_details DROP COLUMN discontinue_remark;
