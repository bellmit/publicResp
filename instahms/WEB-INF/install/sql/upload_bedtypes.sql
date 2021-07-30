--
-- Script to upload Bed Types details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_bed_types;
CREATE TABLE tmp_bed_types (
	bed_type text not null,
	icu_type text,
	billing_bed text,
	display_order integer,
	bed_charge numeric not null,
	duty_doctor_charge numeric,
	maintainance_charge numeric,
	nurse_charge numeric,
	initial_payment numeric,
	lux_tax numeric,
	hourly_charge numeric,
	dc_slab_1_charge numeric,
	dc_slab_2_charge numeric,
	dc_slab_3_charge numeric
);

--
-- Load the sheet
--
COPY tmp_bed_types FROM '/tmp/masters/bedtypes.csv' csv header;

-- cleanup
SELECT remove_dups_on('tmp_bed_types', 'bed_type');
UPDATE tmp_bed_types SET icu_type = 'N' where icu_type IS NULL OR icu_type = '';
UPDATE tmp_bed_types SET icu_type = upper(substring(icu_type,1,1));
UPDATE tmp_bed_types SET billing_bed = 'Y' where billing_bed IS NULL OR billing_bed = '';
UPDATE tmp_bed_types SET billing_bed = upper(substring(billing_bed,1,1));
UPDATE tmp_bed_types SET duty_doctor_charge = 0 where duty_doctor_charge is NULL;
UPDATE tmp_bed_types SET maintainance_charge = 0 where maintainance_charge is NULL;
UPDATE tmp_bed_types SET nurse_charge = 0 where nurse_charge is NULL;
UPDATE tmp_bed_types SET initial_payment = 0 where initial_payment is NULL;
UPDATE tmp_bed_types SET lux_tax = 0 where lux_tax is NULL;
UPDATE tmp_bed_types SET hourly_charge = 0 where hourly_charge is NULL;
UPDATE tmp_bed_types SET dc_slab_1_charge = 0 where dc_slab_1_charge is NULL;
UPDATE tmp_bed_types SET dc_slab_2_charge = 0 where dc_slab_2_charge is NULL;
UPDATE tmp_bed_types SET dc_slab_3_charge = 0 where dc_slab_3_charge is NULL;

DELETE FROM bed_types WHERE bed_type_name != 'GENERAL';
DELETE FROM bed_details WHERE bed_type != 'GENERAL' OR organization != 'ORG0001';
DELETE FROM icu_bed_charges;

--
-- Insert the bed types: all except general
--
INSERT INTO bed_types (bed_type_name, is_icu, is_child_bed, billing_bed_type, display_order, status)
SELECT bed_type, icu_type, 'N', billing_bed, display_order, 'A'
FROM tmp_bed_types
WHERE bed_type != 'GENERAL';

--
-- Insert normal bed charges, except GENERAL
--
INSERT INTO bed_details (bed_type, organization, intensive_bed_status, 
	bed_charge, nursing_charge, initial_payment, duty_charge, maintainance_charge, luxary_tax,
	hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge)
SELECT bed_type, 'ORG0001', 'N',
	bed_charge, nurse_charge, initial_payment, duty_doctor_charge, maintainance_charge, lux_tax,
	hourly_charge, dc_slab_1_charge, dc_slab_2_charge, dc_slab_3_charge
FROM tmp_bed_types
WHERE icu_type = 'N' AND bed_type != 'GENERAL';

--
-- Update GENERAL charges if given
--
UPDATE bed_details b SET bed_charge=t.bed_charge, nursing_charge=t.nurse_charge, 
	initial_payment=t.initial_payment, duty_charge=t.duty_doctor_charge, 
	maintainance_charge=t.maintainance_charge, luxary_tax = t.lux_tax,
	hourly_charge=t.hourly_charge, daycare_slab_1_charge=t.dc_slab_1_charge, 
	daycare_slab_2_charge=t.dc_slab_2_charge, daycare_slab_3_charge=t.dc_slab_3_charge
FROM tmp_bed_types t
WHERE (t.bed_type = 'GENERAL' AND b.bed_type = 'GENERAL');

--
-- Insert ICU charges
--
INSERT INTO icu_bed_charges (intensive_bed_type, organization, bed_type, 
	bed_charge, nursing_charge, initial_payment, duty_charge, maintainance_charge, luxary_tax,
	hourly_charge, daycare_slab_1_charge, daycare_slab_2_charge, daycare_slab_3_charge)
SELECT bed_type, 'ORG0001', 'GENERAL',
	bed_charge, nurse_charge, initial_payment, duty_doctor_charge, maintainance_charge, lux_tax,
	hourly_charge, dc_slab_1_charge, dc_slab_2_charge, dc_slab_3_charge
FROM tmp_bed_types
WHERE icu_type = 'Y' AND bed_type != 'GENERAL';



