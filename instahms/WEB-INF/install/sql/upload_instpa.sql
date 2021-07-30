--
-- Script to upload insurance-company - tpa linkage master from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_instpa;
CREATE TABLE tmp_instpa (
	ins_co_name text not null,
	tpa_name text not null
);

--
-- Load the sheet
--
COPY tmp_instpa FROM '/tmp/masters/instpa.csv' csv header;

-- cleanup
UPDATE tmp_instpa SET ins_co_name = trim(ins_co_name), tpa_name = trim(tpa_name);

-- warnings
SELECT 'IGNORING Duplicate Ins-co-TPA', substring(ins_co_name, 1, 30), 
	substring(tpa_name, 1, 30), count(*)
FROM tmp_instpa
GROUP BY ins_co_name, tpa_name
HAVING count(*) > 1;

SELECT remove_dups('tmp_instpa');

SELECT 'IGNORING missing Ins. Co.', substring(ins_co_name, 1, 60)
FROM (SELECT DISTINCT ins_co_name FROM tmp_instpa) as t
WHERE NOT EXISTS (SELECT * FROM insurance_company_master ic
	WHERE ic.insurance_co_name ILIKE t.ins_co_name);

SELECT 'IGNORING: missing TPA', substring(tpa_name, 1, 60)
FROM (SELECT DISTINCT tpa_name FROM tmp_instpa) as t
WHERE NOT EXISTS (SELECT * FROM tpa_master tpa
	WHERE tpa.tpa_name ILIKE t.tpa_name);

--
-- Wipe out existing master
--
DELETE FROM insurance_company_tpa_master;

--
-- Insert new items
--
INSERT INTO insurance_company_tpa_master (insurance_co_id, tpa_id)
SELECT ic.insurance_co_id, tpa.tpa_id
FROM tmp_instpa t
	JOIN insurance_company_master ic ON (ic.insurance_co_name ILIKE t.ins_co_name)
	JOIN tpa_master tpa ON (tpa.tpa_name ILIKE t.tpa_name);

--
-- Where any insurance company has no tpa, make the insurance company
-- itself into a tpa, insert the tpa master as well as a link.
--
INSERT INTO tpa_master (tpa_id, tpa_name, tpa_code)
SELECT generate_max_id('tpa_master', 'tpa_id', 'TPAID', 4), insurance_co_name, insurance_co_code
FROM insurance_company_master i
WHERE NOT EXISTS (SELECT * FROM insurance_company_tpa_master it
	WHERE it.insurance_co_id = i.insurance_co_id);

INSERT INTO insurance_company_tpa_master (insurance_co_id, tpa_id)
SELECT ic.insurance_co_id, tpa.tpa_id
FROM insurance_company_master ic
	JOIN tpa_master tpa ON (tpa.tpa_name = ic.insurance_co_name)
WHERE NOT EXISTS (SELECT * FROM insurance_company_tpa_master it 
	WHERE it.insurance_co_id = ic.insurance_co_id);

