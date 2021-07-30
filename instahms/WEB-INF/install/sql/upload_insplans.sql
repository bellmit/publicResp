--
-- Script to upload insurance plan details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_insurance_plan;
CREATE TABLE tmp_insurance_plan (
	insurance_co text not null, plan_type text not null, plan_name text not null,
	ip_covered text, op_covered text,
	default_rate_plan text, treatment_limit numeric, plan_notes text, exclusions text,
	base_rate numeric, gap_amount numeric, marginal_percent numeric,
	perdiem_copay_per numeric, perdiem_copay_amount numeric,
	op_visit_copay_limit numeric, ip_visit_copay_limit numeric,
	cat1 text, fixed1 numeric, per1 numeric, limit1 numeric,
	cat2 text, fixed2 numeric, per2 numeric, limit2 numeric,
	cat3 text, fixed3 numeric, per3 numeric, limit3 numeric,
	cat4 text, fixed4 numeric, per4 numeric, limit4 numeric,
	cat5 text, fixed5 numeric, per5 numeric, limit5 numeric,
	cat6 text, fixed6 numeric, per6 numeric, limit6 numeric,
	cat7 text, fixed7 numeric, per7 numeric, limit7 numeric,
	cat8 text, fixed8 numeric, per8 numeric, limit8 numeric,
	cat9 text, fixed9 numeric, per9 numeric, limit9 numeric,
	cat10 text, fixed10 numeric, per10 numeric, limit10 numeric,
	cat11 text, fixed11 numeric, per11 numeric, limit11 numeric,
	cat12 text, fixed12 numeric, per12 numeric, limit12 numeric
);

COPY tmp_insurance_plan FROM '/tmp/masters/insplans.csv' csv header;
ALTER TABLE tmp_insurance_plan ADD COLUMN plan_id integer;

-- cleanups
UPDATE tmp_insurance_plan SET cat1=trim(cat1);
UPDATE tmp_insurance_plan SET cat2=trim(cat2);
UPDATE tmp_insurance_plan SET cat3=trim(cat3);
UPDATE tmp_insurance_plan SET cat4=trim(cat4);
UPDATE tmp_insurance_plan SET cat5=trim(cat5);
UPDATE tmp_insurance_plan SET cat6=trim(cat6);
UPDATE tmp_insurance_plan SET cat7=trim(cat7);
UPDATE tmp_insurance_plan SET cat8=trim(cat8);
UPDATE tmp_insurance_plan SET cat9=trim(cat9);
UPDATE tmp_insurance_plan SET cat10=trim(cat10);
UPDATE tmp_insurance_plan SET cat11=trim(cat11);
UPDATE tmp_insurance_plan SET cat12=trim(cat12);

UPDATE tmp_insurance_plan SET insurance_co = trim(insurance_co);
UPDATE tmp_insurance_plan SET plan_type = trim(plan_type);
UPDATE tmp_insurance_plan SET plan_name = trim(plan_name);
UPDATE tmp_insurance_plan SET default_rate_plan = trim(default_rate_plan);

UPDATE tmp_insurance_plan SET ip_covered = upper(substr(ip_covered,1,1));
UPDATE tmp_insurance_plan SET ip_covered = 'N' WHERE ip_covered IS NULL OR ip_covered = '';
UPDATE tmp_insurance_plan SET op_covered = upper(substr(op_covered,1,1));
UPDATE tmp_insurance_plan SET op_covered = 'N' WHERE op_covered IS NULL OR op_covered = '';

UPDATE tmp_insurance_plan SET base_rate = 0 WHERE base_rate IS NULL;
UPDATE tmp_insurance_plan SET gap_amount = 0 WHERE gap_amount IS NULL;
UPDATE tmp_insurance_plan SET marginal_percent = 0 WHERE marginal_percent IS NULL;

UPDATE tmp_insurance_plan SET perdiem_copay_per = 0 WHERE perdiem_copay_per IS NULL;
UPDATE tmp_insurance_plan SET perdiem_copay_amount = 0 WHERE perdiem_copay_amount IS NULL;

