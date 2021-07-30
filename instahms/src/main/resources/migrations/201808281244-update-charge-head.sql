-- liquibase formatted sql
-- changeset harishm18:updating-charge-head-master-and-bill
-- validCheckSum: ANY

-- Update bill_charge/common_charges_master charge_head = OCOTC

UPDATE bill_charge SET charge_head = 'OCOTC' WHERE charge_group = 'OTC' AND (charge_head = '' OR charge_head IS NULL);
UPDATE common_charges_master SET charge_type = 'OCOTC' WHERE charge_group = 'OTC' AND (charge_type = '' OR charge_type IS NULL);
