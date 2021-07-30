-- liquibase formatted sql
-- changeset tejakilaru:cims_integration_guids

ALTER TABLE generic_name ADD COLUMN cims_guid character varying;
ALTER TABLE store_item_details ADD COLUMN cims_guid character varying;
