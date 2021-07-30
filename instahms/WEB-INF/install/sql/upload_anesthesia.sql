--
-- Script to upload anaesthesia type details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_anesthesia_master;
CREATE TABLE tmp_anesthesia_master (
	anesthesia_type_name text NOT NULL,
	service_group text NOT NULL, service_sub_group text NOT NULL, insurance_category text NOT NULL,
	code_type text, item_code text,
	unit_size integer NOT NULL,
	minimum_duration integer NOT NULL, slab_1_threshold integer NOT NULL, incr_duration integer NOT NULL,
	minimum_charge numeric NOT NULL, slab_1_charge numeric NOT NULL, incr_charge numeric NOT NULL
);

--
-- Load the sheet
--
COPY tmp_anesthesia_master FROM '/tmp/masters/anesthesia.csv' csv header;
ALTER TABLE tmp_anesthesia_master ADD COLUMN anesthesia_id text;

-- cleanup
UPDATE tmp_anesthesia_master SET anesthesia_type_name = trim(anesthesia_type_name),
	service_group = trim(initcap(service_group)), service_sub_group = trim(initcap(service_sub_group)),
	insurance_category = trim(initcap(insurance_category)),
	code_type = trim(code_type), item_code = trim(item_code);

SELECT 'IGNORING Duplicate anaesthesia type', substring(anesthesia_type_name, 1, 60), count(*)
FROM tmp_anesthesia_master
GROUP BY anesthesia_type_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_anesthesia_master', 'anesthesia_type_name');

--
-- Delete existing master data
--
DELETE FROM anesthesia_type_charges;
DELETE FROM anesthesia_type_org_details;
DELETE FROM anesthesia_type_master;
ALTER SEQUENCE anesthesia_type_master_seq RESTART 1;

--
-- Insert missing service groups and sub-groups.
--
INSERT INTO service_groups 
SELECT nextval('service_groups_seq'), 'A', service_group, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group FROM tmp_anesthesia_master) as t
WHERE NOT EXISTS (SELECT * FROM service_groups WHERE service_group_name ILIKE t.service_group);

INSERT INTO service_sub_groups (service_sub_group_id, service_sub_group_name, status,
	service_group_id, display_order, username, mod_time) 
SELECT nextval('service_sub_groups_seq'), t.service_sub_group, 'A',
	sgt.service_group_id, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group, service_sub_group FROM tmp_anesthesia_master) as t
	JOIN service_groups sgt ON (sgt.service_group_name ILIKE t.service_group)
WHERE NOT EXISTS (SELECT * FROM service_sub_groups JOIN service_groups USING (service_group_id)
	WHERE service_group_name ILIKE t.service_group 
		AND service_sub_group_name ILIKE t.service_sub_group);

--
-- Insert missing insurance categories
--
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name)
SELECT nextval('item_insurance_categories_seq'), insurance_category
FROM (SELECT DISTINCT insurance_category FROM tmp_anesthesia_master) t
WHERE NOT EXISTS (SELECT * FROM item_insurance_categories 
		WHERE insurance_category_name ILIKE t.insurance_category);
	
--
-- Insert anesthesia types
--
INSERT INTO anesthesia_type_master (anesthesia_type_id, anesthesia_type_name, duration_unit_minutes,
	min_duration, slab_1_threshold, incr_duration, service_sub_group_id, insurance_category_id)
SELECT nextval('anesthesia_type_master_seq'), t.anesthesia_type_name, t.unit_size,
	t.minimum_duration, t.slab_1_threshold, t.incr_duration, 
	ssg.service_sub_group_id, ic.insurance_category_id
FROM tmp_anesthesia_master t
	LEFT JOIN service_groups sg ON (sg.service_group_name ILIKE t.service_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_name ILIKE t.service_sub_group
		AND sg.service_group_id = ssg.service_group_id)
	LEFT JOIN item_insurance_categories ic ON (ic.insurance_category_name ILIKE t.insurance_category);

UPDATE tmp_anesthesia_master t SET anesthesia_id = a.anesthesia_type_id
FROM anesthesia_type_master a
WHERE a.anesthesia_type_name = t. anesthesia_type_name;

--
-- Rate Plan applicable and code
--
INSERT INTO anesthesia_type_org_details (anesthesia_type_id, org_id, item_code, code_type)
(SELECT anesthesia_id, 'ORG0001', item_code, code_type
FROM tmp_anesthesia_master);

--
-- Charges
--
INSERT INTO anesthesia_type_charges (anesthesia_type_id, org_id, bed_type, 
	min_charge, slab_1_charge, incr_charge)
SELECT anesthesia_id, 'ORG0001', 'GENERAL', minimum_charge, slab_1_charge, incr_charge
FROM tmp_anesthesia_master;


