-- liquibase formatted sql
-- changeset vishwas07:contact-moduser-modtime-changes

ALTER TABLE contact_details ADD COLUMN create_time timestamp without time zone DEFAULT now() NOT NULL;

ALTER TABLE contact_details ADD COLUMN mod_time timestamp without time zone;

ALTER TABLE contact_details ADD COLUMN mod_user character varying(100);

