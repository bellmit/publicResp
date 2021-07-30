-- liquibase formatted sql
-- changeset pallavia08:salucro-role-and-location-mapping-new-tables
CREATE SEQUENCE salucro_role_mapping_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE salucro_role_mapping (
    salucro_role_mapping_id BIGINT DEFAULT nextval('salucro_role_mapping_seq'::regclass) NOT NULL,
    emp_username character varying(30),
    role character varying(100),
    Status character varying(1) DEFAULT 'I'::character varying,
    center_id integer,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    PRIMARY KEY (salucro_role_mapping_id),
    FOREIGN KEY (emp_username) REFERENCES u_user(emp_username),
    FOREIGN KEY (center_id) REFERENCES hospital_center_master(center_id)
);

CREATE SEQUENCE salucro_location_mapping_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE salucro_location_mapping (
    salucro_location_mapping_id BIGINT DEFAULT nextval('salucro_location_mapping_seq'::regclass) NOT NULL,
    id integer,
    center_id integer,
    counter_id character varying(20),
    Status character varying(1) DEFAULT 'I'::character varying,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    user_id character varying(100),
    PRIMARY KEY (salucro_location_mapping_id),
    FOREIGN KEY (counter_id) REFERENCES counters(counter_id),
    FOREIGN KEY (center_id) REFERENCES hospital_center_master(center_id)

);

COMMENT ON table salucro_role_mapping IS '{ "type": "Master", "comment": "Salucro Role to user mapping table - Informationa about user mapping" }';

COMMENT ON sequence salucro_role_mapping_seq IS '{ "type": "Master", "comment": "Salucro Role Mapping sequence" }';

COMMENT ON table salucro_location_mapping IS '{ "type": "Master", "comment": "Salucro Location to counter mapping table - Informationa about counter mapping" }';

COMMENT ON sequence salucro_location_mapping_seq IS '{ "type": "Master", "comment": "Salucro Location Mapping sequence" }';
