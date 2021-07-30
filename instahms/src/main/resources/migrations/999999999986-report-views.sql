-- liquibase formatted sql
-- changeset adityabhatia02:create-report-views.sql runAlways:true
-- validCheckSum: ANY
--
-- Details of a patient (mr_no)
-- To be used in conjunction with PatientDetailFields.srxml as an include.
SET client_min_messages = warning;

DROP VIEW IF EXISTS patient_details_fields_view CASCADE;
DROP VIEW IF EXISTS appointment_details_fields_view CASCADE;
DROP VIEW IF EXISTS rpt_doctor_appointment_details_view CASCADE;
DROP VIEW IF EXISTS rpt_tests_appointment_details_view CASCADE;
DROP VIEW IF EXISTS rpt_services_appointment_details_view CASCADE;
DROP VIEW IF EXISTS rpt_surgery_appointment_details_view CASCADE;
DROP VIEW IF EXISTS rpt_patient_details_view CASCADE;
DROP VIEW IF EXISTS rpt_bill_view CASCADE;
DROP VIEW IF EXISTS rpt_bill_charge_view CASCADE;
DROP VIEW IF EXISTS rpt_revenue_report_view CASCADE;
DROP VIEW IF EXISTS rpt_diagnostics_revenue_view CASCADE;
DROP VIEW IF EXISTS rpt_deposits_view CASCADE;
DROP VIEW IF EXISTS rpt_deposit_receipts_view CASCADE;

--
-- Used by BedOccupancyBuilder.srxml --
--

DROP VIEW IF EXISTS rpt_bed_occupancy_report_view CASCADE;
CREATE VIEW rpt_bed_occupancy_report_view AS
SELECT bsr.mr_no, bsr.patient_id, bsr.patient_name, bsr.age  AS age,
	CASE WHEN bsr.patient_gender = 'M' THEN 'Male'
		WHEN bsr.patient_gender = 'F' THEN 'Female'
		WHEN bsr.patient_gender = 'C' THEN 'Couple'
		WHEN bsr.patient_gender = 'O' THEN 'Others'
		ELSE ''
	END AS patient_gender , bsr.doctor_name AS doctor_name, bsr.bed_type, bsr.ward_name as ward_name,
	COALESCE(bsr.bed_name,'Not Allocated') as bed_name, bsr.reg_date as admission_date,
	to_char(bsr.reg_time,'HH:MI AM') as admission_time,sum(b.total_receipts) as deposit,
	to_char(bsr.reg_date,'dd-mm-yyyy') || ' ' ||to_char(bsr.reg_time,'HH:MI AM') as admission_datetime,
	bsr.primary_tpa_name, bsr.secondary_tpa_name, bsr.plan_name, bsr.primary_insurance_co_name, bsr.secondary_insurance_co_name,
	bsr.plan_type,bsr.discharge_date as discharge_date,
	CASE WHEN bsr.mlc_status='Y' THEN 'MLC' ELSE null END AS mlc_status,
	sum(b.approval_amount) as approval_amount,to_char(bsr.discharge_time,'HH:MI AM') as discharge_time,
	to_char(bsr.discharge_date,'dd-mm-yyyy') || ' ' ||to_char(bsr.discharge_time,'HH:MI AM')
	as discharge_datetime, DATE( pd.death_date) AS death_date,
	(CASE WHEN bsr.status = 'R' THEN 'Retained'
		WHEN bsr.status = 'C' THEN 'Current'
		WHEN bsr.status = 'A' then 'Current'
  	ELSE 'Vacant' END) as occupied_status, case when bsr.occupancy = 'N' then 1 else 0  end as vacant,
	CASE WHEN bsr.occupancy = 'Y' THEN 1 ELSE 0 END AS occupied,
	CASE WHEN bsr.status = 'R' THEN 1 ELSE 0 END AS retained,
	CASE WHEN bsr.occupancy = 'Y' THEN 'Occupied' ELSE 'Vacant' END AS bed_status,
	CASE WHEN pd.visit_id IS NOT NULL AND  pd.visit_id!='' THEN 'Active' ELSE 'Inactive' END AS status,
	TO_CHAR( pd.death_time, 'hh24:mi') AS death_time, pd.email_id, pc.category_name, pd.previous_visit_id,
	CASE WHEN pd.user_name='' THEN null
		WHEN pd.user_name is null THEN null
		ELSE pd.user_name
	END AS user_name,
	pd.patient_address, pd.patient_phone,
	pd.custom_list5_value as religion,pd.custom_list6_value as occupation, pd.custom_list4_value as bloodgroup,
	pd.first_visit_reg_date AS first_visit_date,
	pd.relation, pd.patient_careof_address, pd.patient_care_oftext, pd.casefile_no, drm.reason as death_reason,
  pd.name_local_language,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5,
	pd.custom_field6, pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10,
	pd.custom_field11, pd.custom_field12, pd.custom_field13,pd.custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19, pd.oldmrno,
	pd.custom_list1_value,pd.custom_list2_value,pd.custom_list3_value,pd.custom_list4_value,
	pd.custom_list5_value,pd.custom_list6_value,pd.custom_list7_value,pd.custom_list8_value,
	pd.custom_list9_value,
	TO_CHAR(( pd.death_date+ pd.death_time), 'dd-MM-yyyy hh24:mi') as death_datetime, pd.timeofbirth,
	pd.patient_category_id, pd.remarks,
	CASE WHEN pd.patient_area = '' OR pd.patient_area IS NULL THEN NULL
		ELSE pd.patient_area
	END AS patient_area,
	c.city_name, cm.country_name, stm.state_name, pd.patient_phone2, pd.family_id,
	CASE WHEN bsr.billing_status='F' THEN 'Finalized' ELSE 'Open' END as billing_status,hcm.center_name
FROM bed_status_report bsr
	LEFT JOIN BILL B ON (B.VISIT_ID = bsr.PATIENT_ID AND B.restriction_type = 'N' and b.bill_type='C')
	LEFT JOIN patient_registration pr USING(patient_id)
	LEFT JOIN patient_details pd on bsr.mr_no = pd.mr_no
	LEFT JOIN city c ON c.city_id = pd.patient_city
	LEFT JOIN country_master cm ON pd.country = cm.country_id
	LEFT JOIN state_master stm ON pd.patient_state = stm.state_id
	LEFT JOIN salutation_master sm ON  sm.salutation_id = pd.salutation
	LEFT JOIN patient_category_master  pc on(pd.patient_category_id = pc.category_id)
	LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)
	LEFT JOIN hospital_center_master hcm ON ( pr.center_id = hcm.center_id )
GROUP BY bsr.mr_no,bsr.patient_id, bsr.patient_name,  bsr.patient_gender,bsr.doctor_name , bsr.bed_type,
	bsr.bed_name, bsr.ward_name,bsr.reg_date,bsr.reg_time,bsr.age,bsr.primary_tpa_name,bsr.secondary_tpa_name,
	bsr.plan_name,bsr.plan_type,bsr.primary_insurance_co_name,bsr.secondary_insurance_co_name,
	bsr.refdoctorname,bsr.mlc_status,bsr.discharge_date,bsr.discharge_time,
	bsr.status, bsr.occupancy, pd.visit_id , death_date, pd.death_time,pd.user_name, pd.email_id,
	pc.category_name, pd.previous_visit_id, pd.patient_address, pd.patient_phone,
	religion, occupation, pd.custom_list4_value, pd.first_visit_reg_date, pd.relation,
	pd.patient_careof_address,	pd.patient_care_oftext,pd.casefile_no,  pd.custom_field1, pd.custom_field2,
	pd.custom_field3, pd.custom_field4, pd.custom_field5, pd.custom_field6, pd.custom_field7,
	pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12, pd.custom_field13,
	pd.custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19,
	pd.death_date, pd.death_time,drm.reason, pd.oldmrno,
	pd.timeofbirth, pd.patient_category_id, pd.remarks,pd.patient_area, c.city_name, cm.country_name,stm.state_name,
	pd.patient_phone2, pd.family_id, bsr.billing_status,
	pd.custom_list1_value,pd.custom_list2_value,pd.custom_list3_value,custom_list4_value,pd.name_local_language,
	pd.custom_list5_value,pd.custom_list6_value,pd.custom_list7_value,pd.custom_list8_value,pd.custom_list9_value,hcm.center_name;


DROP VIEW IF EXISTS rpt_birth_details_view CASCADE;

--
-- Used by CaseFile.srxml --
--
DROP VIEW IF EXISTS rpt_case_file_view CASCADE;
CREATE VIEW rpt_case_file_view AS
SELECT mca.mr_no, get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_name,
    CASE WHEN pd.patient_gender = 'M' THEN 'Male'
    	WHEN pd.patient_gender = 'F' THEN 'Female'
    	WHEN pd.patient_gender = 'C' THEN 'Couple'
        ELSE 'Others'
	END AS patient_gender,
    date(mca.issued_on) AS issued_on,
    to_char(EXTRACT(hours FROM mca.issued_on),'00') || ':'
    			||to_char(EXTRACT(minutes FROM mca.issued_on),'00') as issued_time,
    date(mca.returned_on) AS returned_on,
    to_char(EXTRACT(hours FROM mca.returned_on),'00') || ':'
    			||to_char(EXTRACT(minutes FROM mca.returned_on),'00') as returned_time,
    COALESCE(dep.dept_name, mcu.file_user_name) as issued_to,
    CASE WHEN file_status='U' THEN 'Issued'
    	WHEN file_status='L' THEN 'Lost'
        ELSE
			CASE WHEN indented ='Y' THEN 'Indented'
				ELSE CASE WHEN returned_on is null THEN 'Available with MRD' ELSE 'Returned' END
			END
	END as file_status,
    CASE WHEN case_status='A' THEN 'Active'
    	WHEN case_status='I' THEN 'Inactive'
        ELSE 'Pending'
    END as case_status,casefile_no, to_char(created_date,'dd-MM-yyyy') as created_date,
    date(request_date) as indented_date, rdep.dept_name as indented_dept,
    to_char(EXTRACT(hours FROM mca.request_date),'00') || ':'
    			||to_char(EXTRACT(minutes FROM mca.request_date),'00') as indented_time,
    COALESCE(date(issued_on), date(request_date), date(returned_on), date(created_date)) as transaction_date,
	v.visit_type_name as visit_type, pr.op_type, otn.op_type_name,
	CASE WHEN pr.revisit='Y' THEN 'Revisit' ELSE 'First Visit' END AS revisit_flag,
	pd.family_id, pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field6,
	pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12,
	pd.custom_field13, pd.custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19,pd.name_local_language,
	pd.custom_list1_value, pd.custom_list2_value,pd.custom_list3_value,pd.custom_list4_value,
	pd.custom_list5_value,pd.custom_list6_value,pd.custom_list7_value,pd.custom_list8_value,pd.custom_list9_value,
	(SELECT d.doctor_name FROM doctor_consultation dc JOIN doctors d ON (dc.doctor_name=d.doctor_id)
			WHERE dc.patient_id =pr.patient_id ORDER BY consultation_id limit 1) AS consulting_doctor,
	(SELECT center_name FROM hospital_center_master hcm WHERE hcm.center_id = pr.center_id ) AS visit_center_name,prcm.category_name as patient_category_name
FROM mrd_casefile_attributes mca
	LEFT JOIN patient_details pd on mca.mr_no = pd.mr_no
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN patient_registration pr on (pr.patient_id = coalesce(pd.visit_id, pd.previous_visit_id))
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN visit_type_names v on (pr.visit_type=v.visit_type)
	LEFT JOIN department dep on (dep.dept_id=mca.issued_to_dept)
	LEFT JOIN department rdep on (rdep.dept_id=mca.requesting_dept)
	LEFT JOIN mrd_casefile_users mcu on (mcu.file_user_id = mca.issued_to_user)
	LEFT JOIN patient_category_master prcm ON (prcm.category_id = pr.patient_category_id) 
ORDER BY mr_no ;

--
-- Used by Codification.srxml --
--
DROP VIEW IF EXISTS rpt_codification_view CASCADE;
CREATE VIEW rpt_codification_view AS
SELECT *, male+female+couple AS total
FROM
	(SELECT p.*,code_desc, mcm.code_type AS procedure_code_type,md.code_type AS diagnosis_code_type,act_rate_plan_item_code,
		md.description as diag_description,md.icd_code as diag_code,
		CASE WHEN patient_gender = 'M' THEN 1.0 ELSE 0 END AS male,
		CASE WHEN patient_gender = 'F' THEN 1.0 ELSE 0 END AS female,
		CASE WHEN patient_gender = 'C' THEN 1.0 ELSE 0 END AS couple, date(posted_date) as posted_date,
		vtn.visit_type_name,r.patient_id AS pat_id
	FROM  bill_charge bc JOIN bill b USING (bill_no)
		JOIN chargehead_constants cc ON (cc.chargehead_id = bc.charge_head)
		JOIN patient_registration r ON (r.patient_id = b.visit_id)
		JOIN patient_details p  ON (p.mr_no = r.mr_no)
		join mrd_diagnosis md ON(b.visit_id = md.visit_id AND diag_type = 'P')
		JOIN hospital_center_master hcm ON (hcm.center_id=r.center_id)
		JOIN health_authority_preferences hap ON(hap.health_authority=hcm.health_authority)
		LEFT JOIN visit_type_names vtn ON vtn.visit_type = r.visit_type
		JOIN (
			SELECT code,code_type,code_desc
				FROM mrd_codes_master
				where code_type!='Drug' AND code_type!='Encounter Start'
				AND code_type!='Encounter End' AND code_type!='Encounter Type'
			UNION
			SELECT item_code, code_type, null
				FROM store_item_codes
				WHERE item_code IS NOT NULL AND item_code!=''
			UNION
			SELECT code,code_type,code_desc
				FROM encounter_start_types
			UNION
			SELECT code,code_type,code_desc
				FROM encounter_end_types
			UNION
			SELECT encounter_type_id::varchar,code_type,encounter_type_desc
				FROM encounter_type_codes
		) as mcm ON (mcm.code_type = bc.code_type
				AND mcm.code = bc.act_rate_plan_item_code)
		WHERE codification_supported != 'N' AND (mcm.code_type=hap.drug_code_type OR mcm.code_type = bc.code_type)
		ORDER BY act_rate_plan_item_code) foo ;


DROP VIEW IF EXISTS rpt_collection_details_view CASCADE;

