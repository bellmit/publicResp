-- liquibase formatted sql
-- changeset tejasiitb:create-table-equipment-insurance-category-mapping

CREATE TABLE equipment_insurance_category_mapping
(
   equipment_id   character varying(100)   NOT NULL,
   insurance_category_id  integer  NOT NULL
);

CREATE INDEX equipment_id_equipment_insurance_category_mapping ON 
equipment_insurance_category_mapping USING btree (equipment_id);
CREATE INDEX insurance_category_id_equipment_insurance_category_mapping ON 
equipment_insurance_category_mapping USING btree (insurance_category_id);

INSERT INTO equipment_insurance_category_mapping 
SELECT eq_id,insurance_category_id FROM equipment_master;