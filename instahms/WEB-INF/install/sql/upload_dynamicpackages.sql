--
-- Script to upload dyanamic packages from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

--
-- Drop and create tmp table
--
DROP TABLE IF EXISTS tmp_dyna_packages;
CREATE TABLE tmp_dyna_packages (
	package_name character varying(100) NOT NULL,
	itemcode character varying(20),
	codetype character varying(50),
	charges numeric NOT NULL,
	cat1 text, included1 text, limit1 numeric,
	cat2 text, included2 text, limit2 numeric,
	cat3 text, included3 text, limit3 numeric,
	cat4 text, included4 text, limit4 numeric,
	cat5 text, included5 text, limit5 numeric,
	cat6 text, included6 text, limit6 numeric,
	cat7 text, included7 text, limit7 numeric,
	cat8 text, included8 text, limit8 numeric,
	cat9 text, included9 text, limit9 numeric,
	cat10 text, included10 text, limit10 numeric
);

--
-- Load the sheet in tmp table
--
COPY tmp_dyna_packages FROM '/tmp/masters/dynamicpackages.csv' csv header;
ALTER TABLE tmp_dyna_packages ADD COLUMN packageId integer DEFAULT 0;


--
-- cleanup
--
UPDATE tmp_dyna_packages SET 
	package_name = trim(package_name), 
	itemcode=nullif(trim(itemcode),''), codetype=nullif(trim(itemcode),''),
	cat1=nullif(trim(cat1),''), included1=clean_yn(included1, 'Y'), limit1=COALESCE(limit1,0),
	cat2=nullif(trim(cat2),''), included2=clean_yn(included2, 'Y'), limit2=COALESCE(limit2,0),
	cat3=nullif(trim(cat3),''), included3=clean_yn(included3, 'Y'), limit3=COALESCE(limit3,0),
	cat4=nullif(trim(cat4),''), included4=clean_yn(included4, 'Y'), limit4=COALESCE(limit4,0),
	cat5=nullif(trim(cat5),''), included5=clean_yn(included5, 'Y'), limit5=COALESCE(limit5,0),
	cat6=nullif(trim(cat6),''), included6=clean_yn(included6, 'Y'), limit6=COALESCE(limit6,0),
	cat7=nullif(trim(cat7),''), included7=clean_yn(included7, 'Y'), limit7=COALESCE(limit7,0),
	cat8=nullif(trim(cat8),''), included8=clean_yn(included8, 'Y'), limit8=COALESCE(limit8,0),
	cat9=nullif(trim(cat9),''), included9=clean_yn(included9, 'Y'), limit9=COALESCE(limit9,0),
	cat10=nullif(trim(cat10),''), included10=clean_yn(included10, 'Y'), limit10=COALESCE(limit10,0)
;

--
-- Removing Duplicates
--
SELECT 'IGNORING Duplicate Packages', package_name, count(*)
FROM tmp_dyna_packages
GROUP BY package_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_dyna_packages', 'package_name');

SELECT 'IGNORING category', cat, '(Please create manually and re-upload)'
FROM (SELECT DISTINCT cat1 as cat FROM tmp_dyna_packages WHERE cat1 IS NOT NULL AND cat1 != ''
UNION ALL SELECT DISTINCT cat2 FROM tmp_dyna_packages WHERE cat2 IS NOT NULL AND cat2 != ''
UNION ALL SELECT DISTINCT cat3 FROM tmp_dyna_packages WHERE cat3 IS NOT NULL AND cat3 != ''
UNION ALL SELECT DISTINCT cat4 FROM tmp_dyna_packages WHERE cat4 IS NOT NULL AND cat4 != ''
UNION ALL SELECT DISTINCT cat5 FROM tmp_dyna_packages WHERE cat5 IS NOT NULL AND cat5 != ''
UNION ALL SELECT DISTINCT cat6 FROM tmp_dyna_packages WHERE cat6 IS NOT NULL AND cat6 != ''
UNION ALL SELECT DISTINCT cat7 FROM tmp_dyna_packages WHERE cat7 IS NOT NULL AND cat7 != ''
UNION ALL SELECT DISTINCT cat8 FROM tmp_dyna_packages WHERE cat8 IS NOT NULL AND cat8 != ''
UNION ALL SELECT DISTINCT cat9 FROM tmp_dyna_packages WHERE cat9 IS NOT NULL AND cat9 != ''
UNION ALL SELECT DISTINCT cat10 FROM tmp_dyna_packages WHERE cat10 IS NOT NULL AND cat10 != ''
) as allcats
WHERE NOT EXISTS (SELECT * FROM dyna_package_category WHERE dyna_pkg_cat_name ILIKE cat);

--
-- Wipe out existing table data 
--
DELETE FROM dyna_package_category_limits;
DELETE FROM dyna_package_charges;
DELETE FROM dyna_package_org_details;
DELETE FROM dyna_packages;
ALTER SEQUENCE dyna_packages_seq RESTART 1;

--
-- Inserting into dyna package tables
--
INSERT INTO dyna_packages (dyna_package_id, dyna_package_name, status, username)
(SELECT nextval('dyna_packages_seq'), package_name, 'A', 'InstaAdmin' 
FROM tmp_dyna_packages);

UPDATE tmp_dyna_packages tdp SET packageId = dp.dyna_package_id
	FROM dyna_packages dp 
	WHERE dp.dyna_package_name = tdp.package_name;

INSERT INTO dyna_package_org_details(dyna_package_id, org_id, applicable, item_code, code_type)
(SELECT packageId, 'ORG0001', true, itemcode, codetype FROM tmp_dyna_packages order by packageId);

INSERT INTO dyna_package_charges(dyna_package_id, bed_type, org_id, charge, username)
(SELECT packageId, 'GENERAL', 'ORG0001', charges, 'InstaAdmin' FROM tmp_dyna_packages order by packageId);

--
-- Insert into dyna package limits for each category
--
DELETE FROM dyna_package_category_limits;

INSERT INTO dyna_package_category_limits
	(bed_type, org_id, username, dyna_package_id, dyna_pkg_cat_id, pkg_included, amount_limit, qty_limit)
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included1, t.limit1, t.limit1
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat1)
WHERE cat1 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included2, t.limit2, t.limit2
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat2)
WHERE cat2 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included3, t.limit3, t.limit3
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat3)
WHERE cat3 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included4, t.limit4, t.limit4
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat4)
WHERE cat4 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included5, t.limit5, t.limit5
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat5)
WHERE cat5 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included6, t.limit6, t.limit6
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat6)
WHERE cat6 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included7, t.limit7, t.limit7
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat7)
WHERE cat7 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included8, t.limit8, t.limit8
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat8)
WHERE cat8 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included9, t.limit9, t.limit9
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat9)
WHERE cat9 IS NOT NULL
UNION ALL
SELECT 'GENERAL', 'ORG0001', 'InstaAdmin', t.packageId, cat.dyna_pkg_cat_id, t.included10, t.limit10, t.limit10
FROM tmp_dyna_packages t
	JOIN dyna_package_category cat ON (cat.dyna_pkg_cat_name ilike cat10)
WHERE cat10 IS NOT NULL


