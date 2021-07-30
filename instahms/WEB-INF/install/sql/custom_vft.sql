-- Views, Functions, Triggers used for custom reports,data sync : This file will be run on every Upgrade.

--
--  * Changes in the view defnition at customer place
--    should be updated here as well other wise next upgrade will swipe all changes.
--  * To delete obsolete views/functions: remove from this file,as well as add
--    a "drop" statement
--  * Note that functions are sensitive to the parameters, that is, you cannot change a
--    function's parameter list. You have to drop the old one and create a new one.
--  * Ensure that you always drop a view/function using IF EXISTS before
--    creating a new one
--  * Views can depend on other views. Ensure that the order of creation is such that
--    dependent views come later.
--  * View names should be generic ,no prifix with customer name.We may use these view for other customers as well.
--  * Write down a description for every view/function ,any thing specific about the view and where it will be used.
--  * Place views/functions/triggers under respective module


--------------------------- Functions-----------------------------------------


------------------Masters----------------------


---------------------------------Views------------------------

------------Diagnostics----------


---
---- To expose test result details
---

DROP VIEW IF EXISTS praxify_lab_test_results_view;
CREATE VIEW praxify_lab_test_results_view AS
SELECT tp.test_id,d.test_name, td.resultlabel,td.resultlabel_id,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.report_value as value,td.comments as remarks,
	CASE WHEN td.withinnormal='Y' THEN 'Yes' ELSE 'No' END AS withinnormal,
	COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type as patient_type,date(tc.conducted_date) as conducted_date, date(sc.sample_date) as sample_date,
	td.units, td.reference_range,
	COALESCE( convert_to_numeric(report_value),0) AS test_value,td.result_disclaimer,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id)
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.package_ref)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','RP','RBS','RAS') AND td.conducted_in_reportformat = 'N'
AND td.report_value IS NOT NULL AND td.report_value != '' AND td.test_detail_status != 'A' ;

---
---- To expose test result details of current date
---

DROP VIEW IF EXISTS praxify_todays_lab_test_results_view;
CREATE VIEW praxify_todays_lab_test_results_view AS
SELECT tp.test_id,d.test_name, td.resultlabel,td.resultlabel_id,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.report_value as value,td.comments as remarks,
	CASE WHEN td.withinnormal='Y' THEN 'Yes' ELSE 'No' END AS withinnormal,
	COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type  as patient_type,date(tc.conducted_date) as conducted_date, date(sc.sample_date) as sample_date,
	td.units, td.reference_range,
	COALESCE( convert_to_numeric(report_value),0) AS test_value,td.result_disclaimer,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id)
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.prescribed_id)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','RP','RBS','RAS') AND td.conducted_in_reportformat = 'N'
AND td.report_value IS NOT NULL AND td.report_value != '' AND td.test_detail_status != 'A'
AND date(tc.conducted_date) = current_date ;

---
---- To expose radilogy test conduction details
---

DROP VIEW IF EXISTS praxify_radiology_templates_view;
CREATE VIEW praxify_radiology_templates_view AS
SELECT tp.test_id,d.test_name,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.comments as remarks,COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,
	sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type  as patient_type,date(tc.conducted_date) as conducted_date,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,td.patient_report_file,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_RAD')
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id )
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.package_ref)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','MA','CC','TS','CR','RP','RBS','RAS') AND td.conducted_in_reportformat = 'Y'
AND td.test_detail_status != 'A' ;

---
---- Current dates radilogy template test conduction details
---

DROP VIEW IF EXISTS praxify_todays_radiology_templates_view;
CREATE VIEW praxify_todays_radiology_templates_view AS
SELECT tp.test_id,d.test_name,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.comments as remarks,COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,
	sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type  as patient_type,date(tc.conducted_date) as conducted_date,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,td.patient_report_file,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_RAD')
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id)
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.package_ref)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','MA','CC','TS','CR','RP','RBS','RAS') AND td.conducted_in_reportformat = 'Y'
AND td.test_detail_status != 'A' AND date(tc.conducted_date) = current_date ;

--
--- Laboratoty template test coduction details
--

