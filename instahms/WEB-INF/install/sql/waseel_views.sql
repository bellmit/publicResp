/* WASEEL VIEWS */

CREATE OR REPLACE VIEW wsl_ins_claim_amts_view AS
		SELECT SUM(B.TOTAL_AMOUNT) AS TOTCLAIMGRSAMT, SUM(B.TOTAL_CLAIM) AS TOTCLAIMNETAMT,
			SUM(B.TOTAL_DISCOUNT) AS TOTCLAIMDISC,
			(SUM(B.TOTAL_AMOUNT) - SUM(TOTAL_CLAIM)) AS TOTCLAIMPATSHARE, IC.CLAIM_ID, B.VISIT_ID,IC.ACCOUNT_GROUP

			FROM bill b
			JOIN insurance_claim ic ON (b.claim_id = ic.claim_id)
			GROUP BY ic.claim_id, b.visit_id,ic.account_group;

DROP VIEW IF EXISTS wsl_geninfo CASCADE ;
CREATE OR REPLACE VIEW wsl_geninfo AS
SELECT agc.account_group_name AS PROVIDERID, icm.insurance_co_code AS PAYERID, tpa.tpa_id AS TPAID,
	   /*PRIMARY KEY*/ pd.mr_no || TO_CHAR(pr.reg_date, 'dd-MM-yyyy') || dr.doctor_id AS PROVCLAIMNO,
		ppd.member_id AS MEMBERID, ppd.policy_number AS POLICYNO,
		icam.category_name AS PLANTYPE,
		sm.salutation || ' ' || patient_name
		|| CASE WHEN COALESCE(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) END
		|| CASE WHEN COALESCE(last_name, '') = '' THEN '' ELSE (' ' || last_name) END
		AS FULL_NAME,
		pd.patient_name AS FIRSTNAME, pd.middle_name AS MIDDLENAME, pd.last_name AS LASTNAME,
		pd.mr_no AS PATFILENO, ic.policy_holder_name AS ACCCODE,
		TO_CHAR(pd.dateofbirth, 'dd-MM-yyyy') AS MEMBERDOB,
		GET_PATIENT_AGE(pd.dateofbirth, pd.expected_dob) AS MEMBERAGE,
		CASE
		WHEN CURRENT_DATE - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'DAYS'
 		WHEN (CURRENT_DATE - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'MONTHS'
		ELSE 'YEARS'
			END AS UNITAGE,
		CASE WHEN pd.patient_gender='M' THEN 'MALE' WHEN pd.patient_gender='F' THEN 'FEMALE' ELSE 'OTHER' END
		AS GENDER, pd.custom_list1_value AS NATIONALITY, dr.doctor_id AS PHYID,
		dr.doctor_name AS PHYSICIAN_NAME, ''::character varying AS PHYSICIAN_CATEGORY, d.dept_name AS DEPARTMENT,
		CASE WHEN pr.visit_type='I' THEN 'IP' ELSE 'OP' END AS VISITTYPE,
		TO_CHAR(pr.reg_date, 'dd-MM-yyyy') AS CLAIMDATE, CASE WHEN pr.visit_type='I' THEN 'IP' ELSE 'OP' END AS CLAIMTYPE,
		''::character varying AS MAINCLAIMREFNO, ''::character varying AS ELIGREFNO, ''::character varying AS APPREFNO,
		TO_CHAR(adm.admit_date, 'dd-MM-yyyy') AS ADMISSIONDATE, adm.admit_time::time AS ADMISSIONTIME,
		TO_CHAR(pr.discharge_date, 'dd-MM-yyyy') AS DISCHARGEDATE, pr.discharge_time AS DISCHARGETIME, ''::character varying AS LENGTHOFSTAY,
		''::character varying AS ROOMNO,
		''::character varying AS BEDNO, pr.complaint AS MAINSYMPTOM, ''::character varying AS SIGNIFICANTSIGN,
		''::character varying AS OTHERCOND, ''::character varying AS DURATIONOFILLNESS, ''::character varying AS UNITOFDURATION,
		''::character varying AS TEMPERATURE, ''::character varying AS BLOODPRESSURE, ''::character varying AS PULSE, ''::character varying AS RESPIRATORYRATE,
		''::character varying AS WEIGH,TO_CHAR(now()::date, 'dd-MM-yyyy') AS LASTMENSTRUATIONPERIOD,
		WICV.TOTCLAIMGRSAMT, WICV.TOTCLAIMNETAMT, WICV.TOTCLAIMDISC, WICV.TOTCLAIMPATSHARE,''::text AS RADIOREPORT,
		''::text AS COMMREPORT

			FROM wsl_ins_claim_amts_view wicv
	 		JOIN patient_registration pr ON (wicv.visit_id = pr.patient_id)
			JOIN patient_details pd ON(pr.mr_no = pd.mr_no)
			JOIN insurance_case ic ON(ic.insurance_id = pr.insurance_id)
			JOIN tpa_master tpa ON(ic.tpa_id = ic.tpa_id)
			JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id)
			JOIN insurance_plan_main ipm ON (pr.plan_id = ipm.plan_id)
			JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id
			JOIN insurance_claim icl ON (wicv.claim_id = icl.claim_id)
			JOIN account_group_master agc ON(wicv.account_group=agc.account_group_id)
			JOIN insurance_company_master icm ON(icm.insurance_co_id = pr.primary_insurance_co)
			LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN doctors dr ON (dr.doctor_id = pr.doctor)
			LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)
			LEFT JOIN department d ON (pr.dept_name = d.dept_id);

