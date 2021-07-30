-- liquibase formatted sql
-- changeset rajendratalekar:create-table-indication-for-cs-delivery-master

CREATE TABLE ceasarean_indication_master (
	id SERIAL UNIQUE,
	indication character varying(400),
    modified_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    modified_at timestamp without time zone,
    created_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    status character(1)
);

SELECT comment_on_table_or_sequence_if_exists('ceasarean_indication_master',true, 'Master','Indication for CS Delivery Master');
SELECT comment_on_table_or_sequence_if_exists('ceasarean_indication_master_id_seq', false, 'Master','');
