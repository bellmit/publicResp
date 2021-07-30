-- liquibase formatted sql
-- changeset tejasiitb:update-bill-charge-amount-0-deleted

UPDATE bill_charge SET amount=0.00, discount=0.00, insurance_claim_amount=0.00 WHERE status ='X' AND charge_group ='BED';