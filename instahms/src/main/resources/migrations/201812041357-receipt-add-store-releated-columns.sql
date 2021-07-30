-- liquibase formatted sql
-- changeset allabakash:Receipt-add-store-releated-columns

ALTER TABLE receipts ADD COLUMN incoming_visit_id VARCHAR(20) DEFAULT NULL;
ALTER TABLE receipts ADD COLUMN store_retail_customer_id VARCHAR(20) DEFAULT NULL;

UPDATE receipts r SET incoming_visit_id = ins.incoming_visit_id FROM bill_receipts br 
	JOIN bill b ON br.bill_no = b.bill_no 
	JOIN incoming_sample_registration ins ON ins.incoming_visit_id = b.visit_id
	WHERE r.receipt_id = br.receipt_no;
	
	
UPDATE receipts r SET store_retail_customer_id = src.customer_id FROM bill_receipts br 
	JOIN bill b ON br.bill_no = b.bill_no 
	JOIN store_retail_customers src ON src.customer_id = b.visit_id
	WHERE r.receipt_id = br.receipt_no;