-- liquibase formatted sql
-- changeset sreenivasayashwanth:user-theatres

CREATE SEQUENCE user_theatre_mapping_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE TABLE user_theatres (
    user_theatre_mapping_id INTEGER DEFAULT nextval('user_theatre_mapping_seq'::regclass) NOT NULL,
    emp_username character varying(30),
    theatre_id character varying(10),
    default_theatre boolean DEFAULT False,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY(user_theatre_mapping_id),
    UNIQUE (emp_username, theatre_id)
);

COMMENT ON table user_theatres is '{ "type": "Master", "comment": "Holds user and theatre mapped data" }';

COMMENT ON SEQUENCE user_theatre_mapping_seq is '{ "type": "Master", "comment": "Holds user and theatre mapped sequence" }';