--
-- Used by CollectionAllocation.srxml
--
DROP VIEW IF EXISTS rpt_collection_allocation_view CASCADE;
CREATE OR REPLACE VIEW rpt_collection_allocation_view AS
SELECT
  	CASE WHEN rrr.refund_receipt_id IS NOT NULL AND rpt.receipt_type = 'R' THEN 'Refund'
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type,
	-- to separate refund allocations from the receipt allocation.
	CASE WHEN bcra.allocated_amount<0 AND rpt.receipt_type = 'R' THEN 'Refund'  
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' 
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type_with_refund,

	COALESCE(rrr.refund_receipt_id, r.receipt_no) AS receipt_no, r.bill_no, 
	CASE WHEN bcra.allocation_id IS NOT NULL THEN date(bcra.modified_at) ELSE date(r.display_date) END as receipt_date, 
	date(bcra.modified_at) as allocated_date, 
	0.00 as unallocated_amount,

	c.counter_no as counter,
	CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,

	pm.payment_mode, ctm.card_type, rpt.bank_name, rpt.reference_no,
	rpt.bank_batch_no, rpt.card_auth_code, rpt.card_holder_name, rpt.card_exp_date, rpt.card_exp_date AS card_expdate, rpt.card_number,
	fc.currency, rpt.exchange_rate, rpt.exchange_date, rpt.currency_amt,
	r.username, rpt.remarks,rpt.credit_card_commission_amount, rpt.credit_card_commission_percentage,
	COALESCE(
		CASE WHEN bc.charge_id IS NULL THEN rpt.amount
			WHEN b.total_amount != 0 AND rpt.amount = rpt.unallocated_amount AND rpt.receipt_type != 'F' THEN bc.amount*rpt.amount/b.total_amount
			WHEN rpt.receipt_type = 'F' THEN bcra.allocated_amount
			ELSE bcra.allocated_amount END,0) as amt,
	0.00 AS refund_allocated_amt,
	CASE WHEN bc.charge_id IS NULL AND rpt.receipt_type = 'R' THEN
		CASE WHEN NOT rpt.is_settlement THEN 'Advance' 
			 WHEN rpt.is_settlement THEN 'Settlement' END
 	WHEN rpt.receipt_type = 'F' THEN 'Refund'
	ELSE chc.chargehead_name END AS head,

	chc.chargehead_name, cgc.chargegroup_name, bahc.account_head_name as ac_head,
	bcagm.account_group_name AS charge_account_group,
	sg.service_group_name, ssg.service_sub_group_name,
	bc.act_description, bc.act_remarks, bc.user_remarks,
	tdep.dept_name AS treating_dept,

	CASE WHEN b.bill_type='P' THEN 'Bill Now' ELSE 'Bill Later' END as bill_type,
	date(b.open_date) as bill_open_date, date(b.finalized_date) as bill_finalized_date,
	date(b.closed_date) as bill_closed_date,
	CASE WHEN b.restriction_type = 'T' THEN 'Test'
		WHEN b.restriction_type = 'P' THEN 'Pharmacy'
		ELSE 'Hospital'
	END AS bill_usage_type,
	CASE WHEN b.status = 'A' THEN 'Open'
		WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'C' THEN 'Closed'
	ELSE 'Canceled' END AS bill_status,

	CASE WHEN b.is_tpa THEN 'Yes' ELSE 'No' END AS insurance_bill,
	CASE WHEN b.is_tpa THEN ptm.tpa_name ELSE null END AS primary_bill_tpa,
	CASE WHEN b.is_tpa THEN stm.tpa_name ELSE null END AS secondary_bill_tpa,

	b.visit_id, vtn.visit_type_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
		|| case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end,
	prc.customer_name, isr.patient_name) AS bill_patient_full_name,prcm.category_name as patient_category_name, 
	bc.charge_id, bc.amount as charge_amount, bc.act_rate, bcra.allocation_id, bc.act_rate_plan_item_code, pd.name_local_language
	FROM receipts rpt 
	JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no AND rpt.amount>0)
	JOIN bill b USING (bill_no)
	JOIN counters c ON (rpt.counter = c.counter_id)
	JOIN visit_type_names vtn ON (b.visit_type = vtn.visit_type)
	JOIN payment_mode_master pm ON (rpt.payment_mode_id = pm.mode_id)
	JOIN bill_charge bc ON (bc.bill_no = b.bill_no)	
	LEFT JOIN bill_charge_receipt_allocation bcra ON (
		bcra.bill_receipt_id = r.bill_receipt_id
			AND bc.charge_id = bcra.charge_id AND 
			(  'a' = COALESCE(bcra.activity, 'a')))
 	LEFT JOIN receipt_refund_reference rrr on bcra.refund_reference_id = rrr.id
 	LEFT JOIN card_type_master ctm ON (rpt.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN account_group_master bcagm  ON (bcagm.account_group_id = bc.account_group)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN patient_category_master prcm ON (prcm.category_id = pr.patient_category_id) 
	WHERE NOT rpt.is_deposit AND rpt.payment_mode_id<>-9 AND bcra.allocated_amount <> 0 
	
	UNION ALL
	
	SELECT
  	CASE WHEN rrr.refund_receipt_id IS NOT NULL AND rpt.receipt_type = 'R' THEN 'Refund'
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type,
	-- to separate refund allocations from the receipt allocation.
	CASE WHEN bcra.allocated_amount<0 AND rpt.receipt_type = 'R' THEN 'Refund'  
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' 
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type_with_refund,

	COALESCE(rrr.refund_receipt_id, r.receipt_no) AS receipt_no, r.bill_no,CASE WHEN bcra.allocation_id IS NOT NULL THEN date(bcra.modified_at) ELSE date(r.display_date) END as receipt_date, 
	date(bcra.modified_at) as allocated_date, 
	0.00 as unallocated_amount,

	c.counter_no as counter,
	CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,

	pm.payment_mode, ctm.card_type, rpt.bank_name, rpt.reference_no,
	rpt.bank_batch_no, rpt.card_auth_code, rpt.card_holder_name, rpt.card_exp_date, rpt.card_exp_date AS card_expdate, rpt.card_number,
	fc.currency, rpt.exchange_rate, rpt.exchange_date, rpt.currency_amt,
	r.username, rpt.remarks,rpt.credit_card_commission_amount, rpt.credit_card_commission_percentage,

	COALESCE(
		CASE WHEN bc.charge_id IS NULL THEN rpt.amount
			WHEN b.total_amount != 0 AND rpt.amount = rpt.unallocated_amount AND rpt.receipt_type != 'F' THEN bc.amount*rpt.amount/b.total_amount
			WHEN rpt.receipt_type = 'F' THEN bcra.allocated_amount
			ELSE bcra.allocated_amount END,0) as amt,
	0.00 AS refund_allocated_amt,
	CASE WHEN bc.charge_id IS NULL AND rpt.receipt_type = 'R' THEN
		CASE WHEN NOT rpt.is_settlement THEN 'Advance' 
			 WHEN rpt.is_settlement THEN 'Settlement' END
 	WHEN rpt.receipt_type = 'F' THEN 'Refund'
	ELSE chc.chargehead_name END AS head,

	chc.chargehead_name, cgc.chargegroup_name, bahc.account_head_name as ac_head,
	bcagm.account_group_name AS charge_account_group,
	sg.service_group_name, ssg.service_sub_group_name,
	bc.act_description, bc.act_remarks, bc.user_remarks,
	tdep.dept_name AS treating_dept,

	CASE WHEN b.bill_type='P' THEN 'Bill Now' ELSE 'Bill Later' END as bill_type,
	date(b.open_date) as bill_open_date, date(b.finalized_date) as bill_finalized_date,
	date(b.closed_date) as bill_closed_date,
	CASE WHEN b.restriction_type = 'T' THEN 'Test'
		WHEN b.restriction_type = 'P' THEN 'Pharmacy'
		ELSE 'Hospital'
	END AS bill_usage_type,
	CASE WHEN b.status = 'A' THEN 'Open'
		WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'C' THEN 'Closed'
	ELSE 'Canceled' END AS bill_status,

	CASE WHEN b.is_tpa THEN 'Yes' ELSE 'No' END AS insurance_bill,
	CASE WHEN b.is_tpa THEN ptm.tpa_name ELSE null END AS primary_bill_tpa,
	CASE WHEN b.is_tpa THEN stm.tpa_name ELSE null END AS secondary_bill_tpa,

	b.visit_id, vtn.visit_type_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
		|| case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end,
	prc.customer_name, isr.patient_name) AS bill_patient_full_name, 
	prcm.category_name as patient_category_name, bc.charge_id, bc.amount as charge_amount, 
	bc.act_rate, bcra.allocation_id, bc.act_rate_plan_item_code,pd.name_local_language
	FROM receipts rpt 
	JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no AND rpt.amount>0)
	JOIN bill b USING (bill_no)
	JOIN counters c ON (rpt.counter = c.counter_id)
	JOIN visit_type_names vtn ON (b.visit_type = vtn.visit_type)
	JOIN payment_mode_master pm ON (rpt.payment_mode_id = pm.mode_id)
	JOIN bill_charge bc ON (bc.bill_no = b.bill_no)	
	LEFT JOIN bill_charge_receipt_allocation bcra ON (
		bcra.bill_receipt_id = r.bill_receipt_id
			AND bc.charge_id = bcra.charge_id AND 
			(bcra.refund_reference_id>0 AND  bcra.activity = 'c') )
	LEFT JOIN receipt_refund_reference rrr on bcra.refund_reference_id = rrr.id
	LEFT JOIN card_type_master ctm ON (rpt.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN account_group_master bcagm  ON (bcagm.account_group_id = bc.account_group)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN patient_category_master prcm ON (prcm.category_id = pr.patient_category_id) 
	WHERE NOT rpt.is_deposit AND rpt.payment_mode_id<>-9 AND bcra.allocated_amount <> 0 
	
	UNION ALL 
	-- Sale returns
	SELECT
  	CASE WHEN rrr.refund_receipt_id IS NOT NULL AND rpt.receipt_type = 'R' THEN 'Refund'
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type,
	-- to separate refund allocations from the receipt allocation.
	CASE WHEN bcra.allocated_amount<0 AND rpt.receipt_type = 'R' THEN 'Refund'  
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS null THEN 'Receipt' 
  		WHEN rpt.receipt_type = 'R' AND rpt.tpa_id IS NOT null THEN 'Sponsor Receipt'
		ELSE 'Refund' END as payment_type_with_refund,

	COALESCE(rrr.refund_receipt_id, r.receipt_no) AS receipt_no, r.bill_no,CASE WHEN bcra.allocation_id IS NOT NULL THEN date(bcra.modified_at) ELSE date(r.display_date) END as receipt_date, 
	date(bcra.modified_at) as allocated_date, 
	0.00 as unallocated_amount,

	c.counter_no as counter,
	CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,

	pm.payment_mode, ctm.card_type, rpt.bank_name, rpt.reference_no,
	rpt.bank_batch_no, rpt.card_auth_code, rpt.card_holder_name, rpt.card_exp_date, rpt.card_exp_date AS card_expdate, rpt.card_number,
	fc.currency, rpt.exchange_rate, rpt.exchange_date, rpt.currency_amt,
	r.username, rpt.remarks,rpt.credit_card_commission_amount, rpt.credit_card_commission_percentage,

	COALESCE(
		CASE WHEN bc.charge_id IS NULL THEN rpt.amount
			WHEN b.total_amount != 0 AND rpt.amount = rpt.unallocated_amount AND rpt.receipt_type != 'F' THEN bc.amount*rpt.amount/b.total_amount
			WHEN rpt.receipt_type = 'F' THEN bcra.allocated_amount
			ELSE bcra.allocated_amount END,0) as amt,
	0.00 AS refund_allocated_amt,
	CASE WHEN bc.charge_id IS NULL AND rpt.receipt_type = 'R' THEN
		CASE WHEN NOT rpt.is_settlement THEN 'Advance' 
			 WHEN rpt.is_settlement THEN 'Settlement' END
 	WHEN rpt.receipt_type = 'F' THEN 'Pharmacy Return'
	ELSE chc.chargehead_name END AS head,

	chc.chargehead_name, cgc.chargegroup_name, bahc.account_head_name as ac_head,
	bcagm.account_group_name AS charge_account_group,
	sg.service_group_name, ssg.service_sub_group_name,
	bc.act_description, bc.act_remarks, bc.user_remarks,
	tdep.dept_name AS treating_dept,

	CASE WHEN b.bill_type='P' THEN 'Bill Now' ELSE 'Bill Later' END as bill_type,
	date(b.open_date) as bill_open_date, date(b.finalized_date) as bill_finalized_date,
	date(b.closed_date) as bill_closed_date,
	CASE WHEN b.restriction_type = 'T' THEN 'Test'
		WHEN b.restriction_type = 'P' THEN 'Pharmacy'
		ELSE 'Hospital'
	END AS bill_usage_type,
	CASE WHEN b.status = 'A' THEN 'Open'
		WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'C' THEN 'Closed'
	ELSE 'Canceled' END AS bill_status,

	CASE WHEN b.is_tpa THEN 'Yes' ELSE 'No' END AS insurance_bill,
	CASE WHEN b.is_tpa THEN ptm.tpa_name ELSE null END AS primary_bill_tpa,
	CASE WHEN b.is_tpa THEN stm.tpa_name ELSE null END AS secondary_bill_tpa,

	b.visit_id, vtn.visit_type_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
		|| case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end,
	prc.customer_name, isr.patient_name) AS bill_patient_full_name, 
	prcm.category_name as patient_category_name, bc.charge_id, bc.amount as charge_amount, 
	bc.act_rate, bcra.allocation_id, bc.act_rate_plan_item_code,pd.name_local_language
	FROM receipts rpt 
	JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no AND rpt.amount<0 )
	JOIN bill b USING (bill_no)
    JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no)
	JOIN counters c ON (rpt.counter = c.counter_id)
	JOIN visit_type_names vtn ON (b.visit_type = vtn.visit_type)
	JOIN payment_mode_master pm ON (rpt.payment_mode_id = pm.mode_id)
	JOIN bill_charge bc ON (bc.bill_no = b.bill_no )
	LEFT JOIN bill_charge_receipt_allocation bcra ON (
		bcra.bill_receipt_id = r.bill_receipt_id
			AND bc.charge_id = bcra.charge_id )
	LEFT JOIN receipt_refund_reference rrr on bcra.refund_reference_id = rrr.id
	LEFT JOIN card_type_master ctm ON (rpt.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN account_group_master bcagm  ON (bcagm.account_group_id = bc.account_group)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN patient_category_master prcm ON (prcm.category_id = pr.patient_category_id) 
	WHERE rpt.is_deposit='f' and rpt.payment_mode_id<>-9 AND bcra.allocated_amount <> 0
	;





--
-- Used by DayBook.srxml --
--
DROP VIEW IF EXISTS rpt_day_book_view CASCADE;
CREATE VIEW rpt_day_book_view AS
SELECT main_type, sub_type, payment_mode as paymentmode, card_type, bank_name, reference_no, receipt_no, bill_no,
	CASE WHEN main_type = 'Consolidated Claim Receipts' THEN 'Consolidated Claim Receipts'
		 WHEN main_type = 'Consolidated Sponsor Receipts' THEN 'Consolidated Sponsor Receipts'
		 WHEN main_type = 'Sponsor Receipts' THEN 'Sponsor Receipts'
		 WHEN main_type ='Bill Refunds' THEN 'Refund'
		 WHEN main_type = 'Bill Receipts'
				THEN CASE WHEN sub_type='F'
							THEN CASE WHEN bill_type='C' THEN  'Bill Later' ELSE 'Bill Now' END
						  WHEN bill_type='C'
							THEN CASE WHEN sub_type='A' THEN 'Advance' ELSE 'Settlement' END
						ELSE 'Bill Now'
					 END
		 WHEN  main_type = 'Deposits' THEN
					CASE WHEN sub_type='R' THEN 'Deposit Receipts' ELSE 'Deposit Returns' END
		 WHEN main_type ='Payments' THEN
					CASE WHEN sub_type='D' THEN 'Doctor Payments'
						 WHEN sub_type='R' THEN 'Referral Payments'
						 WHEN sub_type='F' THEN 'Referral Payments'
						 WHEN sub_type='P' THEN 'Presc. Dr. Payments'
						 WHEN sub_type='O' THEN 'Outhouse Payments'
						 WHEN sub_type='S' THEN 'Supplier Payments'
						 ELSE 'Misc. Payments'
					END
				ELSE 'Unknown'
	END AS btype,
	vtn.visit_type_name, dv.op_type, otn.op_type_name, mr_no, patname, counter_no AS counter, counter_type, collection_counter,
	DATE(date) AS date, date AS receipt_datetime,
	username, amt, tds_amt,
	bank_batch_no,  card_auth_code, card_holder_name, currency_id, exchange_rate,
	DATE(exchange_date) AS exchange_date, currency_amt, currency, card_number, card_expdate, center_name,
	dv.sponsor_type,dv.patient_category
FROM daybook_view dv
	LEFT JOIN visit_type_names vtn ON dv.visittype = vtn.visit_type
	LEFT JOIN op_type_names otn ON (otn.op_type = dv.op_type);

--
--Used by Diagnostics.srxml --
-- (todo: convert this to include patient detail fields. Watch out for isr.*, though)
--
DROP VIEW IF EXISTS rpt_diagnostics_view CASCADE;
CREATE VIEW rpt_diagnostics_view AS
SELECT doc.doctor_name AS presc_dr, cdoc.doctor_name AS cond_dr, diag.test_name,  diag.type_of_specimen,
	COALESCE (pd.mr_no, isr.incoming_visit_id) AS cust_id, tp.pres_date as pres_datetime, tp.sample_no,
	DATE(tp.pres_date) AS pres_date , to_char(pres_date,'HH24:MI') AS pres_time, ddep.ddept_name,
	to_char(tc.conducted_date,'DD-MM-YYYY hh24:mi:ss') AS conducted_datetime,
	DATE(tc.conducted_date) AS conducted_date, to_char(conducted_date, 'hh24:mi') AS conducted_time,
	CASE WHEN tp.conducted='N' THEN 'Not Conducted'
		WHEN tp.conducted='C' THEN 'Completed'
		WHEN tp.conducted='X' THEN 'Cancelled'
		WHEN tp.conducted='P' THEN 'In Progress'
		WHEN tp.conducted='U' THEN 'No conduction required'
		WHEN tp.conducted='V' THEN 'Validated'
		WHEN tp.conducted='S' THEN 'Signed off'
		WHEN tp.conducted='RP' THEN 'Revision in progress'
		WHEN tp.conducted='RC' THEN 'Revision Completed'
		WHEN tp.conducted='RV' THEN 'Revision Validated'
		WHEN tp.conducted='RAS' THEN 'Reconducted after signoff'
		WHEN tp.conducted='RBS' THEN 'Reconducted before signoff'
	END AS  conducted,
	CASE WHEN diag.sample_needed='n' THEN 'No' ELSE 'Yes' END AS sample_needed,
	cancelled_by,DATE(tp.cancel_date) AS cancel_date,
	CASE WHEN sflag='0' THEN 'Collection Pending' ELSE 'Collection Completed' END AS sflag, labno,
	CASE WHEN re_conduction='f' THEN 'No' ELSE 'Yes' END AS re_conduction,
	COALESCE(get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name),isr.patient_name)
	AS patient_name,COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	CASE WHEN COALESCE(pd.patient_gender,isr.patient_gender)='M' THEN 'Male'
		WHEN COALESCE(pd.patient_gender,isr.patient_gender)='F' THEN 'Female'
		WHEN COALESCE(pd.patient_gender,isr.patient_gender)='C' THEN 'Couple'
		ELSE 'Others'
	END AS gender,
	CASE WHEN pr.visit_type='i' THEN 'In-Patient' WHEN pr.visit_type='o' THEN 'Out-Patient'
		ELSE 'Incoming Sample'
	END AS patient_type, pr.op_type, otn.op_type_name,
	sc.sample_date AS sample_datetime, DATE(sc.sample_date) AS sample_date, sac.sample_type,
	CASE WHEN ddep.category='DEP_LAB' THEN 'Lab' ELSE 'Radiology'END AS dept_category, tvr.report_name,
	COALESCE(tvr.signed_off,'N') as signed_off, tvr.user_name as report_user, tvr.report_date as report_datetime ,
	CASE WHEN (extract ('day' from report_date-pres_date) > 0 ) THEN
		  to_char(tvr.report_date-tp.pres_date,'DD' ) || ' Days ' ||
		  									to_char(tvr.report_date-tp.pres_date,'HH24:MI:SS')
		ELSE to_char(tvr.report_date-tp.pres_date,'HH24:MI:SS')
	END as  pres_turn_around_time,sc.specimen_condition,
	tvr.handed_over,tvr.handed_over_to,tvr.hand_over_time,DATE(tvr.hand_over_time) as handed_over_date,
	sc.assertion_time,
	CASE WHEN (extract ('day' from assertion_time-sample_date) > 0 ) THEN
		  to_char(assertion_time-sc.sample_date,'DD' ) || ' Days ' ||
								to_char(assertion_time-sc.sample_date,'HH24:MI:SS')
		ELSE to_char(assertion_time-sc.sample_date,'HH24:MI:SS')
	END as  assertion_turn_around_time,
	CASE WHEN (extract ('day' from tvr.report_date-sample_date) > 0 ) THEN
		  to_char(tvr.report_date-sc.sample_date,'DD' ) || ' Days ' ||
								to_char(tvr.report_date-sc.sample_date,'HH24:MI:SS')
		ELSE to_char(tvr.report_date-sc.sample_date,'HH24:MI:SS')
	END as  report_turn_around_time,
	CASE WHEN (extract ('day' from hand_over_time-sample_date) > 0 ) THEN
		  to_char(hand_over_time-sc.sample_date,'DD' ) || ' Days ' ||
								to_char(hand_over_time-sc.sample_date,'HH24:MI:SS')
		ELSE to_char(hand_over_time-sc.sample_date,'HH24:MI:SS')
	END as  handover_turn_around_time,
	CASE WHEN (extract ('day' from conducted_date-sample_date) > 0 ) THEN
		  to_char(conducted_date-sc.sample_date,'DD' ) || ' Days ' ||
								to_char(conducted_date-sc.sample_date,'HH24:MI:SS')
		ELSE to_char(conducted_date-sc.sample_date,'HH24:MI:SS')
	END as  conduction_turn_around_time,ss.source_name,sc.handover_by,sc.received_by,
	hcm.center_name,scc.collection_center,
	CASE WHEN
		exists (select * from test_details td
					where original_test_details_id != 0 and td.prescribed_id = tp.prescribed_id)
		THEN 'Yes'
		ELSE 'No'
	 END  as hasamended

