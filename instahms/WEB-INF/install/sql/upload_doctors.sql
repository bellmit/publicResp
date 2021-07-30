--
-- Script to upload Doctors
--

\set QUIET
\set ON_ERROR_STOP
\t on
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_doctor_master;
CREATE TABLE tmp_doctor_master (
	doctor_name text NOT NULL, dept_name text NOT NULL,
	doctor_type text, ot_flag text, schedulable text, overbook text,
	pmt_eligible text, pmt_category text,
	op_revisit_validity integer, op_revisit_count integer, ip_cons_validity integer, ip_cons_count integer,
	qualif text, specialization text, reg_no text, clinic_phone text, mobile text,
	res_phone text, email text, license_no text, address text, practition_type text,
	op_charge numeric not null,
	op_revisit_charge numeric,
	pvt_op_cons_charge numeric,
	pvt_op_revisit_charge numeric,
	odd_hr_charge numeric,
	ip_cons_charge numeric,
	night_ip_charge numeric,
	ip_ward_visit_charge numeric,
	ot_charge numeric,
	asst_ot_charge numeric,
	co_op_surgeon_charge numeric
);

--
-- Load and cleanup the data
--
COPY tmp_doctor_master FROM '/tmp/masters/doctors.csv' csv header;
UPDATE tmp_doctor_master SET doctor_type = upper(trim(doctor_type));
UPDATE tmp_doctor_master SET doctor_type = 'HOSPITAL' WHERE doctor_type is null OR doctor_type = '';
UPDATE tmp_doctor_master SET dept_name = trim(dept_name);
UPDATE tmp_doctor_master SET ot_flag = clean_yn(ot_flag, 'N');
UPDATE tmp_doctor_master SET schedulable = clean_yn(schedulable, 'N');
UPDATE tmp_doctor_master SET overbook = clean_yn(overbook, 'N');
UPDATE tmp_doctor_master SET pmt_eligible = clean_yn(pmt_eligible, 'Y');
UPDATE tmp_doctor_master SET pmt_category = trim(pmt_category);
UPDATE tmp_doctor_master SET op_revisit_validity = 0 WHERE op_revisit_validity IS NULL;
UPDATE tmp_doctor_master SET op_revisit_count = 0 WHERE op_revisit_count IS NULL;
UPDATE tmp_doctor_master SET ip_cons_validity = 0 WHERE ip_cons_validity IS NULL;
UPDATE tmp_doctor_master SET ip_cons_count = 0 WHERE ip_cons_count IS NULL;
UPDATE tmp_doctor_master SET op_revisit_charge = 0 WHERE op_revisit_charge IS NULL;
UPDATE tmp_doctor_master SET pvt_op_cons_charge = 0 WHERE pvt_op_cons_charge IS NULL;
UPDATE tmp_doctor_master SET pvt_op_revisit_charge = 0 WHERE pvt_op_revisit_charge IS NULL;
UPDATE tmp_doctor_master SET odd_hr_charge = 0 WHERE odd_hr_charge IS NULL;
UPDATE tmp_doctor_master SET ip_cons_charge = 0 WHERE ip_cons_charge IS NULL;
UPDATE tmp_doctor_master SET ip_ward_visit_charge = 0 WHERE ip_ward_visit_charge IS NULL;
UPDATE tmp_doctor_master SET night_ip_charge = 0 WHERE night_ip_charge IS NULL;
UPDATE tmp_doctor_master SET ot_charge = 0 WHERE ot_charge IS NULL;
UPDATE tmp_doctor_master SET asst_ot_charge = 0 WHERE asst_ot_charge IS NULL;
UPDATE tmp_doctor_master SET co_op_surgeon_charge = 0 WHERE co_op_surgeon_charge IS NULL;

SELECT 'IGNORING Duplicate doctor', doctor_name, dept_name, count(*)
FROM tmp_doctor_master
GROUP BY doctor_name, dept_name
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_doctor_master', 'doctor_name, dept_name');

