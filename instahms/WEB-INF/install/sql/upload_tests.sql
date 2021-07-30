--
-- Script to upload Test details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_test_master;
CREATE TABLE tmp_test_master (
	test_name text not null,
	diag_dept text not null, dept_type text not null,
	service_group text not null, service_sub_group text not null, insurance_category text not null,
	remarks text,
	house_status text, specimen_type text,
	alias text, code_type text, item_code text,
	conduction_format text, cond_reqd text,
	Rate_plan_applicable boolean, status character(1),
	allow_rate_increase text, allow_rate_decrease text,
	charge numeric not null,
	result1label text, result1units text,
	result2label text, result2units text,
	result3label text, result3units text,
	result4label text, result4units text,
	result5label text, result5units text,
	result6label text, result6units text,
	result7label text, result7units text,
	result8label text, result8units text,
	result9label text, result9units text
);

--
-- Load the sheet
--
COPY tmp_test_master FROM '/tmp/masters/tests.csv' csv header;
ALTER TABLE tmp_test_master ADD COLUMN test_id text;
ALTER TABLE tmp_test_master ADD COLUMN result1labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result2labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result3labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result4labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result5labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result6labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result7labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result8labelid integer;
ALTER TABLE tmp_test_master ADD COLUMN result9labelid integer;

--
-- cleanup
--
UPDATE tmp_test_master SET
	test_name = trim(test_name),
	house_status = upper(substring(trim(house_status), 1, 1)),
	conduction_format = upper(substring(trim(conduction_format), 1, 1)),
	diag_dept = trim(diag_dept),
	service_group = initcap(trim(service_group)), service_sub_group = initcap(trim(service_sub_group)),
	insurance_category = initcap(trim(insurance_category)),
	specimen_type = initcap(trim(specimen_type)), dept_type = trim(dept_type),
	cond_reqd = clean_yn(cond_reqd, 'Y'),
	status = clean_yn(status, 'A'),
	allow_rate_increase = clean_yn(allow_rate_increase, 'Y'),
	allow_rate_decrease = clean_yn(allow_rate_decrease, 'Y');

UPDATE tmp_test_master SET
	Rate_plan_applicable = 't' WHERE Rate_plan_applicable = 'Y' or Rate_plan_applicable = 'y' or Rate_plan_applicable IS NULL;

UPDATE tmp_test_master SET
	Rate_plan_applicable = 'f' WHERE Rate_plan_applicable = 'N' or Rate_plan_applicable = 'n';

UPDATE tmp_test_master SET dept_type = 'DEP_LAB' WHERE dept_type ILIKE 'laboratory'
	OR dept_type IS NULL OR dept_type = '';
UPDATE tmp_test_master SET dept_type = 'DEP_RAD' WHERE dept_type ILIKE 'radiology';
UPDATE tmp_test_master SET house_status = 'I' WHERE house_status IS NULL or house_status = '';
UPDATE tmp_test_master SET conduction_format = 'V' WHERE result1label IS NOT NULL;
UPDATE tmp_test_master SET conduction_format = 'T' WHERE conduction_format NOT IN ('T','V');
UPDATE tmp_test_master SET conduction_format = 'T' WHERE conduction_format IS NULL;

SELECT 'TRUNCATING long names', substring(test_name, 1, 80)
FROM tmp_test_master
WHERE length(test_name)> 600;

UPDATE tmp_test_master SET test_name = substring(test_name, 1, 600)
WHERE length(test_name) > 600;

SELECT 'IGNORING Duplicate tests', test_name, count(*)
FROM tmp_test_master
GROUP BY test_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_test_master', 'test_name');

--
-- function for Updating the 9 result labels
--
DROP FUNCTION IF EXISTS tmp_test_master_resultlable_update();
CREATE FUNCTION tmp_test_master_resultlable_update() returns void as $BODY$
DECLARE
	i integer;
BEGIN
	FOR i IN 1..9 LOOP
	EXECUTE $$UPDATE tmp_test_master SET result$$ || i || $$label = substring(test_name, 1, 100)
						WHERE result$$ || i || $$label IS NULL AND result$$ || i || $$units IS NOT NULL$$;
	END LOOP;
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT tmp_test_master_resultlable_update();