DROP VIEW IF EXISTS wsl_diagnosis_details CASCADE ;
CREATE OR REPLACE VIEW wsl_diagnosis_details AS
	SELECT mrd.icd_code AS DIAGNOSISCODE, description AS DIAGNOSISDESC, diag_type AS DIAGNOSISTYPE,
		pr.mr_no || to_char(pr.reg_date, 'dd-MM-yyyy')|| dr.doctor_id AS PROVCLAIMNO /*FOREIGN KEY*/
			FROM mrd_diagnosis mrd
			JOIN patient_registration pr ON (mrd.visit_id = pr.patient_id)
			JOIN doctors dr ON (dr.doctor_id = pr.doctor);


DROP VIEW IF EXISTS wsl_invoices CASCADE ;
CREATE OR REPLACE VIEW wsl_invoices AS
	SELECT  b.bill_no AS INVOICENO,/*(PRIMARY KEY)*/
		b.total_amount AS TOTINVGRSAMT, b.total_discount AS TOTINVDISC,
		b.total_amount-b.total_claim-b.total_discount AS TOTINVPATSHARE, b.total_claim-b.total_claim_return AS TOTINVNETAMT,
		pd.mr_no || to_char(pr.reg_date, 'dd-MM-yyyy')|| dr.doctor_id AS PROVCLAIMNO, /*(FOREIGN KEY)*/
		d.dept_name AS INVOICEDEPT, to_char(b.open_date::date, 'dd-MM-yyyy') AS INVOICEDATE

			FROM bill b
			JOIN patient_registration pr ON (b.visit_id = pr.patient_id)
			JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
			JOIN doctors dr ON (dr.doctor_id = pr.doctor)
			JOIN department d ON (pr.admitted_dept = d.dept_id);


DROP VIEW IF EXISTS WSL_SERVICE_DETAILS CASCADE ;
CREATE OR REPLACE VIEW WSL_SERVICE_DETAILS AS
	SELECT bc.act_rate_plan_item_code AS SERVICECODE,
		to_char(bc.posted_date, 'dd-MM-yyyy') AS SERVICEDATE,bc.act_description AS SERVICEDESC,
		bc.act_rate AS UNITSERVICEPRICE, ''::character varying AS UNITSERVICETYPE, bc.act_quantity AS QTY,
		''::character varying AS TOOTHNO, bc.amount AS TOTSERVICEGRSAMT, bc.discount AS TOTSERVICEDISC,
		bc.amount-bc.insurance_claim_amount AS TOTSERVICEPATSHARE,
		bc.insurance_claim_amount AS TOTSERVICENETAMT, b.bill_no AS INVOICENO, ''::character varying AS HD_ACTIVITY_TYPE,
		''::character varying AS HD_ACTIVITY_CLINICIAN

			FROM bill b
			JOIN bill_charge bc ON(b.bill_no=bc.bill_no);


DROP VIEW IF EXISTS wsl_lab_result CASCADE ;
CREATE OR REPLACE view wsl_lab_result AS
	SELECT  td.test_id AS LABCODE, to_char(tc.conducted_date, 'dd-MM-yyyy') AS LABTESTDATE, '1'::integer AS SERIAL,
		pr.mr_no || to_char(pr.reg_date, 'dd-MM-yyyy') || dr.doctor_id AS PROVCLAIMNO, d.test_name AS LABDESC
			FROM test_details td
			JOIN tests_conducted tc ON(td.patient_id = tc.patient_id)
			JOIN insurance_claim  ic ON(ic.patient_id=tc.patient_id)
			JOIN diagnostics d ON (d.test_id = td.test_id)
			JOIN patient_registration pr ON (pr.patient_id=ic.patient_id)
			LEFT JOIN doctors dr ON (dr.doctor_id = pr.doctor);


DROP VIEW IF EXISTS WSL_LAB_COMP CASCADE ;
CREATE OR REPLACE VIEW WSL_LAB_COMP AS
	SELECT  tr.result_code AS LABCOMPCODE, ''::character varying AS LABCOMPDESC, td.report_value AS LABRESULT, td.units AS LABRESULTUNIT,
		td.test_id AS LABCODE, td.comments AS LABRESULTCOMMENT,'1'::integer AS SERIAL,
		pr.mr_no || to_char(pr.reg_date, 'dd-MM-yyyy') || dr.doctor_id AS provclaimno
			FROM test_details td
			JOIN patient_registration pr ON (td.patient_id=pr.patient_id)
			JOIN test_results_master tr ON (td.test_id = tr.test_id)
			JOIN doctors dr ON (dr.doctor_id = pr.doctor);
