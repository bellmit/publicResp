-- liquibase formatted sql
-- changeset shilpanr:update-sales-claim-details-table-claim-id

UPDATE sales_claim_details scd set claim_id = bcl.claim_id
	FROM store_sales_details ssd
	JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id)
	JOIN bill_claim bcl ON(ssm.bill_no = bcl.bill_no AND bcl.priority = 1)
	WHERE ssd.sale_item_id = scd.sale_item_id AND 
	scd.claim_id != bcl.claim_id and NOT EXISTS(SELECT bc.bill_no FROM bill_claim bc 
	WHERE priority = 2 AND bc.bill_no = bcl.bill_no);
	