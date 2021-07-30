-- liquibase formatted sql
-- changeset rajendratalekar:alter-patient-details-add-indication-for-cs-field

ALTER TABLE patient_details add column ceasarean_indication_id integer;
ALTER TABLE patient_details 
   ADD CONSTRAINT fk_patient_details_ceasarean_indication_id
   FOREIGN KEY (ceasarean_indication_id) REFERENCES ceasarean_indication_master(id);