FROM tests_prescribed tp
	LEFT JOIN tests_conducted tc ON (tp.prescribed_id = tc.prescribed_id)
	LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
	JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
	LEFT JOIN patient_details pd ON (tp.mr_no=pd.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
	LEFT JOIN hospital_center_master hcm ON (pr.center_id = hcm.center_id or isr.center_id=hcm.center_id)
	LEFT JOIN sample_sources ss ON(ss.source_id = sc.sample_source_id)
	LEFT JOIN sample_type sac ON (sac.sample_type_id = sc.sample_type_id)
	LEFT JOIN doctors doc ON (doc.doctor_id=tp.pres_doctor)
	LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by)
	LEFT JOIN test_visit_reports tvr on (tp.report_id=tvr.report_id)
	LEFT JOIN sample_collection_centers scc ON ( pr.collection_center_id = scc.collection_center_id);


--
-- Used by IncomingOutgoingRegister.srxml --
--
DROP VIEW IF EXISTS rpt_incoming_outgoing_register_view CASCADE;
--
-- Used by IndentFlow.srxml --
--
DROP VIEW IF EXISTS rpt_indent_flow_view CASCADE;
CREATE VIEW rpt_indent_flow_view AS
SELECT sim.indent_no, sim.date_time::Date AS date_time,
	CASE WHEN indent_type='S' THEN 'Stock transfer'
		WHEN indent_type='U' THEN 'Dept / Ward Issue'
	    ELSE 'Request for new Itemrpt_indent_flow_views'
	END AS indent_type,
	( CASE WHEN dept_from IN ( SELECT dept_id FROM department ) THEN d.dept_name  ELSE '' END )AS requesting_dept,
	( CASE WHEN dept_from IN ( SELECT ward_no FROM ward_names ) THEN w.ward_name  ELSE '' END )AS requesting_ward,
	( CASE WHEN dept_from IN (SELECT dept_id FROM department) THEN ''
		   WHEN dept_from IN ( SELECT ward_no FROM ward_names ) THEN ''
		   ELSE st.dept_name
		END) AS requesting_store,
	requester_name,
	(select date_time from store_transfer_main stm where stm.indent_no = sim.indent_no AND username <> '' order by transfer_no DESC limit 1) as processed_date,
	(select username from store_transfer_main stm where stm.indent_no = sim.indent_no AND username <> '' order by transfer_no DESC limit 1) as processed_by,
	(select received_by from store_transfer_main stm where stm.indent_no = sim.indent_no AND received_by <> '' order by transfer_no DESC limit 1) as received_by,
	(select received_date from store_transfer_main stm where stm.indent_no = sim.indent_no AND received_by <> '' order by transfer_no DESC limit 1) as received_date,
	approved_date, approved_by,
	CASE WHEN sim.status='O' THEN 'Open'  WHEN sim.status='A' THEN 'Approved' ELSE 'Closed'  END AS status,
	s.dept_name AS to_dept,hcmt.center_name AS to_center_name,hcmf.center_name AS from_center_name
FROM  store_indent_main sim
	LEFT JOIN stores s ON sim.indent_store = s.dept_id
	LEFT JOIN hospital_center_master hcmt ON(hcmt.center_id=s.center_id)
	LEFT JOIN department d ON d.dept_id = sim.dept_from
	LEFT JOIN ward_names w ON w.ward_no = sim.dept_from
	LEFT JOIN stores st ON (st.dept_id = CASE
	                                          WHEN trim(sim.dept_from) SIMILAR TO '[^a-z^A-Z]+'
	                                          THEN CAST(trim(sim.dept_from) AS integer)
	                                          ELSE '0'
	                                       END )
	LEFT JOIN hospital_center_master hcmf ON(hcmf.center_id=sim.requesting_center_id);
--
-- Used by IndentItemDetails.srxml --
--
DROP VIEW IF EXISTS rpt_indent_item_details_view CASCADE;
CREATE VIEW rpt_indent_item_details_view AS
SELECT sid.indent_no, mf.manf_mnemonic, g.generic_name, scm.category, sid.qty, qty_fullfilled, sid.approved_by,
	CASE  WHEN m.medicine_name IS NULL THEN sid.medicine_name ELSE m.medicine_name END AS medicine_name,m.cust_item_code,
	approved_time::date AS approved_date, po_no, s.dept_name AS to_dept,sim.date_time::date AS indent_date,
	CASE WHEN purchase_flag='N' THEN 'No' ELSE 'Yes'  END AS purchased,
	( CASE WHEN dept_from IN ( SELECT dept_id FROM department ) THEN d.dept_name  ELSE '' END )AS requesting_dept,
	( CASE WHEN dept_from IN ( SELECT ward_no FROM ward_names ) THEN w.ward_name  ELSE '' END )AS requesting_ward,
	( CASE WHEN dept_from IN (SELECT dept_id FROM department) THEN ''
		   WHEN dept_from IN ( SELECT ward_no FROM ward_names ) THEN ''
		   ELSE st.dept_name
		END) AS from_dept,
	hcmt.center_name AS to_center_name,hcmf.center_name AS from_center_name,
	CASE WHEN sid.status='O' THEN 'Open'
	    WHEN sid.status='A' THEN 'Approved'
		WHEN sid.status='R' THEN 'Rejected'
   	   	WHEN (sim.status != 'X' AND  sid.status='C') THEN 'Closed'
   	   	WHEN (sim.status = 'X' AND  sid.status='C') THEN 'Cancelled'
   		WHEN sid.status='T' THEN 'Transferred'
	END AS status,
    CASE WHEN indent_type='S' THEN 'Stock transfer'
         WHEN indent_type='U' THEN 'Dept / Ward Issue'
         ELSE 'Request for new Items'
    END AS indent_type,
    (select COALESCE(sum(qty_rejected),0) from store_transfer_details where indent_no = sid.indent_no and medicine_id = sid.medicine_id and is_rejected_qty_taken='N') as qty_in_rejection,
    purchase_flag_date
FROM store_indent_details sid
	LEFT JOIN store_indent_main sim USING(indent_no)
	LEFT OUTER JOIN store_item_details m ON m.medicine_id = sid.medicine_id
	LEFT JOIN manf_master mf ON mf.manf_code=m.manf_name
	LEFT JOIN generic_name g ON g.generic_code = m.generic_name
	LEFT JOIN store_category_master scm ON scm.category_id = med_category_id
	LEFT JOIN stores s ON sim.indent_store = s.dept_id
	LEFT JOIN hospital_center_master hcmt ON(s.center_id=hcmt.center_id)
	LEFT JOIN department d ON (d.dept_id = sim.dept_from)
	LEFT JOIN ward_names w ON w.ward_no = sim.dept_from
	LEFT JOIN stores st ON (st.dept_id = CASE WHEN trim(sim.dept_from) SIMILAR TO '[0-9]+|(-[0-9]+)'
							THEN CAST(trim(sim.dept_from) AS integer)
							ELSE '0'
							 END )
	LEFT JOIN hospital_center_master hcmf ON(sim.requesting_center_id = hcmf.center_id);

--
-- Used by IndividualStock.srxml  --
--
DROP VIEW IF EXISTS rpt_detailed_stock_report_view CASCADE;
DROP VIEW IF EXISTS rpt_stock_level_view CASCADE;
DROP VIEW IF EXISTS rpt_item_wise_stock_view ;

--
-- Used byInPatientReportBuilder.srxml --
--
DROP VIEW IF EXISTS rpt_in_patient_report_view CASCADE;
CREATE VIEW rpt_in_patient_report_view AS
SELECT pd.mr_no, pd.patient_id, pd.patient_name || ' '||coalesce(pd.last_name,'') as patient_name,pd.age,pd.patient_gender,
	pd.doctor_name AS doctor_name, pd.alloc_bed_type, COALESCE(pd.alloc_bed_name,'Not Allocated')
	as alloc_bed_name, pd.alloc_ward_name as ward_name, pd.reg_date as admission_date,
	to_char(pd.reg_time,'HH:MI AM') as admission_time, sum(r.amount) as deposit,
	to_char(Pd.reg_date,'dd-mm-yyyy') || ' ' ||to_char(Pd.reg_time,'HH:MI AM') as admission_datetime,
	pd.refdoctorname AS REFERAL_DOCTOR, coalesce(pd.primary_tpa_name,'GENERAL') as primary_tpa,
	pd.secondary_tpa_name as secondary_tpa, plan_name, plan_type,pd.name_local_language,
	pd.primary_insurance_co_name, pd.secondary_insurance_co_name, pd.mlc_status,pd.approval_amount, pd.dept_id, pd.complaint,
	to_char(Pd.discharge_date,'dd-mm-yyyy') || ' ' ||to_char(Pd.discharge_time,'HH:MI AM')
	as discharge_datetime,
	CASE WHEN ROUND(EXTRACT(EPOCH from (
		CASE WHEN Pd.discharge_date is null
			THEN LOCALTIMESTAMP(0)
			ELSE
			TO_TIMESTAMP(TO_CHAR(Pd.discharge_date,'dd-MM-yyyy') ||' '
			||TO_CHAR(Pd.discharge_time,'HH24:MI:SS'),'dd-MM-yyyy HH24:MI:SS')
			END)
			-TO_TIMESTAMP(to_char(pd.reg_date,'dd-MM-yyyy')
			||' '||to_char(pd.reg_time,'HH24:MI:SS'),'dd-MM-yyyy HH24:MI:SS'))::numeric/(60*60*24),1) = 0
			THEN
			CASE WHEN Pd.discharge_date is not null THEN 1 ELSE 0 END else
			ROUND(extract(EPOCH from (case when Pd.discharge_date is null THEN LOCALTIMESTAMP(0) ELSE
			TO_TIMESTAMP(TO_CHAR(Pd.discharge_date,'dd-MM-yyyy')
			||' '||TO_CHAR(Pd.discharge_time,'HH24:MI:SS'),'dd-MM-yyyy HH24:MI:SS')
			END)
			-TO_TIMESTAMP(TO_CHAR(pd.reg_date,'dd-MM-yyyy')
			||' '||TO_CHAR(pd.reg_time,'HH24:MI:SS'),'dd-MM-yyyy HH24:MI:SS'))::numeric/(60*60*24),1)::integer
	END as noofdays,
	pd.bed_start_date, pd.bed_end_date, dept_name, pd.bill_bed_type, hcm.center_name
FROM in_patient_chart_view pd
	LEFT OUTER JOIN bill b ON ( b.visit_id = pd.patient_id AND b.restriction_type='N' )
	LEFT OUTER JOIN bill_receipts br ON ( br.bill_no = b.bill_no )
	LEFT OUTER JOIN receipts r ON ( r.receipt_id = br.receipt_no ) AND r.receipt_type = 'R' AND NOT r.is_settlement
	JOIN hospital_center_master hcm ON (hcm.center_id = pd.center_id)
WHERE  ((CASE WHEN pd.visit_type IS NOT NULL THEN pd.visit_type
			ELSE Pd.visit_type END) IN ('i'))
GROUP BY pd.mr_no,pd.patient_id, pd.salutation,pd.patient_name,pd.last_name, pd.patient_gender,
	pd.doctor_name, pd.alloc_bed_type, pd.alloc_bed_name, pd.alloc_ward_name,Pd.reg_date,Pd.reg_time,pd.age,
	pd.primary_tpa_name,pd.secondary_tpa_name,pd.reference_docto_id, pd.mlc_status,pd.approval_amount,Pd.discharge_date,Pd.discharge_time,
	pd.refdoctorname,pd.dept_id, pd.complaint,pd.bed_start_date,pd.bed_end_date,pd.discharge_date,pd.name_local_language,
	dept_name, plan_name, plan_type, primary_insurance_co_name, secondary_insurance_co_name, pd.bill_bed_type, hcm.center_name
ORDER BY ward_name,pd.patient_id ;


--
-- Used by StoreItemMaster.srxml
--
DROP VIEW IF EXISTS rpt_store_item_master_view CASCADE;

DROP VIEW IF EXISTS rpt_linen_view CASCADE;
--
-- Used by LuxuryTaxBuilder.srxml --
--
DROP VIEW IF EXISTS rpt_luxury_tax_view CASCADE;
CREATE VIEW rpt_luxury_tax_view AS
SELECT ipb.mrno, ipb.patient_id, ipb.admit_id, bn.bed_name, bn.bed_type,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) as full_name,
	ipb.start_date::date AS bed_admit_date, ipb.start_date AS bed_admit_date_time, pd.name_local_language,
	ipb.end_date::date AS bed_finalized_date, ipb.end_date AS bed_finalized_date_time,
	bc.act_quantity AS period_of_stay, ipb.bed_id, bc.amount
	AS total_bed_charge, bc.act_rate AS bed_charge, b.bill_no, ipb.bed_state, bc.charge_head,
	b.finalized_date::date AS bill_finalized_date,
	CASE WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'S' THEN 'Settled'
		WHEN b.status = 'C' THEN 'Closed'
	ELSE '' END AS status,
	COALESCE((SELECT sum(amount) as amount FROM bill_charge ibc WHERE
		ibc.charge_ref=bc.charge_id AND ibc.charge_head='LTAX' AND ibc.status != 'X'),0)
	AS total_tax_amount,
	COALESCE((SELECT sum(act_rate) as act_rate FROM bill_charge ibc WHERE
		ibc.charge_ref=bc.charge_id AND ibc.charge_head='LTAX' AND ibc.status != 'X'),0)
	AS tax_amount,
	CASE WHEN bd.luxary_tax IS NULL THEN ibd.luxary_tax ELSE bd.luxary_tax END as tax_percent,center_name
FROM ip_bed_details ipb
	JOIN bill_charge bc ON (ipb.bed_state = 'F' AND ipb.bed_id::text = bc.act_description_id::text
		AND bc.charge_head IN ('BBED','BICU','BYBED') AND bc.status !='X')
	JOIN bill_activity_charge bac ON (bc.charge_id=bac.charge_id
		AND bac.act_description_id::text = ipb.bed_id::text
		AND ( bac.activity_id::text=ipb.admit_id::text OR bac.activity_id::text=ipb.ref_admit_id::text) and activity_code='BED')
	JOIN bill b ON (bc.bill_no = b.bill_no AND b.status !='X' AND b.status != 'A')
	JOIN bed_names bn ON(ipb.bed_id = bn.bed_id)
	JOIN patient_details pd ON(pd.mr_no = ipb.mrno)
	JOIN patient_registration pr ON(pr.patient_id = ipb.patient_id)
	LEFT JOIN bed_details bd ON(pr.org_id = bd.organization AND bn.bed_type=bd.bed_type)
	LEFT JOIN icu_bed_charges ibd ON(pr.org_id = ibd.organization AND ibd.bed_type=pr.bed_type
		AND ibd.intensive_bed_type=bn.bed_type)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
;

DROP VIEW IF EXISTS rpt_ot_schedule_view CASCADE;
DROP VIEW IF EXISTS rpt_visit_details_view CASCADE;

--
-- Used by paymentConsolidated.srxml --
--
DROP VIEW IF EXISTS rpt_payment_consolidated_view CASCADE;
CREATE VIEW rpt_payment_consolidated_view AS
SELECT pcv.bill_no, date as finalized_date, pcv.chargegroup_name,COALESCE (test_name,service_name)
	as activity_name, CASE WHEN pcv.activity_conducted='Y' THEN 'Yes' ELSE 'No' END as activity_conducted,
    pcv.bill_amount, pcv.doctor_name, pcv.doctor_amount, pcv.prescribing_doctor_name,
    pcv.prescribing_dr_amount, pcv.referal_doctor, pcv.referal_amount,pcv.name_local_language,
    pcv.primary_tpa_name, pcv.secondary_tpa_name, pcv.plan_name,pcv.plan_type,
    pcv.primary_insurance_co_name, pcv.secondary_insurance_co_name,pcv.total_to_pay_amt, pcv.hospital_amt,
    pcv.doc_paid_amt,pcv.pres_dr_paid_amt, pcv.ref_paid_amt, pcv.total_paid_amt, pcv.yet_to_pay_amt, pcv.ac_head,
	pcv.account_group_name, pcv.chargehead_name, pcv.act_description, pcv.discount, pcv.visit_id,
	(coalesce(prescribing_dr_amount,0)-coalesce(pres_dr_paid_amt,0)) AS presc_dr_yet_to_pay_amount,
	(coalesce(referal_amount,0)-coalesce(ref_paid_amt,0)) AS ref_dr_yet_to_pay_amount,
	(coalesce(doctor_amount,0)-coalesce(doc_paid_amt,0)) AS conducting_dr_yet_to_pay_amount,
	CASE WHEN pcv.bill_type = 'C' THEN 'Bill Later' ELSE 'Bill Now' END AS bill_type,
	CASE WHEN pcv.bill_status = 'A' THEN 'Open' WHEN pcv.bill_status = 'F' THEN 'Finalized'
		WHEN pcv.bill_status = 'S' THEN 'Settled' WHEN pcv.bill_status = 'C' THEN 'Closed'
		ELSE 'Cancelled' END AS bill_status,
	CASE WHEN pcv.payment_status = 'P' THEN 'Paid' ELSE 'Unpaid' END as payment_status,
	CASE WHEN pcv.primary_claim_status = 'O' THEN 'Open' WHEN pcv.primary_claim_status = 'S' THEN 'Sent'
		ELSE 'Closed'
	END as claim_status, pcv.visit_type_name, pcv.mr_no, pcv.op_type, pcv.op_type_name,
	CASE WHEN pcv.patient_gender='M' THEN 'Male'
		WHEN pcv.patient_gender='F' THEN 'Female'
		WHEN pcv.patient_gender='C' THEN 'Couple'
		ELSE 'Other'
	END AS patient_gender,
	pcv.patient_full_name, pcv.dept_name, pcv.org_name, pcv.category_name, pcv.store_name, pnv.payee_name,
	CASE WHEN pd.payment_type='D' THEN 'Conducting Doctor Payments'
		WHEN pd.payment_type='C' THEN 'Miscellaneous Payments'
		WHEN pd.payment_type in ('R','F') THEN 'Referral Doctor Payments'
		WHEN pd.payment_type='P' THEN 'Prescribed Doctor Payments'
		WHEN pd.payment_type='S' THEN 'Supplier Payments'
		WHEN pd.payment_type='O' THEN 'OutHouse Payments'
	END AS payment_type,
	pd.payment_id, prescribing_dr_payment_id,  ref_payment_id, doc_payment_id, hcm.center_name
