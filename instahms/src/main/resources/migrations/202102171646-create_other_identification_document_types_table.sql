-- liquibase formatted sql
-- changeset eshwar-chandra:<create-other_identification_document_types-table>

--
-- Name: other_identification_doc_types_seq; Type: SEQUENCE; Schema: fresh; Owner: -
--
CREATE SEQUENCE other_identification_document_types_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

COMMENT ON SEQUENCE other_identification_document_types_seq IS '{ "type": "Txn", "comment": "other identification document types seq" }';

--
-- Name: other_identification_document_types; Type: TABLE; Schema: fresh; Owner: -
--
CREATE TABLE other_identification_document_types (
    other_identification_doc_id integer DEFAULT nextval('other_identification_document_types_seq'::regclass) NOT NULL,
    other_identification_doc_name character varying(100) NOT NULL,
    status character varying(1),
    CONSTRAINT other_identification_doc_typ_pkey PRIMARY KEY (other_identification_doc_id),
	CONSTRAINT other_identification_doc_types_unq UNIQUE (other_identification_doc_name)
);

COMMENT ON table other_identification_document_types IS '{ "type": "Master", "comment": "Other identification document types" }';

ALTER TABLE patient_details ADD other_identification_doc_id integer;

ALTER TABLE patient_details ADD other_identification_doc_value character varying(100);
