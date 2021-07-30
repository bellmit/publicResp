-- liquibase formatted sql
-- changeset rajendratalekar:rename-pd-column-ceasarean-indication-id

ALTER TABLE patient_details 
   DROP CONSTRAINT fk_patient_details_ceasarean_indication_id;

ALTER TABLE patient_details RENAME COLUMN ceasarean_indication_id TO caesarean_indication_id;
ALTER TABLE patient_details 
   ADD CONSTRAINT fk_patient_details_caesarean_indication_id
   FOREIGN KEY (caesarean_indication_id) REFERENCES indication_for_caesarean_section(id);