FROM payments_consolidated_ext_view pcv
	LEFT JOIN payments_details pd ON pcv.charge_id=pd.charge_id
	LEFT JOIN payee_names_view_for_voucher pnv ON (pnv.payee_id=pd.payee_name)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pcv.center_id) ;
--
-- Used by PaymentDetails.srxml --
--
DROP VIEW IF EXISTS rpt_payment_details_view CASCADE;
CREATE VIEW rpt_payment_details_view AS
SELECT	payment_id,
	CASE WHEN payment_type='D' THEN 'Conducting Doctor Payments'
		WHEN payment_type='C' THEN 'Miscellaneous Payments'
		WHEN payment_type in ('R','F') THEN 'Referral Doctor Payments'
		WHEN payment_type='P' THEN 'Prescribed Doctor Payments'
		WHEN payment_type='S' THEN 'Supplier Payments'
		WHEN payment_type='O' THEN 'OutHouse Payments'
	END AS payment_type,
	CASE WHEN bc.activity_conducted='Y' THEN 'Yes' ELSE 'No' END AS activity_conducted, pd.voucher_no,
	pd.amount, pd.description, pd.category AS payment_category, pcm.category_name AS patient_category,
	DATE(pd.posted_date) AS posted_date, DATE(b.finalized_date) AS finalized_date, pd.username, pv.payee_name,
	pd.charge_id, account_head_name, agm.account_group_name, chc.chargehead_name, cgc.chargegroup_name,
	bc.amount as charge_amt, bc.discount as charge_discount, bc.bill_no,	bc.act_description,
	CASE 	WHEN b.bill_type = 'C' THEN 'Bill Later' ELSE 'Bill Now' END AS bill_type,
	CASE 	WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized'
			WHEN b.status = 'C' THEN 'Closed'
			WHEN b.status ='X' THEN 'Cancelled' ELSE NULL
	END AS bill_status,
	CASE WHEN b.payment_status = 'P' THEN 'Paid' ELSE 'Unpaid' END as payment_status,
	CASE WHEN b.primary_claim_status = 'O' THEN 'Open' WHEN b.primary_claim_status = 'S' THEN 'Sent'
		ELSE 'Closed'
	END as claim_status,
	vtn.visit_type_name, pr.mr_no, pr.op_type, otn.op_type_name,
	CASE 	WHEN COALESCE(patd.patient_gender,isr.patient_gender)='M' THEN 'Male'
			WHEN COALESCE(patd.patient_gender,isr.patient_gender)='F' THEN 'Female'
			WHEN COALESCE(patd.patient_gender,isr.patient_gender)='C' THEN 'Couple' ELSE 'Other'
	END AS patient_gender,
	COALESCE (get_patient_full_name(sm.salutation, patd.patient_name, patd.middle_name, patd.last_name),
		prc.customer_name, isr.patient_name) AS patient_full_name, dep.dept_name AS admit_dept,
	tdep.dept_name AS treating_dept,doc.doctor_name, (cdoc.doctor_name) AS conducting_doctor,
	coalesce(rdoc.doctor_name, ref.referal_name) AS referer, pdoc.doctor_name AS prescribing_doctor_name,
	ptm.tpa_name as primary_tpa_name, stm.tpa_name as secondary_tpa_name,
	ipm.plan_name, in_cat.category_name as plan_type,
	picm.insurance_co_name as primary_insurance_co_name, sicm.insurance_co_name as secondary_insurance_co_name,
	gd.dept_name as store_name, COALESCE (bc.referal_amount,0.00) AS referal_amount,
	COALESCE (bc.prescribing_dr_amount,0.00) AS prescribing_dr_amount,
	CASE 	WHEN bc.charge_head='PKGPKG' THEN COALESCE (bac.doctor_amount,0.00)
			ELSE COALESCE (bc.doctor_amount,0.00)
	END  AS conducting_doctor_amount,
	org_name,bc.status as charge_status, hcm.center_name, doc.custom_field1_value AS dr_custom1,
	doc.custom_field2_value AS dr_custom2, doc.custom_field3_value AS dr_custom3, doc.custom_field4_value
	AS dr_custom4, doc.custom_field5_value AS dr_custom5, cdoc.custom_field1_value AS cond_doc_custom1,
	cdoc.custom_field2_value AS cond_doc_custom2, cdoc.custom_field3_value AS cond_doc_custom3,
	cdoc.custom_field4_value AS cond_doc_custom4, cdoc.custom_field5_value AS cond_doc_custom5,
	rdoc.custom_field1_value AS ref_custom1, rdoc.custom_field2_value AS ref_custom2,
	rdoc.custom_field3_value AS ref_custom3, rdoc.custom_field4_value AS ref_custom4,
	rdoc.custom_field5_value AS ref_custom5,
	pdoc.custom_field1_value AS pres_doc_custom1, pdoc.custom_field2_value AS pres_doc_custom2,
	pdoc.custom_field3_value AS pres_doc_custom3, pdoc.custom_field4_value AS pres_doc_custom4,
	pdoc.custom_field5_value AS pres_doc_custom5

FROM payments_details pd
	LEFT JOIN bill_charge bc ON bc.charge_id = pd.charge_id
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	JOIN bill b USING (bill_no)
	JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
	JOIN hospital_center_master hcm ON (hcm.center_id = pd.expense_center_id)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN patient_details patd ON (patd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = patd.salutation)
	LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
	LEFT JOIN doctors cdoc ON(cdoc.doctor_id=bc.payee_doctor_id)
	LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id )
	LEFT JOIN doctors pdoc on (pdoc.doctor_id =bc.prescribing_dr_id)
	LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
	LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
	LEFT JOIN tpa_master ptm ON (ptm.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
	LEFT JOIN insurance_category_master in_cat ON (in_cat.category_id = ipm.category_id)
	LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = pr.primary_insurance_co)
	LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = pr.secondary_sponsor_id)
	LEFT JOIN patient_category_master pcm ON (pcm.category_id = pr.patient_category_id)
	LEFT JOIN store_sales_main pm ON (bc.charge_id = pm.charge_id)
	LEFT JOIN stores gd ON (gd.dept_id = pm.store_id)
	LEFT JOIN account_group_master agm  ON (agm.account_group_id = bc.account_group)
	LEFT JOIN payee_names_view_for_voucher pv ON (pv.payee_id=pd.payee_name)
	LEFT JOIN bill_account_heads bah ON (pd.account_head= bah.account_head_id)
	LEFT JOIN bill_activity_charge bac ON (pd.payment_id=bac.doctor_payment_id and pd.payment_type='D' )
WHERE  (b.status IN ('F','C','S') AND bc.status!='X' AND pd.payment_type in ('D','P','R','F','O') )

UNION ALL

SELECT	payment_id,
	CASE WHEN payment_type='D' THEN 'Conducting Doctor Payments'
		WHEN payment_type='C' THEN 'Miscellaneous Payments'
		WHEN payment_type in ('R','F') THEN 'Referral Doctor Payments'
		WHEN payment_type='P' THEN 'Prescribed Doctor Payments'
		WHEN payment_type='S' THEN 'Supplier Payments'
		WHEN payment_type='O' THEN 'OutHouse Payments'
	END AS payment_type,
	NULL AS activity_conducted, pd.voucher_no, pd.amount, pd.description, pd.category AS payment_category,
	NULL AS patient_category, DATE(pd.posted_date) AS posted_date, NULL AS finalized_date,
	pd.username, pv.payee_name, pd.charge_id, account_head_name, agm.account_group_name,
	NULL as chargehead_name, NULL AS chargegroup_name, 0 as charge_amt, 0 as charge_discount,
	NULL as bill_no,NULL as act_description, NULL AS bill_type, NULL AS bill_status, NULL as payment_status,
	NULL as claim_status, NULL as visit_type_name, NULL AS mr_no, NULL AS op_type, NULL AS op_type_name, NULL AS patient_gender,
	NULL AS patient_full_name, NULL AS admit_dept,NULL AS treating_dept, NULL AS doctor_name,
	NULL  AS conducting_doctor, NULL as referer, NULL AS prescribing_doctor_name, NULL AS primary_tpa_name,
	NULL AS secondary_tpa_name,NULL AS plan_name, NULL AS plan_type, NULL AS primary_insurance_co_name,
	NULL as secondary_insurance_co_name, NULL as store_name, 0 AS referal_amount,
	0 AS prescribing_dr_amount, 0  AS conducting_doctor_amount, NULL as org_name, NULL as charge_status, hcm.center_name,
	NULL AS dr_custom1,	NULL AS dr_custom2, NULL AS dr_custom3, NULL AS dr_custom4, NULL AS dr_custom5,
	NULL AS cond_doc_custom1, NULL AS cond_doc_custom2, NULL AS cond_doc_custom3, NULL AS cond_doc_custom4,
	NULL AS cond_doc_custom5, NULL AS ref_custom1, NULL AS ref_custom2,	NULL AS ref_custom3, NULL AS ref_custom4,
	NULL AS ref_custom5, NULL AS pres_doc_custom1, NULL AS pres_doc_custom2, NULL AS pres_doc_custom3,
	NULL AS pres_doc_custom4, NULL AS pres_doc_custom5
FROM payments_details pd
	LEFT JOIN account_group_master agm  ON (agm.account_group_id = pd.account_group)
	LEFT JOIN payee_names_view_for_voucher pv ON (pv.payee_id=pd.payee_name)
	LEFT JOIN bill_account_heads bah ON (pd.account_head= bah.account_head_id)
	JOIN hospital_center_master hcm ON (hcm.center_id = pd.expense_center_id)
WHERE (pd.payment_type in ('C','S') OR (pd.payment_type in ('D','P','R','F','S','O') and charge_id is null)) ;

--
-- Used by PaymentForInsurance.srxml --
--
DROP VIEW IF EXISTS rpt_payment_for_insurance_view CASCADE;
CREATE VIEW rpt_payment_for_insurance_view AS
SELECT pdet.patient_name,pdet.name_local_language,
	CASE WHEN p.payment_type='D' THEN 'Conducting Doctor'
		WHEN p.payment_type='R' THEN 'Referral Doctor'
		WHEN p.payment_type='P' THEN 'Prescribing Doctor'
	END AS payment_type,pdet.mr_no,
	(CASE WHEN  p.payment_type in('D','P','R')
		THEN (SELECT doctor_name FROM doctors WHERE doctor_id=p.payee_name)
		WHEN p.payment_type= 'F' THEN (SELECT referal_name FROM referral
										WHERE 	referal_no=p.payee_name)  WHEN p.payment_type='O' THEN
		(SELECT oh_name FROM  outhouse_master WHERE oh_id=p.payee_name) ELSE p.payee_name END )
	as payee_name,  p.date, pd.description, pd.amount, date(br.display_date) as bill_receipt_date,
	date(br.display_date) as bill_sponsor_date ,
	CASE WHEN b.primary_claim_status='O' THEN 'Open'
		WHEN b.primary_claim_status='S' THEN 'Sent'
		WHEN b.primary_claim_status='R' THEN 'Closed' END as claim_status,hcm.center_name
FROM payments p
	JOIN (select pd.amount,pd.voucher_no,bill_no,pd.description,
		pd.category from payments_details pd join bill_charge bc using(charge_id)) as pd using(voucher_no)
	JOIN bill b on b.bill_no=pd.bill_no
	JOIN patient_registration pr on pr.patient_id=b.visit_id and pr.primary_sponsor_id !=''
	LEFT JOIN bill_receipts br on ( br.receipt_no = b.last_sponsor_receipt_no)
	JOIN patient_details pdet on pdet.visit_id=b.visit_id
	LEFT JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id)
	;

--
-- Used by payments.srxml --
--
DROP VIEW IF EXISTS rpt_payment_view CASCADE;
CREATE VIEW rpt_payment_view AS
SELECT amount as net_amount, (amount-tax_amount+tds_amount) as amt, tax_amount, tds_amount, 
	tds_amount AS wht_amount, username,
	reference_no, bank, voucher_no, DATE(date) AS date, pv.payee_name as payeename,
	CASE WHEN voucher_category='R' THEN 'Payment Reversal'
		ELSE 'Payments'
	END AS voucher_category,
	payment_mode,
	card_type,
	CASE WHEN payment_type='D' THEN 'Conducting Doctor Payments'
		WHEN payment_type='C' THEN  'Miscellaneous Payments'
		WHEN payment_type in ('R','F') THEN 'Referral Doctor Payments'
		WHEN payment_type='P' THEN 'Prescribed Doctor Payments'
		WHEN payment_type='S' THEN 'Supplier Payments'
		WHEN payment_type='O' THEN 'OutHouse Payments'
	END AS payment_type,
	mod_time, counter_no,
	CASE WHEN counter_type='B' THEN 'Billing'
		WHEN counter_type='P' THEN 'Pharmacy'
	END AS counter_type,
	CASE WHEN collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter, remarks, hcm.center_name
FROM payments p
	JOIN payment_mode_master pmn on (p.payment_mode_id=pmn.mode_id)
	LEFT JOIN card_type_master ctm on (p.card_type_id=ctm.card_type_id)
	JOIN payee_names_view_for_voucher pv on (pv.payee_id=p.payee_name)
	JOIN counters c on c.counter_id=p.counter
	JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id);

--
--Used by PharmacyInvoice.srxml --
--
DROP VIEW IF EXISTS rpt_purchase_invoice_view CASCADE;
CREATE VIEW rpt_purchase_invoice_view AS
SELECT store_name,center_name,store_type_name, purchase_type, supplier_name,cust_supplier_code, invoice_no, invoice_date, po_no,
	invoice_status,po_date, due_date, paid_date, tax_name, grn_nos, date(grn_date) as grn_date,
	item_amount, item_discount,item_scheme_discount,
	item_tax, discount, round_off, other_charges, cess,
	case when purchase_type = 'Debit Note' then -(received_debit_amt) else
	item_amount-(item_discount+item_scheme_discount)+item_tax + other_charges-discount+round_off+cess+ced_amt+tcs_amount end AS invoice_amount,
	tcs_amount, received_debit_amt, raised_amt, coalesce(raised_amt - received_debit_amt, 0) as difference_amt,ced_amt,
	purchasetype, case when consignment_stock='t' then 'Consignment' else 'Normal' end as stock_type, user_name, mod_datetime,
	CASE WHEN form_8h  THEN 'Yes' ELSE 'No' END as form_8h,supplier_state,supplier_tin_no,
	pharmacy_tin_no, drug_license_no,pan_no,cin_no,transportation_charges,remarks,purpose_of_purchase,return_type,return_reason,
	return_remarks
FROM store_purchase_invoice_report_view ;


--
-- Used by PharmacyPurchaseItems.srxml --
--
DROP VIEW IF EXISTS rpt_pharmacy_purchase_items_view CASCADE;
CREATE VIEW rpt_pharmacy_purchase_items_view AS
SELECT s.dept_name, CASE WHEN gm.debit_note_no IS NULL THEN 'Purchase' ELSE 'Debit Note' END AS purchase_type,
	sm.supplier_name,sm.cust_supplier_code, inv.invoice_no, inv.invoice_date, inv.po_no, gm.grn_no, gm.grn_date::DATE,
	COALESCE(inv.tax_name, dn.tax_name) as tax_name, i.medicine_name,i.cust_item_code, gn.generic_name,
	cm.category as category_name, sg.service_group_name, ssg.service_sub_group_name, sict.control_type_name,
	mm.manf_name, mm.manf_mnemonic, g.grn_pkg_size, sibd.batch_no, sibd.exp_dt::DATE,
	to_char(sibd.exp_dt, 'Mon-yyyy') AS exp_dt_str, g.billed_qty, sibd.mrp, g.bonus_qty, g.cost_price, g.tax,
	g.tax_rate, g.discount,g.item_ced, (g.billed_qty/g.grn_pkg_size*g.cost_price) as amount,
	(g.billed_qty/g.grn_pkg_size*g.cost_price - (g.discount+g.scheme_discount) + g.tax + g.item_ced) as net_amount,
	round((g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount + g.item_ced),2) AS purch_amt_witht_tax,
	coalesce(supplier_tin_no,'') as supplier_tin_no,
	store_type_name,CASE WHEN g.tax_type='MB' then 'MRP with bonus'
	WHEN g.tax_type='M' then 'MRP without bonus'
	WHEN g.tax_type='CB' then 'Cost price with bonus'
	WHEN g.tax_type='C' then 'Cost price without bonus' END AS tax_type,
	g.orig_debit_rate, g.orig_discount, g.orig_tax,
	(g.orig_debit_rate - g.cost_price) as rate_diff,
	case when g.cost_price=0 then 100 else 100 * (sibd.mrp - g.cost_price) / g.cost_price
	end as margin,spm.po_date,hcm.center_name,
	((g.billed_qty+g.bonus_qty) / g.grn_pkg_size) as pkg_qty,
	(g.billed_qty / g.grn_pkg_size) as pkg_billed_qty,
	(g.bonus_qty / g.grn_pkg_size) as pkg_bonus_qty,
	i.issue_units, g.grn_package_uom,i.package_type,
	round((g.discount /((CASE WHEN g.cost_price = 0 OR g.billed_qty = 0 THEN 1 ELSE g.cost_price*g.billed_qty END) /g.grn_pkg_size)*100),2) as discount_per,
	g.scheme_discount,CASE WHEN gm.form_8h :: boolean  THEN 'Yes' ELSE 'No' END as form_8h,
	round((g.scheme_discount /((CASE WHEN g.cost_price = 0 OR g.billed_qty = 0 THEN 1 
	ELSE g.cost_price*g.billed_qty END) /g.grn_pkg_size)*100),2) as scheme_discount_per,
	CASE WHEN inv.status = 'O' THEN 'Open' WHEN inv.status = 'F' THEN 'Finalized' ELSE 'Closed' END as invoice_status, 
	sic.item_code,sm.supplier_state,s.pharmacy_tin_no,sm.drug_license_no,sm.pan_no,sm.cin_no,g.medicine_id,sibd.item_batch_id
