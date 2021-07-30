-- liquibase formatted sql
-- changeset raeshmika:<create-gender-master-table>

--  religion master sequence
CREATE sequence gender_master_seq
START 1
INCREMENT 1
MINVALUE 1
NO MAXVALUE
CACHE 1;

-- religion master table
CREATE TABLE gender_master(gender_id integer default nextval('gender_master_seq') primary key,
gender_name character varying(200) not null,
status character(1) default 'A',
created_by  character varying(100),
created_time timestamp without time zone DEFAULT now(),
mod_user character varying(100),
mod_time timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON table gender_master is '{ "type": "Master", "comment": "Master for gender master" }';
COMMENT ON sequence gender_master_seq is '{ "type": "Master", "comment": "gender master sequence" }';