UPDATE tmp_insurance_plan SET op_visit_copay_limit = 0 WHERE op_visit_copay_limit IS NULL;
UPDATE tmp_insurance_plan SET ip_visit_copay_limit = 0 WHERE ip_visit_copay_limit IS NULL;

SELECT 'IGNORING Duplicate plan', substring(insurance_co,1,30), substring(plan_name,1,40), count(*) as c
FROM tmp_insurance_plan
GROUP BY insurance_co, plan_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_insurance_plan', 'insurance_co, plan_name');

SELECT 'IGNORING category', cat, '(Please create manually and re-upload)'
FROM (SELECT DISTINCT cat1 as cat FROM tmp_insurance_plan WHERE cat1 IS NOT NULL AND cat1 != ''
UNION ALL SELECT DISTINCT cat2 FROM tmp_insurance_plan WHERE cat2 IS NOT NULL AND cat2 != ''
UNION ALL SELECT DISTINCT cat3 FROM tmp_insurance_plan WHERE cat3 IS NOT NULL AND cat3 != ''
UNION ALL SELECT DISTINCT cat4 FROM tmp_insurance_plan WHERE cat4 IS NOT NULL AND cat4 != ''
UNION ALL SELECT DISTINCT cat5 FROM tmp_insurance_plan WHERE cat5 IS NOT NULL AND cat5 != ''
UNION ALL SELECT DISTINCT cat6 FROM tmp_insurance_plan WHERE cat6 IS NOT NULL AND cat6 != ''
UNION ALL SELECT DISTINCT cat7 FROM tmp_insurance_plan WHERE cat7 IS NOT NULL AND cat7 != ''
UNION ALL SELECT DISTINCT cat8 FROM tmp_insurance_plan WHERE cat8 IS NOT NULL AND cat8 != ''
UNION ALL SELECT DISTINCT cat9 FROM tmp_insurance_plan WHERE cat9 IS NOT NULL AND cat9 != ''
UNION ALL SELECT DISTINCT cat10 FROM tmp_insurance_plan WHERE cat10 IS NOT NULL AND cat10 != ''
UNION ALL SELECT DISTINCT cat11 FROM tmp_insurance_plan WHERE cat11 IS NOT NULL AND cat11 != ''
UNION ALL SELECT DISTINCT cat12 FROM tmp_insurance_plan WHERE cat12 IS NOT NULL AND cat12 != ''
) as allcats
WHERE NOT EXISTS (SELECT * from item_insurance_categories WHERE insurance_category_name ILIKE cat);

SELECT 'IGNORING Missing Insurance Co', m.insurance_co, count(*) || ' plans'
FROM (SELECT DISTINCT insurance_co FROM tmp_insurance_plan
	WHERE NOT EXISTS (SELECT * FROM insurance_company_master WHERE insurance_co_name ILIKE insurance_co)
) as m
	JOIN tmp_insurance_plan t ON (t.insurance_co = m.insurance_co)
GROUP BY m.insurance_co;

--
-- Create missing Plan types
--
INSERT INTO insurance_category_master (category_id, insurance_co_id, category_name, status)
SELECT nextval('insurance_category_master_seq'), insurance_co_id, plan_type, 'A'
FROM (SELECT DISTINCT insurance_co, plan_type FROM tmp_insurance_plan) as t
	JOIN insurance_company_master comp ON (comp.insurance_co_name ilike t.insurance_co)
WHERE NOT EXISTS (SELECT * FROM insurance_category_master
	WHERE category_name ilike t.plan_type AND insurance_co_id = comp.insurance_co_id);

--
-- Proceed to insert the plans
--
DELETE FROM insurance_plan_main;
ALTER SEQUENCE insurance_plan_main_seq RESTART 1;

