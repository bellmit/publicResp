-- liquibase formatted sql
-- changeset manika09:create-case-rate-main-sequence-and-table

CREATE SEQUENCE case_rate_main_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE case_rate_main (
    case_rate_id integer DEFAULT nextval('case_rate_main_seq'::regclass) NOT NULL,
    insurance_company_id character varying(15) NOT NULL,
    network_type_id integer NOT NULL,
    plan_id integer NOT NULL,
    code_type character varying(50) NOT NULL,
    code character varying(100) NOT NULL,
    code_description character varying NOT NULL,
    case_rate_number integer NOT NULL,
    status character varying(1) DEFAULT 'A'::character varying NOT NULL,
    created_by character varying(50),
    modified_by character varying(50),
    modified_at timestamp without time zone,
    PRIMARY KEY (case_rate_id),
    FOREIGN KEY (insurance_company_id) REFERENCES insurance_company_master(insurance_co_id),
    FOREIGN KEY (network_type_id) REFERENCES insurance_category_master(category_id),
    FOREIGN KEY (plan_id) REFERENCES insurance_plan_main(plan_id),
    UNIQUE (plan_id, code_type, code, case_rate_number)
);

COMMENT ON table case_rate_main IS '{ "type": "Master", "comment": "Case rate main table" }';

COMMENT ON sequence case_rate_main_seq IS '{ "type": "Master", "comment": "Case rate main sequence" }';
