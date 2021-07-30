-- liquibase formatted sql
-- changeset satishl2772:adding-sponsor-id-column-for-tax-tables-regarding-accounting


ALTER TABLE bill_charge_claim_tax ADD COLUMN sponsor_id character varying(20);

ALTER TABLE bill_charge_details_adjustment ADD COLUMN sponsor_id character varying(20);

ALTER TABLE bill_charge_details_adjustment ADD COLUMN old_sponsor_id character varying(20);
