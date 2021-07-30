-- liquibase formatted sql
-- changeset raeshmika:<mapping-for-dcouments-type-category>

CREATE TABLE doc_category_master(doc_category_id character varying(100) PRIMARY KEY NOT NULL,
doc_category_name character varying(200) NOT NULL);

CREATE SEQUENCE doc_type_category_mapping_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE doc_type_category_mapping(
doc_type_category_mapping_id integer DEFAULT nextval('doc_type_category_mapping_seq'::regclass) PRIMARY KEY NOT NULL,
doc_type_id character varying(20),
doc_type_category_id character varying(100),
created_at TIMESTAMP DEFAULT NOW(),
modified_at TIMESTAMP,
FOREIGN KEY (doc_type_id) references doc_type(doc_type_id),
FOREIGN KEY (doc_type_category_id) references doc_category_master(doc_category_id),
UNIQUE (doc_type_id, doc_type_category_id)
);

COMMENT ON table doc_category_master is '{ "type": "Master", "comment": "contains document categories" }';

COMMENT ON SEQUENCE doc_type_category_mapping_seq is '{ "type": "Txn", "comment": "contains document types and categories mapped sequence" }';

COMMENT ON table doc_type_category_mapping is '{ "type": "Txn", "comment": "contains document type and category mapping" }';