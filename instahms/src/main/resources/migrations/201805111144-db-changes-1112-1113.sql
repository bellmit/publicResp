-- liquibase formatted sql

-- changeset irshadmohamad:{sales-migration-db-changes}

-- Update bill_charge amount = amount - tax 
UPDATE bill_charge bc SET amount = 
	COALESCE((SELECT (SUM(ssd.amount-ssd.tax) - ssm.discount + ssm.round_off) FROM store_sales_details ssd WHERE ssd.sale_id = ssm.sale_id), 0)
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no) 
where ssm.charge_id = bc.charge_id AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));