UPDATE tmp_test_master SET result1label = substring(test_name, 1, 100)
WHERE result1label IS NULL OR result1label = '';

--
-- Trim the result units to 30
--
DROP FUNCTION IF EXISTS tmp_test_master_trim_units_update();
CREATE FUNCTION tmp_test_master_trim_units_update() returns void as $BODY$
DECLARE
	m integer;
BEGIN
	FOR m IN 1..9 LOOP
	EXECUTE $$UPDATE tmp_test_master SET result$$ || m || $$units = substring(result$$ || m || $$units, 1, 30)
						WHERE result$$ || m || $$units IS NOT NULL$$;
	END LOOP;
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT tmp_test_master_trim_units_update();

--
-- Ensure all diag departments have the same dept_type
--
SELECT 'IGNORING different department types, using first', diag_dept, count(*) FROM
(SELECT DISTINCT diag_dept, dept_type FROM tmp_test_master) as t
GROUP BY diag_dept HAVING count(*) > 1;

--
-- Wipe out existing tests and charges.
--
DELETE FROM diagnostic_charges;
DELETE FROM test_org_details;
DELETE FROM test_results_master;
ALTER SEQUENCE resultlabel_seq RESTART 1;
DELETE FROM diagnostics;

--
-- Insert missing Sample Types
-- Duplicate sample prefix can occur, give a warning, but it will fail as a duplicate key violation
--
SELECT 'ERROR Duplicate Sample Prefix (please create via UI)',
	upper(substring(ds.specimen_type, 1, 2)), count(*)
FROM (SELECT DISTINCT specimen_type FROM tmp_test_master) as ds
GROUP BY upper(substring(ds.specimen_type, 1, 2))
HAVING count(*) > 1;

INSERT INTO sample_type (sample_type_id, sample_type, sample_prefix, start_number, num_pattern, status)
SELECT nextval('sample_type_seq'), ds.specimen_type, upper(substring(ds.specimen_type, 1, 2)), 1,'000000','A'
FROM (SELECT DISTINCT specimen_type FROM tmp_test_master WHERE specimen_type IS NOT NULL) AS ds
WHERE NOT EXISTS (SELECT * FROM sample_type WHERE ds.specimen_type = sample_type);

--
-- Insert missing diag departments
--
INSERT INTO diagnostics_departments (ddept_id, ddept_name, category)
SELECT generate_max_id('diagnostics_departments', 'ddept_id', 'DDept', 4), diag_dept,
	(SELECT dept_type FROM tmp_test_master tt WHERE tt.diag_dept = t.diag_dept LIMIT 1)
FROM (SELECT DISTINCT diag_dept FROM tmp_test_master) as t
WHERE NOT EXISTS (SELECT * FROM diagnostics_departments WHERE ddept_name ILIKE t.diag_dept);

--
-- Every diag department needs one entry in test_dept_tokens per center.
--
INSERT INTO test_dept_tokens (dept_id, center_id, token_number)
SELECT ddept_id, center_id, 0
FROM diagnostics_departments dd
CROSS JOIN hospital_center_master hcm
WHERE NOT EXISTS (SELECT * FROM test_dept_tokens WHERE dept_id = dd.ddept_id and center_id = hcm.center_id);

--
-- Insert missing service groups and sub-groups
--
INSERT INTO service_groups
SELECT nextval('service_groups_seq'), 'A', service_group, 1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group FROM tmp_test_master) as sgt
WHERE NOT EXISTS (SELECT * FROM service_groups WHERE service_group_name ILIKE sgt.service_group);

INSERT INTO service_sub_groups
SELECT nextval('service_sub_groups_seq'), 'A', sgt.service_group_id, ssgt.service_sub_group,
	1, 'admin', current_timestamp
FROM (SELECT DISTINCT service_group, service_sub_group FROM tmp_test_master) as ssgt
	JOIN service_groups sgt ON (sgt.service_group_name ILIKE ssgt.service_group)
WHERE NOT EXISTS (SELECT * FROM service_sub_groups JOIN service_groups USING (service_group_id)
	WHERE service_group_name ILIKE ssgt.service_group
		AND service_sub_group_name ILIKE ssgt.service_sub_group);