DROP VIEW IF EXISTS praxify_laboratory_templates_view;
CREATE VIEW praxify_laboratory_templates_view AS
SELECT tp.test_id,d.test_name,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.comments as remarks,COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,
	sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type  as patient_type,date(tc.conducted_date) as conducted_date,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,td.patient_report_file,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_LAB')
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id)
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.package_ref)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','RP','RBS','RAS') AND td.conducted_in_reportformat = 'Y'
AND td.test_detail_status != 'A' ;

--
--- Current dats Laboratoty template test coduction details
--

DROP VIEW IF EXISTS praxify_todays_laboratory_templates_view;
CREATE VIEW praxify_todays_laboratory_templates_view AS
SELECT tp.test_id,d.test_name,tp.prescribed_id as order_id,tp.pres_date as order_date,
	td.comments as remarks,COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,
	sm.salutation,
	COALESCE(pd.patient_name,isr.patient_name) as patient_name,
	pd.middle_name, pd.last_name,coalesce(pr.reg_date, isr.date) as reg_date,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	COALESCE(pd.patient_gender,isr.patient_gender) AS gender, pr.op_type, otn.op_type_name,
	pr.visit_type  as patient_type,date(tc.conducted_date) as conducted_date,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name,td.test_details_id as result_id,td.patient_report_file,
	pr.doctor as reg_doctor,tc.conducted_by as conducting_doctor,
	cdoc.doctor_name AS conducting_doctor_name,rdoc.doctor_name as reg_doctor_name,
	coalesce(pa.external_order_no, ps.external_order_no,pps.external_order_no) as external_order_no,
	coalesce(ps.consultation_id,pps.consultation_id) as op_consultation_id,
	CASE WHEN
			pp.doc_presc_id IS NOT NULL THEN 1 ELSE 0
		 END as is_package_result
FROM  test_details td
JOIN  tests_prescribed tp using(prescribed_id)
LEFT JOIN tests_conducted tc using(prescribed_id)
LEFT JOIN  sample_collection sc on(sc.sample_sno = tp.sample_no )
JOIN diagnostics d on (d.test_id = tp.test_id )
JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_LAB')
LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = td.patient_id)
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = td.patient_id)
LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id OR isr.center_id=hcm.center_id)
LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
LEFT JOIN patient_activities pa ON (pa.order_no=tp.prescribed_id)
LEFT JOIN patient_prescription ps ON (ps.patient_presc_id = tp.doc_presc_id)
LEFT JOIN package_prescribed pp ON (pp.prescription_id = tp.package_ref)
LEFT JOIN patient_prescription pps ON (pps.patient_presc_id = pp.doc_presc_id)
WHERE tp.conducted NOT IN ('N','P','RP','RBS','RAS') AND td.conducted_in_reportformat = 'Y'
AND td.test_detail_status != 'A' AND date(tc.conducted_date) = current_date ;

------------Vitals-------------

--patient vitals view

DROP VIEW IF EXISTS praxify_patient_visit_vitals_view;
CREATE VIEW praxify_patient_visit_vitals_view AS
SELECT vr.param_value, date_time::time AS v_time, vv.user_name, vpm.param_label,
	CASE WHEN pd.patient_gender = 'M' THEN 'Male' WHEN pd.patient_gender = 'F' THEN 'Female' ELSE 'Other' END  AS patient_gender,
	CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31
		THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer
	WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730
	THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer
	ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer END
	AS age, vpm.param_uom, date(date_time) AS date_time, pr.reg_date, pd.mr_no,
	sm.salutation || ' ' || pd.patient_name || CASE WHEN coalesce(pd.middle_name, '') = ''
	THEN '' ELSE (' ' || pd.middle_name) END  || CASE WHEN coalesce(pd.last_name, '') = ''
	THEN '' ELSE (' ' || pd.last_name) END  AS full_name
FROM visit_vitals vv
  LEFT JOIN patient_registration pr ON (pr.patient_id = vv.patient_id)
  LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
  LEFT JOIN vital_reading vr ON vr.vital_reading_id = vv.vital_reading_id
  LEFT JOIN vital_parameter_master vpm ON vr.param_id = vpm.param_id
  LEFT JOIN salutation_master sm ON pd.salutation= sm.salutation_id
WHERE vpm.param_container='V';

------------Masters--------------

--
---View to expose Medicine master details as code|brandname|form|generic|strength
--

