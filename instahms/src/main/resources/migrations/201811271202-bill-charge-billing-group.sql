-- liquibase formatted sql
-- changeset harishm18:bill-charge-billing-group

ALTER TABLE bill_charge ADD COLUMN billing_group_id integer,
ADD COLUMN revenue_department_id character varying(100);
