--
-- Script to upload initial master of outhouse tests
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_ohtests;
CREATE TABLE tmp_ohtests (
	oh_name text,
	test_name text,
	charge numeric
);

-- Load the csv

COPY tmp_ohtests FROM '/tmp/masters/ohtests.csv' csv header;

-- cleanup
UPDATE tmp_ohtests SET oh_name = trim(oh_name), test_name = trim(test_name);

SELECT 'IGNORING Duplicate Outhouse test', oh_name, test_name, count(*) as c
FROM tmp_ohtests
GROUP BY oh_name, test_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_ohtests', 'oh_name, test_name');

SELECT 'IGNORING missing test', test_name
FROM (SELECT DISTINCT test_name FROM tmp_ohtests) t
WHERE NOT EXISTS (SELECT * FROM diagnostics d WHERE d.test_name ILIKE t.test_name);

--
-- Insert all missing Out Houses into Out House master
--
INSERT INTO outhouse_master (oh_id, oh_name)
SELECT 'OHHOSP' || to_char(nextval('outhouseid_sequence'), 'FM0000'), oh_name
FROM (SELECT DISTINCT oh_name FROM tmp_ohtests) t
WHERE NOT EXISTS (SELECT * FROM outhouse_master oh WHERE oh.oh_name ILIKE t.oh_name);

INSERT INTO diag_outsource_master (outsource_dest_id, outsource_dest, outsource_dest_type, status)
SELECT nextval('diag_outsource_master_seq'), oh_id, 'O', status FROM (SELECT DISTINCT oh_name FROM tmp_ohtests) t
JOIN outhouse_master oh ON (oh.oh_name ILIKE t.oh_name);


--
-- Insert all charges after deleting existing
--
DELETE FROM ohmaster_detail;
INSERT INTO ohmaster_detail (oh_id, oh_testid, oh_charge)
SELECT oh.oh_id, d.test_id, t.charge
FROM tmp_ohtests t
	JOIN outhouse_master oh ON (oh.oh_name ILIKE t.oh_name)
	JOIN diagnostics d ON (d.test_name ILIKE t.test_name);


INSERT INTO diag_outsource_detail  (outsource_dest_id, test_id, charge)
SELECT dom.outsource_dest_id, d.test_id, t.charge
FROM tmp_ohtests t
	JOIN outhouse_master oh ON (oh.oh_name ILIKE t.oh_name)
	JOIN diag_outsource_master dom ON (dom.outsource_dest ILIKE oh.oh_id)
	JOIN diagnostics d ON (d.test_name ILIKE t.test_name);