DROP VIEW IF EXISTS praxify_drug_master_template CASCADE;
CREATE OR REPLACE VIEW praxify_drug_master_template AS
SELECT sid.medicine_id as code,sid.medicine_name as brandname,ifm.item_form_name as form,
       gn.generic_name as generic,sid.item_strength as strength
FROM  store_item_details sid
LEFT JOIN generic_name gn ON(sid.generic_name = gn.generic_code)
LEFT JOIN item_form_master ifm USING(item_form_id) where service_sub_group_id in (37);
-- service_subgroup_id is using for filtering only Pharmacy item


--
--- View to expose Lab,Radilogy,Services,Packages master data as
--  code(id)|codetype(identification 'L'-Lab,'R'-Radiology,'P' - Services,'G'-Packages)|description(name)|categorycode(service sub group id)|categoryname(service sub group name)
--
DROP VIEW IF EXISTS praxify_codes_master_template CASCADE;
CREATE OR REPLACE VIEW praxify_codes_master_template AS
SELECT d.test_id as code,'L' as codetype,d.test_name::character varying as description,
       d.test_name::character varying as shortName,d.service_sub_group_id as categorycode,ssg.service_sub_group_name::character varying as categoryname

FROM diagnostics d
JOIN diagnostics_departments dd USING(ddept_id)
JOIN service_sub_groups ssg USING(service_sub_group_id) WHERE dd.category = 'DEP_LAB'
UNION ALL
SELECT d.test_id as code,'R' as codetype,d.test_name as description,
       d.test_name as shortName,d.service_sub_group_id as categorycode,ssg.service_sub_group_name as categoryname

FROM diagnostics d
JOIN diagnostics_departments dd USING(ddept_id)
JOIN service_sub_groups ssg USING(service_sub_group_id) WHERE dd.category = 'DEP_RAD'
UNION ALL
SELECT s.service_id as code,'P' as codetype,s.service_name as description,
	s.service_name as shortName,s.service_sub_group_id as categorycode,ssg.service_sub_group_name as categoryname
FROM services s
JOIN service_sub_groups ssg USING(service_sub_group_id)
UNION ALL
SELECT pkg.package_id :: text as code,'G' as codetype,pkg.package_name as description,
	pkg.package_name as shortName,pkg.service_sub_group_id as categorycode,ssg.service_sub_group_name as categoryname
FROM pack_master pkg
JOIN service_sub_groups ssg USING(service_sub_group_id)
where pkg.package_type='d';

DROP VIEW IF EXISTS praxify_ip_frequency CASCADE;
CREATE OR REPLACE VIEW praxify_ip_frequency AS
select * from recurrence_daily_master;

DROP VIEW IF EXISTS praxify_op_frequency CASCADE;
CREATE OR REPLACE VIEW praxify_op_frequency AS
select * from medicine_dosage_master;


---------------------Scheduler-----------------

