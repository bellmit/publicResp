-- liquibase formatted sql

-- changeset SirishaRL:update_total_claim_of_cash_bill_to_0
UPDATE bill b SET total_claim = 0 WHERE b.is_tpa = false 
	and b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));
		
UPDATE bill b SET total_amount = (SELECT COALESCE(SUM(amount),0) FROM bill_charge bc 
WHERE bc.bill_no = b.bill_no AND bc.status != 'X' ) where b.status IN ('A', 'F', 'C') 
AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));
