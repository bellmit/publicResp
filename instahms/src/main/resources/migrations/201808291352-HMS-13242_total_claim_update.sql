-- liquibase formatted sql
-- changeset SirishaRL:update_total_claim_from_bill_chare_claim

UPDATE bill b SET total_claim = (SELECT COALESCE(SUM(insurance_claim_amount),0) 
FROM bill_charge bc WHERE bc.bill_no = b.bill_no and bc.status!='X') 
where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

--CREATE INDEX idx_bill_charge_tax_charge_id ON bill_charge_tax USING btree(charge_id);
CREATE INDEX bct_tax_sub_group_id_idx on bill_charge_tax(tax_sub_group_id);
CREATE INDEX sctd_sale_iem_id_idx on sales_claim_tax_details (sale_item_id);
CREATE INDEX sctd_claim_id_idx on sales_claim_tax_details (claim_id);
