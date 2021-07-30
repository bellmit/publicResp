-- liquibase formatted sql
-- changeset shilpanr:create-visit-case-rate-detail-sequence-and-table

CREATE SEQUENCE visit_case_rate_detail_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE visit_case_rate_detail (
    visit_case_rate_detail_id integer DEFAULT nextval('visit_case_rate_detail_seq'::regclass) NOT NULL,
    visit_id character varying(15) NOT NULL,
    case_rate_detail_id integer NOT NULL,
    case_rate_id integer NOT NULL,
    insurance_category_id integer NOT NULL,
    amount numeric(15,2) DEFAULT 0 NOT NULL,
    PRIMARY KEY (visit_case_rate_detail_id),
    UNIQUE (visit_id, case_rate_id, insurance_category_id) 
);

COMMENT ON table visit_case_rate_detail IS '{ "type": "Txn", "comment": "Visit Case rate detail table - Informationa about case rate limit" }';

COMMENT ON sequence visit_case_rate_detail_seq IS '{ "type": "Txn", "comment": "Visit case rate detail sequence" }';