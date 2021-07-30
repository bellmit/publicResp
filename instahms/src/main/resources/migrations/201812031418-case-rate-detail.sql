-- liquibase formatted sql
-- changeset manika09:create-case-rate-detail-sequence-and-table

CREATE SEQUENCE case_rate_detail_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE case_rate_detail (
    case_rate_detail_id integer DEFAULT nextval('case_rate_detail_seq'::regclass) NOT NULL,
    case_rate_id integer NOT NULL,
    insurance_category_id integer NOT NULL,
    amount numeric(15,2) DEFAULT 0 NOT NULL,
    created_by character varying(50),
    modified_by character varying(50),
    modified_at timestamp without time zone,
    PRIMARY KEY (case_rate_detail_id),
    UNIQUE (case_rate_id, insurance_category_id),
    FOREIGN KEY (case_rate_id) REFERENCES case_rate_main(case_rate_id), 
    FOREIGN KEY (insurance_category_id) REFERENCES item_insurance_categories(insurance_category_id) 
);

COMMENT ON table case_rate_detail IS '{ "type": "Master", "comment": "Case rate detail table - Informationa about categories" }';

COMMENT ON sequence case_rate_detail_seq IS '{ "type": "Master", "comment": "Case rate detail sequence" }';