--
-- Clean out existing tables
--
DELETE FROM doctor_consultation_charge;
DELETE FROM doctor_op_consultation_charge;
DELETE FROM doctors;

--
-- Create missing payment categories (no clean out)
--
INSERT INTO category_type_master (cat_id, cat_name, status)
SELECT nextval('category_type_master_seq'), pmt_category, 'A'
FROM (SELECT DISTINCT pmt_category FROM tmp_doctor_master) as t
WHERE NOT EXISTS (SELECT * FROM category_type_master WHERE cat_name ilike t.pmt_category);

--
-- Create missing departments (no clean out)
--
INSERT INTO department (dept_id, dept_name, status, allowed_gender)
SELECT generate_max_id('department', 'dept_id', 'DEP', 4), dept_name, 'A', 'ALL'
FROM (SELECT DISTINCT dept_name FROM tmp_doctor_master) AS t
WHERE NOT EXISTS (SELECT * FROM department WHERE dept_name ilike t.dept_name);

--
-- Insert the doctors
--
INSERT INTO doctors (doctor_id, doctor_name, dept_id, doctor_type,
	ot_doctor_flag, schedule, overbook, payment_eligible, payment_category,
	op_consultation_validity, allowed_revisit_count, ip_discharge_consultation_validity,
	ip_discharge_consultation_count, qualification, specialization, registration_no, clinic_phone,
	doctor_mobile, res_phone, doctor_mail_id, doctor_license_number, doctor_address, practition_type,
	status, service_sub_group_id)
SELECT generate_max_id('doctors','doctor_id', 'DOC', 4), t.doctor_name, d.dept_id, t.doctor_type,
	t.ot_flag, (t.schedulable = 'Y'), (t.overbook = 'Y'), t.pmt_eligible, 0,
	t.op_revisit_validity, t.op_revisit_count, t.ip_cons_validity, t.ip_cons_count,
	t.qualif, t.specialization, t.reg_no, t.clinic_phone, t.mobile,
	t.res_phone, t.email, t.license_no, t.address, t.practition_type,
	'A', -1
FROM tmp_doctor_master t
	LEFT JOIN department d ON (t.dept_name ilike d.dept_name) ;

--
-- Insert the doctor OP charges (no bed_type)
--
INSERT INTO doctor_op_consultation_charge (doctor_id, org_id, op_charge, op_revisit_charge,
	private_cons_charge, private_cons_revisit_charge, op_oddhr_charge)
SELECT d.doctor_id, 'ORG0001', t.op_charge, t.op_revisit_charge,
	t.pvt_op_cons_charge, t.pvt_op_revisit_charge, t.odd_hr_charge
FROM doctors d
	JOIN department dep ON (d.dept_id = dep.dept_id)
	JOIN tmp_doctor_master t ON (t.doctor_name = d.doctor_name AND t.dept_name ilike dep.dept_name);

--
-- Insert the doctor IP charges
--
INSERT INTO doctor_consultation_charge (doctor_name, bed_type, organization,
	doctor_ip_charge, night_ip_charge, ward_ip_charge,
	ot_charge, assnt_surgeon_charge, co_surgeon_charge)
SELECT d.doctor_id, 'GENERAL', 'ORG0001',
	t.ip_cons_charge, t.night_ip_charge, t.ip_ward_visit_charge,
	t.ot_charge, t.asst_ot_charge, t.co_op_surgeon_charge
FROM doctors d
	JOIN department dep ON (d.dept_id = dep.dept_id)
	JOIN tmp_doctor_master t ON (t.doctor_name = d.doctor_name AND t.dept_name ilike dep.dept_name);



--
-- Insert the doctor org details
--

INSERT INTO doctor_org_details (doctor_id,org_id,
	applicable, is_override)
SELECT d.doctor_id, 'ORG0001', 't', 'N'
FROM doctors d
	JOIN department dep ON (d.dept_id = dep.dept_id)
	JOIN tmp_doctor_master t ON (t.doctor_name = d.doctor_name AND t.dept_name ilike dep.dept_name);




