-- liquibase formatted sql
-- changeset rajendratalekar:create-bill-charge-status-idx

DROP INDEX IF EXISTS bill_charge_status_idx; 
CREATE INDEX bill_charge_status_idx ON bill_charge(status);
