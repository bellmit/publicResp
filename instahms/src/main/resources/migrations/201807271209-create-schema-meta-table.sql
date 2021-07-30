-- liquibase formatted sql
-- changeset raj-nt:create-schema-meta-table

CREATE TABLE IF NOT EXISTS schema_meta (
	organization_name varchar(500),
	address varchar(1024),
	city_id bigint,
	country char(2),
	parent_entity_id bigint,
	tzdata_identifier varchar(50),
	schema_type varchar(15),
	status varchar(50),
	ext_ids text,
	installation_date date
);

CREATE TABLE IF NOT EXISTS schema_point_of_contacts (
	center_id int,
	poc_type varchar(50),
	name varchar(500),
	mobile varchar(15),
	email varchar(400),
	address varchar(1024),
	city_id bigint
);