--
-- Insert missing insurance categories
--
INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name)
SELECT nextval('item_insurance_categories_seq'), insurance_category
FROM (SELECT DISTINCT insurance_category FROM tmp_test_master) t
WHERE NOT EXISTS (SELECT * FROM item_insurance_categories
		WHERE insurance_category_name ILIKE t.insurance_category);


--
-- Insert all the Tests
--
ALTER TABLE diagnostics DISABLE TRIGGER z_diagnostictest_master_audit_trigger;

INSERT INTO diagnostics (test_id, test_name, ddept_id,
	sample_needed, type_of_specimen,
	house_status, diag_code, conduction_applicable,
	conduction_format, service_sub_group_id,
	insurance_category_id, remarks, status, allow_rate_increase, allow_rate_decrease)
SELECT generate_max_id('diagnostics','test_id','DGC',4), t.test_name, d.ddept_id,
	CASE WHEN t.specimen_type IS NULL OR t.specimen_type = '' THEN 'n' ELSE 'y' END, t.specimen_type,
	t.house_status, t.alias, (cond_reqd = 'Y'),
	t.conduction_format, ssg.service_sub_group_id,
	ic.insurance_category_id, t.remarks, t.status, (allow_rate_increase = 'Y'), (allow_rate_decrease = 'Y')
FROM tmp_test_master t
	LEFT JOIN diagnostics_departments d ON (d.ddept_name ILIKE t.diag_dept)
	LEFT JOIN service_groups sg ON (sg.service_group_name ILIKE t.service_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_name ILIKE t.service_sub_group
		AND sg.service_group_id = ssg.service_group_id)
	LEFT JOIN item_insurance_categories ic ON (ic.insurance_category_name ILIKE t.insurance_category);

ALTER TABLE diagnostics ENABLE TRIGGER z_diagnostictest_master_audit_trigger;

UPDATE tmp_test_master t SET test_id = d.test_id
	FROM diagnostics d WHERE d.test_name = t.test_name;

--
-- Insert the Test Results Master
--
DROP FUNCTION IF EXISTS test_results_master_insert_resultlabel();
CREATE FUNCTION test_results_master_insert_resultlabel() returns void as $BODY$
DECLARE
	j integer;
BEGIN
	FOR j IN 1..9 LOOP
	EXECUTE $$INSERT INTO test_results_master (test_id, resultlabel, units, display_order, resultlabel_id)
		SELECT test_id, result$$ || j || $$label, result$$ || j || $$units, $$ || j || $$, nextval('resultlabel_seq')
		FROM tmp_test_master
		WHERE conduction_format = 'V' AND result$$ || j || $$label IS NOT NULL$$;
	END LOOP;
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT test_results_master_insert_resultlabel();

--
-- Update result label id in tmp table
--
DROP FUNCTION IF EXISTS tmp_test_master_resultlabelid_update();
CREATE FUNCTION tmp_test_master_resultlabelid_update() returns void as $BODY$
DECLARE
	l integer;
BEGIN
	FOR l IN 1..9 LOOP
	EXECUTE $$UPDATE tmp_test_master ttm SET result$$ || l || $$labelid =
						(SELECT resultlabel_id FROM test_results_master
							WHERE ttm.result$$ || l || $$label IS NOT NULL AND test_id = ttm.test_id AND resultlabel = ttm.result$$ || l || $$label)$$;
	END LOOP;
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT tmp_test_master_resultlabelid_update();

DELETE FROM test_template_master;
INSERT INTO test_template_master (SELECT test_id, 'FORMAT_DEF' FROM diagnostics);

--
-- Rate Plan Codes and rate plan applicable
--
INSERT INTO test_org_details (test_id, org_id, applicable, item_code, code_type)
SELECT test_id, 'ORG0001', Rate_plan_applicable, item_code, code_type
FROM tmp_test_master;

--
-- Charges
--
ALTER TABLE diagnostic_charges DISABLE TRIGGER z_diagnostictest_charges_audit_trigger;

INSERT INTO diagnostic_charges (test_id, org_name, charge, bed_type, priority)
SELECT test_id, 'ORG0001', charge, 'GENERAL', 'R'
FROM tmp_test_master;

ALTER TABLE diagnostic_charges ENABLE TRIGGER z_diagnostictest_charges_audit_trigger;
