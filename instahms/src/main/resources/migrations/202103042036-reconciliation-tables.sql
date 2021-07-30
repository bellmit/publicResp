-- liquibase formatted sql
-- changeset krishnasameerachanta:reconciliation tables

CREATE SEQUENCE reconciliation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 5;
COMMENT ON SEQUENCE reconciliation_seq IS '{ "type": "Txn", "comment": "Holds sequence for reconciliation" }';


CREATE TABLE reconciliation  (
	reconciliation_id integer DEFAULT nextval('reconciliation_seq') PRIMARY KEY,
	receipt_id VARCHAR(15) REFERENCES receipts(receipt_id) NOT NULL,
	allocated_amount NUMERIC(16,2) NOT NULL DEFAULT 0, 
	created_by  VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
	created_at TIMESTAMP default now(),
	modified_by  VARCHAR(30) REFERENCES u_user(emp_username),
	modified_at TIMESTAMP default now()
);
COMMENT ON TABLE reconciliation IS '{"type": "Txn", "comment": "Stores reconciliation receipt mapping"}';

CREATE TABLE reconciliation_activity_details (
	reconciliation_id INTEGER REFERENCES reconciliation(reconciliation_id) NOT NULL,
	activity_id VARCHAR(100) NOT NULL,
	claim_id VARCHAR(50) NOT NULL,
	denial_remarks VARCHAR(50),
	allocated_amount NUMERIC(16,2),
	created_at  timestamp without time zone NOT NULL DEFAULT NOW(),
	created_by VARCHAR(30) REFERENCES u_user(emp_username),
	status VARCHAR(2)
);

COMMENT ON TABLE reconciliation_activity_details IS '{"type": "Txn", "comment": "Stores reconciliation item level allocations and details"}';


ALTER TABLE receipts DROP COLUMN remittance_id;