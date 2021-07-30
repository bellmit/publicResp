-- liquibase formatted sql
-- changeset sanjana.goyal:race-master

CREATE SEQUENCE race_master_seq start with 1;

CREATE TABLE race_master (race_id integer DEFAULT nextval('race_master_seq'::regclass) PRIMARY KEY, race_name character varying(100) NOT NULL UNIQUE, status character(1) default 'A' );

COMMENT ON table race_master is '{ "type": "Master", "comment": "Master for race" }';
COMMENT ON sequence race_master_seq is '{ "type": "Master", "comment": "race sequence" }';
