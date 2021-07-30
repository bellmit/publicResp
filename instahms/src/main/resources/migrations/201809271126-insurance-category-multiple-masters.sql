-- liquibase formatted sql
-- changeset tejasiitb:insurance-category-mapping-masters
CREATE TABLE store_items_insurance_category_mapping (
	medicine_id integer NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX medicine_id_store_items_insurance_category_mapping  ON
store_items_insurance_category_mapping USING btree(medicine_id);
CREATE INDEX insurance_category_id_store_items_insurance_category_mapping  ON 
store_items_insurance_category_mapping USING btree(insurance_category_id);

UPDATE store_item_details SET insurance_category_id = -1
WHERE insurance_category_id IS NULL;
INSERT INTO store_items_insurance_category_mapping 
SELECT medicine_id,insurance_category_id FROM store_item_details;

CREATE TABLE bed_types_insurance_category_mapping (
	bed_type_name character varying(50) NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX bed_type_name_bed_types_insurance_category_mapping  ON 
bed_types_insurance_category_mapping USING btree(bed_type_name);
CREATE INDEX insurance_category_id_bed_types_insurance_category_mapping  ON 
bed_types_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO bed_types_insurance_category_mapping 
SELECT bed_type_name,insurance_category_id FROM bed_types;

CREATE TABLE consultation_types_insurance_category_mapping (
	consultation_type_id integer NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX consultation_type_id_consultation_types_insurance_category_mapping  ON 
consultation_types_insurance_category_mapping USING btree(consultation_type_id);
CREATE INDEX insurance_category_id_consultation_types_insurance_category_mapping  ON 
consultation_types_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO consultation_types_insurance_category_mapping 
SELECT consultation_type_id,insurance_category_id FROM consultation_types;

CREATE TABLE anesthesia_types_insurance_category_mapping (
	anesthesia_type_id character varying(50) NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX anesthesia_type_id_anesthesia_types_insurance_category_mapping  ON 
anesthesia_types_insurance_category_mapping USING btree(anesthesia_type_id);
CREATE INDEX insurance_category_id_anesthesia_types_insurance_category_mapping  ON 
anesthesia_types_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO anesthesia_types_insurance_category_mapping 
SELECT anesthesia_type_id,insurance_category_id FROM anesthesia_type_master;

CREATE TABLE packages_insurance_category_mapping (
	package_id integer NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX package_id_packages_insurance_category_mapping  ON 
packages_insurance_category_mapping USING btree(package_id);
CREATE INDEX insurance_category_id_packages_insurance_category_mapping  ON 
packages_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO packages_insurance_category_mapping 
SELECT package_id,insurance_category_id FROM pack_master;

CREATE TABLE common_charges_insurance_category_mapping (
	charge_name character varying(50) NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX charge_name_common_charges_insurance_category_mapping  ON
common_charges_insurance_category_mapping USING btree(charge_name);
CREATE INDEX insurance_category_id_common_charges_insurance_category_mapping  ON 
common_charges_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO common_charges_insurance_category_mapping 
SELECT charge_name,insurance_category_id FROM common_charges_master;


