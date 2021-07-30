--
-- Upload patients from a spreadsheet for initial master data.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_patient_details;
CREATE TABLE tmp_patient_details (
	old_mrno text,
	salutation text,
	first_name text not null,
	last_name text,
	gender text not null,
	age numeric not null,
	age_in text not null,
	phone text,
	address text,
	area text,
	city text not null,
	state text not null,
	country text not null,
	first_visit_reg_date date,
	field1_name text, field1_value text,
	field2_name text, field2_value text,
	field3_name text, field3_value text,
	field4_name text, field4_value text,
	field5_name text, field5_value text,
	field6_name text, field6_value text
);

COPY tmp_patient_details FROM '/tmp/masters/patients.csv' csv header;
ALTER TABLE tmp_patient_details ADD COLUMN mr_no text;
-- We generate the MR Nos. now itself so as to retain the original
-- spreadsheet order. Otherwise, the updates for cleanups will reorder the table.
UPDATE tmp_patient_details SET mr_no=generate_id('MRNO');

-- cleanups/calculated values
UPDATE tmp_patient_details SET gender = trim(upper(gender)),
	phone = trim(phone),
	salutation = trim(salutation),
	old_mrno = trim(old_mrno),
	first_name = trim(first_name),
	last_name = trim(last_name),
	country = trim(country),
	state = trim(state),
	area = trim(area),
	city = trim(city),
	first_visit_reg_date = coalesce(first_visit_reg_date, current_date);

UPDATE tmp_patient_details SET gender = 'M' where gender = 'MALE';
UPDATE tmp_patient_details SET gender = 'F' where gender = 'FEMALE';
UPDATE tmp_patient_details SET gender = 'O' WHERE gender NOT IN ('M','F');

ALTER TABLE tmp_patient_details ADD COLUMN dob date;
UPDATE tmp_patient_details SET dob = '2011-01-01'::date - (age* (CASE
	WHEN age_in ilike 'years' THEN 365
	WHEN age_in ilike 'yrs' THEN 365
	WHEN age_in ilike 'y' THEN 365
	WHEN age_in ilike 'months' THEN 30
	WHEN age_in ilike 'mths' THEN 30
	WHEN age_in ilike 'm' THEN 30
	ELSE 1 END))::integer ;

SELECT 'TRUNCATING long phone number', old_mrno, phone
FROM tmp_patient_details
WHERE length(phone) > 15;

UPDATE tmp_patient_details SET phone=substring(phone,1,15)
WHERE length(phone) > 15;

--
-- Insert new salutations if necessary
--

INSERT INTO salutation_master (salutation_id, salutation, status)
SELECT generate_max_id('salutation_master','salutation_id','SALU', 4), salutation, 'A'
FROM (SELECT DISTINCT salutation FROM tmp_patient_details) AS newsals
WHERE NOT EXISTS (SELECT * FROM salutation_master WHERE salutation ILIKE newsals.salutation);


--
-- Insert new states if necessary
--
INSERT INTO state_master (state_id, country_id, state_name, status)
SELECT generate_max_id('state_master', 'state_id', 'ST', 4), country_id, state, 'A'
FROM (SELECT DISTINCT state, country FROM tmp_patient_details) as newstate
	JOIN country_master cm ON (cm.country_name ilike newstate.country)
WHERE NOT EXISTS (SELECT * FROM state_master JOIN country_master USING (country_id)
	WHERE state_name ilike newstate.state AND country_id = cm.country_id);

--
-- Insert new cities
--
INSERT INTO city (city_id, state_id, city_name, status)
SELECT generate_max_id('city', 'city_id', 'CT', 4), state_id, city, 'A'
FROM (SELECT DISTINCT city, state, country FROM tmp_patient_details) AS newcity
	JOIN state_master sm ON (sm.state_name ilike newcity.state)
	JOIN country_master cm ON (cm.country_id = sm.country_id AND newcity.country ilike cm.country_name)
