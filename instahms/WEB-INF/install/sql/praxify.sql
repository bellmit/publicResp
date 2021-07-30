DROP VIEW IF EXISTS praxify_drug_master_template CASCADE;
CREATE OR REPLACE VIEW praxify_drug_master_template AS
SELECT sid.medicine_id as code,sid.medicine_name as brandname,ifm.item_form_name as form,
       gn.generic_name as generic,sid.item_strength as strength
FROM  store_item_details sid
LEFT JOIN generic_name gn ON(sid.generic_name = gn.generic_code)
LEFT JOIN item_form_master ifm USING(item_form_id) where service_sub_group_id in (37);
-- service_subgroup_id is using for filtering only Pharmacy item

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


CREATE TABLE praxify_master_sync_table (
	id integer,
	status character,
	operation character,
	type character,
	category integer,
	code character varying(20),
	name character varying(200),
	tariff character varying(100),
	generic character varying(30),
	form integer,
	strength character varying(50),
	strength_unit integer,
	strength_value character,
	stock numeric
);


DROP FUNCTION IF EXISTS update_praxify_sync_table() CASCADE;
CREATE FUNCTION update_praxify_sync_table() RETURNS TRIGGER AS $BODY$
DECLARE
	operationValue character;
	serviceSuGrpName character varying;
	genericName character varying;
	itemFormName character varying;
BEGIN
	IF ( TG_OP = 'INSERT' ) THEN
		operationValue := 'A'; 
	ELSEIF ( TG_OP = 'UPDATE') THEN
		IF(TG_TABLE_NAME = 'pack_master') THEN
			IF (NEW.package_active = 'I' AND OLD.package_active = 'A') THEN
				operationValue := 'D';	
			ELSE
				operationValue := 'U';
			END IF;
		ELSEIF (NEW.status = 'I' AND OLD.status = 'A') THEN
			operationValue := 'D';
		ELSE
			operationValue := 'U';
		END IF;
	ELSEIF ( TG_OP = 'DELETE' ) THEN
		operationValue := 'D';
	END IF;

	SELECT service_sub_group_name
		FROM service_sub_groups
		WHERE service_sub_group_id = NEW.service_sub_group_id
	INTO  serviceSuGrpName;

-- service_subgroup_id is using for filtering only Pharmacy item

	IF ( TG_TABLE_NAME = 'store_item_details' AND NEW.service_sub_group_id = '37' ) THEN

		SELECT generic_name
			FROM generic_name
			WHERE generic_code = NEW.generic_name
		INTO genericName;

		SELECT item_form_name
			FROM item_form_master
			WHERE item_form_id = NEW.item_form_id
		INTO itemFormName;

		INSERT into praxify_master_sync_table (operation,type,category,code,name,generic,form,strength,strength_value)
			values(operationValue ,'M',serviceSuGrpName,NEW.medicine_id,
				NEW.medicine_name,genericName,itemFormName,NEW.item_strength,NEW.value);

	ELSEIF ( TG_TABLE_NAME = 'diagnostics' ) THEN
		INSERT into praxify_master_sync_table (operation,type,category,code,name)
			values(operationValue ,'L',serviceSuGrpName,NEW.test_id,NEW.test_name);

	ELSEIF ( TG_TABLE_NAME = 'services' ) THEN
		INSERT into praxify_master_sync_table (operation,type,category,code,name)
			values(operationValue ,'S',serviceSuGrpName,NEW.service_id,NEW.service_name);

	ELSEIF (TG_TABLE_NAME = 'pack_master') THEN
		-- Need export only Diag Package
		IF(NEW.package_type = 'd') THEN
			INSERT into praxify_master_sync_table (operation,type,category,code,name)
				values(operationValue,'G', serviceSuGrpName, NEW.package_id, NEW.package_name);
		END IF;
	END IF;

RETURN NEW;
END;
$BODY$ language plpgsql;



DROP TRIGGER IF EXISTS praxify_sync_table_tigger ON store_item_details;
CREATE TRIGGER praxify_sync_table_tigger
  AFTER INSERT OR UPDATE OR DELETE
  ON store_item_details
  FOR EACH ROW
  EXECUTE PROCEDURE update_praxify_sync_table();

DROP TRIGGER IF EXISTS praxify_sync_table_tigger ON services;
CREATE TRIGGER praxify_sync_table_tigger
  AFTER INSERT OR UPDATE OR DELETE
  ON services
  FOR EACH ROW
  EXECUTE PROCEDURE update_praxify_sync_table();

DROP TRIGGER IF EXISTS praxify_sync_table_tigger ON diagnostics;
CREATE TRIGGER praxify_sync_table_tigger
  AFTER INSERT OR UPDATE OR DELETE
  ON diagnostics
  FOR EACH ROW
  EXECUTE PROCEDURE update_praxify_sync_table();

DROP TRIGGER IF EXISTS praxify_sync_table_tigger ON pack_master;
CREATE TRIGGER praxify_sync_table_tigger
  AFTER INSERT OR UPDATE OR DELETE
  ON pack_master
  FOR EACH ROW
  EXECUTE PROCEDURE update_praxify_sync_table();

DROP SEQUENCE IF EXISTS praxify_master_sync_table_seq;
CREATE SEQUENCE praxify_master_sync_table_seq;
ALTER TABLE praxify_master_sync_table ALTER COLUMN id SET DEFAULT nextval('praxify_master_sync_table_seq'::regclass);
ALTER TABLE praxify_master_sync_table ALTER COLUMN status SET DEFAULT 'N';
ALTER TABLE praxify_master_sync_table ALTER COLUMN operation SET NOT NULL;
ALTER TABLE praxify_master_sync_table ALTER COLUMN type SET NOT NULL;
ALTER TABLE praxify_master_sync_table ALTER COLUMN code SET NOT NULL;
ALTER TABLE praxify_master_sync_table ALTER COLUMN name SET NOT NULL;
ALTER TABLE praxify_master_sync_table ADD CONSTRAINT praxify_master_sync_table_pkey PRIMARY KEY (id);
ALTER TABLE praxify_master_sync_table ALTER COLUMN category TYPE character varying(100);
ALTER TABLE praxify_master_sync_table ALTER COLUMN form TYPE character varying(100);
ALTER TABLE praxify_master_sync_table ALTER COLUMN strength_unit TYPE character varying(100);



--lab results view

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



CREATE SEQUENCE praxify_result_status_seq;

CREATE TABLE praxify_result_status(
	id integer NOT NULL DEFAULT nextval('praxify_master_sync_table_seq'::regclass),
	result_id integer NOT NULL,
	order_id integer NOT NULL,
	patient_id character varying(15),
	mr_no character varying(15),
	type character,
	comment	TEXT,
	date_time timestamp,
	CONSTRAINT praxify_result_status_pkey PRIMARY KEY (id)
);
ALTER TABLE praxify_result_status ADD COLUMN status character DEFAULT 'N';
ALTER TABLE praxify_result_status ALTER COLUMN id SET DEFAULT nextval('praxify_result_status_seq'::regclass);


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



DROP VIEW IF EXISTS praxify_ip_frequency CASCADE;
CREATE OR REPLACE VIEW praxify_ip_frequency AS
select * from recurrence_daily_master;

DROP VIEW IF EXISTS praxify_op_frequency CASCADE;
CREATE OR REPLACE VIEW praxify_op_frequency AS
select * from medicine_dosage_master;

ALTER TABLE praxify_master_sync_table ALTER COLUMN generic type character varying(400);


