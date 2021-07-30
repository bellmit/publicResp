--
-- Script to upload prescribed medicines master details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

--
-- Drop and create tmp table
--
DROP TABLE IF EXISTS tmp_prescribed_medicines_master;
CREATE TABLE tmp_prescribed_medicines_master (
	medicinename 	text not null,
	status 		text,
	genericname 	text,
	routeofadmin	text
);

--
-- Copying Data from CSV to tmp table
--
COPY tmp_prescribed_medicines_master FROM '/tmp/masters/prescribed_medicines_master.csv' csv header;

--
-- cleanup
--
UPDATE tmp_prescribed_medicines_master SET
					medicinename = trim(medicinename),
					genericname  = trim(genericname),
					status       = trim(status),
					routeofadmin = trim(routeofadmin);

UPDATE tmp_prescribed_medicines_master SET status = 'A' WHERE status = '';

--
-- Removing duplicate from tmp table
--
SELECT 'IGNORING Duplicate Medicine Name', medicinename, count(*)
FROM tmp_prescribed_medicines_master
GROUP BY medicinename
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_prescribed_medicines_master', 'medicinename');

--
-- Wipe out existing prescribed medicines master
--
DELETE FROM prescribed_medicines_master;

--
-- Insert prescribed medicines master
--
INSERT INTO prescribed_medicines_master (medicine_name, status, generic_name, route_of_admin)
SELECT medicinename, status, genericname, routeofadmin
FROM tmp_prescribed_medicines_master;

