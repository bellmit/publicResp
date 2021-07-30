--
-- Script to upload operation details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_operation_master;
CREATE TABLE tmp_operation_master (
	operation_name text not null, dept_name text not null,
	service_group text not null, service_sub_group text not null, insurance_category text not null,
	remarks text,
	alias text, code_type text, item_code text, cond_reqd text,
	allow_rate_increase text, allow_rate_decrease text,
	sac_charge numeric not null,
	surgeon_charge numeric,
	anaesthetist_charge numeric
);

--
-- Load and cleanup
--
COPY tmp_operation_master FROM '/tmp/masters/operations.csv' csv header;
ALTER TABLE tmp_operation_master ADD COLUMN op_id text;

UPDATE tmp_operation_master SET operation_name = trim(operation_name), dept_name = trim(dept_name),
	service_group = trim(initcap(service_group)), service_sub_group = trim(initcap(service_sub_group)),
	insurance_category = trim(initcap(insurance_category)),
	alias = trim(alias), code_type = trim(code_type), item_code = trim(item_code),
	cond_reqd = clean_yn(cond_reqd, 'Y'),
	allow_rate_increase = clean_yn(allow_rate_increase, 'Y'),
	allow_rate_decrease = clean_yn(allow_rate_decrease, 'Y');

UPDATE tmp_operation_master SET surgeon_charge = 0 WHERE surgeon_charge IS NULL;
UPDATE tmp_operation_master SET anaesthetist_charge = 0 WHERE anaesthetist_charge IS NULL;

SELECT 'IGNORING Duplicate operations', substring(operation_name, 1, 60), count(*) as count
FROM tmp_operation_master
GROUP BY operation_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_operation_master', 'operation_name');

--
-- Delete existing masters
--
DELETE FROM operation_org_details;
DELETE FROM operation_charges;
DELETE FROM operation_master;

--
-- Insert missing departments
--
INSERT INTO department (dept_id, dept_name, status)
SELECT generate_max_id('department','dept_id','DEP', 4), newname, 'A'
FROM (SELECT DISTINCT dept_name AS newname FROM tmp_operation_master) as newdeps
WHERE NOT EXISTS (SELECT * FROM department WHERE dept_name = newname);

--
-- Insert missing service groups and sub-groups.
--
INSERT INTO service_groups 
SELECT nextval('service_groups_seq'), 'A', service_group, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group FROM tmp_operation_master) as t
WHERE NOT EXISTS (SELECT * FROM service_groups WHERE service_group_name ILIKE t.service_group);

INSERT INTO service_sub_groups (service_sub_group_id, service_sub_group_name, status,
	service_group_id, display_order, username, mod_time) 
SELECT nextval('service_sub_groups_seq'), t.service_sub_group, 'A',
	sgt.service_group_id, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group, service_sub_group FROM tmp_operation_master) as t
	JOIN service_groups sgt ON (sgt.service_group_name ILIKE t.service_group)
WHERE NOT EXISTS (SELECT * FROM service_sub_groups JOIN service_groups USING (service_group_id)
	WHERE service_group_name ILIKE t.service_group 
		AND service_sub_group_name ILIKE t.service_sub_group);

--
-- Insert missing insurance category.
--
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name)
SELECT nextval('item_insurance_categories_seq'), insurance_category
FROM (SELECT DISTINCT insurance_category FROM tmp_operation_master) t
WHERE NOT EXISTS (SELECT * FROM item_insurance_categories 
		WHERE insurance_category_name ILIKE t.insurance_category);

--
-- Insert Operations
--
ALTER TABLE operation_master DISABLE TRIGGER z_operation_master_audit_trigger;
INSERT INTO operation_master (op_id, operation_name, dept_id, status, operation_code,
	conduction_applicable, service_sub_group_id, insurance_category_id, remarks,
	allow_rate_increase, allow_rate_decrease)
SELECT generate_max_id('operation_master','op_id','OPID',4), t.operation_name, d.dept_id, 'A', t.alias,
	t.cond_reqd = 'Y', ssg.service_sub_group_id, ic.insurance_category_id, t.remarks,
	allow_rate_increase = 'Y', allow_rate_decrease = 'Y'
FROM tmp_operation_master t
	LEFT JOIN department d ON (d.dept_name = t.dept_name)
	LEFT JOIN service_groups sg ON (sg.service_group_name ILIKE t.service_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_name ILIKE t.service_sub_group
		AND sg.service_group_id = ssg.service_group_id)
	LEFT JOIN item_insurance_categories ic ON (ic.insurance_category_name ILIKE t.insurance_category);
ALTER TABLE operation_master ENABLE TRIGGER z_operation_master_audit_trigger;

UPDATE tmp_operation_master t SET op_id = om.op_id
FROM operation_master om 
WHERE om.operation_name = t.operation_name;

--
-- Rate Plan applicable and code
--
INSERT INTO operation_org_details (operation_id, org_id, item_code, code_type)
SELECT op_id, 'ORG0001', item_code, code_type
FROM tmp_operation_master;

--
-- Charges
--
ALTER TABLE operation_charges DISABLE TRIGGER z_operation_charges_audit_trigger;
INSERT INTO operation_charges (op_id, org_id, bed_type, surg_asstance_charge, surgeon_charge,
	anesthetist_charge)
SELECT op_id, 'ORG0001', 'GENERAL', sac_charge, surgeon_charge, anaesthetist_charge
FROM tmp_operation_master;

ALTER TABLE operation_charges ENABLE TRIGGER z_operation_charges_audit_trigger;

