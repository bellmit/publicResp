-- liquibase formatted sql
-- changeset raeshmika:<visit-types-for-encounter-types-master>

--  encounter types visits master sequence
CREATE sequence encounter_types_visits_seq
START 1
INCREMENT 1
MINVALUE 1
NO MAXVALUE
CACHE 1;

-- religion master table
CREATE TABLE encounter_types_visits(encounter_types_visit_id integer default nextval('encounter_types_visits_seq') primary key,
encounter_types_visit_name character varying(200) not null,
status character(1) default 'A',
created_by  character varying(100),
created_time timestamp without time zone DEFAULT now(),
mod_user character varying(100),
mod_time timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON table encounter_types_visits is '{ "type": "Master", "comment": "Master for encounter types visit" }';
COMMENT ON sequence encounter_types_visits_seq is '{ "type": "Master", "comment": "encounter types visits master sequence" }';

Insert into encounter_types_visits (encounter_types_visit_name, status, created_by, mod_user) values('In-person', 'A', 'InstaAdmin', 'InstaAdmin');
Insert into encounter_types_visits (encounter_types_visit_name, status, created_by, mod_user) values('Homecare', 'A', 'InstaAdmin', 'InstaAdmin');
Insert into encounter_types_visits (encounter_types_visit_name, status, created_by, mod_user) values('Telemedicine', 'A', 'InstaAdmin', 'InstaAdmin');

ALTER TABLE encounter_type_codes ADD COLUMN encounter_visit_type Integer REFERENCES encounter_types_visits(encounter_types_visit_id);
