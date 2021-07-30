-- liquibase formatted sql
-- changeset riyapoddar-13:adding-vaccine-category-master-and-vaccine-master-category-mapping

--vaccination category master--

CREATE SEQUENCE vaccine_category_master_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE vaccine_category_master(vaccine_category_id integer DEFAULT nextval('vaccine_category_master_seq') PRIMARY KEY NOT NULL,
 vaccine_category_name character varying(200) NOT NULL,
 age_applicability character varying(1) DEFAULT 'N'::bpchar,
 gender_applicability character varying(1) DEFAULT 'N'::bpchar,
 from_age integer,
 from_age_unit character varying(1),
 to_age integer,
 to_age_unit character varying(1),
 gender character varying(1));

--vaccine_master and vaccine_category_master association--

CREATE SEQUENCE vaccine_master_category_mapping_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE vaccine_master_category_mapping(
vaccine_master_category_mapping_id integer DEFAULT nextval('vaccine_master_category_mapping_seq'::regclass) PRIMARY KEY NOT NULL,
vaccine_id integer,
vaccine_category_id integer,
FOREIGN KEY (vaccine_id) references vaccine_master(vaccine_id),
FOREIGN KEY (vaccine_category_id) references vaccine_category_master(vaccine_category_id),
UNIQUE (vaccine_id, vaccine_category_id)
);

COMMENT ON SEQUENCE vaccine_category_master_seq is '{ "type": "Master", "comment": "contains vaccine categories sequence" }';

COMMENT ON table vaccine_category_master is '{ "type": "Master", "comment": "contains vaccine categories" }';

COMMENT ON SEQUENCE vaccine_master_category_mapping_seq is '{ "type": "Master", "comment": "contains vaccine_master and vaccine_category_master mapped sequence" }';

COMMENT ON table vaccine_master_category_mapping is '{ "type": "Master", "comment": "contains vaccine_master and vaccine_category_master mapping" }';

--pre defined vaccination categories--

INSERT INTO vaccine_category_master (vaccine_category_name) VALUES ('COVID-19 Vaccination');

INSERT INTO vaccine_category_master (vaccine_category_name) VALUES ('COVID-19 Vaccine Clinical Trial-Chinese');

INSERT INTO vaccine_category_master (vaccine_category_name) VALUES ('COVID-19 Vaccine Clinical Trial-Russian');

INSERT INTO vaccine_category_master (vaccine_category_name) VALUES ('Others');