FROM store_grn_details g
	JOIN store_grn_main gm USING (grn_no)
	JOIN store_item_batch_details sibd USING(item_batch_id)
	JOIN store_item_details i ON (i.medicine_id = g.medicine_id)
	LEFT JOIN store_item_codes sic ON (sic.medicine_id = i.medicine_id) 
	LEFT JOIN service_sub_groups ssg USING (service_sub_group_id)
	LEFT JOIN service_groups sg USING (service_group_id)
	JOIN manf_master mm ON (mm.manf_code = i.manf_name)
	JOIN stores s ON (s.dept_id = gm.store_id)
	LEFT JOIN hospital_center_master hcm ON(s.center_id=hcm.center_id)
	JOIN store_category_master cm ON (cm.category_id = i.med_category_id)
	LEFT JOIN store_invoice inv USING (supplier_invoice_id)
	LEFT JOIN store_po_main spm on spm.po_no=inv.po_no
	LEFT JOIN store_debit_note dn ON (dn.debit_note_no = gm.debit_note_no)
	LEFT JOIN generic_name gn ON (gn.generic_code=i.generic_name)
	LEFT JOIN supplier_master sm on (sm.supplier_code = inv.supplier_id
		OR sm.supplier_code = dn.supplier_id)
	LEFT JOIN store_type_master stm ON (s.store_type_id = stm.store_type_id)
	LEFT JOIN store_item_controltype sict ON (sict.control_type_id = i.control_type_id) ;




DROP VIEW IF EXISTS rpt_pharmacy_sale_bill_view CASCADE;
DROP VIEW IF EXISTS rpt_pharmacy_sale_item_view CASCADE;

--
-- Used by PharmacyStockAdjustment.srxml --
--
DROP VIEW IF EXISTS rpt_store_stock_adjustment_view CASCADE;
CREATE VIEW rpt_store_stock_adjustment_view AS
SELECT  DISTINCT(adj_detail_no) AS adj_detail_no, s.dept_name AS store, sitd.medicine_name,sitd.cust_item_code, sibd.batch_no, sam.date_time::DATE AS adj_date,
	sam.username, sam.reason,  description, sitd.issue_base_unit,adj_no, sam.stock_take_id,
    CASE WHEN sad.type = 'A' THEN sad.qty ELSE -(sad.qty) END AS qty,
    CASE WHEN sad.type = 'A' THEN 'Increase' ELSE 'Decrease' END AS "adj_type",
 	case when sad.type  = 'A' then cost_value ELSE  0-cost_value end as value_cp,
   	case when sad.type  = 'A' then trunc((sibd.mrp/sitd.issue_base_unit)*sad.qty,2)
  		else 0-trunc((sibd.mrp/sitd.issue_base_unit)*sad.qty,2) end as value_mrp,
   	sic.control_type_name,to_char(sibd.exp_dt, 'Mon-yyyy') as exp_dt, COALESCE(isld.bin,sitd.bin) as bin, store_type_name, category,hcm.center_name
FROM store_adj_details sad
    LEFT JOIN store_adj_main sam  USING (adj_no)
    LEFT JOIN store_item_batch_details sibd ON (sibd.item_batch_id = sad.item_batch_id)
    LEFT JOIN stores s ON sam.store_id=s.dept_id
    LEFT JOIN hospital_center_master hcm ON(s.center_id=hcm.center_id)
    LEFT JOIN store_type_master stm ON s.store_type_id = stm.store_type_id
    LEFT JOIN store_item_details  sitd ON sitd.medicine_id=sad.medicine_id
    LEFT JOIN item_store_level_details  isld ON isld.medicine_id=sad.medicine_id AND isld.dept_id = sam.store_id
    LEFT JOIN store_category_master scm ON sitd.med_category_id=scm.category_id
    LEFT JOIN store_item_controltype sic ON sic.control_type_id=sitd.control_type_id ;

--
-- Used by PharmacyStockIssue.srxml--
--
DROP VIEW IF EXISTS rpt_store_stock_issue_view CASCADE;
CREATE VIEW rpt_store_stock_issue_view AS
SELECT sibd.batch_no, std.qty, sitd.medicine_name, stm.username, stm.reason,
	DATE_TRUNC('day',stm.date_time)::DATE AS issue_date, s.dept_name AS from_store, st.dept_name AS to_store,
	DATE(sibd.exp_dt) AS exp_dt, COALESCE(isld.bin,sitd.bin) as bin, std.transfer_no,sitd.issue_base_unit, control_type_name,
	(ROUND((ssd.package_cp/sitd.issue_base_unit),2)*std.qty) AS amt,
	(ROUND((ssd.tax/sitd.issue_base_unit),2)*std.qty) AS tax
FROM store_transfer_details std
	JOIN store_transfer_main  stm USING(transfer_no)
	JOIN store_item_batch_details sibd USING(item_batch_id)
	LEFT JOIN store_item_details sitd ON (sitd.medicine_id=std.medicine_id)
	LEFT JOIN stores s ON (s.dept_id= stm.store_from)
	LEFT JOIN stores st ON (st.dept_id=stm.store_to)
	LEFT JOIN item_store_level_details  isld ON isld.medicine_id=std.medicine_id AND isld.dept_id = stm.store_from
	LEFT JOIN store_stock_details ssd ON (std.medicine_id=ssd.medicine_id
			  AND s.dept_id=ssd.dept_id AND std.batch_no=ssd.batch_no)
	LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sitd.control_type_id) ;

--
-- Used by PharmacyStockTransfer.srxml --
--
DROP VIEW IF EXISTS rpt_store_stock_transfer_view CASCADE;
CREATE VIEW rpt_store_stock_transfer_view AS
SELECT sibd.batch_no,stm.indent_no, case when std.item_unit = 'I' then (std.qty-std.qty_rejected) else
	ROUND( (std.qty-std.qty_rejected)/std.trn_pkg_size, 2) end as qty, sitd.medicine_name,sitd.cust_item_code, stm.username, stm.reason,
	DATE_TRUNC('day',stm.date_time)::DATE AS transfer_date, s.dept_name AS from_store,
	st.dept_name AS to_store, sty.store_type_name AS from_store_type, styp.store_type_name AS to_store_type,
	to_char(sibd.exp_dt, 'Mon-yyyy') as exp_dt, COALESCE(isld.bin,sitd.bin) as bin, std.transfer_no, sitd.issue_base_unit, sic.control_type_name,
	(std.cost_value) AS value_cp,
	(sibd.mrp/sitd.issue_base_unit) AS unit_mrp,
	(sibd.mrp/sitd.issue_base_unit)*std.qty AS value_mrp,
	(sibd.mrp - sibd.mrp/(100 + sitd.tax_rate)*100)*qty/issue_base_unit AS tax,
	(sibd.mrp/sitd.issue_base_unit)*std.qty
	- (sibd.mrp - sibd.mrp/(100 + sitd.tax_rate)*100)*qty/issue_base_unit AS value_sp,
	scm.category, case when std.item_unit = 'I' then sitd.issue_units else sitd.package_uom end as uom,
	sitd.issue_units as issue_uom, sitd.package_uom AS pkg_uom,
	CASE WHEN std.item_unit='I' THEN 'Unit' ELSE 'Pkg' END AS transfer_uom_type,
	hcmf.center_name AS from_center_name, hcmt.center_name AS to_center_name,std.processed_date,std.processed_by,
	std.received_date,std.received_by,std.received_cost_value,sg.service_group_name,ssg.service_sub_group_name
FROM store_transfer_details std
	LEFT JOIN store_transfer_main  stm USING(transfer_no)
	LEFT JOIN store_item_details sitd ON (sitd.medicine_id=std.medicine_id)
	LEFT JOIN stores s ON (s.dept_id= stm.store_from)
	LEFT JOIN stores st ON (st.dept_id=stm.store_to)
	LEFT JOIN store_type_master sty ON (s.store_type_id=sty.store_type_id)
	LEFT JOIN store_type_master styp ON (st.store_type_id = styp.store_type_id)
	LEFT JOIN store_item_batch_details sibd ON (sibd.item_batch_id = std.item_batch_id)
	LEFT JOIN item_store_level_details  isld ON isld.medicine_id=std.medicine_id AND isld.dept_id = stm.store_from
	LEFT JOIN store_category_master scm ON (scm.category_id = sitd.med_category_id)
	LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sitd.control_type_id)
	LEFT JOIN hospital_center_master hcmf ON(s.center_id=hcmf.center_id)
	LEFT JOIN hospital_center_master hcmt ON(st.center_id=hcmt.center_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = sitd.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
;

DROP VIEW IF EXISTS rpt_po_items_view CASCADE;
DROP VIEW IF EXISTS rpt_po_pending_items_view CASCADE;

--
--Used by PrescriptionLeadTime.srxml --
--
DROP VIEW IF EXISTS rpt_prescription_lead_time_view ;
CREATE VIEW rpt_prescription_lead_time_view AS
SELECT ltv.*, pr.center_id, c.center_name,
	((extract (days from (ltv.initial_sale_date - ltv.prescribed_date_str))*24) +
		(extract (hours from (ltv.initial_sale_date - ltv.prescribed_date_str)))
            			||':'||(extract(minutes from(ltv.initial_sale_date - ltv.prescribed_date_str))))
            			as initial_sale_aging,
            			 ((extract (days from (ltv.final_sale_date - ltv.prescribed_date_str))*24) +
            			 (extract (hours from (ltv.final_sale_date - ltv.prescribed_date_str)))
            			||':'||(extract(minutes from(ltv.final_sale_date - ltv.prescribed_date_str))))
            			as final_sale_aging
FROM prescription_lead_time_view ltv
JOIN patient_registration pr on pr.patient_id = ltv.patient_id
JOIN hospital_center_master c on c.center_id = pr.center_id;

--
-- Used by PurchaseOrder.srxml --
--
DROP VIEW IF EXISTS rpt_purchase_order_view CASCADE;
CREATE VIEW rpt_purchase_order_view AS
SELECT 	po_no, po_date, supplier_name, cust_supplier_code,sm.drug_license_no,sm.pan_no,sm.cin_no, dept_name, store_type_name, user_id,
	CASE WHEN spm.status='O' THEN 'Open'
		WHEN spm.status='V' THEN 'Validated'
		WHEN spm.status='A' THEN 'Approved'
		WHEN spm.status='AO' THEN 'Amended Open'
		WHEN spm.status='AV' THEN 'Amended Validated'
		WHEN spm.status='AA' THEN 'Amended Approved'
		WHEN spm.status='C' THEN 'Closed'
		WHEN spm.status='X' THEN 'Cancelled'
		WHEN spm.status='FC' THEN 'Force Closed'
	END AS status,
	((select sum(discount) from store_po po where spm.po_no=po.po_no)+discount) as discount,
	po_total, tcs_amount,
	CASE WHEN spm.status!='O' AND  spm.status!='X' AND (approved_by IS NULL OR approved_by = '')
	AND po_total<= (SELECT COALESCE(po_approval_reqd_more_than_amt,0)::numeric FROM generic_preferences )
	THEN spm.user_id
	ELSE spm.approved_by
	END AS approved_by,
	CASE WHEN spm.status!='O'  AND  spm.status!='X' AND (approved_by IS NULL OR approved_by = '')
	AND po_total<= (SELECT COALESCE(po_approval_reqd_more_than_amt,0)::numeric FROM generic_preferences )
	THEN date(po_date)
	ELSE date(approved_time) END AS approved_date, delivery_date, hcm.center_name,
	spm.validated_by,spm.validated_time,spm.po_alloted_to,spm.vat_type,amended_reason, amendment_time, amendment_validated_time,
	amendment_approved_time ,amended_by ,amendment_validated_by ,amendment_approved_by ,
	amendment_validator_remarks, amendment_approver_remarks, CASE WHEN spm.form_8h  THEN 'Yes' ELSE 'No' END as form_8h,
	spm.transportation_charges,spm.remarks,spm.purpose_of_purchase
FROM store_po_main spm
LEFT JOIN supplier_master sm ON spm.supplier_id = sm.supplier_code
LEFT JOIN stores s ON s.dept_id = spm.store_id
LEFT JOIN hospital_center_master hcm ON(s.center_id=hcm.center_id)
LEFT JOIN store_type_master stm on stm.store_type_id = s.store_type_id ;


--
-- Used by RateMasterCharges.srxml--
--
DROP VIEW IF EXISTS rpt_diagnostics_rates_view CASCADE;
CREATE VIEW rpt_diagnostics_rates_view AS
SELECT test_name AS item_name,od.org_name,od.org_id,bed_type,service_group_name,service_sub_group_name,
	0 AS doctor_charge,0 AS anae_charge,charge AS hosp_charge,(0+0+charge) AS total_charge,
	0 AS doctor_discount,0 AS anae_discount,discount AS hosp_discount,(0+0+discount) AS total_discount,
   	0 AS doctor_charge_on_discount,0 AS  anae_charge_on_discount,(charge-discount)
   	AS hosp_charge_on_discount,(0+0+(charge-discount)) AS total_charge_on_discount,
    diag_code AS code
FROM diagnostics
	JOIN diagnostic_charges dc using(test_id)
	JOIN organization_details od ON ( org_id = dc.org_name )
	JOIN service_sub_groups using(service_sub_group_id)
	JOIN service_groups using(service_group_id);

DROP VIEW IF EXISTS rpt_services_rates_view CASCADE;
CREATE VIEW rpt_services_rates_view AS
SELECT service_name AS item_name,org_name,od.org_id,bed_type,service_group_name,service_sub_group_name,
	0 AS doctor_charge,0 AS anae_charge,unit_charge AS hosp_charge,(0+0+unit_charge) AS total_charge ,
	0 AS doctor_discount,0 AS anae_discount,discount AS hosp_discount,(0+0+discount) AS total_discount,
	0 AS doctor_charge_on_discount,0 AS  anae_charge_on_discount,(unit_charge-discount)
	AS hosp_charge_on_discount,(0+0+(unit_charge-discount)) AS total_charge_on_discount,
	service_code as code
FROM services
	JOIN service_master_charges USING(service_id)
	JOIN organization_details od USING ( org_id  )
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id);

DROP VIEW IF EXISTS rpt_operations_rates_view CASCADE;
CREATE VIEW rpt_operations_rates_view AS
SELECT operation_name AS item_name,org_name,od.org_id,bed_type,service_group_name,service_sub_group_name,
	surgeon_charge AS doctor_charge,anesthetist_charge  AS anae_charge,surg_asstance_charge AS hosp_charge,
	(surgeon_charge+anesthetist_charge+surg_asstance_charge) AS total_charge, surg_discount AS
	doctor_discount,anest_discount AS anae_discount,surg_asst_discount AS hosp_discount,
	(surg_discount+anest_discount+surg_asst_discount) AS total_discount, (surgeon_charge-surg_discount)
	AS doctor_charge_on_discount,(anesthetist_charge-anest_discount) AS  anae_charge_on_discount,
	(surg_asstance_charge-surg_asst_discount) AS hosp_charge_on_discount,
	((surgeon_charge-surg_discount)+(anesthetist_charge-anest_discount)+
	(surg_asstance_charge-surg_asst_discount)) AS total_charge_on_discount, operation_code AS code
FROM operation_master
	JOIN operation_charges USING(op_id)
	JOIN organization_details od USING ( org_id  )
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id);

DROP VIEW IF EXISTS rpt_equipment_rates_view CASCADE;
CREATE VIEW rpt_equipment_rates_view AS
SELECT  equipment_name AS item_name,org_name,od.org_id,bed_type,service_group_name,service_sub_group_name,
	0 AS doctor_charge,0 AS anae_charge,
	(CASE WHEN charge_basis ='D' THEN daily_charge WHEN charge_basis='H' THEN incr_charge
		ELSE daily_charge END) AS hosp_charge,
	(0+0+(CASE WHEN charge_basis ='D' THEN daily_charge WHEN charge_basis='H' THEN incr_charge
		ELSE daily_charge END)) AS total_charge ,0 AS doctor_discount,0 AS anae_discount,
	(CASE WHEN charge_basis ='D' THEN daily_charge_discount
		WHEN charge_basis='H' THEN incr_charge_discount ELSE daily_charge_discount END) AS hosp_discount,
	(0+0+(CASE WHEN charge_basis ='D' THEN daily_charge_discount
		WHEN charge_basis='H' THEN incr_charge_discount ELSE daily_charge_discount END)) AS total_discount,
	0 AS doctor_charge_on_discount,0 as  anae_charge_on_discount,
	((case when charge_basis ='D' THEN daily_charge WHEN charge_basis='H' THEN incr_charge
		ELSE daily_charge END)
	-(CASE WHEN charge_basis ='D' THEN daily_charge_discount WHEN charge_basis='H' THEN incr_charge_discount
		ELSE daily_charge_discount END)) AS hosp_charge_on_discount,
	(0+0+((CASE WHEN charge_basis ='D' THEN daily_charge WHEN charge_basis='H' THEN incr_charge
		ELSE daily_charge END)
	-(CASE WHEN charge_basis ='D' THEN daily_charge_discount WHEN charge_basis='H' THEN incr_charge_discount
		ELSE daily_charge_discount END))) AS total_charge_on_discount, equipment_code as code
