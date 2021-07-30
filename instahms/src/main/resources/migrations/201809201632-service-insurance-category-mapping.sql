-- liquibase formatted sql
-- changeset tejasiitb:create-table-service-insurance-category-mapping
CREATE TABLE service_insurance_category_mapping
(
   service_id     varchar(10)   NOT NULL,
   insurance_category_id  integer       NOT NULL
);

CREATE INDEX service_id_service_insurance_category_mapping ON 
service_insurance_category_mapping USING btree (service_id);
CREATE INDEX insurance_category_id_service_insurance_category_mapping ON 
service_insurance_category_mapping USING btree (insurance_category_id);

INSERT INTO service_insurance_category_mapping 
SELECT service_id,insurance_category_id FROM services;