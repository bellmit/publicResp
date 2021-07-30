--
-- Script to upload Test result ranges from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_result_ranges;
CREATE TABLE tmp_result_ranges (
	test_name text NOT NULL,
	resultlabel text NOT NULL,
	priority integer NOT NULL,
	patient_gender text,
	min_patient_age integer,
	max_patient_age integer,
	age_unit text,
	min_improbable_value numeric,
	min_critical_value numeric,
	min_normal_value numeric NOT NULL, 
	max_normal_value numeric NOT NULL, 
	max_critical_value numeric,
	max_improbable_value numeric,
	reference_range_txt text
);

COPY tmp_result_ranges FROM '/tmp/masters/resultranges.csv' csv header;
ALTER TABLE tmp_result_ranges ADD COLUMN resultlabel_id integer;

UPDATE tmp_result_ranges SET
	test_name = trim(test_name),
	resultlabel = trim(resultlabel),
	reference_range_txt = trim(reference_range_txt),
	patient_gender = upper(substring(trim(patient_gender), 1, 1)),
	age_unit = upper(substring(trim(age_unit), 1, 1))
;

UPDATE tmp_result_ranges SET patient_gender = 'N' WHERE patient_gender NOT IN ('M','F','O');
UPDATE tmp_result_ranges SET age_unit = 'Y' WHERE age_unit NOT IN ('Y','D');

UPDATE tmp_result_ranges t SET resultlabel_id = trm.resultlabel_id
	FROM test_results_master trm, diagnostics d
WHERE trm.resultlabel = t.resultlabel AND d.test_id = trm.test_id AND d.test_name = t.test_name;

DELETE FROM test_result_ranges;
ALTER SEQUENCE test_result_ranges_seq RESTART 1;

SELECT 'IGNORING missing resultlabel', substring(resultlabel, 1, 60)
FROM tmp_result_ranges
WHERE resultlabel_id IS NULL;

SELECT 'IGNORING Duplicate resultlabel+priority', substring(test_name, 30), substring(resultlabel, 60), 
	priority, count(*) as c
FROM tmp_result_ranges
GROUP BY test_name, resultlabel, priority
HAVING count(*) > 1;

SELECT remove_dups_on('tmp_result_ranges', 'test_name, resultlabel, priority');

INSERT INTO test_result_ranges (result_range_id, resultlabel_id, min_patient_age, max_patient_age, age_unit,
	range_for_all, 
	priority, patient_gender, reference_range_txt,
	min_improbable_value, min_critical_value, min_normal_value, 
	max_normal_value, max_critical_value, max_improbable_value)
SELECT nextval('test_result_ranges_seq'), resultlabel_id, min_patient_age, max_patient_age, age_unit,
	CASE WHEN patient_gender = 'N' AND min_patient_age IS NULL AND max_patient_age IS NULL THEN 'Y'
		ELSE 'N' END,
	priority, patient_gender, reference_range_txt,
	min_improbable_value, min_critical_value, min_normal_value,
	max_normal_value, max_critical_value, max_improbable_value
FROM tmp_result_ranges 
WHERE resultlabel_id is not null;


