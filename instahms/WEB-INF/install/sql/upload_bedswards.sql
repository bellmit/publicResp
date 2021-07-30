--
-- Script to upload Beds and Wards details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t on
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_beds_wards;
CREATE TABLE tmp_beds_wards (
	bed_name text not null,
	ward_name text not null,
	bed_type text not null
);

--
-- Load the sheet
--
COPY tmp_beds_wards FROM '/tmp/masters/bedswards.csv' csv header;

SELECT 'IGNORING Missing bed type', t.bed_name, t.ward_name, t.bed_type
FROM tmp_beds_wards t
WHERE NOT EXISTS (SELECT * FROM bed_types WHERE bed_type_name = t.bed_type);

--
-- Delete existing
--
DELETE FROM ward_names;
DELETE FROM bed_names;
ALTER SEQUENCE bedid_sequence RESTART 1;

--
-- Add all wards first
--
INSERT INTO ward_names (ward_no, ward_name)
SELECT generate_max_id('ward_names','ward_no','WARD',4), ward_name
FROM (SELECT DISTINCT ward_name FROM tmp_beds_wards) as foo;

--
-- Add bed names
--
INSERT INTO bed_names (bed_id, bed_name, ward_no, bed_type)
SELECT nextval('bedid_sequence'), t.bed_name, w.ward_no, t.bed_type
FROM tmp_beds_wards t
	JOIN ward_names w ON (w.ward_name = t.ward_name);


