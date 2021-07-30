-- liquibase formatted sql
-- changeset tejasiitb:changes-billing-group-masters

INSERT INTO item_group_type VALUES('BILLGRP','Billing Group','S','','A');

ALTER TABLE diagnostics
ADD COLUMN billing_group_id integer;

ALTER TABLE services
ADD COLUMN billing_group_id integer;

ALTER TABLE operation_master
ADD COLUMN billing_group_id integer;

ALTER TABLE equipment_master
ADD COLUMN billing_group_id integer;

ALTER TABLE store_item_details
ADD COLUMN billing_group_id integer;

ALTER TABLE bed_types
ADD COLUMN billing_group_id integer;

ALTER TABLE consultation_types
ADD COLUMN billing_group_id integer;

ALTER TABLE anesthesia_type_master
ADD COLUMN billing_group_id integer;

ALTER TABLE pack_master
ADD COLUMN billing_group_id integer;

ALTER TABLE common_charges_master
ADD COLUMN billing_group_id integer;

ALTER TABLE diet_master
ADD COLUMN billing_group_id integer;

ALTER TABLE theatre_master
ADD COLUMN billing_group_id integer;
