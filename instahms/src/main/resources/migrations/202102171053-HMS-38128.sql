-- liquibase formatted sql
-- changeset manjular:adding-column-preauth_presc_req_id failOnError:false

create sequence preauth_prescription_request_seq;

ALTER TABLE preauth_prescription_request ADD COLUMN preauth_presc_req_id integer DEFAULT nextval('preauth_prescription_request_seq'::regclass) NOT NULL  PRIMARY KEY ;

COMMENT ON SEQUENCE preauth_prescription_request_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID of the preauth prescription request" }';