WHERE NOT EXISTS (
	SELECT * FROM city
		JOIN state_master USING (state_id)
		JOIN country_master USING (country_id)
	WHERE city_name ILIKE newcity.city
		AND state_id = sm.state_id AND country_id = cm.country_id);

--
-- Insert new areas
--
INSERT INTO area_master (area_id, city_id, area_name, status)
SELECT 'AR' || to_char(nextval('area_master_seq'),'FM0000'), city_id, area, 'A'
FROM (SELECT DISTINCT area, city, state, country FROM tmp_patient_details
	WHERE area IS NOT NULL and area != '') as newarea
	JOIN city c ON (c.city_name ilike newarea.city)
	JOIN state_master sm ON (sm.state_id = c.state_id AND sm.state_name ilike newarea.state)
	JOIN country_master cm ON (cm.country_id = sm.country_id AND newarea.country ilike cm.country_name)
WHERE NOT EXISTS (
	SELECT * FROM area_master
		JOIN city USING (city_id)
		JOIN state_master USING (state_id)
		JOIN country_master USING (country_id)
	WHERE area_name ILIKE newarea.area
		AND city_id = c.city_id AND state_id = sm.state_id AND country_id = cm.country_id);

ALTER TABLE patient_details DISABLE TRIGGER z_patient_details_audit_trigger;

INSERT INTO patient_details (mr_no, oldmrno, salutation, patient_name, last_name, patient_gender,
	expected_dob, patient_phone, patient_address, country, patient_state, patient_city,
	patient_area, patient_category_id, first_visit_reg_date)
SELECT mr_no, old_mrno, s.salutation_id, first_name, last_name, tp.gender,
	dob, coalesce(phone,''), trim(coalesce(address, '')), cm.country_id, sm.state_id, cc.city_id,
	tp.area, 1, first_visit_reg_date
FROM tmp_patient_details tp
	LEFT JOIN salutation_master s ON (s.salutation ilike tp.salutation)
	LEFT JOIN country_master cm ON (cm.country_name ilike tp.country)
	LEFT JOIN state_master sm ON (sm.state_name ilike tp.state AND sm.country_id = cm.country_id)
	LEFT JOIN city cc ON (cc.city_name ilike tp.city AND cc.state_id = sm.state_id);

--
-- Any additional fields have to be updated
--
DROP FUNCTION IF EXISTS update_patient_field(text, text, text);
CREATE FUNCTION update_patient_field(mrno text, name text, value text) RETURNS void AS $BODY$
BEGIN
	EXECUTE 'UPDATE patient_details SET ' || quote_ident(name) || '=''' || value || ''''
		|| 'WHERE mr_no = ' || '''' || mrno || '''';
END;
$BODY$ LANGUAGE 'plpgsql';

-- Each row will produce one blank line. We don't want this.
-- So, send output to /dev/null
\o /dev/null

SELECT update_patient_field (mr_no, field1_name, field1_value)
FROM tmp_patient_details
WHERE field1_name IS NOT NULL AND field1_name != '';

SELECT update_patient_field (mr_no, field2_name, field2_value)
FROM tmp_patient_details
WHERE field2_name IS NOT NULL AND field2_name != '';

SELECT update_patient_field (mr_no, field3_name, field3_value)
FROM tmp_patient_details
WHERE field3_name IS NOT NULL AND field3_name != '';

SELECT update_patient_field (mr_no, field4_name, field4_value)
FROM tmp_patient_details
WHERE field4_name IS NOT NULL AND field4_name != '';

SELECT update_patient_field (mr_no, field5_name, field5_value)
FROM tmp_patient_details
WHERE field5_name IS NOT NULL AND field5_name != '';

SELECT update_patient_field (mr_no, field6_name, field6_value)
FROM tmp_patient_details
WHERE field6_name IS NOT NULL AND field6_name != '';

\o

ALTER TABLE patient_details ENABLE TRIGGER z_patient_details_audit_trigger;

