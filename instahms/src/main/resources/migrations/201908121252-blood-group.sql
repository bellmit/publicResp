-- liquibase formatted sql
-- changeset sanjana.goyal:blood-group-master

CREATE SEQUENCE blood_group_master_seq start with 1;

CREATE TABLE blood_group_master (blood_group_id integer DEFAULT nextval('blood_group_master_seq'::regclass) PRIMARY KEY, blood_group_name character varying(50) NOT NULL UNIQUE, status character(1) default 'A' );

COMMENT ON table blood_group_master is '{ "type": "Master", "comment": "Master for blood group" }';
COMMENT ON sequence blood_group_master_seq is '{ "type": "Master", "comment": "blood group sequence" }';
