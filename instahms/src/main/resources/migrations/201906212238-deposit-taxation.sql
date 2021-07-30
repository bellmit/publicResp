-- liquibase formatted sql
-- changeset mancini2802:deposit-taxation


alter table center_preferences add column tax_selection_mandatory character(1) not null default 'N';

alter table generic_preferences add column tax_sub_groups_supported integer not null default 0;

alter table receipts  add column total_tax_rate numeric(15,2) default 0.00 NOT NULL;

CREATE SEQUENCE receipt_tax_seq 
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE receipt_tax (
	receipt_tax_id integer DEFAULT nextval('receipt_tax_seq'::regclass) NOT NULL,
    receipt_id character varying(15) NOT NULL,
    tax_sub_group_id integer NOT NULL,
    tax_rate numeric(15,2) DEFAULT 0.00 NOT NULL,
    tax_amount numeric(15,2) DEFAULT 0.00 NOT NULL
);

COMMENT ON TABLE receipt_tax IS 
	'{ "type": "Txn", "comment": "Contains details of receipt tax at sub grp level." }';

COMMENT ON SEQUENCE receipt_tax_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID receipt tax table." }';