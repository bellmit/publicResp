-- liquibase formatted sql
-- changeset raeshmika:<create-marital-status-master>

--  marital status master sequence
CREATE sequence marital_status_master_seq
START 1
INCREMENT 1
MINVALUE 1
NO MAXVALUE
CACHE 1;

-- marital status master table
CREATE TABLE marital_status_master(marital_status_id integer default nextval('marital_status_master_seq') primary key,
marital_status_name character varying(200) not  null UNIQUE,
status character(1) default 'A',
created_by  character varying(100),
created_time timestamp without time zone DEFAULT now(),
mod_user character varying(100),
mod_time timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON table marital_status_master is '{ "type": "Master", "comment": "Master for Marital status" }';
COMMENT ON sequence marital_status_master_seq is '{ "type": "Master", "comment": "marital status master sequence" }';
