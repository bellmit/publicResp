--__________________________________________________________________________________________

-------------~Steps to merge and delete mr_no's of duplicate patients:~-----------------------


--1) Create the view "duplicate_patients_view"  and the function "merge_mrnos()":
--	Using queries (1) and (3)
----------------------------------------------------------------------------------------------
--2) Find out the list of duplicate patients as:
--		SELECT * FROM duplicate_patients_view;
-----------------------------------------------------------------------------------------------
--3) Find out the most likely list of patients to be retained using query (2)
-----------------------------------------------------------------------------------------------
--4) Call the function "merge_mrnos( mr1::text, mr2::text, schema::text )", by passing:
--	mr1->the mr_no of the duplicate patient, being retained.
--	mr2->the mr_no of the duplicate patient, being replaced.
--	schema->the schema where the mr_nos are being updated.
--  Eg:
--    select * from merge_mrnos('MR000019', 'MR000175','fresh')

-- 	returns-> the number of tables wherein the mr_no has been successfully updated.
--_____________________________________________________________________________________________


--------------------(1)The list of duplicate patients identified----------------------------------------------------------------

CREATE OR REPLACE VIEW duplicate_patients_view AS
(
  SELECT dp.* ,
  MIN(to_TIMESTAMP(TO_CHAR(pr.reg_date + pr.reg_time, 'dd-mon-yyyy hh24:mi:ss'::text), 'dd-mon-yyyy hh24:mi:ss'::text)) AS first_visit_time
  FROM (
	SELECT distinct( a.mr_no) ,
	coalesce(a.patient_name||' '||b.last_name) AS dup_patient_name,
	get_patient_age(a.dateofbirth,a.expected_dob) AS age ,
	a.patient_gender AS gender,
	count(coalesce(a.patient_name||' '||b.last_name)) AS num_occurances
	FROM
		patient_details a
		INNER JOIN patient_details b
		ON a.patient_name=b.patient_name
	WHERE
		(a.last_name=b.last_name)
		AND (get_patient_age(a.dateofbirth,a.expected_dob)=get_patient_age(b.dateofbirth,b.expected_dob))
		AND a.patient_gender=b.patient_gender
	GROUP BY a.mr_no, age, a.patient_gender,
		coalesce(a.patient_name||' '||b.last_name)
	HAVING COUNT (coalesce(a.patient_name||' '||b.last_name)) >=2
	ORDER BY coalesce(a.patient_name||' '||b.last_name) , age
	) AS dp
  LEFT JOIN patient_registration pr ON dp.mr_no=pr.mr_no
  GROUP BY dp.mr_no, dup_patient_name, age, gender, num_occurances
  ORDER BY dup_patient_name, age,first_visit_time
)


---------------------(2)The list of most probable patient mr_no's being retained-------------------------------------------------

SELECT dpv1.mr_no, dpv1.dup_patient_name, dpv1.age,dpv1.gender,  dpv1.first_visit_time FROM duplicate_patients_view dpv1
	INNER JOIN
	(SELECT dup_patient_name,MIN(first_visit_time) AS mini,age, gender
		FROM duplicate_patients_view
		GROUP BY  dup_patient_name,age, gender ) dpv2
	ON dpv2.dup_patient_name=dpv1.dup_patient_name
	AND dpv2.mini=dpv1.first_visit_time
	AND dpv2.age=dpv1.age
	AND dpv2.gender=dpv1.gender
GROUP BY dpv1.mr_no,dpv1.age, dpv1.dup_patient_name,dpv1.first_visit_time, dpv1.gender
ORDER BY dpv1.dup_patient_name, dpv1.age



-----------------------(3)The function to merge and delete duplicate patients--------------------------------------------------------
------Here: mr1--> mr_no being retained, mr2-->the mr_no being replaced and deleted, schema-->the schema which is currently in use.--


CREATE OR REPLACE function merge_mrnos("mr1" text, "mr2" text , "schema" text)
RETURNS integer AS
$BODY$
DECLARE
	obj record;
	num integer;
	s text;
	vid1 record;
	vid2 text;
	vid3 text;

BEGIN
	num:=0;
	FOR obj IN SELECT  DISTINCT typname FROM pg_attribute , pg_type WHERE
	typrelid=attrelid AND (attname = 'mr_no' ) AND typname IN
	(SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg_%' AND
	tablename NOT LIKE 'patient_details' AND  schemaname=schema )
	LOOP
		s:=obj::text;
		s:=SUBSTRING(s,2,LENGTH(s)-2);
		EXECUTE 'UPDATE ' || s || ' SET mr_no='|| quote_literal(mr1) || ' WHERE mr_no= '||quote_literal(mr2)  ;
		num := num + 1;
	END LOOP;
	FOR obj IN SELECT  DISTINCT typname FROM pg_attribute , pg_type WHERE
	typrelid=attrelid AND (attname = 'mrno' ) AND typname IN
	(SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg_%' AND
	tablename NOT LIKE 'patient_details' AND  schemaname=schema )
	LOOP
		s:=obj::text;
		s:=substring(s,2,length(s)-2);
		EXECUTE 'UPDATE ' || s || ' set mrno='|| quote_literal(mr1) || ' WHERE mrno= '||quote_literal(mr2)  ;
		num := num + 1;
	END LOOP;
	----can be customized: so as to consider more column values from patient_details
	SELECT visit_id into vid2 FROM patient_details WHERE mr_no=mr2;
	IF (vid2 IS NOT NULL AND vid2!='')  THEN
	EXECUTE 'UPDATE patient_details  set visit_id='|| quote_literal(vid2) || ' WHERE mr_no= '||quote_literal(mr1)  ;
	END IF;
	SELECT previous_visit_id into vid3 FROM patient_details WHERE mr_no=mr2;
	IF (vid3 IS NOT NULL AND vid3!='') THEN
	EXECUTE 'UPDATE patient_details  set previous_visit_id='|| quote_literal(vid3) || ' WHERE mr_no= '||quote_literal(mr1)  ;
	END IF;
	EXECUTE 'DELETE FROM patient_details WHERE mr_no= '||quote_literal(mr2);
	RETURN num;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;