INSERT INTO insurance_plan_main (plan_id, insurance_co_id, category_id, plan_name,
	overall_treatment_limit, plan_notes, plan_exclusions, default_rate_plan,
	ip_applicable, op_applicable, status, base_rate, gap_amount, marginal_percent,
	perdiem_copay_per, perdiem_copay_amount, op_visit_copay_limit, ip_visit_copay_limit)
SELECT nextval('insurance_plan_main_seq'), comp.insurance_co_id, cat.category_id, t.plan_name,
	t.treatment_limit, t.plan_notes, t.exclusions, od.org_id,
	t.ip_covered, t.op_covered, 'A', t.base_rate, t.gap_amount, t.marginal_percent,
	t.perdiem_copay_per, t.perdiem_copay_amount,
	t.op_visit_copay_limit, t.ip_visit_copay_limit
FROM tmp_insurance_plan t
	JOIN insurance_company_master comp ON (comp.insurance_co_name ilike t.insurance_co)
	JOIN insurance_category_master cat ON (cat.category_name ilike t.plan_type
			AND cat.insurance_co_id = comp.insurance_co_id)
	LEFT JOIN organization_details od ON (t.default_rate_plan ilike od.org_name);

--
--
UPDATE tmp_insurance_plan t SET plan_id =
	(SELECT plan_id
	FROM insurance_plan_main ipm
		JOIN insurance_company_master comp USING (insurance_co_id)
	WHERE comp.insurance_co_name ILIKE t.insurance_co
		AND ipm.plan_name = t.plan_name);

--
-- Insert the plan details
--
DELETE FROM insurance_plan_details;
INSERT INTO insurance_plan_details (plan_id, patient_type, insurance_category_id,
	patient_amount, patient_percent, patient_amount_cap, per_treatment_limit)
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed1, per1, null::numeric, limit1
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat1)
WHERE cat1 IS NOT NULL AND cat1 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed2, per2, null, limit2
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat2)
WHERE cat2 IS NOT NULL AND cat2 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed3, per3, null, limit3
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat3)
WHERE cat3 IS NOT NULL AND cat3 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed4, per4, null, limit4
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat4)
WHERE cat4 IS NOT NULL AND cat4 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed5, per5, null, limit5
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat5)
WHERE cat5 IS NOT NULL AND cat5 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed6, per6, null, limit6
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat6)
WHERE cat6 IS NOT NULL AND cat6 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed7, per7, null, limit7
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat7)
WHERE cat7 IS NOT NULL AND cat7 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed8, per8, null, limit8
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat8)
WHERE cat8 IS NOT NULL AND cat8 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed9, per9, null, limit9
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat9)
WHERE cat9 IS NOT NULL AND cat9 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed10, per10, null, limit10
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat10)
WHERE cat10 IS NOT NULL AND cat10 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed11, per11, null, limit11
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat11)
WHERE cat11 IS NOT NULL AND cat11 != ''
UNION ALL
SELECT ipm.plan_id, 'o', cat.insurance_category_id, fixed12, per12, null, limit12
FROM tmp_insurance_plan t
	JOIN insurance_plan_main ipm ON (ipm.plan_id = t.plan_id)
	JOIN item_insurance_categories cat ON (cat.insurance_category_name ilike cat12)
WHERE cat12 IS NOT NULL AND cat12 != '';

--
-- Copy OP to IP
--
INSERT INTO insurance_plan_details (plan_id, patient_type, insurance_category_id,
	patient_amount, patient_percent, patient_amount_cap, per_treatment_limit)
SELECT plan_id, 'i', insurance_category_id,
	patient_amount, patient_percent, patient_amount_cap, per_treatment_limit
FROM insurance_plan_details
	WHERE patient_type = 'o';
--
-- Insert all missing categories as 0 copay
--
INSERT INTO insurance_plan_details (plan_id, patient_type, insurance_category_id,
	patient_amount, patient_percent, patient_amount_cap, per_treatment_limit)
SELECT plan_id, pat_type, insurance_category_id,
	0, 0, null, null
FROM missing_plan_details_view;

