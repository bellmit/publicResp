--
-- Script to upload Service details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_service_master;
CREATE TABLE tmp_service_master (
	service_name text not null,
	dept_name text not null,
	service_group text not null,
	service_sub_group text not null,
	insurance_category text not null,
	remarks text,
	alias text, code_type text, item_code text, cond_reqd text,
	allow_rate_increase text, allow_rate_decrease text,
	charge numeric NOT NULL
);

--
-- Load the sheet
--
COPY tmp_service_master FROM '/tmp/masters/services.csv' csv header;
ALTER TABLE tmp_service_master ADD COLUMN service_id text;

-- cleanup
UPDATE tmp_service_master SET service_name = trim(service_name),
	dept_name = trim(dept_name), service_group = trim(service_group),
	service_sub_group = trim(service_sub_group), insurance_category = trim(insurance_category),
	alias = trim(alias), code_type = trim(code_type), item_code = trim(item_code),
	cond_reqd = clean_yn(cond_reqd, 'Y'),
	allow_rate_increase = clean_yn(allow_rate_increase, 'Y'),
	allow_rate_decrease = clean_yn(allow_rate_decrease, 'Y');

SELECT 'TRUNCATING long names', substring(service_name, 1, 80)
FROM tmp_service_master
WHERE length(service_name)> 600;

UPDATE tmp_service_master SET service_name = substring(service_name, 1, 600)
WHERE length(service_name) > 600;

SELECT 'IGNORING Duplicate services', substring(service_name, 1, 60), count(*) as count
FROM tmp_service_master
GROUP BY service_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_service_master', 'service_name');

UPDATE tmp_service_master SET service_group = initcap(service_group);
UPDATE tmp_service_master SET service_sub_group = initcap(service_sub_group);
UPDATE tmp_service_master SET insurance_category = initcap(insurance_category);

--
-- Delete existing master data
--
DELETE FROM service_master_charges;
DELETE FROM service_org_details;
DELETE FROM services;
DELETE FROM services_departments;

--
-- Add all Service Departments anew
--
CREATE TEMP SEQUENCE serv_dept_seq;
INSERT INTO services_departments (department, serv_dept_id) (
	SELECT dept_name, nextval('serv_dept_seq')
	FROM (SELECT DISTINCT dept_name FROM tmp_service_master) as foo
);
DROP SEQUENCE serv_dept_seq;

--
-- Insert missing service groups and sub-groups.
--
INSERT INTO service_groups 
SELECT nextval('service_groups_seq'), 'A', service_group, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group FROM tmp_service_master) as t
WHERE NOT EXISTS (SELECT * FROM service_groups WHERE service_group_name ILIKE t.service_group);

INSERT INTO service_sub_groups (service_sub_group_id, service_sub_group_name, status,
	service_group_id, display_order, username, mod_time) 
SELECT nextval('service_sub_groups_seq'), t.service_sub_group, 'A',
	sgt.service_group_id, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group, service_sub_group FROM tmp_service_master) as t
	JOIN service_groups sgt ON (sgt.service_group_name ILIKE t.service_group)
WHERE NOT EXISTS (SELECT * FROM service_sub_groups JOIN service_groups USING (service_group_id)
	WHERE service_group_name ILIKE t.service_group 
		AND service_sub_group_name ILIKE t.service_sub_group);

--
-- Insert missing insurance categories
--
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name)
SELECT nextval('item_insurance_categories_seq'), insurance_category
FROM (SELECT DISTINCT insurance_category FROM tmp_service_master) t
WHERE NOT EXISTS (SELECT * FROM item_insurance_categories 
		WHERE insurance_category_name ILIKE t.insurance_category);
	
--
-- Services
--
ALTER TABLE services DISABLE TRIGGER z_services_master_audit_trigger;
INSERT INTO services (service_id, service_name, status, serv_dept_id, service_code,
	conduction_applicable, service_sub_group_id, insurance_category_id, remarks,
	allow_rate_increase, allow_rate_decrease)
SELECT generate_max_id('services','service_id','SERV',4), t.service_name, 'A', sd.serv_dept_id, t.alias,
	t.cond_reqd = 'Y', ssg.service_sub_group_id, ic.insurance_category_id, t.remarks,
	allow_rate_increase = 'Y', allow_rate_decrease = 'Y'
FROM tmp_service_master t
	LEFT JOIN services_departments sd ON (sd.department ILIKE t.dept_name)
	LEFT JOIN service_groups sg ON (sg.service_group_name ILIKE t.service_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_name ILIKE t.service_sub_group
		AND sg.service_group_id = ssg.service_group_id)
	LEFT JOIN item_insurance_categories ic ON (ic.insurance_category_name ILIKE t.insurance_category)
;
ALTER TABLE services ENABLE TRIGGER z_services_master_audit_trigger;

UPDATE tmp_service_master t SET service_id = s.service_id
FROM services s 
WHERE s.service_name = t.service_name;

--
-- Rate Plan applicable and code
--
INSERT INTO service_org_details (service_id, org_id, item_code, code_type)
SELECT service_id, 'ORG0001', item_code, code_type
FROM tmp_service_master;

--
-- Charges
--
ALTER TABLE service_master_charges DISABLE TRIGGER z_services_charges_audit_trigger;

INSERT INTO service_master_charges (service_id, org_id, bed_type, unit_charge) 
SELECT service_id, 'ORG0001', 'GENERAL', charge
FROM tmp_service_master;

ALTER TABLE service_master_charges ENABLE TRIGGER z_services_charges_audit_trigger;
