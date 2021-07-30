-- liquibase formatted sql
-- changeset pallavia08:adding-available-for-online-consults-column-in-doctors-visit-mode-column-in-sch-default-res-availability-details-sch-resource-availability-details

ALTER TABLE doctors ADD column available_for_online_consults character varying(1);
UPDATE doctors SET available_for_online_consults = 'N';
ALTER TABLE sch_default_res_availability_details ADD column visit_mode character varying(1);
UPDATE sch_default_res_availability_details SET visit_mode = 'I';
ALTER TABLE sch_resource_availability_details ADD column visit_mode character varying(1);
UPDATE sch_resource_availability_details SET visit_mode = 'I';
