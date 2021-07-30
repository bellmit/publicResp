-- liquibase formatted sql
-- changeset junaidahmed:add-column-file-id

ALTER TABLE insurance_remittance ADD COLUMN file_id varchar;
UPDATE insurance_remittance SET processing_status = 'N' WHERE processing_status IS NULL;
ALTER TABLE ONLY insurance_remittance ALTER COLUMN processing_status SET DEFAULT 'N'; 