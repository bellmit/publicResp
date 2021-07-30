CREATE TABLE tpa_center_master (
      tpa_center_id integer primary key,
      tpa_id character varying(15),
      center_id integer,
      status character(1)
);
CREATE sequence tpa_center_master_seq;

CREATE TABLE remittance_receipts (LIKE bill_receipts INCLUDING CONSTRAINTS);
ALTER TABLE remittance_receipts ALTER COLUMN mod_time SET DEFAULT now();
ALTER TABLE remittance_receipts ALTER COLUMN tds_amt SET DEFAULT 0.00;
ALTER TABLE remittance_receipts ALTER COLUMN payment_mode_id SET DEFAULT (-1);
ALTER TABLE remittance_receipts ALTER COLUMN card_type_id SET DEFAULT 0;

-- DROP TABLE IF EXISTS revenue_allocation_map CASCADE;
CREATE TABLE revenue_allocation_map (
	charge_head varchar,
	item_id varchar,
	allocation_department varchar,
	allocation_percent numeric(10, 2)
);

CREATE TABLE revenue_allocation_block (
	indicator_charge_head varchar,
	charge_head varchar,
	item_id varchar,
	allocation_department varchar,
	allocation_percent numeric(10, 2),
	priority integer
);

CREATE INDEX ram_charge_head_idx ON revenue_allocation_map (charge_head);
CREATE INDEX ram_item_idx ON revenue_allocation_map (item_id);

-- adjustments start ---
-- table to track finalized bills
CREATE TABLE bills_finalized (
	bill_no varchar not null,
	finalization_date timestamp without time zone not null default now()
);

-- table to track adjustments to bill charges and bills
CREATE TABLE bill_charge_adjustment (LIKE bill_charge EXCLUDING CONSTRAINTS);
CREATE TABLE bill_adjustment (LIKE bill EXCLUDING CONSTRAINTS);

ALTER TABLE bill_charge_adjustment ADD COLUMN ref_bill_no character varying;
ALTER TABLE bill_charge_adjustment ADD COLUMN ref_charge_id character varying;

ALTER TABLE bill_adjustment ADD COLUMN ref_bill_no character varying;

-- preference to indicate auto posting receipts
-- This should go into db_changes_100_101, once tested on customer schema

ALTER TABLE generic_preferences ADD COLUMN auto_post_sponsor_receipts character(1) NOT NULL DEFAULT 'N';


CREATE SEQUENCE bill_adjustment_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE TABLE doctor_allocation_map (
	charge_head varchar,
	item_id varchar,
	allocation_department varchar,
	allocation_percent numeric(10, 2)
);

alter table doctor_allocation_map add column service_group text;