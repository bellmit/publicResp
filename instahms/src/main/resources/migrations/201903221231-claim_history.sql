-- liquibase formatted sql
-- changeset shilpanr:create-claim-history-table-and-sequence

CREATE SEQUENCE claim_submission_history_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


COMMENT ON sequence claim_submission_history_seq IS '{ "type": "Txn", "comment": "Claim Submission History Sequence" }';

CREATE TABLE claim_submission_history (
claim_submission_hist_id integer DEFAULT nextval('claim_submission_history_seq'::regclass) NOT NULL,
submission_batch_id character varying(15) NOT NULL,
claim_id character varying(15) NOT NULL,
transaction_date timestamp without time zone,
insurance_co_id character varying(10)  NOT NULL,
tpa_id character varying(15) NOT NULL, 
sender_id character varying(25) NOT NUll,
receiver_id character varying(25) NOT NULL,
member_id character varying(40),
total_amount numeric(15,2),
total_claim_amount numeric(15,2),
total_patient_amount numeric(15,2),
total_claim_tax numeric(15,2),
resubmission_type character varying(50),
resubmission_remarks character varying(200),
submission_number integer,
remittance_id  integer default 0,
received_amt numeric(15,2) default 0.00,
recovery_remittance_id integer default 0,
recovery_amt numeric(15,2) default 0.00,
created_by character varying(50),
created_at timestamp without time zone,
PRIMARY KEY (claim_submission_hist_id),
FOREIGN KEY (claim_id) REFERENCES insurance_claim(claim_id),
FOREIGN KEY (insurance_co_id) REFERENCES insurance_company_master(insurance_co_id),
FOREIGN KEY (tpa_id) REFERENCES tpa_master(tpa_id)
);

COMMENT ON table claim_submission_history IS '{ "type": "Txn", "comment": "To store details of claims which are part of a submission batch" }';

CREATE SEQUENCE claim_activity_history_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

COMMENT ON sequence claim_activity_history_seq IS '{ "type": "Txn", "comment": "Claim Activity History Sequence" }';

CREATE TABLE claim_activity_history (
claim_activity_hist_id integer DEFAULT nextval('claim_activity_history_seq'::regclass) NOT NULL,
claim_submission_hist_id integer NOT NULL,
claim_id character varying(15) NOT NULL,
activity_id character varying(100) NOT NULL,
claim_activity_id character varying(100) NOT NULL,
charge_id character varying(15) NOT NULL,
sale_item_id integer NOT NULL,
charge_type character varying(20) NOT NULL,
activity_type character varying(50) NOT NULL,
activity_code character varying(600) NOT NULL,
quantity numeric,
activity_start timestamp with time zone NOT NULL,
ordering_clinician character varying(50),
clinician character varying(50),
claim_amount numeric(15,2) NOT NULL,
activity_vat numeric(15,2),
activity_vat_percent numeric(15,2),
remittance_id integer DEFAULT 0,
recovery_remittance_id integer DEFAULT 0,
received_amt numeric(15,2) DEFAULT 0.00,
recovery_amt numeric(15,2) DEFAULT 0.00,
activity_status character varying(1) DEFAULT 'O',
created_by character varying(50),
created_at timestamp without time zone,
PRIMARY KEY (claim_activity_hist_id),
FOREIGN KEY (claim_submission_hist_id) REFERENCES claim_submission_history(claim_submission_hist_id)
);

COMMENT ON table claim_activity_history IS '{ "type": "Txn", "comment": "To store details of claim activities which are part of a submission batch" }';
