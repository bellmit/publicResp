-- liquibase formatted sql
-- changeset shilpanr:update-sales-claim-details-table-insurance-category-id-from-sales-details
UPDATE sales_claim_details scd SET insurance_category_id = ssd.insurance_category_id 
FROM store_sales_details ssd 
WHERE ssd.sale_item_id =scd.sale_item_id AND ssd.insurance_category_id IS NULL;
