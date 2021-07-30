--
-- Script to upload insurance company details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_inscomp;
CREATE TABLE tmp_inscomp (
	co_name text not null,
	code text,
	default_rate_plan text
);

--
-- Load the sheet
--
COPY tmp_inscomp FROM '/tmp/masters/inscomps.csv' csv header;

-- cleanup
UPDATE tmp_inscomp SET co_name = trim(co_name), code = trim(code),
	default_rate_plan = trim(default_rate_plan);
UPDATE tmp_inscomp SET default_rate_plan = NULL where default_rate_plan = '';

SELECT 'IGNORING Duplicate Insruance Co.', substring(co_name, 1, 60), count(*)
FROM tmp_inscomp
GROUP BY co_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_inscomp', 'co_name');

--
-- Wipe out existing master
--
DELETE FROM insurance_company_master;

--
-- Insert companies
--
INSERT INTO insurance_company_master (insurance_co_id, insurance_co_name, 
	default_rate_plan, insurance_co_code)
SELECT generate_max_id('insurance_company_master', 'insurance_co_id', 'ICM',4),
	co_name, default_rate_plan, code
FROM tmp_inscomp;