FROM equipment_master
	JOIN  equipement_charges ON(equip_id = eq_id)
	JOIN organization_details od USING ( org_id  )
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id);

DROP VIEW IF EXISTS rpt_package_rates_view CASCADE;
CREATE VIEW rpt_package_rates_view AS
SELECT package_name AS item_name,org_name,od.org_id,bed_type,service_group_name,service_sub_group_name,
	0 AS doctor_charge,0 AS anae_charge,charge AS hosp_charge,0+0+charge AS total_charge,
	0 AS doctor_discount,0 AS anae_discount,discount AS hosp_discount,0+0+discount AS total_discount,
	0 AS doctor_charge_on_discount,0 AS anae_charge_on_discount,charge-discount AS
	hosp_charge_on_discount,0+0+charge-discount AS total_charge_on_discount, package_code AS  code
FROM packages
	JOIN package_charges USING(package_id)
	JOIN organization_details od USING ( org_id  )
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id);

DROP VIEW IF EXISTS rpt_other_item_rates_view CASCADE;
CREATE VIEW rpt_other_item_rates_view AS
SELECT charge_name AS item_name,'GENERAL'::text AS org_name,'ORG0001'::text AS org_id,'GENERAL'::text AS bed_type,
	service_group_name,service_sub_group_name, 0 AS doctor_charge,0 AS anae_charge,charge AS hosp_charge,
	0+0+charge AS total_charge, 0 AS doctor_discount,0 AS anae_discount,0 AS hosp_discount,
	0 AS total_discount, 0 AS doctor_charge_on_discount,0 AS anae_charge_on_discount,
	charge AS hosp_charge_on_discount, 0+0+charge AS total_charge_on_discount, othercharge_code AS  code
FROM common_charges_master
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id);

DROP VIEW IF EXISTS rpt_consultation_rates_view CASCADE;
CREATE VIEW rpt_consultation_rates_view AS
SELECT consultation_type as item_name,org_name,org_id,bed_type,service_group_name,service_sub_group_name,
	0 AS doctor_charge,0 AS anae_charge,charge AS hosp_charge,0+0+charge AS total_charge,
	0 AS doctor_discount,0 AS anae_discount,discount AS hosp_discount,0 AS total_discount,
	0 AS doctor_charge_on_discount,0 AS anae_charge_on_discount,charge-discount AS hosp_charge_on_discount,
	0+0+charge-discount AS total_charge_on_discount,
	consultation_code AS  code
FROM consultation_types
	JOIN consultation_charges USING(consultation_type_id)
	JOIN organization_details od USING ( org_id  )
	JOIN service_sub_groups USING(service_sub_group_id)
	JOIN service_groups USING(service_group_id) ;

DROP VIEW IF EXISTS rpt_rate_master_charges_view ;
CREATE VIEW rpt_rate_master_charges_view AS
SELECT * FROM rpt_diagnostics_rates_view
UNION ALL
SELECT * FROM rpt_services_rates_view
UNION ALL
SELECT * FROM rpt_operations_rates_view
UNION ALL
SELECT * FROM rpt_equipment_rates_view
UNION ALL
SELECT * FROM rpt_package_rates_view
UNION ALL
SELECT * FROM rpt_other_item_rates_view
UNION ALL
SELECT * FROM rpt_consultation_rates_view;



--
--Used by ReagentsConsumables.srxml --
--
DROP VIEW IF EXISTS rpt_reagent_consumables_view CASCADE;
CREATE VIEW rpt_reagent_consumables_view AS
SELECT  CASE WHEN (um.reagent_type = 'S') THEN s.service_name
			WHEN (um.reagent_type = 'D') THEN d.test_name
			WHEN (um.reagent_type = 'O') THEN om.operation_name
	     	ELSE '(None)'
	     END AS activity_name,
	    CASE WHEN (um.reagent_type = 'S') THEN 'Service'
			WHEN (um.reagent_type = 'D') THEN 'Test'
			WHEN (um.reagent_type = 'O') THEN 'Operation'
	     	ELSE 'General'
	    END AS reagent_type,
	sitd.medicine_name, sibd.batch_no, ud.qty AS consumed_qty,
	COALESCE(COALESCE(sc.quantity_needed,dr.quantity_needed),oc.qty_needed) AS actual_qty,
	date(um.date_time) as date_time, um.user_name, st.dept_name,
	round((sibd.mrp/sitd.issue_base_unit)*ud.qty::numeric,2) AS mrp,
	round((ssd.package_cp/sitd.issue_base_unit)*ud.qty::numeric,2) AS cost_price,hcm.center_name
FROM store_reagent_usage_main um
	JOIN store_reagent_usage_details ud USING(reagent_usage_seq)
	JOIN store_stock_details ssd ON(ud.item_batch_id=ssd.item_batch_id AND ssd.dept_id=um.store_id)
	JOIN store_item_batch_details sibd ON(ssd.item_batch_id = ud.item_batch_id)
	LEFT JOIN diagnostics d ON (d.test_id = um.consumer_id)
	LEFT JOIN services s ON (s.service_id = um.consumer_id)
	LEFT JOIN operation_master om ON (om.op_id = um.consumer_id)
	LEFT JOIN store_item_details sitd ON (sitd.medicine_id = sibd.medicine_id)
	LEFT JOIN diagnostics_reagents dr ON (dr.test_id = d.test_id AND dr.reagent_id=sitd.medicine_id)
	LEFT JOIN service_consumables sc ON (sc.service_id = s.service_id AND sc.consumable_id = sitd.medicine_id)
	LEFT JOIN ot_consumables oc ON (oc.operation_id = om.op_id and oc.consumable_id = sitd.medicine_id)
	LEFT JOIN stores st ON (st.dept_id = um.store_id)
	LEFT JOIN hospital_center_master hcm ON(st.center_id=hcm.center_id) ;

--
-- Used by RevenueAccrualReport.srxml --
--
DROP VIEW IF EXISTS rpt_revenue_accrual_report_view CASCADE;
CREATE VIEW rpt_revenue_accrual_report_view AS
SELECT *, incr_amount + incr_discount as incr_billed_amount FROM (
SELECT date(al.mod_time) AS incr_date,wn.ward_name,wn.ward_no, al.charge_id,b.bill_no,pd.mr_no,
	sm.salutation || ' ' || pd.patient_name
        || case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
        || case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end
    as full_name,
	CASE
		WHEN al.field_name = 'amount' THEN (al.new_value::numeric - COALESCE(al.old_value::numeric, 0))
		WHEN al.field_name = 'status' AND al.new_value = 'X' THEN (0 - bc.amount)
		ELSE 0
	END AS incr_amount,
	CASE
		WHEN al.field_name = 'discount' THEN (al.new_value::numeric - COALESCE(al.old_value::numeric, 0))
		WHEN al.field_name = 'status' AND al.new_value = 'X' THEN (0 - bc.discount)
		ELSE 0
	END AS incr_discount,

    CASE WHEN b.bill_type = 'C'::bpchar THEN 'Bill Later'::text ELSE 'Bill Now'::text END AS bill_type,
    CASE
        WHEN b.restriction_type = 'P'::bpchar THEN 'Pharmacy'::text
        WHEN b.restriction_type = 'T'::bpchar THEN 'Test'::text
        ELSE 'Hospital'::text
    END AS restriction_type,
    CASE
        WHEN b.status = 'A'::bpchar THEN 'Open'::text
        WHEN b.status = 'F'::bpchar THEN 'Finalized'::text
        WHEN b.status = 'S'::bpchar THEN 'Settled'::text
        WHEN b.status = 'C'::bpchar THEN 'Closed'::text
        ELSE 'Cancelled'::text
    END AS bill_status,
    CASE WHEN pr.primary_sponsor_id IS NULL OR pr.primary_sponsor_id::text = ''::text THEN 'N'::text ELSE 'I'::text END AS insured,
	date(b.finalized_date) AS finalized_date, date(b.open_date) AS open_date, date(b.closed_date) AS
	closed_date, date(bc.posted_date) AS posted_date,
	vtn.visit_type_name AS visit_type, pr.op_type, otn.op_type_name, bc.act_description,
	dep.dept_name, tdep.dept_name AS treating_dept, doc.doctor_name, cdoc.doctor_name AS conducting_doctor,
	COALESCE(rdoc.doctor_name, ref.referal_name) AS referer, od.org_name AS rate_plan, tm.tpa_name,
	ipm.plan_name, in_cat.category_name as plan_type, icm.insurance_co_name, cgc.chargegroup_name,
	chc.chargegroup_id as charge_group, chc.chargehead_name, bahc.account_head_name AS ac_head,
	agm.account_group_name,act_description_id,
	dep.dept_id AS admitting_dept_id,tdep.dept_id AS treating_dept_id, pd.patient_area, s.service_name,
	tdep.dept_category, bc.charge_head, bhcm.center_id
FROM bill_charge_audit_log al
	JOIN bill_charge bc ON al.charge_id::text = bc.charge_id::text
	JOIN bill b ON bc.bill_no::text = b.bill_no::text
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id
	JOIN visit_type_names vtn ON vtn.visit_type = b.visit_type
	LEFT JOIN department dep ON dep.dept_id::text = pr.dept_name::text
	LEFT JOIN treating_departments_view tdep ON tdep.dept_id = bc.act_department_id::text
	LEFT JOIN doctors doc ON doc.doctor_id::text = pr.doctor::text
	LEFT JOIN doctors cdoc ON cdoc.doctor_id::text = bc.payee_doctor_id::text
	LEFT JOIN doctors rdoc ON rdoc.doctor_id::text = pr.reference_docto_id::text
	LEFT JOIN referral ref ON ref.referal_no::text = pr.reference_docto_id::text
	LEFT JOIN organization_details od ON od.org_id::text = pr.org_id::text
	LEFT JOIN tpa_master tm ON tm.tpa_id::text = pr.primary_sponsor_id::text
	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
	LEFT JOIN insurance_category_master in_cat ON (in_cat.category_id = ipm.category_id)
	LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co)
	JOIN chargehead_constants chc ON chc.chargehead_id::text = bc.charge_head::text
	JOIN chargegroup_constants cgc ON cgc.chargegroup_id::text = chc.chargegroup_id::text
	LEFT JOIN account_group_master agm ON agm.account_group_id = bc.account_group
	JOIN bill_account_heads bahc ON bahc.account_head_id = chc.account_head_id
	LEFT JOIN services s ON s.service_id = act_description_id
	LEFT JOIN admission adm ON(adm.patient_id = b.visit_id)
	LEFT JOIN bed_names bn ON(bn.bed_id=adm.bed_id)
	LEFT JOIN ward_names wn ON(bn.ward_no=wn.ward_no)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
   LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
   LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE al.field_name IN ('amount', 'discount')
	OR (al.field_name = 'status' AND al.operation = 'UPDATE')
) as q
;

--
-- Used by Services.srxml--
--
DROP VIEW IF EXISTS rpt_services_view CASCADE;
CREATE VIEW rpt_services_view AS
SELECT sp.mr_no, sp.patient_id, serv.service_name, sdep.department, presc_date::DATE ,
	to_char(presc_date, 'hh24:mi') AS presc_time, doc.doctor_name as pres_doctor,
	CASE WHEN sp.conducted='N' THEN 'Not Conducted'
		WHEN sp.conducted='C' OR sp.conducted='Y' THEN 'Conducted'
		WHEN sp.conducted='Cancel' OR sp.conducted='X' THEN 'Cancelled'
		WHEN sp.conducted='P' THEN 'Partially Conducted'
		WHEN sp.conducted='U' THEN 'Condn. Unnecessary'
		WHEN sp.conducted='R' THEN 'Reverted Conduction/Reopened'
	END AS  conducted,
	conductedby, cdoc.doctor_name as condby, conducteddate::DATE, to_char(conducteddate, 'hh24:mi')
	AS cond_time, cancelled_by, sp.cancel_date::DATE,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_name,
	get_patient_age(pd.dateofbirth, pd.expected_dob)AS age,pd.name_local_language,
	CASE WHEN pd.patient_gender='M' THEN 'Male'
		WHEN pd.patient_gender='F' THEN 'Female'
		WHEN pd.patient_gender='C' THEN 'Couple'
		ELSE 'Others'
	END AS gender,
	CASE WHEN pr.visit_type='i' THEN 'In-Patient'
		WHEN pr.visit_type='o' THEN 'Out-Patient'
	END AS patient_type, pr.op_type, otn.op_type_name, hcm.center_name
	,sp.quantity::integer, coalesce(sp.tooth_unv_number, sp.tooth_fdi_number) as tooth_number,sp.remarks
