-- liquibase formatted sql
-- changeset harishm18:adding-estiamate-charge-tax

-- === added new column for estimate_charge geration changes ==== 
ALTER TABLE estimate_charge ADD COLUMN tax_amt numeric(15,2) DEFAULT 0;
ALTER TABLE estimate_charge ADD COLUMN sponsor_tax numeric(15,2) DEFAULT 0;
ALTER TABLE estimate_charge ADD COLUMN patient_tax numeric(15,2) DEFAULT 0;
ALTER TABLE estimate_bill ADD COLUMN nationality_id character varying(50);
