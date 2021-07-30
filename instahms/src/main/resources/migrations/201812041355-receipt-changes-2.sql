-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes

CREATE TABLE receipts (
    receipt_id VARCHAR(15) PRIMARY KEY,
    receipt_type VARCHAR(1) NOT NULL,
    tpa_id VARCHAR(15),
    mr_no VARCHAR(15),
    center_id INTEGER,
    amount NUMERIC(16,2) NOT NULL,
    display_date TIMESTAMP DEFAULT now(),
    counter VARCHAR(20),
    bank_name VARCHAR(50),
    reference_no VARCHAR(100),
    created_by VARCHAR(30) NOT NULL, 
    created_at TIMESTAMP default now(),
    modified_by VARCHAR(30),
    modified_at TIMESTAMP default now(),
    remarks VARCHAR(100),
    tds_amount NUMERIC(16,2) default 0.00 NOT NULL,
    paid_by VARCHAR(30),
    payment_mode_id INTEGER DEFAULT -1,
    card_type_id INTEGER DEFAULT 0,
    bank_batch_no VARCHAR(100),
    card_auth_code VARCHAR(100),
    card_holder_name VARCHAR(100),
    currency_id INTEGER,
    exchange_rate NUMERIC(16,2),
    exchange_date TIMESTAMP,
    currency_amt NUMERIC(16,2),
    card_exp_date DATE,
    card_number VARCHAR(150),
    credit_card_commission_percentage NUMERIC(16,2),
    credit_card_commission_amount NUMERIC(16,2),
    totp VARCHAR(10),
    mob_number VARCHAR(20),
    edc_imei VARCHAR(100),
    other_deductions NUMERIC(16,2) DEFAULT 0.00,
    payment_received_date DATE,
    remittance_id INTEGER,
    is_settlement BOOLEAN NOT NULL,
    unallocated_amount NUMERIC(16,2) DEFAULT 0.00,
    realized VARCHAR NOT NULL DEFAULT 'Y',
    is_deposit BOOLEAN NOT NULL DEFAULT 'false',
    payer_name CHARACTER VARYING(100), 
    payer_mobile_number CHARACTER VARYING(16),
    payer_address CHARACTER VARYING(300),
    points_redeemed INTEGER DEFAULT 0.00
);
COMMENT ON COLUMN receipts.receipt_type IS 'Valid values are: R/F (Receipt/Refund)';
COMMENT ON TABLE receipts IS '{ "type": "Txn", "comment": "Holds all external receipts" }';

CREATE TABLE receipts_audit_log (
    log_id BIGINT DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name CHARACTER VARYING,
    mod_time TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    operation CHARACTER VARYING,
    field_name CHARACTER VARYING,
    old_value CHARACTER VARYING,
    new_value CHARACTER VARYING,
    receipt_id CHARACTER VARYING(15)
);
COMMENT ON TABLE receipts_audit_log IS '{ "type": "Txn", "comment": "Receipts related audit log table" }';

CREATE SEQUENCE bill_receipts_id_sequence START 1;
COMMENT ON SEQUENCE bill_receipts_id_sequence IS '{ "type": "Txn", "comment": "Holds sequence for bill receipt" }';

ALTER TABLE bill_receipts DROP CONSTRAINT bill_receipts_pkey;

ALTER TABLE bill_receipts ADD COLUMN allocated_amount NUMERIC(16,2) NOT NULL DEFAULT 0.00;
ALTER TABLE bill_receipts ADD COLUMN bill_receipt_id BIGINT PRIMARY KEY DEFAULT nextval('bill_receipts_id_sequence');

CREATE SEQUENCE allocation_id_sequence START 1;
COMMENT ON SEQUENCE allocation_id_sequence IS '{ "type": "Txn", "comment": "Holds sequence for bill_charge_receipt_allocation" }';

CREATE TABLE bill_charge_receipt_allocation (
  allocation_id BIGINT PRIMARY KEY DEFAULT nextval('allocation_id_sequence'),
  charge_id VARCHAR(15) REFERENCES bill_charge(charge_id) NOT NULL,
  bill_receipt_id BIGINT REFERENCES bill_receipts(bill_receipt_id) NOT NULL,
  claim_id VARCHAR(15) DEFAULT NULL,
  allocated_amount NUMERIC(16,2) NOT NULL,
  modified_by VARCHAR(30),
  modified_at TIMESTAMP default now()
);
COMMENT ON TABLE bill_charge_receipt_allocation IS '{"type": "Txn", "comment": "Holds the internal bill charge allocations and the receipts which paid for it."}';

-- Create Indexes for the bill_charge_receipt_allocation table.

-- Create receipt_refund_reference table and its sequence.
CREATE SEQUENCE receipt_refund_reference_sequence START 1;
COMMENT ON SEQUENCE receipt_refund_reference_sequence IS '{ "type": "Txn", "comment": "Holds sequence for receipt_refund_reference" }';

CREATE TABLE receipt_refund_reference (
  id BIGINT PRIMARY KEY DEFAULT nextval('receipt_refund_reference_sequence'),
  refund_receipt_id VARCHAR(15) NOT NULL,
  receipt_id VARCHAR(15) NOT NULL,
  amount NUMERIC(15,2) NOT NULL
);
COMMENT ON TABLE receipt_refund_reference IS '{"type": "Txn", "comment": "Shows from which receipts the refund was given"}';