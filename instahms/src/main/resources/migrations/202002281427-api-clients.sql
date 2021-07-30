-- liquibase formatted sql
-- changeset sanjana.goyal:api-clients-table


CREATE SEQUENCE client_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE api_clients (
	client_id INTEGER PRIMARY KEY DEFAULT nextval('client_id_seq'::regclass),
	client_uuid CHARACTER VARYING unique,
	hash CHARACTER VARYING,
	callback_url CHARACTER VARYING,
	label CHARACTER VARYING
);

COMMENT ON table api_clients is '{ "type": "Master", "comment": "API Clients" }';
COMMENT ON sequence client_id_seq is '{ "type": "Master", "comment": "" }';