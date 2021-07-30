--
-- Script to upload payment rules details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_payment_rules;
CREATE TABLE tmp_payment_rules (
	priority integer not null, head text not null, rate_plan text not null, 
	doc_cat text, ref_cat text, pres_cat text, activity text,
	doc_payment_type text, doc_payment_value numeric, doc_payment_expr text,
	ref_payment_type text, ref_payment_value numeric, ref_payment_expr text,
	pres_payment_type text, pres_payment_value numeric, pres_payment_expr text,
	pkg_amt numeric
);

COPY tmp_payment_rules FROM '/tmp/masters/pmtrules.csv' csv header;

UPDATE tmp_payment_rules SET
	doc_payment_type = lower(trim(doc_payment_type)),
	pres_payment_type = lower(trim(pres_payment_type)),
	ref_payment_type = lower(trim(ref_payment_type)),
	doc_payment_value = coalesce(doc_payment_value, 0),
	pres_payment_value = coalesce(pres_payment_value, 0),
	ref_payment_value = coalesce(ref_payment_value, 0)
;

--
-- Check duplicates
--
SELECT 'IGNORING duplicate priority', priority, count(*)
FROM tmp_payment_rules
GROUP BY priority HAVING count(*) > 1;

SELECT remove_dups_on('tmp_payment_rules', 'priority');

ALTER TABLE tmp_payment_rules ADD COLUMN org_id text;
ALTER TABLE tmp_payment_rules ADD COLUMN doc_cat_id text;
ALTER TABLE tmp_payment_rules ADD COLUMN pres_cat_id text;
ALTER TABLE tmp_payment_rules ADD COLUMN ref_cat_id text;
ALTER TABLE tmp_payment_rules ADD COLUMN activity_id text;
--
-- Convert names to IDs : rate plan
--
UPDATE tmp_payment_rules t SET org_id = '*' WHERE t.rate_plan = '*';

UPDATE tmp_payment_rules t SET org_id = o.org_id FROM organization_details o
WHERE o.org_name ilike t.rate_plan;

SELECT 'IGNORING rule with unknown rate plan', rate_plan
FROM tmp_payment_rules
WHERE org_id IS NULL;

DELETE FROM tmp_payment_rules WHERE org_id IS NULL;

--
-- Convert names to IDs : doc cat
--
UPDATE tmp_payment_rules t SET doc_cat_id = '*' WHERE t.doc_cat = '*';
UPDATE tmp_payment_rules t SET doc_cat_id = '' WHERE t.doc_cat IS NULL OR t.doc_cat ilike 'none';
UPDATE tmp_payment_rules t SET doc_cat_id = c.cat_id
FROM category_type_master c WHERE c.cat_name ilike t.doc_cat;

SELECT 'IGNORING rule with unknown doc category', doc_cat
FROM tmp_payment_rules
WHERE doc_cat_id IS NULL;

DELETE FROM tmp_payment_rules WHERE doc_cat_id IS NULL;

--
-- Convert names to IDs : pres cat
--
UPDATE tmp_payment_rules t SET pres_cat_id = '*' WHERE t.pres_cat = '*';
UPDATE tmp_payment_rules t SET pres_cat_id = '' WHERE t.pres_cat IS NULL OR t.pres_cat ilike 'none';

UPDATE tmp_payment_rules t SET pres_cat_id = c.cat_id 
FROM category_type_master c WHERE c.cat_name ilike t.pres_cat;

SELECT 'IGNORING rule with unknown pres category', pres_cat
FROM tmp_payment_rules
WHERE pres_cat_id IS NULL;

DELETE FROM tmp_payment_rules WHERE pres_cat_id IS NULL;

--
-- Convert names to IDs : ref cat
--
UPDATE tmp_payment_rules t SET ref_cat_id = '*' WHERE t.ref_cat = '*';
UPDATE tmp_payment_rules t SET ref_cat_id = '' WHERE t.ref_cat IS NULL OR t.ref_cat ilike 'none';

UPDATE tmp_payment_rules t SET ref_cat_id = c.cat_id
FROM category_type_master c WHERE c.cat_name ilike t.ref_cat;

SELECT 'IGNORING rule with unknown ref category', ref_cat
FROM tmp_payment_rules
WHERE ref_cat_id IS NULL;

DELETE FROM tmp_payment_rules WHERE ref_cat_id IS NULL;

--
-- Convert activities to IDs: tests
--
UPDATE tmp_payment_rules t SET activity_id = '' WHERE t.activity_id IS NULL OR t.activity_id ilike 'any';
UPDATE tmp_payment_rules t SET activity_id = d.test_id
FROM diagnostics d
WHERE t.head IN ('LTDIA','RTDIA') AND lower(d.test_name) = lower(t.activity);

SELECT 'IGNORING unknown test', activity
FROM tmp_payment_rules
WHERE head IN ('RTDIA', 'LTDIA') AND activity_id IS NULL;

--
-- Convert activities to IDs: services
--
UPDATE tmp_payment_rules t SET activity_id = s.service_id
FROM services s
WHERE t.head IN ('SERSNP') AND s.service_name ilike t.activity;

SELECT 'IGNORING unknown service', activity
FROM tmp_payment_rules
WHERE head IN ('SERSNP') AND activity_id IS NULL;

DELETE from tmp_payment_rules WHERE activity_id IS NULL;

--
-- Category text to ID mapping: enums
--
DROP VIEW IF EXISTS tmp_pmt_categories; 
CREATE VIEW tmp_pmt_categories AS 
SELECT * FROM (VALUES ('expr',5),('lessbill',4),('fixed',2),('percent',1)) AS foo (type, type_id);

--
-- Insert the payment rules
--
DELETE FROM payment_rules;
INSERT INTO payment_rules (precedance, charge_head, rate_plan, 
	doctor_category, prescribed_category, referrer_category, activity_id, 
	dr_payment_option, dr_payment_value, dr_payment_expr,
	presc_payment_option, presc_payment_value, presc_payment_expr,
	ref_payment_option, ref_payment_value, ref_payment_expr,
	dr_pkg_amt)
SELECT t.priority, t.head, t.org_id,
	t.doc_cat_id, t.pres_cat_id, t.ref_cat_id, t.activity_id, 
	coalesce(dc.type_id, 2), t.doc_payment_value, t.doc_payment_expr,
	coalesce(pc.type_id, 2), t.pres_payment_value, t.pres_payment_expr,
	coalesce(rc.type_id, 2), t.ref_payment_value, t.ref_payment_expr,
	pkg_amt
FROM tmp_payment_rules t
	LEFT JOIN tmp_pmt_categories dc ON (dc.type = t.doc_payment_type)
	LEFT JOIN tmp_pmt_categories pc ON (pc.type = t.pres_payment_type)
	LEFT JOIN tmp_pmt_categories rc ON (rc.type = t.ref_payment_type)
;

DROP VIEW IF EXISTS tmp_pmt_categories; 

