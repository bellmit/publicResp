-- liquibase formatted sql
-- changeset raeshmika:<religion-master-table>

--  religion master sequence
CREATE sequence religion_master_seq
START 1
INCREMENT 1
MINVALUE 1
NO MAXVALUE
CACHE 1;

-- religion master table
CREATE TABLE religion_master(religion_id integer default nextval('religion_master_seq') primary key,
religion_name character varying(200) not null UNIQUE,
status character(1) default 'A',
created_by  character varying(100),
created_time timestamp without time zone DEFAULT now(),
mod_user character varying(100),
mod_time timestamp without time zone DEFAULT now() NOT NULL
);
COMMENT ON table religion_master is '{ "type": "Master", "comment": "Master for Religion master" }';
COMMENT ON sequence religion_master_seq is '{ "type": "Master", "comment": "religion master sequence" }';
