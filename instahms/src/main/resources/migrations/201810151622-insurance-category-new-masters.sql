-- liquibase formatted sql
-- changeset tejasiitb:insurance-category-multiselect-new-masters

CREATE TABLE diet_insurance_category_mapping (
	diet_id integer NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX diet_id_diet_insurance_category_mapping  ON
diet_insurance_category_mapping USING btree(diet_id);
CREATE INDEX insurance_category_id_diet_insurance_category_mapping  ON 
diet_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO diet_insurance_category_mapping 
SELECT diet_id,insurance_category_id FROM diet_master;

CREATE TABLE theatre_insurance_category_mapping (
	theatre_id character varying(10) NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX theatre_id_theatre_insurance_category_mapping  ON
theatre_insurance_category_mapping USING btree(theatre_id);
CREATE INDEX insurance_category_id_theatre_insurance_category_mapping  ON 
theatre_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO theatre_insurance_category_mapping 
SELECT theatre_id,'-1' FROM theatre_master;