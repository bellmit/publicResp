-- liquibase formatted sql
-- changeset MohamedRiyaj:alter-columns-of-contact-details-table

ALTER TABLE contact_details ALTER COLUMN patient_name TYPE character varying(50);
ALTER TABLE contact_details ALTER COLUMN patient_gender TYPE character varying(1);
ALTER TABLE contact_details ALTER COLUMN middle_name TYPE character varying(200);