FROM services_prescribed sp
	JOIN services serv ON (serv.service_id = sp.service_id)
	JOIN services_departments sdep ON (sdep.serv_dept_id = serv.serv_dept_id)
	LEFT JOIN doctors doc ON doc.doctor_id=sp.doctor_id
	LEFT JOIN doctors cdoc ON (cdoc.doctor_id=sp.conductedby)
	LEFT JOIN patient_details pd ON (sp.mr_no=pd.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN patient_registration pr ON (pr.patient_id = sp.patient_id)
	LEFT JOIN hospital_center_master hcm ON (pr.center_id = hcm.center_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type);

--
--Used by StockConsumption.srxml --
--
DROP VIEW IF EXISTS rpt_stock_consumption_view CASCADE;
CREATE VIEW rpt_stock_consumption_view AS
SELECT dept_name,hcm.center_name,medicine_name,cust_item_code, sum(sale_qty) as sales, sum(sale_return_qty) as returns,
	sum(purchase_qty) as purchase, sum(issue_out_qty) as transfer_out, sum(issue_in_qty) as transfer_in,
	sum(adj_dec_qty) as adj_dec, sum(adj_incr_qty) as adj_inc,sum(current_stock) as cstock,
	sum(return_stock) as supreturns, sum(open_stock) as openstock,sum(mrp) as mrp, sum(cost) as cost,
	sum(replaced_stock) as replaced_stock, sum(debit_qty) as debit_qty, sum(reagent_qty) as reagent_qty,
	sum(user_issue_out_qty) as userissue_out_qty, sum(user_issue_in_qty) as userissue_in_qty, mov_date,scm.category
FROM (

		-- sales and sales returns from store_sales_details
		SELECT store_id, medicine_id, CASE WHEN sm.type='S' THEN sum(quantity) ELSE 0 END as sale_qty,
			CASE WHEN sm.type='R' THEN sum(quantity) ELSE 0 END as sale_return_qty, 0 as purchase_qty,
		  	0 as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty, 0 as current_stock,
		  	0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock, 0 as debit_qty,
	        0 as reagent_qty, 0 as user_issue_out_qty, 0 as user_issue_in_qty, date(sm.sale_date) as mov_date
		FROM store_sales_details s
			JOIN store_sales_main sm using(sale_id)
		GROUP BY sm.type,  sm.store_id, s.medicine_id, sm.sale_date

		-- purchases , purchases returns and debit_qty from store_grn_details
		UNION ALL
		SELECT store_id, medicine_id, 0 as sale_qty, 0 as sale_return_qty,
			CASE WHEN gm.debit_note_no IS  NULL THEN sum(g.billed_qty + g.bonus_qty) ELSE 0 END as
			purchase_qty, 0 as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty,
			0 as current_stock, 0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock,
		   	CASE WHEN gm.debit_note_no IS  NOT NULL THEN sum((g.billed_qty + g.bonus_qty)) ELSE 0
		   	END as debit_qty, 0 as reagent_qty,
		  	0 as user_issue_out_qty,
		  	0 as user_issue_in_qty,
		  	date(grn_date) as mov_date
		FROM store_grn_details g
			JOIN store_grn_main gm USING (grn_no)
			JOIN store_item_details m using(medicine_id)
		GROUP BY gm.debit_note_no, store_id, medicine_id,grn_date


		-- Transfer out
		UNION ALL
		SELECT store_from, medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			sum(s.qty) as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty,
	        0 as current_stock, 0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock,
	        0 as debit_qty, 0 as reagent_qty, 0 as user_issue_out_qty, 0 as user_issue_in_qty,
		  	date(sm.date_time) as mov_date
	  	FROM store_transfer_details s
	  		JOIN store_transfer_main sm using(transfer_no)
		GROUP BY store_from, medicine_id,sm.date_time

		-- Transfer in
		UNION ALL
		SELECT store_to, medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			0 as issue_out_qty, sum(s.qty) as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty,
	        0 as current_stock, 0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock,
	        0 as debit_qty, 0 as reagent_qty, 0 as user_issue_out_qty, 0 as user_issue_in_qty,
		  	date(sm.date_time) as mov_date
		FROM store_transfer_details s
			JOIN store_transfer_main sm using(transfer_no)
		GROUP BY store_to, medicine_id,mov_date

		-- Reagents / consumables used by tests, services, ot
		UNION ALL
		SELECT store_id, sibd.medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
		  0 as issue_out_qty, 0 as issue_in_qty,  0 as adj_dec_qty, 0 as adj_incr_qty, 0 as current_stock,
		  0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock, 0 as debit_qty,
	      sum(srd.qty) as reagent_qty, 0 as user_issue_out_qty, 0 as user_issue_in_qty,
		  date(srm.date_time) as mov_date
		FROM store_reagent_usage_details srd
			JOIN store_reagent_usage_main srm using(reagent_usage_seq)
			JOIN store_item_batch_details sibd using(item_batch_id)
		GROUP BY store_id, sibd.medicine_id,srm.date_time

		-- User issues
		UNION ALL
		SELECT dept_from, medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			0 as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty, 0 as current_stock,
		  	0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock, 0 as debit_qty,
	        0 as reagent_qty, sum(sid.qty) as user_issue_out_qty, 0 as user_issue_in_qty,
		    date(sim.date_time) as mov_date
		FROM stock_issue_details sid
			JOIN stock_issue_main sim using(user_issue_no)
		GROUP BY dept_from, medicine_id,sim.date_time

		-- User issue returns
		UNION ALL
		SELECT dept_to, medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			0 as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty, 0 as current_stock,
		  	0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock, 0 as debit_qty,
	        0 as reagent_qty, 0 as user_issue_out_qty, sum(sid.qty) as user_issue_in_qty,
	        date(sim.date_time) as mov_date
		FROM store_issue_returns_details sid
			JOIN store_issue_returns_main sim using(user_return_no)
		GROUP BY dept_to, medicine_id,sim.date_time

		-- Stock adj
		UNION ALL
		SELECT store_id, a.medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			0 as issue_out_qty, 0 as issue_in_qty,
		    CASE WHEN (a.type LIKE 'A-' OR a.type = 'R') THEN sum(a.qty) ELSE 0 END as adj_dec_qty,
		    CASE WHEN a.type LIKE 'A+' OR a.type = 'A' THEN sum(a.qty) ELSE 0 END as adj_incr_qty,
	        0 as current_stock, 0 as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, 0 as replaced_stock,
	        0 as debit_qty, 0 as reagent_qty,  0 as user_issue_out_qty, 0 as user_issue_in_qty,
		  	date(am.date_time) as mov_date
		FROM store_adj_details a
			JOIN store_adj_main am using(adj_no)
		GROUP BY a.type, store_id, a.medicine_id,am.date_time

		-- Supplier returns
		UNION ALL
		SELECT store_id, medicine_id, 0 as sale_qty, 0 as sale_return_qty, 0 as purchase_qty,
			0 as issue_out_qty, 0 as issue_in_qty, 0 as adj_dec_qty, 0 as adj_incr_qty, 0 as current_stock,
		  	sum(qty) as return_stock, 0 as open_stock, 0 as mrp, 0 as cost, sum(replaced_qty) as
		  	replaced_stock, 0 as debit_qty, 0 as reagent_qty, 0 as user_issue_out_qty, 0 as user_issue_in_qty,
		  	date(psm.date_time) as mov_date
		FROM store_supplier_returns_main psm
			JOIN  store_supplier_returns ps on ps.return_no = psm.return_no and psm.orig_return_no is null
			JOIN store_item_details m using(medicine_id)
		GROUP BY  store_id, medicine_id, psm.date_time

	) AS smview
	JOIN store_item_details sid using (medicine_id)
	JOIN stores s on (s.dept_id=smview.store_id)
	JOIN hospital_center_master hcm ON(s.center_id = hcm.center_id)
	LEFT OUTER JOIN store_category_master scm ON(scm.category_id=sid.med_category_id)
GROUP BY dept_name, medicine_name,cust_item_code, mov_date,center_name,scm.category
ORDER BY medicine_name ;

-- used by scheduledAppointment.srxml--

DROP VIEW IF EXISTS rpt_appointment_details_view CASCADE;
CREATE VIEW rpt_appointment_details_view AS
 SELECT sa.mr_no,sa.visit_id,sa.patient_name,sa.waitlist,sa.patient_contact,sa.appointment_id,sa.res_sch_id,sa.changed_by,cgm.name AS patient_group,
	get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age,CASE WHEN pd.patient_gender ='M' Then 'Male'
	WHEN pd.patient_gender ='F' THEN 'Female' WHEN pd.patient_gender ='C' THEN 'Couple' WHEN pd.patient_gender ='O' THEN 'Others' END AS patient_gender,
	CASE  WHEN sa.mr_no='' THEN '' WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'
    WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M' ELSE 'Y' END as patient_age_in,
	sa.res_sch_name, sa.booked_by,sa.complaint AS complaint_name , pd.name_local_language,
	CASE WHEN sa.appointment_status = 'Arrived' THEN 'Arrived'
	     WHEN sa.appointment_status = 'Noshow' THEN 'No Show'
	     WHEN sa.appointment_status = 'Confirmed' THEN 'Confirmed'
	     WHEN sa.appointment_status = 'Booked' THEN 'Booked'
	     WHEN sa.appointment_status = 'Completed' THEN 'Completed'
	     WHEN sa.appointment_status = 'Cancel' THEN 'Cancelled' END AS appointment_status,
	CASE WHEN sa.rescheduled = 'Y' THEN 'Yes' ELSE 'No' END AS rescheduled,sa.cancel_reason,
 	date(appointment_time) AS appointment_date,date(arrival_time) AS arrival_date,
 	date(booked_time) AS booked_date,date(orig_appt_time) AS orig_appt_date, sa.duration,(sa.duration::float/60)::numeric(10,2) as duration_hrs,
	ct.consultation_type,
	md.icd_code as diag_code, md.description as diag_descr,sa.remarks,
 	date(completed_time) AS completed_date,bn.bed_name AS bed_name,wn.ward_name AS ward_name,
 	TO_CHAR((appointment_time::date + appointment_time::time ), 'dd-MM-yyyy hh24:mi')AS appointment_time,
 	TO_CHAR((arrival_time::date + arrival_time::time ), 'dd-MM-yyyy hh24:mi') AS arrival_time,
	TO_CHAR((booked_time::date + booked_time::time ),'dd-MM-yyyy hh24:mi') AS booked_time,
	TO_CHAR((orig_appt_time::date + orig_appt_time::time  ),'dd-MM-yyyy hh24:mi') AS orig_appt_time,
 	TO_CHAR((completed_time::date + completed_time::time ), 'dd-MM-yyyy hh24:mi') AS completed_time,
 	CASE WHEN sa.visit_mode = 'I' THEN 'In Person' WHEN sa.visit_mode = 'O' THEN 'Online'  ELSE '' END as visit_mode,
 	CASE WHEN sa.scheduler_visit_type = 'M' THEN 'Main' WHEN sa.scheduler_visit_type = 'F' THEN 'Follow Up'  ELSE '' END as scheduler_visit_type,hcm.center_name,
 	CASE WHEN sa.prim_res_id ILIKE 'DOC%' THEN
 			(SELECT doctor_name FROM doctors  WHERE doctor_id=sa.prim_res_id)
 	  	WHEN sa.res_sch_name ILIKE 'OPID%'  THEN
 	  		(SELECT theatre_name FROM theatre_master  WHERE theatre_id=sai.resource_id)
 	  	WHEN sa.res_sch_name ILIKE 'SERV%' THEN
			(SELECT serv_resource_name  FROM service_resource_master  WHERE serv_res_id::text=sai.resource_id )
		WHEN sa.res_sch_name ILIKE 'DGC%' THEN
 	  		(SELECT equipment_name FROM test_equipment_master  WHERE eq_id::text=sai.resource_id)
 	END AS booked_resource,
	CASE WHEN sm.res_sch_id = 1 THEN (SELECT doctor_name FROM DOCTORS WHERE doctor_id=sa.res_sch_name)
 	END AS doc_name,
 	CASE WHEN sm.res_sch_id = 3 THEN (SELECT service_name FROM services WHERE service_id=sa.res_sch_name)
 	END AS service_name,
	CASE WHEN sm.res_sch_id = 4 THEN (SELECT test_name FROM diagnostics WHERE test_id=sa.res_sch_name)
	END AS test_name,
	CASE WHEN sm.res_sch_id = 2 THEN (SELECT operation_name FROM operation_master WHERE op_id = sa.res_sch_name)
	END AS surgery_name,
 	CASE WHEN sm.res_sch_id = 1 THEN 'Consultation'
	     WHEN sm.res_sch_id = 2 THEN 'Surgery'
	     WHEN sm.res_sch_id = 3 THEN 'Service'
	     WHEN sm.res_sch_id = 4 THEN 'Test'
	END AS appointment_type,
(SELECT textcat_commacat(doctor_name)
	 FROM scheduler_appointment_items  sai
	 JOIN doctors doc ON(doc.doctor_id = sai.resource_id  AND sai.resource_type = 'SUDOC')
	WHERE sa.appointment_id = sai.appointment_id AND sa.res_sch_id=2)
 AS surgeon_name,
 (SELECT textcat_commacat(doctor_name)
	FROM scheduler_appointment_items  sai
	JOIN doctors doc ON(doc.doctor_id=sai.resource_id AND sai.resource_type='ANEDOC')
	WHERE sai.appointment_id=sa.appointment_id)
 AS anesthetist_name,
 (SELECT textcat_commacat(equipment_name)
	FROM scheduler_appointment_items  sai
	JOIN test_equipment_master tem ON(tem.eq_id::text=sai.resource_id AND sai.resource_type='EQID')
	WHERE sai.appointment_id=sa.appointment_id)
 AS equipment_name,
 (select textcat_commacat(doctor_name)
	 from scheduler_appointment_items  sai
	 JOIN doctors doc ON(doc.doctor_id = sai.resource_id  AND sai.resource_type = 'OPDOC')
	where sa.appointment_id = sai.appointment_id)
 AS consultant_name,
 (select textcat_commacat(doctor_name)
	 from scheduler_appointment_items  sai
	 JOIN doctors doc ON(doc.doctor_id = sai.resource_id  AND sai.resource_type = 'DOC')
	where sa.appointment_id = sai.appointment_id)
 AS doctor_name,
 (select doc.doctor_name from doctors doc
     WHERE sa.presc_doc_id = doc.doctor_id)
 AS prescribing_doctor_name,
 (select textcat_commacat(doctor_name)
	 from scheduler_appointment_items  sai
	 JOIN doctors doc ON(doc.doctor_id = sai.resource_id  AND sai.resource_type = 'LABTECH')
	where sa.appointment_id = sai.appointment_id )
 AS technician_name,
 (select textcat_commacat(theatre_name)
	 from scheduler_appointment_items  sai
	 JOIN theatre_master tm ON(tm.theatre_id = sai.resource_id  AND sai.resource_type = 'THID')
	where sa.appointment_id = sai.appointment_id )
 AS theatre_name,
 (select textcat_commacat(resourcename)
	FROM (SELECT CASE WHEN sa.res_sch_id=1 THEN
		(CASE WHEN sai1.resource_type = 'EQID'
		THEN (select equipment_name from test_equipment_master WHERE eq_id::text = sai1.resource_id)
		ELSE
		(SELECT generic_resource_name
		FROM generic_resource_type grt
		JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
		END)
		WHEN sa.res_sch_id = 2 THEN
		(CASE WHEN sai1.resource_type = 'EQID'
		      THEN (select equipment_name from test_equipment_master WHERE eq_id::text = sai1.resource_id)
		      WHEN sai1.resource_type = 'SRID'
		    THEN(select serv_resource_name from service_resource_master WHERE serv_res_id::text = sai1.resource_id)
		      WHEN sai1.resource_type = 'SUDOC'
		      THEN (select doctor_name from doctors where doctor_id = sai1.resource_id)
		      WHEN sai1.resource_type = 'ANEDOC'
		      THEN (select doctor_name from doctors where doctor_id = sai1.resource_id)
		ELSE
		(SELECT generic_resource_name
		FROM generic_resource_type grt
		JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
		END)
		WHEN sa.res_sch_id = 3 THEN
		(CASE WHEN sai1.resource_type = 'DOC'
		      THEN (select doctor_name from doctors where doctor_id = sai1.resource_id)
		ELSE
		(SELECT generic_resource_name
		FROM generic_resource_type grt
		JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
		END)
		WHEN sa.res_sch_id = 4 THEN
		(CASE WHEN sai1.resource_type = 'LABTECH'
		      THEN (select doctor_name from doctors where doctor_id = sai1.resource_id)
		ELSE
		(SELECT generic_resource_name
		FROM generic_resource_type grt
		JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)
		AND grm.generic_resource_id::text = sai1.resource_id
		WHERE sa.appointment_id = sai1.appointment_id AND grt.scheduler_resource_type = sai1.resource_type)
		END)
		END AS resourcename
		FROM scheduler_appointment_items sai1
		WHERE sa.appointment_id = sai1.appointment_id) as foo
	)as other_secondary_resources
FROM scheduler_appointments sa
LEFT JOIN patient_details pd ON(pd.mr_no=sa.mr_no)
LEFT JOIN confidentiality_grp_master cgm ON (pd.patient_group = cgm.confidentiality_grp_id)
LEFT JOIN admission adm ON (sa.visit_id = adm.patient_id)
LEFT JOIN bed_names bn ON(bn.bed_id = adm.bed_id)
LEFT JOIN ward_names wn ON(wn.ward_no = bn.ward_no)
LEFT JOIN consultation_types ct USING(consultation_type_id)
LEFT JOIN scheduler_master sm ON (sm.res_sch_id=sa.res_sch_id)
LEFT JOIN scheduler_resource_types srt ON ( srt.category=sm.res_sch_category AND srt.primary_resource=true )
LEFT JOIN scheduler_appointment_items sai ON (sa.appointment_id=sai.appointment_id AND sai.resource_type=srt.resource_type)
LEFT JOIN hospital_center_master hcm ON(sa.center_id = hcm.center_id)
LEFT JOIN mrd_diagnosis md ON (md.visit_id = sa.visit_id AND md.diag_type = 'P');

DROP VIEW IF EXISTS rpt_insurance_claim_view CASCADE;
DROP VIEW IF EXISTS rpt_insurance_claim_det_ph_view CASCADE;
DROP VIEW IF EXISTS rpt_insurance_claim_det_hosp_view CASCADE;
DROP VIEW IF EXISTS rpt_insurance_claim_batch_view CASCADE;


--
-- Used by StoresSupplierReturns.srxml--
--
DROP VIEW IF EXISTS rpt_store_supp_ret_view CASCADE;
CREATE VIEW rpt_store_supp_ret_view AS
SELECT s.supplier_name,s.cust_supplier_code,s.drug_license_no,s.pan_no,s.cin_no,medicine_name,cust_item_code,sibd.batch_no,sr.qty AS qty,user_name,return_no,date(srm.date_time) AS date,
	CASE WHEN return_type='D' THEN 'Damage'
		WHEN return_type='E' THEN 'Expiry'
		WHEN return_type='N' THEN 'Non-Moving'
		ELSE 'Others'
	END AS returntype,
	cost_value AS amt,orig_return_no,
	CASE WHEN srm.status='O' THEN 'Open'
		WHEN srm.status='C' THEN 'Closed'
		WHEN srm.status='P' THEN 'Partially Received'
		ELSE 'Received'
	END AS retstatus,
	CASE WHEN orig_return_no IS NULL THEN 'Return' ELSE 'Replace' END AS txntype,
	scm.category,gn.generic_name,issue_units,package_type,item_code,item_barcode_id,
		dept_name,
	CASE WHEN value='M' THEN 'Medium' WHEN value='H' THEN 'High' ELSE 'Low' END AS itemvalue,
	(sr.qty / sitd.issue_base_unit) as pkg_qty, hcm.center_name, sitd.tax_rate
FROM store_supplier_returns sr
	JOIN store_item_batch_details sibd USING(item_batch_id)
	JOIN store_supplier_returns_main srm using (return_no)
	JOIN stores st ON (srm.store_id=dept_id)
	LEFT JOIN hospital_center_master hcm ON(st.center_id=hcm.center_id)
	JOIN supplier_master s ON (s.supplier_code = supplier_id)
	JOIN store_item_details sitd ON(sitd.medicine_id = sibd.medicine_id)
	LEFT JOIN generic_name gn ON (gn.generic_code=sitd.generic_name)
	JOIN store_category_master scm ON (med_category_id=category_id)
	LEFT JOIN health_authority_preferences hap ON(hap.health_authority = hcm.health_authority)
	LEFT JOIN store_item_codes sic ON(sic.medicine_id=sitd.medicine_id AND hap.drug_code_type=sic.code_type)
GROUP BY s.supplier_name,s.cust_supplier_code,s.drug_license_no,s.pan_no,s.cin_no,medicine_name,cust_item_code,sibd.batch_no,sr.qty,user_name,srm.status,returntype,return_no,
	date,cost_value,sitd.issue_base_unit,orig_return_no,ret_qty_unit, scm.category,gn.generic_name,
	issue_units,value,package_type,issue_units,item_code,item_barcode_id,dept_name,hcm.center_name,tax_rate;

--
-- Used by LabTestResults.srxml
--
DROP VIEW IF EXISTS rpt_lab_test_results_view CASCADE;
CREATE VIEW rpt_lab_test_results_view AS
SELECT tp.test_id,d.test_name, td.resultlabel,tp.prescribed_id,dmm.method_name,
	td.report_value as value,td.comments as remarks,
	CASE WHEN td.withinnormal='Y' THEN 'Yes' ELSE 'No' END AS withinnormal,
	COALESCE (pr.patient_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (pd.mr_no, '') AS mrno,
	COALESCE(get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name),isr.patient_name)
	AS patient_name,
	COALESCE(get_patient_age(pd.dateofbirth, pd.expected_dob),isr.patient_age) AS age,
	CASE WHEN COALESCE(pd.patient_gender,isr.patient_gender)='M' THEN 'Male'
		WHEN COALESCE(pd.patient_gender,isr.patient_gender)='F' THEN 'Female'
		WHEN COALESCE(pd.patient_gender,isr.patient_gender)='C' THEN 'Couple'
		ELSE 'Others'
	END AS gender, pr.op_type, otn.op_type_name,
	CASE WHEN pr.visit_type='i' THEN 'In-Patient' WHEN pr.visit_type='o' THEN 'Out-Patient'
		ELSE 'Incoming Sample'
	END AS patient_type,date(tc.conducted_date) as conducted_date, date(sc.sample_date) as sample_date,
	td.units, td.reference_range, report_value as test_result,
	COALESCE( convert_to_numeric(report_value),0) AS test_value,td.result_disclaimer,
	case when td.test_detail_status='S' then 'Yes' else 'No' end as signed_off,
	hcm.center_name
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
LEFT JOIN diag_methodology_master dmm ON (td.method_id = dmm.method_id)
WHERE tp.conducted NOT IN ('N','P','RP','RBS','RAS') AND td.conducted_in_reportformat = 'N'
AND td.report_value IS NOT NULL AND td.report_value != '' AND td.test_detail_status != 'A' ;


DROP VIEW IF EXISTS rpt_patient_feedback_details_view CASCADE;
DROP VIEW IF EXISTS rpt_patient_feedback_topic_view CASCADE;
DROP VIEW IF EXISTS rpt_patient_feedback_topic_details_view CASCADE;
DROP VIEW IF EXISTS rpt_patient_feedback_summary_view CASCADE;
DROP VIEW IF EXISTS rpt_patient_feedback_summary_details_view CASCADE;
DROP VIEW IF EXISTS rpt_test_equipment_result_values_view CASCADE;


CREATE VIEW rpt_test_equipment_result_values_view AS
SELECT etv.equipment_id,equipment_name,etr.resultlabel_id,resultlabel,
	   test_value,conducted_on::date,conducted_by,remarks,sample_info
FROM equipment_test_conducted
JOIN equipment_test_values etv USING(equipment_conducted_id)
JOIN equipment_test_result etr
ON(etr.resultlabel_id = etv.resultlabel_id AND etr.equipment_id = etv.equipment_id)
JOIN test_equipment_master ON(eq_id = etv.equipment_id)
JOIN test_results_master trm ON(trm.resultlabel_id=etv.resultlabel_id)
WHERE test_record_complete = 'Y';

--
-- Used by BedOccupancyBuilder.srxml: one row per day
--
DROP VIEW IF EXISTS rpt_duration_bed_occupancy_view CASCADE;
CREATE VIEW rpt_duration_bed_occupancy_view AS
SELECT ref_date, trim(to_char(ref_date,'MONTH')) as month, extract(day from ref_date)::integer as day,
	coalesce(occ.occ_days,0) as opening_count,
	coalesce(ipst.count,0) as allocations, coalesce(ipend.count,0) as  deallocations,
	coalesce(occ.occ_days,0)+coalesce(ipst.count,0)-coalesce(ipend.count,0) as closing_count,
	(select count(*) from bed_names where status='A')::numeric as total_beds,
	(coalesce(occ.occ_days,0)+coalesce(ipst.count,0)-coalesce(ipend.count,0))/
		(select count(*) from bed_names where status='A')::numeric * 100 as closing_per,
	coalesce(pra.count,0) as admission,
	coalesce(prd.discharge,0) as discharges,
	coalesce(prd.normal_count,0) as normal_dis,coalesce(prd.absconded_count,0) as abscond_dis,
	coalesce(prd.death_count,0) as death_dis,coalesce(prd.dama_count,0) as dama_dis,
	coalesce(prd.ref_count,0) as ref_dis,
	coalesce(prd.admn_cancelled_count, 0) as admn_cancelled,
	coalesce(prd.other_discharges_count,0) as other_discharges,
	coalesce(iprv.reg_occ_days,0) as opening_ip_count,
	coalesce(iprv.reg_occ_days,0) + coalesce(pra.count,0)-coalesce(prd.discharge,0) as closing_ip_count,occ.center_name
FROM all_days_view ab
	LEFT JOIN total_bed_occupied_days_view occ ON (occ.occ_date = ab.ref_date-1)
	LEFT JOIN patient_discharges prd on (prd.discharge_date = ab.ref_date)
	LEFT JOIN patient_admissions pra ON (pra.reg_date = ab.ref_date)
	LEFT JOIN total_bed_start_dates ipst on(ipst.start_date = ab.ref_date)
	LEFT JOIN total_bed_end_dates ipend ON(ipend.end_date = ab.ref_date)
	LEFT JOIN in_patient_reg_days_view iprv ON (iprv.reg_occ_date = ab.ref_date-1)
;

--
-- Used by WardWiseBedOccupancyBuilder.srxml: one row per bed-day
--
DROP VIEW IF EXISTS rpt_ward_wise_bed_occupancy_view CASCADE;
DROP VIEW IF EXISTS rpt_bed_occupancy_detailed_view CASCADE;
CREATE VIEW rpt_bed_occupancy_detailed_view AS
SELECT ref_date, trim(to_char(ref_date,'MONTH')) as month, trim(to_char(ref_date,'YYYY')) as year,
	ward_name, bed_name, bed_type, is_icu, 1::numeric as total_count,
	coalesce(occ.occ_days,0) as opening_count,
	coalesce(allocs.count,0) as allocations, coalesce(deallocs.count,0) as deallocations,
	coalesce(occ.occ_days,0)+coalesce(allocs.count,0)-coalesce(deallocs.count,0) as closing_count,
	(coalesce(occ.occ_days,0)+coalesce(allocs.count,0)-coalesce(deallocs.count,0))*100::numeric as closing_per,
	hcm.center_name
FROM all_beds_days_view ab
	LEFT JOIN bed_occupied_days_view occ ON (occ.occ_date = ab.ref_date - 1 AND occ.bed_id = ab.bed_id)
	LEFT JOIN bed_start_dates allocs ON (allocs.start_date = ab.ref_date AND allocs.bed_id = ab.bed_id)
	LEFT JOIN bed_end_dates deallocs ON (deallocs.end_date = ab.ref_date AND deallocs.bed_id = ab.bed_id)
	JOIN bed_names bn ON (bn.bed_id = ab.bed_id)
	JOIN ward_names wn ON (wn.ward_no= bn.ward_no)
	JOIN bed_types bt ON (bed_type_name=bn.bed_type)
	JOIN hospital_center_master hcm USING (center_id)
;


DROP VIEW IF EXISTS rpt_service_prescriptions_ordered_details_view CASCADE;
DROP VIEW IF EXISTS rpt_test_prescriptions_ordered_details_view CASCADE;
DROP VIEW IF EXISTS rpt_doctor_prescriptions_ordered_details_view CASCADE;


---
---Used By MaintenanceActivityReport.srjs--
---

DROP VIEW IF EXISTS rpt_maint_activity_view CASCADE;
CREATE VIEW rpt_maint_activity_view AS
SELECT ama.maint_activity_id as activity_id, sitd.medicine_name as asset_name, scheduled_date,
	ama.maint_date as completed_date, maint_by, sum(coalesce(amai.labor_cost,0))+sum(coalesce(amai.part_cost,0))
	as total_cost, s.dept_name, hcm.center_name, amai.labor_cost, amai.part_cost,
	amai.component, ama.batch_no as serial_no
	FROM asset_maintenance_activity ama
	LEFT JOIN fixed_asset_master fam ON (fam.asset_id = ama.asset_id and fam.asset_serial_no = ama.batch_no)
	LEFT JOIN asset_maintenance_activity_item amai ON (amai.maint_activity_id = ama.maint_activity_id)
	LEFT JOIN store_item_details sitd ON (sitd.medicine_id = ama.asset_id)
	LEFT JOIN stores s ON (s.dept_id = fam.asset_dept)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
	GROUP BY ama.maint_activity_id, sitd.medicine_name, scheduled_date, ama.maint_date, maint_by,amai.labor_cost,
	amai.part_cost,s.dept_name, hcm.center_name,amai.component, ama.batch_no;

---
---Used By ComplaintsReport.srjs--
---

DROP VIEW IF EXISTS rpt_complaint_view CASCADE;
CREATE VIEW rpt_complaint_view AS
SELECT acm.complaint_id, sitd.medicine_name as asset_name, s.dept_name, hcm.center_name,
    acm.emp_name as complainant, acm.complaint_type,
    (CASE WHEN acm.complaint_status =0 THEN 'Recorded'
	  	WHEN acm.complaint_status =1 THEN 'Assigned'
	  	WHEN acm.complaint_status =2 THEN 'Resolved'
	  	WHEN acm.complaint_status =3 THEN 'Closed'
	  	ELSE 'NONE'
    END)  as complaint_status,
    raised_date, resolved_date, assigned_date, closed_date, complaint_closure_note,
    complaint_desc, acm.batch_no as serial_no
    FROM assert_complaint_master acm
    LEFT JOIN fixed_asset_master fam ON (fam.asset_id = acm.asset_id and fam.asset_serial_no = acm.batch_no)
    LEFT JOIN store_item_details sitd ON (sitd.medicine_id = acm.asset_id)
    LEFT JOIN stores s ON (s.dept_id = fam.asset_dept)
    LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id);
;

---
---Used By MaintenanceSchedule.srjs--
---

DROP VIEW IF EXISTS rpt_maintenance_schedule CASCADE;
CREATE VIEW rpt_maintenance_schedule AS
SELECT maint_id, am.asset_id, sitd.medicine_name as asset_name, am.batch_no as serial_no,
	maint_frequency, next_maint_date, cm.contractor_name, department, department_contact,
	fam.installation_date,fam.asset_purchase_val,fam.asset_make,fam.asset_model,
	cat.category as asset_category, s.dept_name, hcm.center_name
	FROM asset_maintenance_master  am
	LEFT JOIN fixed_asset_master fam ON (fam.asset_id = am.asset_id and fam.asset_serial_no = am.batch_no)
	LEFT JOIN store_item_details sitd ON (sitd.medicine_id = am.asset_id)
	LEFT JOIN contractor_master cm ON (cm.contractor_id = am.contractor_id)
	LEFT JOIN store_category_master cat ON (cat.category_id = sitd.med_category_id)
	LEFT JOIN stores s ON (s.dept_id = fam.asset_dept)
    LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id);

----
----
----

DROP VIEW IF EXISTS rpt_patient_package_view CASCADE;
CREATE OR REPLACE VIEW rpt_patient_package_view AS
SELECT
	pps.patient_id, pps.prescription_id, pps.common_order_id,
	pps.package_id::text AS item_id, pm.package_name,patd.name_local_language,
	pd.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name, presc_date,
	pps.remarks AS remarks,date(presc_date) as pres_date,
	b.bill_no, b.status as bill_status,bc.amount,coalesce(get_patient_name(pps.mr_no),isr.patient_name) as patient_name,pps.mr_no,
	ppsv.status,pps.handed_over,
	(CASE WHEN pps.handed_over = 'Y' THEN 'Handed over' WHEN pps.status = 'N' THEN 'Ordered' WHEN pps.status = 'X' THEN 'Cancelled' ELSE 'Completed' END) as completion_status,
	CASE WHEN pm.handover_to = 'P' THEN 'Patient' ELSE 'Sponsor' END as package_handover_to,
	CASE WHEN pm.multi_visit_package THEN 'Multi Visit' ELSE 'Single Visit' END as package_visit_type,
	hcm.center_name,date(pps.completion_time) as completion_date,pps.completion_time,
	pps.completion_by,pps.completion_by as handedover_by,pps.handover_to,pps.handover_time,date(pps.handover_time) as handover_date,
	pr.visit_type,pr.primary_sponsor_id,tm.tpa_name, pcm.package_category
FROM package_prescribed pps
	JOIN patient_package_status_view ppsv USING(prescription_id)
	LEFT JOIN patient_registration pr ON(pr.patient_id = pps.patient_id)
	LEFT JOIN patient_details patd ON (patd.mr_no = pr.mr_no)
	LEFT JOIN incoming_sample_registration isr ON (pps.patient_id = isr.incoming_visit_id)
	LEFT JOIN tpa_master tm ON(pr.primary_sponsor_id = tm.tpa_id)
	LEFT join hospital_center_master hcm ON (coalesce(pr.center_id,isr.center_id)=hcm.center_id)
	JOIN packages pm on pm.package_id = pps.package_id
	LEFT JOIN package_category_master pcm USING (package_category_id)
	LEFT OUTER JOIN doctors pd on pd.doctor_id = pps.doctor_id
	LEFT JOIN bill_activity_charge bac ON bac.activity_id=pps.prescription_id::text AND bac.activity_code='PKG'
	LEFT JOIN bill_charge bc USING(charge_id)
	LEFT JOIN bill b ON (b.bill_no = bc.bill_no)
;


DROP VIEW IF EXISTS rpt_coder_claim_review_view CASCADE;
CREATE OR REPLACE VIEW rpt_coder_claim_review_view AS 
SELECT 
	t.id ticket_id, t.title, t.body, t.created_by, t.created_at as created_date_time, t.created_at::date created_date , 
	t.status, t.patient_id, pr.mr_no, sm.salutation || ' ' || patient_name || CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' 
	ELSE (' ' || pd.middle_name) END || CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) END AS patient_name , 
	tr.user_id assignedTo, uu.temp_username assignedToName , ur.role_name assigned_to_role_name, pr.reg_date::Date reg_date , 
	cmt.review_type, commented_rows.comment_by, commented_rows.comment_at, hcm.center_name,pd.name_local_language,
  	CASE WHEN (dc.status='A' AND (dc.cancel_status = '' OR dc.cancel_status IS NULL )) THEN 'Active' 
	    WHEN (dc.status='P' AND (dc.cancel_status = '' OR dc.cancel_status IS NULL )) THEN 'Partial' 
	    WHEN (dc.status='C' AND (dc.cancel_status = '' OR dc.cancel_status IS NULL )) THEN 'Completed' 
	    WHEN (dc.status='U' AND (dc.cancel_status = '' OR dc.cancel_status IS NULL )) THEN 'Not Applicable'
	    WHEN dc.cancel_status='C' THEN 'Cancelled'
	    ELSE '' 
	END consultation_status, 
	CASE WHEN pr.codification_status='P' THEN 'In-Progress' 
	    WHEN pr.codification_status='C' THEN 'Completed' 
	    WHEN pr.codification_status='R' THEN 'Completed-Needs Verification' 
	    WHEN codification_status='V' THEN 'Verified and Closed' 
	    ELSE '' 
	END codification_status,
	dr.doctor_name as doctor_name, dc.cancel_status,
	otn.op_type_name
	FROM  reviews t INNER JOIN review_details ctd ON t.id = ctd.ticket_id LEFT JOIN review_recipients tr ON t.id=tr.ticket_id 
	INNER JOIN patient_registration pr ON t.patient_id = pr.patient_id 
	INNER JOIN patient_details pd ON pr.mr_no = pd.mr_no 
	LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id 
	INNER JOIN u_role ur ON ur.role_id = ctd.assigned_to_role 
	LEFT JOIN u_user uu ON tr.user_id = uu.emp_username  
	INNER JOIN  review_types cmt ON cmt.review_type_id = ctd.review_type_id 
	INNER JOIN hospital_center_master hcm ON pr.center_id = hcm.center_id 
	LEFT JOIN doctor_consultation dc ON pr.patient_id = dc.patient_id AND pr.doctor = dc.doctor_name 
	LEFT JOIN doctors dr ON dc.doctor_name = dr.doctor_id 
	INNER JOIN (
	    SELECT DISTINCT ON(t.id) t.id, MIN(tc.change_at) AS comment_at, tc.user_id AS comment_by FROM reviews t
        LEFT JOIN review_activities tc ON t.id = tc.ticket_id AND t.created_by != tc.user_id
        and tc.activity='COMMENT' GROUP BY t.id, tc.ticket_id,tc.user_id
    ) AS commented_rows  ON commented_rows.id = t.id
    LEFT JOIN op_type_names otn ON otn.op_type = pr.op_type
