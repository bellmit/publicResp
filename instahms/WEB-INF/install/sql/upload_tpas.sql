--
-- Script to upload tpa master details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_tpas;
CREATE TABLE tmp_tpas (
	tpa_name text not null,
	tpa_code text
);

--
-- Load the sheet
--
COPY tmp_tpas FROM '/tmp/masters/tpas.csv' csv header;

-- cleanup
UPDATE tmp_tpas SET tpa_name = trim(tpa_name), tpa_code = trim(tpa_code);

SELECT 'IGNORING Duplicate TPA', substring(tpa_name, 1, 60), count(*)
FROM tmp_tpas
GROUP BY tpa_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_tpas', 'tpa_name');

--
-- Wipe out existing master
--
DELETE FROM tpa_master;

--
-- Insert new master
--
INSERT INTO tpa_master (tpa_id, tpa_name, tpa_code)
SELECT generate_max_id('tpa_master', 'tpa_id', 'TPAID', 4), tpa_name, tpa_code
FROM tmp_tpas;


