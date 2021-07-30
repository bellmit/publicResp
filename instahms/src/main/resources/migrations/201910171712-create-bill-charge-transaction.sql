-- liquibase formatted sql
-- changeset manika.singh:create-bill-charge-transaction-table

CREATE SEQUENCE bill_charge_transaction_seq
 START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE bill_charge_transaction (
    transaction_id integer DEFAULT nextval('bill_charge_transaction_seq'::regclass) NOT NULL,
    bill_charge_id character varying(15) REFERENCES bill_charge (charge_id),
    special_service_code character varying(15),
    special_service_contract_name character varying(100)
);

COMMENT ON SEQUENCE bill_charge_transaction_seq IS
	'{ "type": "Txn", "comment": "Sequence ID of the bill charge transaction table" }';

COMMENT ON table bill_charge_transaction is '{ "type": "Txn", "comment": "Table for bill charge transaction details" }';

