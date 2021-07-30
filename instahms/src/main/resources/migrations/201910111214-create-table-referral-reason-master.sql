-- liquibase formatted sql
-- changeset rajendratalekar:create-table-outbound-referral-reason

CREATE TABLE outbound_referral_reason_master (
	id SERIAL UNIQUE,
	reason character varying(400),
    modified_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    modified_at timestamp without time zone,
    created_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    status character(1)
);

SELECT comment_on_table_or_sequence_if_exists('outbound_referral_reason_master',true, 'Master','Outbound referral reasons Master');
SELECT comment_on_table_or_sequence_if_exists('outbound_referral_reason_master_id_seq', false, 'Master','');