;

DROP VIEW IF EXISTS rpt_stock_take_view CASCADE;
CREATE VIEW rpt_stock_take_view AS 
SELECT 
  stock_take_id, center_id, store_id, store_name, center_name,
  initiated_by, initiated_datetime, reconciled_by, reconciled_datetime,
  approved_by, approved_datetime, status, adj_no, COUNT(item_batch_id) as total_count,
        COUNT(counted_batch_id) as completed_count, 
        round(coalesce(SUM(abs(coalesce(physical_stock_qty, 0.00) - 
          coalesce(system_stock_qty, 0.00))) * 100 / 
        SUM((NULLIF(system_stock_qty, 0.00))), 0.00), 2) as variance_perc, 
        round(coalesce(SUM(item_cost_value), 0.00), 2) as total_cost_value, 
        round(coalesce(SUM(item_cost_value * (coalesce(physical_stock_qty, 0.00) - 
          coalesce(system_stock_qty, 0.00)) / NULLIF(item_qty, 0.00)), 0.00), 2) 
        as cost_value 
FROM 
  ( SELECT 
        pst.stock_take_id, s.center_id, pst.store_id, 
        s.dept_name as store_name, hcm.center_name, 
        pst.initiated_by, pst.initiated_datetime::date, 
        pst.reconciled_by, pst.reconciled_datetime::date, 
        pst.approved_by, pst.approved_datetime::date, 
        CASE WHEN pst.status = 'I' THEN 'In Progress'
          WHEN pst.status = 'P' THEN 'In Progress'
          WHEN pst.status = 'C' THEN 'Pending Reconciliation'
          WHEN pst.status = 'R' THEN 'Pending Approval'
          WHEN pst.status = 'A' THEN 'Completed'
          WHEN pst.status = 'X' THEN 'Cancelled'
          ELSE '' END status,
        ssd.item_batch_id as item_batch_id,
        pstd.item_batch_id as counted_batch_id,
        pstd.physical_stock_qty, pstd.system_stock_qty,
        SUM(ssd.qty) as item_qty, 
        SUM(ssd.qty * sild.package_cp) as item_cost_value
        FROM physical_stock_take pst 
        JOIN stores s ON (pst.store_id = s.dept_id)
    JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id) 
    LEFT JOIN store_stock_details ssd USING (dept_id)
    LEFT JOIN store_item_lot_details sild USING (item_lot_id)
    LEFT JOIN physical_stock_take_detail pstd 
      ON (pstd.stock_take_id = pst.stock_take_id 
        AND pstd.item_batch_id = ssd.item_batch_id)
    GROUP BY pst.stock_take_id, s.center_id, pst.store_id, store_name, hcm.center_name,
          pst.initiated_by, pst.initiated_datetime, 
          pst.reconciled_by, pst.reconciled_datetime, 
          pst.approved_by, pst.approved_datetime, 
          pst.status, 
          ssd.item_batch_id, counted_batch_id, pstd.physical_stock_qty, 
          pstd.system_stock_qty) as foo 
LEFT JOIN (select stock_take_id, string_agg(adj_no::varchar, ',') as adj_no 
      FROM store_adj_main WHERE stock_take_id is not null GROUP by stock_take_id) as sam 
      USING (stock_take_id) 
GROUP BY stock_take_id, center_id, center_name, foo.store_id, store_name, 
  initiated_by, initiated_datetime, reconciled_by, 
  reconciled_datetime, approved_by, approved_datetime, status, adj_no;