--
--- To expose doctor appointments
--
DROP VIEW IF EXISTS praxify_doctor_appointment_details_view CASCADE;
CREATE OR REPLACE VIEW praxify_doctor_appointment_details_view AS
SELECT sa.complaint AS complaint_name,
	(SELECT textcat_commacat(resourcename)
	FROM (SELECT
	CASE WHEN sai1.resource_type = 'EQID'  THEN (select equipment_name from test_equipment_master WHERE eq_id::text = sai1.resource_id)
	ELSE
		(SELECT generic_resource_name FROM generic_resource_type grt
			JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
	END AS resourcename FROM scheduler_appointment_items sai1 WHERE sa.appointment_id = sai1.appointment_id)  as foo) AS other_resources,
	sa.duration AS duration,   (SELECT textcat_commacat(equipment_name)
		FROM scheduler_appointment_items  sai
		JOIN test_equipment_master tem ON (tem.eq_id::text=sai.resource_id)
		WHERE sa.appointment_id = sai.appointment_id AND sai.resource_type = 'EQID')
	AS equipment_name, ct.consultation_type,sa.appointment_id,
	TO_CHAR((appointment_time::date +appointment_time::time ), 'dd-MM-yyyy hh24:mi') AS appointment_time, sa.patient_name as app_patient_name,
	date(appointment_time) AS 		  appointment_date, sa.visit_id, doc.doctor_name AS consultant_name, sa.mr_no,
	CASE WHEN sa.appointment_status = 'Arrived' THEN 'Arrived' WHEN
	sa.appointment_status = 'Noshow' THEN 'No Show' WHEN sa.appointment_status = 'Confirmed'
	THEN 'Confirmed' WHEN sa.appointment_status = 'Booked' THEN 'Booked'
	WHEN sa.appointment_status = 'Completed' THEN 'Completed' WHEN sa.appointment_status = 'Cancel'
	THEN 'Cancelled' END AS appointment_status,
	slm.salutation,pd.patient_name,pd.middle_name, pd.last_name,pr.reg_date,rdoc.doctor_name as reg_doctor_name,
	pr.doctor as reg_doctor,get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,sa.scheduler_visit_type,COALESCE(pd.patient_gender ,'') as patient_gender
FROM scheduler_appointments sa
LEFT JOIN patient_details pd ON (sa.mr_no=pd.mr_no)
LEFT JOIN salutation_master slm ON (slm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = sa.visit_id)
LEFT JOIN scheduler_master sm ON (sm.res_sch_id=sa.res_sch_id)
LEFT JOIN consultation_types ct USING (consultation_type_id)
LEFT JOIN doctors doc ON (doc.doctor_id = sa.res_sch_name)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
WHERE sm.res_sch_category = 'DOC';

--
--- To expose current days doctor appointments
--

DROP VIEW IF EXISTS praxify_todays_doctor_appointment_details_view CASCADE;
CREATE OR REPLACE VIEW praxify_todays_doctor_appointment_details_view AS
SELECT sa.complaint AS complaint_name,
	(SELECT textcat_commacat(resourcename)
	FROM (SELECT
	CASE WHEN sai1.resource_type = 'EQID'  THEN (select equipment_name from test_equipment_master WHERE eq_id::text = sai1.resource_id)
	ELSE
		(SELECT generic_resource_name FROM generic_resource_type grt
			JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
	END AS resourcename FROM scheduler_appointment_items sai1 WHERE sa.appointment_id = sai1.appointment_id)  as foo) AS other_resources,
	sa.duration AS duration,   (SELECT textcat_commacat(equipment_name)
		FROM scheduler_appointment_items  sai
		JOIN test_equipment_master tem ON (tem.eq_id::text=sai.resource_id)
		WHERE sa.appointment_id = sai.appointment_id AND sai.resource_type = 'EQID')
	AS equipment_name, ct.consultation_type,sa.appointment_id,
	TO_CHAR((appointment_time::date +appointment_time::time ), 'dd-MM-yyyy hh24:mi') AS appointment_time, sa.patient_name as app_patient_name,
	date(appointment_time) AS 		  appointment_date, sa.visit_id, doc.doctor_name AS consultant_name, sa.mr_no,
	CASE WHEN sa.appointment_status = 'Arrived' THEN 'Arrived' WHEN
	sa.appointment_status = 'Noshow' THEN 'No Show' WHEN sa.appointment_status = 'Confirmed'
	THEN 'Confirmed' WHEN sa.appointment_status = 'Booked' THEN 'Booked'
	WHEN sa.appointment_status = 'Completed' THEN 'Completed' WHEN sa.appointment_status = 'Cancel'
	THEN 'Cancelled' END AS appointment_status,
	slm.salutation,pd.patient_name,pd.middle_name, pd.last_name,pr.reg_date,rdoc.doctor_name as reg_doctor_name,
	pr.doctor as reg_doctor,get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,sa.scheduler_visit_type,COALESCE(pd.patient_gender,'') as patient_gender
FROM scheduler_appointments sa
LEFT JOIN patient_details pd ON (sa.mr_no=pd.mr_no)
LEFT JOIN salutation_master slm ON (slm.salutation_id = pd.salutation)
LEFT JOIN patient_registration pr ON (pr.patient_id = sa.visit_id)
LEFT JOIN scheduler_master sm ON (sm.res_sch_id=sa.res_sch_id)
LEFT JOIN consultation_types ct USING (consultation_type_id)
LEFT JOIN doctors doc ON (doc.doctor_id = sa.res_sch_name)
LEFT JOIN doctors rdoc ON (rdoc.doctor_id=pr.doctor)
WHERE sm.res_sch_category = 'DOC' AND date(appointment_time) >= current_date ;



------------------------------Triggers-------------------------------------


---------------------Masters----------------------

