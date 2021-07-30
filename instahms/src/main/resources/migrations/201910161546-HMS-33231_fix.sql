-- liquibase formatted sql
-- changeset manjular:add-column-for-special-package-service

ALTER TABLE service_org_details ADD COLUMN special_service_code character varying(15);
ALTER TABLE service_org_details ADD COLUMN special_service_contract_name character varying(100);

