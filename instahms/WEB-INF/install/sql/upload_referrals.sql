--
-- Script to upload referral details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

--
-- Drop and create tmp table
--
DROP TABLE IF EXISTS tmp_referral;
CREATE TABLE tmp_referral (
	referal_name text,
	referal_mobileno text,
	payment_category integer,
	payment_eligible text, 
	referal_doctor_address text,
	referal_doctor_phone text,
	referal_doctor_email text,
	clinician_id text
);

--
-- Copying Data from CSV to tmp table
--
COPY tmp_referral FROM '/tmp/masters/referrals.csv' csv header;

--
-- cleanup
--
UPDATE tmp_referral SET 
		referal_name = trim(referal_name),
		referal_mobileno = trim(referal_mobileno),
		payment_eligible = trim(payment_eligible),
		referal_doctor_address = trim(referal_doctor_address),
		referal_doctor_phone = trim(referal_doctor_phone),
		referal_doctor_email = trim(referal_doctor_email),
		clinician_id = trim(clinician_id);

UPDATE tmp_referral SET clinician_id = NULL WHERE clinician_id = '';
UPDATE tmp_referral SET referal_name = NULL WHERE referal_name = '';
UPDATE tmp_referral SET referal_mobileno = NULL WHERE referal_mobileno = '';
UPDATE tmp_referral SET payment_eligible = 'N' WHERE payment_eligible = '';
UPDATE tmp_referral SET referal_doctor_address = NULL WHERE referal_doctor_address = '';
UPDATE tmp_referral SET referal_doctor_phone = NULL WHERE referal_doctor_phone = '';
UPDATE tmp_referral SET referal_doctor_email = NULL WHERE referal_doctor_email = '';
UPDATE tmp_referral SET payment_category = 1 WHERE payment_category = 0 or payment_category is null;

--
-- Removing duplicate from tmp table
--
SELECT 'IGNORING Duplicate referral Name', referal_name, count(*)
FROM referral
GROUP BY referal_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_referral', 'referal_name');

--
-- Wipe out existing referrals
--
DELETE FROM referral;

ALTER SEQUENCE referal_id_sequence RESTART 1;

--
-- Insert referrals
--
INSERT INTO referral (referal_no, referal_name, referal_mobileno,
	payment_category, payment_eligible, status, referal_doctor_address,
	referal_doctor_phone, referal_doctor_email, clinician_id)
SELECT nextval('referal_id_sequence'), referal_name, referal_mobileno,
	 payment_category, payment_eligible, 'A',
	 referal_doctor_address, referal_doctor_phone, referal_doctor_email,
	 clinician_id 
FROM tmp_referral;
