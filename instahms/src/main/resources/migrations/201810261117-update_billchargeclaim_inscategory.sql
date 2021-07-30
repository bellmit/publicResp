-- liquibase formatted sql
-- changeset shilpanr:update-bill-charge-claim-table-insurance-category-id-from-bill-charge-table
UPDATE bill_charge_claim bcc SET insurance_category_id = bc.insurance_category_id 
FROM bill_charge bc 
WHERE bc.charge_id = bcc.charge_id AND bcc.insurance_category_id IS NULL;