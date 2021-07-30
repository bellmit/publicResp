-- liquibase formatted sql
-- changeset tejakilaru:creating-code_sets

CREATE SEQUENCE code_sets_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE code_sets (
	id INTEGER PRIMARY KEY DEFAULT nextval('code_sets_seq'::regclass),
	code_system_category_id INTEGER REFERENCES code_system_categories(id),
	code_system_id INTEGER REFERENCES code_systems(id),
	entity_id INTEGER NOT NULL,
	label CHARACTER VARYING NOT NULL,
	short_code CHARACTER VARYING
);

COMMENT ON table code_sets is '{ "type": "Master", "comment": "Code Sets" }';
COMMENT ON sequence code_sets_seq is '{ "type": "Master", "comment": "" }';

COMMENT ON COLUMN code_sets.entity_id IS 'FK reference of hms masters table';
