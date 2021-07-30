-- liquibase formatted sql

-- changeset irshadmohammad:sales_return_tax_migration.sql splitStatements:false

-- === migration script to update return original tax amount with return tax amount ==== 
UPDATE store_sales_details ssd SET return_original_tax_amt = ssd.return_tax_amt FROM store_sales_main ssm 
JOIN bill b ON (ssm.bill_no = b.bill_no) 
WHERE ssd.sale_id = ssm.sale_id AND b.status IN ('A', 'F', 'C') AND ssd.return_original_tax_amt = 0 AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND
EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));