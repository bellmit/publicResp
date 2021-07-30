-- liquibase formatted sql
-- changeset adityabhatia02:create-accounting-tables-sequences.sql splitStatements:false
-- validCheckSum: ANY
-- sequence for the voucher guid
--

-------- FUNCTIONS ---------------------
--
-- Generates an ID from a pattern
-- Input: name of the pattern as in hosp_id_patterns
--
DROP FUNCTION IF EXISTS generate_id(patternId text) CASCADE;
CREATE FUNCTION generate_id(patternId text) RETURNS text AS $BODY$
DECLARE
    rec RECORD;
BEGIN
    SELECT std_prefix || date_prefix ||
        trim(to_char(nextval(sequence_name), num_pattern)) as id
    INTO rec
    FROM hosp_id_patterns
    WHERE pattern_id = patternId;

    return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';

CREATE SEQUENCE accounting_voucher_seq
	START 1
	INCREMENT 1
	MINVALUE 1
	NO MAXVALUE
	CACHE 1;

-- pattern for the voucher guid
INSERT INTO hosp_id_patterns(pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name,
           sequence_reset_freq, date_prefix, type)
    SELECT 'ACCOUNTING_VOUCHER', 'ACC', '', '0000000000000', 'accounting_voucher_seq',
            '', '', 'Txn' WHERE NOT EXISTS (SELECT 1 FROM hosp_id_patterns WHERE pattern_id = 'ACCOUNTING_VOUCHER');

-- accounting table
CREATE TABLE hms_accounting_info (
    center_id integer,
    center_name character(100),
    visit_type character(1),
    mr_no character(15),
    visit_id character(15),
    charge_group character(50),
    charge_head character(50),
    account_group integer,
    service_group character(100),
    service_sub_group character(100),
    bill_no character(15),
    audit_control_number character(15),
    voucher_no character(15),
    voucher_type character(15),
    voucher_date timestamp without time zone,
    item_code character varying,
    item_name character varying,
    receipt_store character(200),
    issue_store character(200),
    currency character(300),
    currency_conversion_rate numeric(15,2),
    quantity numeric(10,2),
    unit character(100),
    unit_rate numeric(15,2),
    gross_amount numeric(15,2),
    round_off_amount numeric(15,2),
    discount_amount numeric(15,2),
    points_redeemed integer,
    points_redeemed_rate numeric(15,2),
    points_redeemed_amount numeric(15,2),
    item_category_id integer,
    purchase_vat_amount numeric(15,2),
    purchase_vat_percent numeric(10,2),
    sales_vat_amount numeric(15,2),
    sales_vat_percent numeric(10,2),
    debit_account character(100),
    credit_account character(100),
    tax_amount numeric(15,2),
    net_amount numeric(15,2),
    admitting_doctor character(100),
    prescribing_doctor character(100),
    conductiong_doctor character(100),
    referral_doctor character(100),
    payee_doctor character(100),
    outhouse_name character(100),
    incoimng_hospital character(100),
    admitting_department character(100),
    conducting_department character(100),
    cost_amount numeric(15,2),
    supplier_name character(100),
    invoice_no character(100),
    invoice_date timestamp without time zone,
    voucher_ref character(100),
    remarks character(200),
    mod_time timestamp without time zone,
    counter_no character varying(100),
    bill_open_date timestamp without time zone,
    bill_finalized_date timestamp without time zone,
    is_tpa character(1),
    insurance_co character varying,
    old_mr_no character varying,
    issue_store_center character varying,
    receipt_store_center character varying,
    po_number character varying,
    po_date date,
    transaction_type character(1),
    custom_1 character varying,
    custom_2 character varying,
    custom_3 character varying,
    custom_4 character varying,
    cust_supplier_code character varying,
    grn_date timestamp without time zone,
    cust_item_code character varying,
    custom_7 character varying,
    custom_8 character varying,
    custom_9 character varying,
    custom_10 character varying,
    custom_11 character varying,
    guid character varying DEFAULT generate_id('ACCOUNTING_VOUCHER'::text) NOT NULL,
    update_status integer DEFAULT 0
);
