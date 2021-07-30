-- === Master views required by the accounting application for lokokup and mapping ==== 
DROP VIEW IF EXISTS acc_hospital_center_master_view;
CREATE VIEW acc_hospital_center_master_view AS
SELECT * FROM hospital_center_master;

DROP VIEW IF EXISTS acc_account_group_master_view;
CREATE VIEW acc_account_group_master_view AS
SELECT * FROM account_group_master;

DROP VIEW IF EXISTS acc_store_category_master_view;
CREATE VIEW acc_store_category_master_view AS
SELECT * FROM store_category_master;

DROP VIEW IF EXISTS acc_supplier_master_view;
CREATE VIEW acc_supplier_master_view AS
SELECT * FROM supplier_master;

DROP VIEW IF EXISTS acc_tpa_master_view;
CREATE VIEW acc_tpa_master_view AS
SELECT tpa_id, tpa_name, state, city, country, pincode,
phone_no, mobile_no, email_id, fax, contact_name, contact_designation, contact_phone, contact_mobile, contact_email FROM tpa_master;

DROP VIEW IF EXISTS acc_department_master_view;
CREATE VIEW acc_department_master_view AS
SELECT dept_id, dept_name, cost_center_code FROM department;

DROP VIEW IF EXISTS acc_doctor_master_view;
CREATE VIEW acc_doctor_master_view AS
SELECT doctor_id, doctor_name, specialization, doctor_address, doctor_mobile, doctor_mail_id, doctor_type,
dept_id, registration_no, doctor_license_number FROM doctors;

DROP VIEW IF EXISTS acc_charge_heads_view;
CREATE VIEW acc_charge_heads_view AS
SELECT chargehead_id, chargehead_name, account_head_id, chargegroup_id FROM chargehead_constants;

DROP VIEW IF EXISTS acc_charge_groups_view;
CREATE VIEW acc_charge_groups_view AS
SELECT chargegroup_id, chargegroup_name FROM chargegroup_constants;

DROP VIEW IF EXISTS acc_account_heads_view;
CREATE VIEW acc_account_heads_view AS
SELECT account_head_id, account_head_name FROM bill_account_heads;

DROP VIEW IF EXISTS acc_payment_modes_view;
CREATE VIEW acc_payment_modes_view AS
SELECT mode_id, payment_mode FROM payment_mode_master;

--==============Views for extracting the raw records, which serve as base views for voucher views =================

DROP VIEW IF EXISTS acc_all_inv_issue_bills CASCADE;

CREATE VIEW acc_all_inv_issue_bills AS
SELECT b.bill_no, b.finalized_date, date(bc.posted_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name,
	b.insurance_deduction, bc.insurance_claim_amount,
	bc.amount, bc.discount as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type,  b.visit_id, b.restriction_type, mbc.act_description_id as item_code, mbc.act_description as item_name,
	pd.patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	round(((isu.amount*isu.qty*isu.vat)/100),2) AS final_tax, 0 as discount, 0 as round_off,
	isum.user_issue_no::text, isu.vat AS vat_rate, 'ISSUE'::text as type, bc.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Store Issue Credit Item'::text as charge_item_type,
	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	hcm.center_code,
	pr.op_type, d.cost_center_code as dept_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number,
	s.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	isu.qty, ssg.service_sub_group_name, sg.service_group_name,
	doc.doctor_name as admitting_doctor, d.dept_name as admitting_department, coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor,
	pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor, cdoc.doctor_name as payee_doctor,
	cdept.dept_name as conducting_department, isu.cost_value, b.open_date, shcm.center_name as store_center,
	sid.cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
 FROM stock_issue_details isu
	JOIN bill_activity_charge bac ON isu.item_issue_no::text = bac.activity_id AND bac.activity_code = ('PHI')
	JOIN bill_charge_adjustment bc ON bc.charge_id = bac.charge_id
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = bc.bill_no
	JOIN stock_issue_main isum ON isu.user_issue_no = isum.user_issue_no
	JOIN stores s ON s.dept_id=isum.dept_from
	JOIN account_group_master gm ON gm.account_group_id=mbc.account_group
	JOIN store_item_details sid ON sid.medicine_id=isu.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)
	LEFT JOIN hospital_center_master shcm ON (shcm.center_id=s.center_id)
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
 WHERE b.restriction_type='N' AND bc.charge_head = 'INVITE';

 
DROP VIEW IF EXISTS acc_all_inv_return_bills CASCADE;
CREATE VIEW acc_all_inv_return_bills AS
SELECT b.bill_no, b.finalized_date, date(bc.posted_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name,
	b.insurance_deduction, bc.insurance_claim_amount,
	bc.amount, bc.discount as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type,  b.visit_id, b.restriction_type, mbc.act_description_id as item_code, mbc.act_description as item_name,
	pd.patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	0 AS final_tax, 0 as discount, 0 as round_off,
	sirm.user_return_no::text, 0 AS vat_rate, 'ISSUE_RETURN'::text as type, bc.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Store Return Credit Item'::text as charge_item_type,
	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	hcm.center_code,
	pr.op_type, d.cost_center_code as dept_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number,
	s.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	sird.qty, ssg.service_sub_group_name, sg.service_group_name,
	doc.doctor_name as admitting_doctor, d.dept_name as admitting_department, coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor,
	pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor, cdoc.doctor_name as payee_doctor,
	cdept.dept_name as conducting_department, sird.cost_value, b.open_date, shcm.center_name as store_center,
	sid.cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
 FROM store_issue_returns_details sird
	JOIN bill_activity_charge bac ON sird.item_return_no::text = bac.activity_id AND bac.activity_code = ('PHI')
	JOIN bill_charge_adjustment bc ON bc.charge_id = bac.charge_id
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = bc.bill_no
	JOIN store_issue_returns_main sirm ON sirm.user_return_no = sird.user_return_no
	JOIN stores s ON s.dept_id=sirm.dept_to
	JOIN account_group_master gm ON gm.account_group_id=mbc.account_group
	JOIN store_item_details sid ON sid.medicine_id=sird.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON pr.center_id=hcm.center_id
	LEFT JOIN hospital_center_master shcm ON s.center_id=shcm.center_id
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
 WHERE b.restriction_type='N' AND bc.charge_head = 'INVRET';

DROP VIEW IF EXISTS acc_all_pharmacy_bills CASCADE;
CREATE OR REPLACE VIEW acc_all_pharmacy_bills AS
SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name,
	b.insurance_deduction, pms.insurance_claim_amt,
	(pms.amount-pms.tax) as amount, pms.disc as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type,  b.visit_id, b.restriction_type, pms.medicine_id::text as item_code, sid.medicine_name as item_name,
	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	pms.tax AS final_tax, 0 as discount, 0 as round_off,
	pmsm.sale_id, pms.tax_rate AS vat_rate, pmsm.type, pmsm.date_time as mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Pharmacy Credit Item'::text as charge_item_type,
	scm.category_id, scm.sales_cat_vat_account_prefix, gd.sales_store_vat_account_prefix,
	hcm.center_code,
	pr.op_type, d.cost_center_code as dept_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number,
	gd.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	ssg.service_sub_group_name, sg.service_group_name, pms.cost_value, b.open_date, shcm.center_name as store_center,
	sid.cust_item_code,date(pmsm.sale_date) as sale_date,pms.sale_item_id,pms.quantity
FROM store_sales_main pmsm
	JOIN bill b ON b.bill_no = pmsm.bill_no
--	LEFT JOIN bill_charge_adjustment bca ON(bca.charge_id = pmsm.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=(case when b.bill_type='P' and pmsm.type='S' then 'PHMED'
		when b.bill_type='P' and pmsm.type='R' then 'PHRET'
		when b.bill_type='C' and pmsm.type='S' then 'PHCMED'
		when b.bill_type='C' and pmsm.type='R' then 'PHCRET'
			else 'HOSPITAL_OR_ISSUE_ITEM' end))
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=cc.chargegroup_id)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN store_sales_details pms ON pmsm.sale_id = pms.sale_id
	JOIN store_item_details sid ON pms.medicine_id=sid.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN stores gd ON pmsm.store_id=gd.dept_id
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id or hcm.center_id=prc.center_id)
	LEFT JOIN hospital_center_master shcm ON (shcm.center_id = gd.center_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	JOIN account_group_master gm ON gm.account_group_id=gd.account_group  -- (dont move this to the top joins: performance issue)
WHERE b.status != 'X' AND bill_type IN ('C', 'P');

DROP VIEW IF EXISTS acc_all_pharmacy_discounts CASCADE;
CREATE VIEW acc_all_pharmacy_discounts AS
SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name,picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	''::text as account_head_name, 0 as insurance_deduction, 0.00 as insurance_claim_amt,
	pmsm.discount AS amount, 0 as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type, b.visit_id as visit_id, b.restriction_type, ''::text as item_code, ''::text as item_name,
	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	0 AS final_tax, 0.0 as discount, pmsm.round_off, pmsm.sale_id, 0 AS vat_rate, pmsm.type, b.mod_time,
	b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Pharmacy Credit Discount'::text as charge_item_type,
	0 as category_id, ''::text as sales_cat_vat_account_prefix, ''::text as sales_store_vat_account_prefix,
	hcm.center_code, pr.op_type, d.cost_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number as audit_control_number,
	gd.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	''::text as service_sub_group_name, ''::text as service_group_name, b.open_date, 
	shcm.center_name as store_center,date(pmsm.sale_date) as sale_date
FROM store_sales_main pmsm
	JOIN bill b ON b.bill_no = pmsm.bill_no
   	JOIN chargehead_constants cc ON (cc.chargehead_id=(case when b.bill_type='P' and pmsm.type='S' then 'PHMED'
		when b.bill_type='C' and pmsm.type='S' then 'PHCMED'
		when b.bill_type='P' and pmsm.type='R' then 'PHRET'
		when b.bill_type='C' and pmsm.type='R' then 'PHCRET'
			else 'HOSPITAL_OR_ISSUE_ITEM' end))
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=cc.chargegroup_id)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN stores gd ON pmsm.store_id=gd.dept_id
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id or hcm.center_id=prc.center_id)
	LEFT JOIN hospital_center_master shcm ON (shcm.center_id=gd.center_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	JOIN account_group_master gm ON gm.account_group_id=gd.account_group  -- (dont move this to the top joins: performance issue)
WHERE b.status != 'X' AND bill_type IN ('C', 'P') AND (pmsm.discount > 0)
;

DROP VIEW IF EXISTS acc_all_pharmacy_roundoffs CASCADE;
CREATE VIEW acc_all_pharmacy_roundoffs AS
SELECT b.bill_no, b.finalized_date, date(finalized_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	''::text as account_head_name, 0 as insurance_deduction, 0.00 as insurance_claim_amt,
	pmsm.round_off AS amount, 0 as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type, b.visit_id as visit_id, b.restriction_type, ''::text as item_code, ''::text as item_name,
	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	0 AS final_tax, 0.0 as discount, 0.00 as round_off, pmsm.sale_id, 0 AS vat_rate, pmsm.type, b.mod_time,
	b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Pharmacy Credit Discount'::text as charge_item_type,
	0 as category_id, ''::text as sales_cat_vat_account_prefix, ''::text as sales_store_vat_account_prefix,
	hcm.center_code, pr.op_type, d.cost_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number as audit_control_number,
	gd.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	''::text as service_sub_group_name, ''::text as service_group_name, b.open_date, 
	shcm.center_name as store_center,date(pmsm.sale_date) as sale_date
FROM store_sales_main pmsm
	JOIN bill b ON b.bill_no = pmsm.bill_no
   	JOIN chargehead_constants cc ON (cc.chargehead_id=(case when b.bill_type='P' and pmsm.type='S' then 'PHMED'
		when b.bill_type='C' and pmsm.type='S' then 'PHCMED'
		when b.bill_type='P' and pmsm.type='R' then 'PHRET'
		when b.bill_type='C' and pmsm.type='R' then 'PHCRET'
			else 'HOSPITAL_OR_ISSUE_ITEM' end))
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=cc.chargegroup_id)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN stores gd ON pmsm.store_id=gd.dept_id
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id or hcm.center_id=prc.center_id)
	LEFT JOIN hospital_center_master shcm ON (shcm.center_id=gd.center_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	JOIN account_group_master gm ON gm.account_group_id=gd.account_group  -- (dont move this to the top joins: performance issue)
WHERE b.status != 'X' AND bill_type IN ('C', 'P') AND (pmsm.round_off != 0);



--DROP VIEW IF EXISTS acc_all_changed_sponsor_charges_view CASCADE;
--DROP VIEW IF EXISTS acc_all_changed_sponsor_charges CASCADE;
--CREATE VIEW acc_all_changed_sponsor_charges AS
-- Join bill charge adjustments with the inner window query to pull out all records where the sponsor = prev sponsor
-- but the adjustment id prior to the sponsor switch. This represents all adjustments that happened against the old
-- sponsor. We also select the new sponsor, simultaneously so that we can use the same view for posting a contra against 
-- the old sponsor and an adjustment against the new one. 
--SELECT bca.charge_id, bca.bill_charge_adjustment_id, bca.primary_sponsor_id, fbca.primary_sponsor_id as new_sponsor_id,
--fbca.posted_date as new_posted_date, fbca.mod_time as new_mod_time
--FROM bill_charge_adjustment bca JOIN
--	(SELECT * from
-- select all bill charge adjustment records grouped by charge id and ordered by adjustment id within the group
-- along with the previous sponsor id and the next sponsor id and filter all the records where previous sponsor was 
-- different from the current sponsor. These represent the records where tpa switch happened
--		(SELECT charge_id, bill_charge_adjustment_id, posted_date, mod_time,
	--		lag(primary_sponsor_id) OVER (PARTITION BY charge_id order by bill_charge_adjustment_id) as prev_sponsor_id,
		--	primary_sponsor_id, 
			--lead(primary_sponsor_id) OVER (PARTITION BY charge_id order by bill_charge_adjustment_id) as next_sponsnor_id
--		FROM bill_charge_adjustment) gbca where prev_sponsor_id != primary_sponsor_id) fbca 
	--ON (fbca.charge_id = bca.charge_id) 
--	WHERE bca.bill_charge_adjustment_id < fbca.bill_charge_adjustment_id 
--	AND bca.primary_sponsor_id = fbca.prev_sponsor_id;

	
	
DROP VIEW IF EXISTS acc_all_changed_sponsor_charges_view CASCADE;
DROP VIEW IF EXISTS acc_all_changed_sponsor_charges CASCADE;
CREATE VIEW acc_all_changed_sponsor_charges AS
-- Join bill charge adjustments with the inner window query to pull out all records where the sponsor = prev sponsor
-- but the adjustment id prior to the sponsor switch. This represents all adjustments that happened against the old
-- sponsor. We also select the new sponsor, simultaneously so that we can use the same view for posting a contra against 
-- the old sponsor and an adjustment against the new one. 
SELECT bca.charge_id, bca.bill_charge_adjustment_id, fbca.prev_sponsor_id as primary_sponsor_id, fbca.primary_sponsor_id as new_sponsor_id,
fbca.posted_date as new_posted_date, fbca.mod_time as new_mod_time
FROM bill_charge_adjustment bca JOIN
	(SELECT * from
-- select all bill charge adjustment records grouped by charge id and ordered by adjustment id within the group
-- along with the previous sponsor id and the next sponsor id and filter all the records where previous sponsor was 
-- different from the current sponsor. These represent the records where tpa switch happened
		(SELECT sponsor_switch,charge_id, bill_charge_adjustment_id, posted_date, mod_time,
			lag(primary_sponsor_id) OVER (PARTITION BY charge_id order by bill_charge_adjustment_id) as prev_sponsor_id,
			primary_sponsor_id, 
			lead(primary_sponsor_id) OVER (PARTITION BY charge_id order by bill_charge_adjustment_id) as next_sponsnor_id
		FROM bill_charge_adjustment) gbca where sponsor_switch = true ) fbca 
	ON (fbca.charge_id = bca.charge_id) 
	WHERE bca.bill_charge_adjustment_id < fbca.bill_charge_adjustment_id 
	AND bca.sponsor_switch = false;
	
--SELECT b.charge_id, b.bill_charge_adjustment_id, b.posted_date as new_posted_date, b.mod_time as new_mod_time, 
--	max(a.bill_charge_adjustment_id) AS old_charge_adjustment_id 
--FROM bill_charge_adjustment b JOIN bill_charge_adjustment a 
--ON (a.charge_id = b.charge_id AND a.primary_sponsor_id != b.primary_sponsor_id
--	AND b.bill_charge_adjustment_id > a.bill_charge_adjustment_id )
--GROUP BY b.charge_id, b.bill_charge_adjustment_id, b.posted_date, b.mod_time ;

-- same as inventory issue bills, except that adjustments are not considered, since adjustments lead to duplicate 
-- entries as far as cost accounting is concerned
  
DROP VIEW IF EXISTS acc_all_stock_issues CASCADE;

CREATE VIEW acc_all_stock_issues AS
SELECT b.bill_no, b.finalized_date, date(mbc.posted_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name,
	b.insurance_deduction, mbc.insurance_claim_amount,
	mbc.amount, mbc.discount as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type,  b.visit_id, b.restriction_type, mbc.act_description_id as item_code, mbc.act_description as item_name,
	pd.patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	round(((isu.amount*isu.qty*isu.vat)/100),2) AS final_tax, 0 as discount, 0 as round_off,
	isum.user_issue_no::text, isu.vat AS vat_rate, 'ISSUE'::text as type, mbc.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Store Issue Credit Item'::text as charge_item_type,
	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	hcm.center_code,
	pr.op_type, d.cost_center_code as dept_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number,
	s.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	isu.qty, ssg.service_sub_group_name, sg.service_group_name,
	doc.doctor_name as admitting_doctor, d.dept_name as admitting_department, coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor,
	pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor, cdoc.doctor_name as payee_doctor,
	cdept.dept_name as conducting_department, isu.cost_value, b.open_date, shcm.center_name as store_center,sid.cust_item_code
 FROM stock_issue_details isu
	JOIN bill_activity_charge bac ON isu.item_issue_no::text = bac.activity_id AND bac.activity_code = ('PHI')
--	JOIN bill_charge_adjustment bc ON bc.charge_id = bac.charge_id
	JOIN bill_charge mbc ON (mbc.charge_id=bac.charge_id)
	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = mbc.bill_no
	JOIN stock_issue_main isum ON isu.user_issue_no = isum.user_issue_no
	JOIN stores s ON s.dept_id=isum.dept_from
	JOIN account_group_master gm ON gm.account_group_id=mbc.account_group
	JOIN store_item_details sid ON sid.medicine_id=isu.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)
	LEFT JOIN hospital_center_master shcm ON (shcm.center_id=s.center_id)
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
WHERE b.restriction_type='N' AND mbc.charge_head = 'INVITE';

DROP VIEW IF EXISTS acc_all_stock_returns CASCADE;

CREATE VIEW acc_all_stock_returns AS
SELECT b.bill_no, b.finalized_date, date(mbc.posted_date) as voucher_date, b.is_tpa, tm.tpa_name as primary_sponsor_name,
	stpa.tpa_name as secondary_sponsor_name, picm.insurance_co_name as primary_insurance_co, sicm.insurance_co_name as secondary_insurance_co,
	case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end as account_head_name,
	b.insurance_deduction, mbc.insurance_claim_amount,
	mbc.amount, mbc.discount as item_discount, b.primary_total_claim, b.secondary_total_claim,
	b.total_receipts, coalesce(b.deposit_set_off, 0) as deposit_set_off, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	b.bill_type, b.visit_type,  b.visit_id, b.restriction_type, mbc.act_description_id as item_code, mbc.act_description as item_name,
	pd.patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, pd.oldmrno,
	0 AS final_tax, 0 as discount, 0 as round_off,
	sirm.user_return_no::text, 0 AS vat_rate, 'ISSUE_RETURN'::text as type, mbc.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id as inter_comp_account_group_id, 'Store Return Credit Item'::text as charge_item_type,
	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	hcm.center_code,
	pr.op_type, d.cost_center_code as dept_center_code,
	hcm.center_id as visit_center_id, hcm.center_name as visit_center_name, pr.mr_no, b.audit_control_number,
	s.dept_name as store_name, cc.chargehead_name as charge_head, cg.chargegroup_name as charge_group,
	sird.qty, ssg.service_sub_group_name, sg.service_group_name,
	doc.doctor_name as admitting_doctor, d.dept_name as admitting_department, coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor,
	pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor, cdoc.doctor_name as payee_doctor,
	cdept.dept_name as conducting_department, sird.cost_value, b.open_date, shcm.center_name as store_center,
	sid.cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
 FROM store_issue_returns_details sird
	JOIN bill_activity_charge bac ON sird.item_return_no::text = bac.activity_id AND bac.activity_code = ('PHI')
--	JOIN bill_charge_adjustment bc ON bc.charge_id = bac.charge_id
	JOIN bill_charge mbc ON (mbc.charge_id=bac.charge_id)
	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = mbc.bill_no
	JOIN store_issue_returns_main sirm ON sirm.user_return_no = sird.user_return_no
	JOIN stores s ON s.dept_id=sirm.dept_to
	JOIN account_group_master gm ON gm.account_group_id=mbc.account_group
	JOIN store_item_details sid ON sid.medicine_id=sird.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON pr.center_id=hcm.center_id
	LEFT JOIN hospital_center_master shcm ON s.center_id=shcm.center_id
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
 WHERE b.restriction_type='N' AND mbc.charge_head = 'INVRET';

 DROP VIEW IF EXISTS acc_consolidated_tax_adjustments_view CASCADE;
 
 CREATE VIEW acc_consolidated_tax_adjustments_view AS
 SELECT charge_id, tax_sub_group_id, old_tax_sub_group_id, txn_id,
 	max(COALESCE(claim_id, '')) as claim_id, max(COALESCE(old_claim_id, '')) as old_claim_id, max(mod_time) as mod_time, 
 	sum(COALESCE(tax_amt, 0.00)) as tax_amt, sum(COALESCE(sponsor_tax_amount, 0.00)) as sponsor_tax_amount,
 	sum(COALESCE(old_tax_amt, 0.00)) as old_tax_amt, sum(COALESCE(old_sponsor_tax_amount, 0.00)) as old_sponsor_tax_amount
 FROM bill_charge_details_adjustment
 GROUP BY charge_id, tax_sub_group_id, old_tax_sub_group_id, txn_id;
 
DROP VIEW IF EXISTS acc_all_sub_group_sponsor_tax_amt_view CASCADE ;
CREATE VIEW acc_all_sub_group_sponsor_tax_amt_view AS 
SELECT (sstd.tax_amt-sum(sctd.tax_amt)) as tax_amt,sstd.item_subgroup_id,sstd.sale_item_id,sstd.tax_rate FROM store_sales_tax_details sstd JOIN sales_claim_tax_details sctd 
ON(sstd.sale_item_id = sctd.sale_item_id AND sstd.item_subgroup_id = sctd.item_subgroup_id) JOIN store_sales_details ssd ON(ssd.sale_item_id = sstd.sale_item_id) 
JOIN store_sales_main ssm ON (ssm.sale_id=ssd.sale_id) WHERE ssm.type = 'S'  group by sstd.item_subgroup_id, sstd.tax_amt, sstd.tax_rate,sstd.sale_item_id; 
 --==================All Accounting voucher views. These will be used by the cron job to export the vouchers ==================
 --===================================RECEIPT VOUCHERS ==========================================================

-- old insurance remittance receipts

DROP VIEW IF EXISTS acc_all_remittance_receipts_view_old;
CREATE VIEW acc_all_remittance_receipts_view_old AS
SELECT ir.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head,
	b.account_group, sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group,
	b.bill_no, b.audit_control_number, ir.remittance_id as voucher_no, 'RECEIPT'::text as voucher_type, date(ir.received_date) as voucher_date,
	bc.act_description_id::text as item_code, bc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN ipa.amount >= 0.00 THEN ipa.amount ELSE -ipa.amount END as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN ipa.amount >= 0.00 THEN 'Bank A/C'::text ELSE tpa.tpa_name END as debit_account,
	CASE WHEN ipa.amount >= 0.00 THEN tpa.tpa_name ELSE 'Bank A/C'::text END as credit_account,
	0.00 as tax_amount,
	CASE WHEN ipa.amount  >= 0.00 THEN ipa.amount ELSE -ipa.amount END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conducting_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoming_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ipa.payment_reference as voucher_ref, ''::text as remarks,
	ir.mod_time, ''::text as counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN ipa.amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type, 
	ipa.amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM insurance_payment_allocation ipa
	JOIN insurance_remittance ir ON (ipa.remittance_id = ir.remittance_id)
	JOIN bill_charge bc ON (bc.charge_id = ipa.charge_id)
	JOIN tpa_master tpa ON (ir.tpa_id = tpa.tpa_id)
 	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN bill_claim c ON (c.claim_id = ipa.claim_id AND c.bill_no = bc.bill_no)
	JOIN bill b ON (b.bill_no = bc.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	JOIN patient_details pd on (pr.mr_no = pd.mr_no)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	JOIN hospital_center_master hcm ON (hcm.center_id = ir.center_id) ;
	
-- insurance remittance receipts

DROP VIEW IF EXISTS acc_all_remittance_receipts_view;

CREATE VIEW acc_all_remittance_receipts_view AS
SELECT ir.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head,
	b.account_group, sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group,
	b.bill_no, b.audit_control_number, ir.remittance_id as voucher_no, 'RECEIPT'::text as voucher_type, date(ir.received_date) as voucher_date,
	bc.act_description_id::text as item_code, bc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN irad.payment_amount >= 0.00 THEN irad.payment_amount ELSE -irad.payment_amount END as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN irad.payment_amount >= 0.00 THEN 'Bank A/C'::text ELSE tpa.tpa_name END as debit_account,
	CASE WHEN irad.payment_amount >= 0.00 THEN tpa.tpa_name ELSE 'Bank A/C'::text END as credit_account,
	0.00 as tax_amount,
	CASE WHEN irad.payment_amount  >= 0.00 THEN irad.payment_amount ELSE -irad.payment_amount END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conducting_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ird.payment_reference as voucher_ref, ''::text as remarks,
	ir.mod_time as mod_time, ''::text as counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN irad.payment_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type, 
	irad.payment_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
 FROM insurance_remittance ir
    JOIN insurance_remittance_details ird  ON ird.remittance_id = ir.remittance_id
    JOIN insurance_remittance_activity_details irad ON ird.remittance_id = irad.remittance_id and irad.claim_id = ird.claim_id
    LEFT JOIN bill_charge bc ON (split_part(irad.activity_id, '-', 2) = bc.charge_id)
	JOIN tpa_master tpa ON (ir.tpa_id = tpa.tpa_id)
 	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	JOIN bill_claim c ON (c.claim_id = irad.claim_id AND c.bill_no = bc.bill_no)
	JOIN bill b ON (b.bill_no = bc.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	JOIN patient_details pd on (pr.mr_no = pd.mr_no)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	JOIN hospital_center_master hcm ON (hcm.center_id = ir.center_id) WHERE ir.processing_status = 'C' AND split_part(irad.activity_id, '-', 2) != 'ACT'
	
	UNION ALL
	
	SELECT ir.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head,
	b.account_group, sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group,
	b.bill_no, b.audit_control_number, ir.remittance_id as voucher_no, 'RECEIPT'::text as voucher_type, date(ir.received_date) as voucher_date,
	bc.act_description_id::text as item_code, bc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN irad.payment_amount >= 0.00 THEN irad.payment_amount ELSE -irad.payment_amount END as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN irad.payment_amount >= 0.00 THEN 'Bank A/C'::text ELSE tpa.tpa_name END as debit_account,
	CASE WHEN irad.payment_amount >= 0.00 THEN tpa.tpa_name ELSE 'Bank A/C'::text END as credit_account,
	0.00 as tax_amount,
	CASE WHEN irad.payment_amount  >= 0.00 THEN irad.payment_amount ELSE -irad.payment_amount END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conducting_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ird.payment_reference as voucher_ref, ''::text as remarks,
	ir.mod_time as mod_time, ''::text as counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN irad.payment_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type, 
	irad.payment_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
 FROM insurance_remittance ir
    JOIN insurance_remittance_details ird  ON ird.remittance_id = ir.remittance_id
    JOIN insurance_remittance_activity_details irad ON ird.remittance_id = irad.remittance_id and irad.claim_id = ird.claim_id
    LEFT JOIN bill_charge bc ON (split_part(irad.activity_id, '-', 3) = bc.charge_id)
	JOIN tpa_master tpa ON (ir.tpa_id = tpa.tpa_id)
 	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	JOIN bill_claim c ON (c.claim_id = irad.claim_id AND c.bill_no = bc.bill_no)
	JOIN bill b ON (b.bill_no = bc.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	JOIN patient_details pd on (pr.mr_no = pd.mr_no)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	JOIN hospital_center_master hcm ON (hcm.center_id = ir.center_id) WHERE ir.processing_status = 'C' AND split_part(irad.activity_id, '-', 2) = 'ACT';



-- all receipts 
-- filtered on mod_time, account_group, center_id
DROP VIEW IF EXISTS acc_all_receipts_view CASCADE;
CREATE VIEW acc_all_receipts_view AS
SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id,
	'' as charge_group, '' as charge_head, b.account_group,
	'' as service_group, '' as service_sub_group, b.bill_no, b.audit_control_number,
	r.receipt_no as voucher_no, 'RECEIPT' as voucher_type, date(r.display_date) as voucher_date,
	'' as item_code, '' as item_name, ''::text as receipt_store, ''::text as issue_store,
	cur.currency as currency, r.exchange_rate as currency_conversion_rate,
	0.00 as quantity, '' as unit, 0.00 as unit_rate,
	r.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	pm.payment_mode as debit_account,  
	case when r.payment_type = 'S' then ( case when r.sponsor_index = 'P' then tm.tpa_name::text else stpa.tpa_name::text end) 
	else 'Counter Receipts'::text end as credit_account,
	r.tds_amt as tax_amount, r.amount - r.tds_amt as net_amount,
	'' as admitting_doctor, '' as prescribing_doctor, '' as conductiong_doctor,
	'' as referral_doctor, '' as payee_doctor, '' as outhouse_name, '' as incoimng_hospital,
	'' as admitting_department, '' as conducting_department, 0.00 as cost_amount, '' as supplier_name,
	'' as invoice_no, NULL::timestamp as invoice_date, '' as voucher_ref, r.remarks, r.mod_time, c.counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	picm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	r.amount as transaction_amount, tm.tpa_name as sponsor_name, tm.sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code ,''::text as prescribing_doctor_dept_name  
FROM bill_receipts r
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no=pr.mr_no)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=b.visit_id)
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN salutation_master sm ON (sm.salutation_id=pd.salutation)
	LEFT JOIN tpa_master tm ON (pr.primary_sponsor_id = tm.tpa_id)
	LEFT JOIN tpa_master stpa ON (pr.secondary_sponsor_id=stpa.tpa_id)
	JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id)
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id)
	JOIN counters c ON (r.counter=c.counter_id and collection_counter='Y')
	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
	LEFT JOIN foreign_currency cur ON (cur.currency_id = r.currency_id)
WHERE r.payment_type != 'F'

UNION ALL
-- all refunds
SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id,
	'' as charge_group, '' as charge_head, b.account_group,
	'' as service_group, '' as service_sub_group, b.bill_no, b.audit_control_number,
	r.receipt_no as voucher_no, 'PAYMENT' as voucher_type, date(r.display_date) as voucher_date,
	'' as item_code, '' as item_name, '' as receipt_store, '' as issue_store,
	cur.currency as currency, r.exchange_rate as currency_conversion_rate,
	0.00 as quantity, '' as unit, 0.00 as unit_rate,
	-r.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Counter Receipts'::text as debit_account, pm.payment_mode as credit_account,
	r.tds_amt as tax_amount, -r.amount + r.tds_amt as net_amount,
	'' as admitting_doctor, '' as prescribing_doctor, '' as conductiong_doctor,
	'' as referral_doctor, '' as payee_doctor, '' as outhouse_name, '' as incoimng_hospital,
	'' as admitting_department, '' as conducting_department, 0.00 as cost_amount, '' as supplier_name,
	'' as invoice_no, NULL::timestamp as invoice_date, '' as voucher_ref, r.remarks, r.mod_time, c.counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	picm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, -- voucher type is payment so transaction is normal
	r.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM bill_receipts r
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no=pr.mr_no)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=b.visit_id)
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN salutation_master sm ON (sm.salutation_id=pd.salutation)
	LEFT JOIN tpa_master tm ON (pr.primary_sponsor_id = tm.tpa_id)
	LEFT JOIN tpa_master stpa ON (pr.secondary_sponsor_id=stpa.tpa_id)
	LEFT JOIN insurance_company_master picm on (pr.primary_insurance_co = picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm on (pr.secondary_insurance_co = sicm.insurance_co_id)
	JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id)
	LEFT JOIN card_type_master cm ON (cm.card_type_id = r.card_type_id)
	JOIN counters c ON (r.counter=c.counter_id and collection_counter='Y')
	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
	LEFT JOIN foreign_currency cur ON (cur.currency_id = r.currency_id)
WHERE r.payment_type = 'F';


-- filtered on mod_time, account_group, center_id
DROP VIEW IF EXISTS acc_all_deposits_view CASCADE;
CREATE VIEW acc_all_deposits_view AS
-- deposit receipts
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, r.mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, cav.account_group_id as account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	r.receipt_no as voucher_no, 'RECEIPT'::text as voucher_type, date(r.display_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	r.currency as currency, r.exchange_rate as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	r.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	r.payment_mode as debit_account,
	CASE WHEN (r.deposit_available_for = 'B') THEN
		CASE WHEN r.package_id is null THEN 'General Deposit Liability Account'::text ELSE 'Package Deposit Liability Account'::text END
	ELSE 'IP Deposit Liability Account'::text END as credit_account,
	r.tds_amt as tax_amount, r.amount - r.tds_amt as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, r.remarks, r.mod_time, r.counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	r.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM deposits_receipts_view r
	LEFT JOIN patient_details pd ON (r.mr_no = pd.mr_no)
	JOIN counter_associated_accountgroup_view cav ON r.counter = counter_id
	JOIN hospital_center_master hcm ON (hcm.center_id=r.center_id)
WHERE payment_type IN ('DR')
UNION ALL
-- deposit refunds
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, r.mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, cav.account_group_id as account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	r.receipt_no as voucher_no, 'PAYMENT'::text as voucher_type, date(r.display_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	r.currency as currency, r.exchange_rate as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	-r.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (r.deposit_available_for = 'B') THEN
		CASE WHEN r.package_id is null THEN 'General Deposit Liability Account'::text ELSE 'Package Deposit Liability Account'::text END
	ELSE 'IP Deposit Liability Account'::text END as debit_account,
	r.payment_mode as credit_account,
	r.tds_amt as tax_amount, -r.amount + r.tds_amt as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, r.remarks, r.mod_time, r.counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, -- voucher type is payment so tx type is normal
	r.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM deposits_receipts_view r
	LEFT JOIN patient_details pd ON (r.mr_no = pd.mr_no)
	JOIN counter_associated_accountgroup_view cav ON r.counter = counter_id
	JOIN hospital_center_master hcm ON (hcm.center_id=r.center_id)
WHERE payment_type IN ('DF');


-- filtered on mod_time, COALESCE(cav.account_group_id, 1), center_id
DROP VIEW IF EXISTS acc_all_consolidate_sponsor_receipts_view CASCADE;

CREATE VIEW acc_all_consolidate_sponsor_receipts_view AS
-- consolidated sponsor receipts
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, 1 as account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	r.receipt_no as voucher_no, 'RECEIPT'::text as voucher_type, date(r.display_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	fc.currency as currency, r.exchange_rate as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	r.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	pm.payment_mode as debit_account, 'Counter Receipts'::text as credit_account,
	0.00 as tax_amount, r.amount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, r.remarks, r.mod_time, c.counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	icm.insurance_co_name as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	r.amount as transaction_amount, tm.tpa_name as sponsor_name, tm.sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM insurance_claim_receipt r
	JOIN tpa_master tm ON (tm.tpa_id = r.tpa_id)
	JOIN counters c ON (c.counter_id = r.counter)
	JOIN payment_mode_master pm ON (pm.mode_id = r.payment_mode_id)
	JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)
	LEFT JOIN card_type_master ctm ON (ctm.card_type_id = r.card_type_id)
	LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = r.insurance_co_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
WHERE c.collection_counter = 'Y';


--==========================PAYMENTS VOUCHERS =======================================================

-- filtered on mod_time, account_group, center_id
DROP VIEW IF EXISTS acc_all_payment_vouchers_view CASCADE ;
CREATE VIEW acc_all_payment_vouchers_view AS
-- all payments done through insta
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, apvv.account_group_id as account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	apvv.voucher_no as voucher_no, 'PAYMENT'::text as voucher_type, date(apvv.date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	apvv.amount as gross_amount, apvv.round_off as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN apvv.voucher_category = 'R' THEN apvv.payment_mode ELSE apvv.payment_receiver END as debit_account, 
	CASE WHEN apvv.voucher_category = 'R' THEN apvv.payment_receiver ELSE apvv.payment_mode END as credit_account,
	apvv.tax_amount as tax_amount, apvv.amount - apvv.tax_amount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor, ''::text as referral_doctor,
	CASE WHEN apvv.payment_type = 'D' OR apvv.payment_type ='R' OR apvv.payment_type = 'P' OR apvv.payment_type = 'F' THEN apvv.payment_receiver ELSE ''::text END as payee_doctor,
	CASE WHEN apvv.payment_type = 'O' THEN apvv.payment_receiver ELSE ''::text END as outhouse_name,
	''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount,
	CASE WHEN apvv.payment_type = 'S' THEN apvv.payment_receiver ELSE ''::text END as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, apvv.reference_no::text as voucher_ref, ''::text as remarks, apvv.mod_time, c.counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	apvv.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	sm.cust_supplier_code as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM all_payment_vouchers_view apvv
LEFT JOIN supplier_master sm ON apvv.payee_name = sm.supplier_code 
LEFT JOIN hospital_center_master hcm ON (apvv.center_id = hcm.center_id)
LEFT JOIN counters c ON apvv.counter = c.counter_id
ORDER BY voucher_category;

-- filtered on mod_time, account_group, expense_center_id
DROP VIEW IF EXISTS acc_all_payments_due_view CASCADE ;
CREATE VIEW acc_all_payments_due_view AS
-- All payments due 
SELECT hcm.center_id, hcm.center_name, apdv.visit_type, coalesce(pr.mr_no, '')::text as mr_no, coalesce(b.visit_id, '')::text as visit_id,
	''::text as charge_group, ''::text as charge_head, apdv.account_group,
	''::text as service_group, ''::text as service_sub_group, apdv.bill_no, b.audit_control_number as audit_control_number,
	apdv.voucher_no as voucher_no, 'PAYMENTDUE'::text as voucher_type, date(apdv.posted_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	apdv.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Payment Due A/C'::text as debit_account, apdv.name as credit_account,
	0.00 as tax_amount, apdv.amount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor, ''::text as referral_doctor,
	CASE WHEN apdv.payment_type = 'D' OR apdv.payment_type ='R' OR apdv.payment_type = 'P' OR apdv.payment_type = 'F' THEN apdv.name ELSE ''::text END as payee_doctor,
	CASE WHEN apdv.payment_type = 'O' THEN apdv.name ELSE ''::text END as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, apdv.mod_time, ''::text as counter_no,
	b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	apdv.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	sm.cust_supplier_code as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name   
FROM all_payments_due_view apdv
LEFT JOIN supplier_master sm ON apdv.payee_name = sm.supplier_code
LEFT JOIN bill b ON (b.bill_no = apdv.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
LEFT JOIN hospital_center_master hcm ON (apdv.expense_center_id = hcm.center_id)
ORDER BY payment_id;

--=============================================== PURCHASE TRANSACTIONS ======================================================

-- filtered on date_time, account_group, center_id
DROP VIEW IF EXISTS acc_all_invoices_view CASCADE ;
CREATE VIEW acc_all_invoices_view AS
-- Supplier invoices
SELECT * FROM (
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	i.invoice_no||'/'||gm.grn_no as voucher_no, 'PURCHASE'::text as voucher_type, i.invoice_date as voucher_date,
	pmd.medicine_id::text as item_code, pmd.medicine_name as item_name, s.dept_name as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	g.billed_qty as quantity, g.grn_pkg_size as unit, g.cost_price as unit_rate,
	(g.cost_price*g.billed_qty/g.grn_pkg_size) as gross_amount, 0.00 as round_off_amount, g.discount+g.scheme_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	icm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Inventory A/C'::text as debit_account, sup.supplier_name as credit_account,
	0.00 as tax_amount, (g.cost_price*g.billed_qty/g.grn_pkg_size) - (g.discount+g.scheme_discount) as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	i.invoice_no as invoice_no, i.invoice_date as invoice_date, gm.grn_no::text as voucher_ref, ''::text as remarks, i.date_time as mod_time, ''::text as counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	i.po_no::text as po_number, p.po_date::date as po_date, 'N'::character as transaction_type,
	(g.cost_price*g.billed_qty/g.grn_pkg_size) - (g.discount+g.scheme_discount) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	''::text as account_name, sup.cust_supplier_code as cust_supplier_code,gm.grn_date as grn_date,
	pmd.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name 
FROM store_invoice i
	JOIN store_grn_main gm USING (supplier_invoice_id)
	JOIN store_grn_details g USING (grn_no)
	JOIN store_item_details pmd using (medicine_id)
	JOIN stores s on s.dept_id=gm.store_id
	JOIN hospital_center_master hcm ON s.center_id=hcm.center_id
	JOIN supplier_master sup ON (i.supplier_id = sup.supplier_code)
	JOIN store_category_master icm ON (pmd.med_category_id=icm.category_id)
	LEFT JOIN store_po_main p ON (p.po_no = i.po_no) 
WHERE i.consignment_stock=false and i.status!='O'

UNION ALL
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	i.invoice_no||'/'||gm.grn_no as voucher_no, 'INVDISC'::text as voucher_type, i.invoice_date as voucher_date,
	''::text as item_code, ''::text as item_name, s.dept_name as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0 as quantity, 0 as unit, 0.00 as unit_rate,
	i.discount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	sup.supplier_name as debit_account,'Inventory A/C'::text  as credit_account,
	0 as tax_amount, i.discount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	i.invoice_no as invoice_no, i.invoice_date as invoice_date, gm.grn_no::text as voucher_ref, ''::text as remarks, i.date_time as mod_time, ''::text as counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	i.po_no::text as po_number, p.po_date::date as po_date, 'N'::character as transaction_type,
	i.discount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	sup.cust_supplier_code as cust_supplier_code,gm.grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name 
FROM store_invoice i
	JOIN store_grn_main gm USING (supplier_invoice_id)
	--JOIN store_grn_details g USING (grn_no)
	--JOIN store_item_details pmd using (medicine_id)
	JOIN stores s on s.dept_id=gm.store_id
	JOIN hospital_center_master hcm ON s.center_id=hcm.center_id
	JOIN supplier_master sup ON (i.supplier_id = sup.supplier_code)
	--JOIN store_category_master icm ON (pmd.med_category_id=icm.category_id)
	LEFT JOIN store_po_main p ON (p.po_no = i.po_no) 
WHERE i.consignment_stock=false and i.status!='O' AND i.discount > 0) as foo

ORDER BY voucher_no, item_category_id;

-- filtered on date_time, account_group, center_id
DROP VIEW IF EXISTS acc_all_returns_with_debitnote_view CASCADE ;

CREATE VIEW acc_all_returns_with_debitnote_view AS
-- supplier returns with debit note
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	sdn.debit_note_no as voucher_no, 'STORERETURNS'::text as voucher_type, sdn.debit_note_date as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	g.billed_qty as quantity, g.grn_pkg_size as unit, g.cost_price as unit_rate,
	trunc((g.cost_price*g.billed_qty/g.grn_pkg_size),2) as gross_amount, sdn.round_off as round_off_amount,
	trunc(g.discount+g.scheme_discount,2) as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	sm.supplier_name as debit_account, 'Inventory A/C'::text as credit_account,
	0.00 as tax_amount, trunc((g.cost_price*g.billed_qty/g.grn_pkg_size) - (g.discount+g.scheme_discount), 2) as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, sm.supplier_name as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, sdn.remarks as remarks, sdn.date_time as mod_time,
	''::text as counter_no,	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, -- voucher type is store returns, so transaction type is N
	trunc((g.cost_price*g.billed_qty/g.grn_pkg_size),2) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	''::text as account_name,sm.cust_supplier_code as cust_supplier_code,NULL::timestamp as grn_date,
	sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name 
FROM store_debit_note sdn
	JOIN store_grn_main sgm ON (sgm.debit_note_no = sdn.debit_note_no)
	JOIN store_grn_details g ON (g.grn_no=sgm.grn_no)
	JOIN store_item_details sid ON (sid.medicine_id=g.medicine_id)
	JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
	JOIN stores s ON (sgm.store_id=s.dept_id)
	JOIN supplier_master sm ON (supplier_code = supplier_id)
	JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
WHERE sdn.status='C'
ORDER BY sdn.debit_note_no ;

-- filtered on con_invoice_date, account_group, center_id
-- consignment stock issues.
DROP VIEW IF EXISTS acc_all_cs_issued_view CASCADE ;
CREATE VIEW acc_all_cs_issued_view AS
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, si.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	si.invoice_no||'/'||sgm.grn_no as voucher_no, 'CSISSUE'::text as voucher_type, date(sci.con_invoice_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	((g.cost_price/g.grn_pkg_size*sid.qty) - ((g.discount+g.scheme_discount)/g.total_qty*sid.qty)) as gross_amount, si.round_off as round_off_amount,
	si.discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Inventory A/C'::text as debit_account, sm.supplier_name as credit_account, 0.00 as tax_amount,
	((g.cost_price/g.grn_pkg_size*sid.qty) - ((g.discount+g.scheme_discount)/g.total_qty*sid.qty)) - si.discount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, sid.cost_value as cost_amount, sm.supplier_name as supplier_name,
	si.invoice_no as invoice_no, si.invoice_date as invoice_date, ''::text as voucher_ref,
	si.remarks as remarks, si.date_time as mod_time, ''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	si.po_no::text as po_number, p.po_date::date as po_date, 'N'::character as transaction_type,
	((g.cost_price/g.grn_pkg_size*sid.qty) - ((g.discount+g.scheme_discount)/g.total_qty*sid.qty)) - si.discount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	''::text as account_name,sm.cust_supplier_code as cust_supplier_code,sgm.grn_date,pmd.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name 
FROM store_consignment_invoice sci
	JOIN store_invoice si USING (supplier_invoice_id)
	JOIN store_grn_main sgm ON (sci.grn_no = sgm.grn_no)
	JOIN store_grn_details g ON (sgm.grn_no=g.grn_no and sci.medicine_id=g.medicine_id and sci.batch_no=g.batch_no)
	JOIN store_item_details pmd ON (g.medicine_id=pmd.medicine_id)
	JOIN stock_issue_details sid ON (sid.user_issue_no=sci.issue_id
		and sid.medicine_id=sci.medicine_id and sid.batch_no=sci.batch_no)
	JOIN stock_issue_main sim ON (sim.user_issue_no=sci.issue_id)
	JOIN supplier_master sm ON (si.supplier_id=sm.supplier_code)
	JOIN store_category_master scm ON (pmd.med_category_id=scm.category_id)
	JOIN stores s ON (s.dept_id = sgm.store_id)
	LEFT JOIN store_po_main p ON (p.po_no = si.po_no)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
WHERE si.consignment_stock=true;

---======================================================= HOSPITAL BILLS ===================================================

DROP VIEW IF EXISTS acc_all_hospital_bills_view CASCADE ;
CREATE VIEW acc_all_hospital_bills_view AS
-- non-insurance bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(bc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.amount + bc.discount) >= 0 THEN (bc.amount + bc.discount) ELSE 0.00 - (bc.amount + bc.discount) END AS gross_amount,
	0.00 AS round_off_amount,
	bc.discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.amount) >= 0 THEN 'Counter Receipts'::text ELSE
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	END as debit_account,
	CASE WHEN (bc.amount) >= 0 THEN
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, CASE WHEN bc.amount >= 0 THEN bc.amount ELSE 0.00 - bc.amount END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name,	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	bc.mod_time as mod_time,	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN bc.amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	bc.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '') as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (bc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
WHERE bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true

UNION ALL
-- cash portion of tpa bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(bc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN  ((bc.amount + CASE WHEN (bc.insurance_claim_amount) != bc.amount THEN bc.discount ELSE 0.00 END)-bc.insurance_claim_amount) >= 0
		THEN ((bc.amount + CASE WHEN (bc.insurance_claim_amount) != bc.amount THEN bc.discount ELSE 0.00 END)-bc.insurance_claim_amount)
		ELSE 0.00 - ((bc.amount + CASE WHEN (bc.insurance_claim_amount) != bc.amount THEN bc.discount ELSE 0.00 END)-bc.insurance_claim_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (bc.insurance_claim_amount) != bc.amount THEN bc.discount ELSE 0.00 END as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.amount - bc.insurance_claim_amount) >= 0 THEN 'Counter Receipts'::text ELSE
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	END as debit_account,
	CASE WHEN (bc.amount - bc.insurance_claim_amount) >= 0 THEN
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount,
	CASE WHEN (bc.amount-bc.insurance_claim_amount) >= 0 THEN (bc.amount-bc.insurance_claim_amount) ELSE 0.00 - (bc.amount-bc.insurance_claim_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN bc.amount - bc.insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	bc.amount-bc.insurance_claim_amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '') as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name 
FROM bill_charge_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (bc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bc.primary_sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
WHERE bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true
	AND bc.insurance_claim_amount != bc.amount
UNION ALL
-- insurance portion of tpa bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(bc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.insurance_claim_amount) >= 0 
	THEN 
		CASE WHEN (bc.insurance_claim_amount) = bc.amount 
		THEN bc.insurance_claim_amount + bc.discount 
		ELSE (bc.insurance_claim_amount) END 
	ELSE 
		CASE WHEN (bc.insurance_claim_amount) = bc.amount
		THEN 0.00 - (bc.insurance_claim_amount) - bc.discount
		ELSE 0.00 - bc.insurance_claim_amount END
	END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (bc.insurance_claim_amount) = bc.amount THEN bc.discount ELSE 0.00 END as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.insurance_claim_amount) >= 0 THEN tpa.tpa_name ELSE
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	END as debit_account,
	CASE WHEN (bc.insurance_claim_amount) >= 0 THEN
		CASE WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END
	ELSE tpa.tpa_name END as credit_account,
	0 AS tax_amount, CASE WHEN (bc.insurance_claim_amount) >= 0 THEN (bc.insurance_claim_amount) ELSE 0.00 - (bc.insurance_claim_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN bc.insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	bc.insurance_claim_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '') as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name 
FROM bill_charge_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (bc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bc.primary_sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
WHERE bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND bc.insurance_claim_amount != 0.00
ORDER BY bill_no ;


-- filter on mod_time, account_group, center_id

DROP VIEW IF EXISTS acc_all_incoming_sample_bills_view CASCADE ;
CREATE VIEW acc_all_incoming_sample_bills_view AS
-- incoming sample bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, isr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(bc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.amount + bc.discount) >= 0 THEN (bc.amount + bc.discount) ELSE 0.00 - (bc.amount + bc.discount) END AS gross_amount,
	0.00 AS round_off_amount, (bc.discount) as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, coalesce(b.points_redeemed_amt, 0) as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.amount) >= 0 THEN 'Counter Receipts'::text ELSE
		CASE WHEN sbah.account_head_name IS NULL THEN bah.account_head_name ELSE sbah.account_head_name END
	END as debit_account,
	CASE WHEN (bc.amount) >= 0 THEN
		CASE WHEN sbah.account_head_name IS NULL THEN bah.account_head_name ELSE sbah.account_head_name END
	ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, CASE WHEN bc.amount >= 0.00 THEN bc.amount ELSE 0.00 - bc.amount END AS net_amount,
	''::text as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(doc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name, ih.hospital_name as incoimng_hospital,
	''::text as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN bc.amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	bc.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '') as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=bc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (bc.bill_no=b.bill_no)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id
	LEFT JOIN incoming_hospitals ih ON (isr.orig_lab_name = ih.hospital_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = isr.center_id)
	LEFT JOIN doctors doc ON (isr.referring_doctor = doc.doctor_id)
	LEFT JOIN referral ref ON (isr.referring_doctor = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
WHERE bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('T') AND b.is_tpa != true;

-- there is no tpa for an incoming sample registration
-- NOTE : This view has a dependent view, be cautious with a DROP 

DROP VIEW IF EXISTS acc_all_hosp_issue_returns_bills_view CASCADE;
-- cash bills with issues
CREATE VIEW acc_all_hosp_issue_returns_bills_view AS
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0 THEN (amount+item_discount) ELSE 0.00 - (amount + item_discount) END AS gross_amount,
	round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (amount) >= 0 THEN 'Counter Receipts'::text ELSE account_head_name END as debit_account,
	CASE WHEN (amount) >= 0 THEN account_head_name ELSE 'Counter Receipts'::text END as credit_account,
	final_tax AS tax_amount, CASE WHEN (amount) >= 0 THEN (amount) ELSE 0.00 - amount END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, account_head_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_issue_bills where is_tpa != true

UNION ALL
-- cash portion of insurance bill with issues

SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + CASE WHEN (insurance_claim_amount) != amount THEN item_discount ELSE 0.00 END   - insurance_claim_amount) >= 0 THEN
		(amount + CASE WHEN (insurance_claim_amount) != amount THEN item_discount ELSE 0.00 END  - insurance_claim_amount) ELSE
		0.00 - (amount + CASE WHEN (insurance_claim_amount) != amount THEN item_discount ELSE 0.00 END   - insurance_claim_amount) END AS gross_amount,
	round_off AS round_off_amount,
	CASE WHEN (insurance_claim_amount) != amount THEN item_discount ELSE 0.00 END as discount_amount ,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (amount - insurance_claim_amount) >= 0 THEN 'Counter Receipts'::text ELSE account_head_name END as debit_account,
	CASE WHEN (amount - insurance_claim_amount) >= 0 THEN account_head_name ELSE 'Counter Receipts'::text  END as credit_account,
	final_tax AS tax_amount,
		CASE WHEN (amount - insurance_claim_amount) >= 0
			THEN (amount - insurance_claim_amount)
			ELSE 0.00 - (amount - insurance_claim_amount) END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount - insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount - insurance_claim_amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_issue_bills where is_tpa = true AND insurance_claim_amount != amount


UNION ALL
-- insurance portion of tpa bill with issues
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (insurance_claim_amount) >= 0 THEN
		(insurance_claim_amount) ELSE
		0.00 - (insurance_claim_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (insurance_claim_amount) = amount THEN item_discount ELSE 0.00 END as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (insurance_claim_amount) >= 0 THEN primary_sponsor_name ELSE account_head_name END as debit_account,
	CASE WHEN (insurance_claim_amount) >= 0 THEN account_head_name ELSE primary_sponsor_name END as credit_account,
	final_tax AS tax_amount,
		CASE WHEN (insurance_claim_amount) >= 0
			THEN (insurance_claim_amount)
			ELSE 0.00 - (insurance_claim_amount) END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	insurance_claim_amount as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_issue_bills where is_tpa = true AND insurance_claim_amount != 0.00
-- TODO : sponsor_type 
UNION ALL
-- cash bills with returns 
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0 THEN (amount + item_discount) ELSE 0.00 - (amount + item_discount) END AS gross_amount,
	round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (amount) >= 0 THEN 'Counter Receipts'::text ELSE account_head_name END as debit_account,
	CASE WHEN (amount) >= 0 THEN account_head_name ELSE 'Counter Receipts'::text END as credit_account,
	final_tax AS tax_amount, CASE WHEN amount >= 0 THEN amount ELSE 0.00 - (amount) END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_return_bills WHERE is_tpa != true

UNION ALL

-- cash portion of insurance bill returns

SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount - insurance_claim_amount) >= 0 THEN (amount + item_discount - insurance_claim_amount)
		ELSE 0.00 - (amount + item_discount - insurance_claim_amount) END AS gross_amount,
	round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (amount - insurance_claim_amount) >= 0 THEN 'Counter Receipts'::text ELSE account_head_name END as debit_account,
	CASE WHEN (amount - insurance_claim_amount) >= 0 THEN account_head_name ELSE 'Counter Receipts'::text END as credit_account,
	final_tax AS tax_amount, CASE WHEN amount - insurance_claim_amount >= 0 THEN amount  - insurance_claim_amount
		ELSE 0.00 - (amount - insurance_claim_amount) END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount - insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount - insurance_claim_amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_return_bills WHERE is_tpa = true AND insurance_claim_amount != amount

UNION ALL
-- insurance portion of tpa bill returns
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (insurance_claim_amount) >= 0 THEN (insurance_claim_amount)
		ELSE 0.00 - (insurance_claim_amount) END AS gross_amount,
	round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	final_tax as sales_vat_amount, vat_rate as sales_vat_percent,
	CASE WHEN (insurance_claim_amount) >= 0 THEN primary_sponsor_name ELSE account_head_name END as debit_account,
	CASE WHEN (insurance_claim_amount) >= 0 THEN account_head_name ELSE  primary_sponsor_name END as credit_account,
	final_tax AS tax_amount, CASE WHEN insurance_claim_amount >= 0 THEN insurance_claim_amount
		ELSE 0.00 - (insurance_claim_amount) END AS net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor,
	referral_doctor, payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN insurance_claim_amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	insurance_claim_amount as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,prescribing_doctor_dept_name
FROM acc_all_inv_return_bills WHERE is_tpa = true AND insurance_claim_amount != 0.0

ORDER BY bill_no ;

-- pharma bills added to hospital bills

DROP VIEW IF EXISTS acc_all_hospital_pharma_bills_view CASCADE ;
CREATE VIEW acc_all_hospital_pharma_bills_view AS
-- All pharmacy bills added to hospital bill, cash
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0 THEN amount + item_discount
	ELSE -amount - item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount) >= 0 THEN 'Counter Receipts'::text
	ELSE account_head_name END as debit_account,
	CASE WHEN (amount) >= 0 THEN account_head_name
	ELSE 'Counter Receipts' END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, account_head_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type IN ('N', 'T') and is_tpa != true
UNION ALL
-- All pharmacy bills added to hospital bills - cash portion of tpa bills
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN  ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END)-insurance_claim_amt) >= 0
		THEN ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END)-insurance_claim_amt)
		ELSE 0.00 - ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END)-insurance_claim_amt) END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount - insurance_claim_amt) >= 0 THEN 'Counter Receipts'::text
	ELSE account_head_name END as debit_account,
	CASE WHEN (amount - insurance_claim_amt) >= 0 THEN account_head_name
	ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, 
	CASE WHEN (amount - insurance_claim_amt) > 0.00 THEN (amount  - insurance_claim_amt) ELSE -(amount - insurance_claim_amt) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN amount - insurance_claim_amt > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount - insurance_claim_amt as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, account_head_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type IN ('N', 'T') and is_tpa = true AND (amount != insurance_claim_amt )
UNION ALL
-- All pharmacy bills added to hospital bill, insurance
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	
	CASE WHEN (insurance_claim_amt) >= 0 
	THEN 
		CASE WHEN (insurance_claim_amt) = amount 
		THEN insurance_claim_amt + item_discount 
		ELSE (insurance_claim_amt) END 
	ELSE 
		CASE WHEN (insurance_claim_amt) = amount
		THEN 0.00 - (insurance_claim_amt) - item_discount
		ELSE 0.00 - insurance_claim_amt END
	END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (insurance_claim_amt) = amount THEN item_discount ELSE 0.00 END as discount_amount,
	
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (insurance_claim_amt) >= 0 THEN primary_sponsor_name::text
	ELSE account_head_name END as debit_account,
	CASE WHEN (insurance_claim_amt) >= 0 THEN account_head_name
	ELSE primary_sponsor_name::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN (insurance_claim_amt) > 0.00 THEN (insurance_claim_amt) ELSE -(insurance_claim_amt) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN insurance_claim_amt > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	insurance_claim_amt as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type IN ('N', 'T') and is_tpa = true AND insurance_claim_amt > 0.00
UNION ALL
-- All pharmacy bills added to hospital bill, discounts
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0.00 THEN amount + item_discount
	ELSE -amount-item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Discounts A/C'::text
	ELSE 'Counter Receipts'::text END as debit_account,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Counter Receipts'::text
	ELSE 'Discounts A/C'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	-amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, ''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_discounts where restriction_type IN ('N', 'T')
UNION ALL
-- All pharmacy bills added to hospital bill, discounts
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0.00 THEN amount + item_discount
	ELSE -amount-item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Counter Receipts'::text
	ELSE 'Roundoff A/C' END as debit_account,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Roundoff A/C'::text
	ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, ''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_roundoffs where restriction_type IN ('N', 'T')
ORDER BY bill_no;

-- Write off vouchers

DROP VIEW IF EXISTS acc_all_hosp_bills_writeoff_view CASCADE;
CREATE VIEW acc_all_hosp_bills_writeoff_view AS
-- Cash write-off
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	''::text as charge_group, ''::text as charge_head, b.account_group,
	''::text as service_group, ''::text as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(b.closed_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN 
		CASE WHEN (b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN
		(b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) ELSE
		0.00 - (b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) END
	ELSE 
		CASE WHEN (b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN
		(b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) ELSE
		0.00 - (b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) END
	END AS gross_amount,
	0.00 AS round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN 'Write Off A/C'::text ELSE 'Counter Receipts'::text END 
	ELSE
		CASE WHEN (b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN 'Write Off A/C'::text ELSE 'Counter Receipts'::text END
	END as debit_account,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN  'Counter Receipts'::text ELSE 'Write Off A/C'::text END 
	ELSE
		CASE WHEN (b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN  'Counter Receipts'::text ELSE 'Write Off A/C'::text END
	END as credit_account,
	0 AS tax_amount,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim >= 0 THEN
		b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim ELSE
		0.00 - (b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) END
	ELSE 
		CASE WHEN b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt >= 0 THEN
		b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt ELSE
		0.00 - (b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt ) END 
	END AS net_amount,
	doc.doctor_name as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, ''::text as payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	d.dept_name as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	b.closed_date as mod_time, ''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		b.total_amount - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim
	ELSE 
		b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt
	END as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM
	bill b
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim FROM bill_credit_notes bcn
				LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  )  AS creditNote ON(creditNote.bill_no = b.bill_no)
WHERE
	b.bill_type in ('P', 'C') AND b.restriction_type in ('N') AND b.is_tpa != true AND
	b.total_amount + b.total_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt != 0 AND
	b.status = 'C' AND b.total_amount >= 0

UNION ALL
-- cash portion of tpa bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	''::text as charge_group, ''::text as charge_head, b.account_group,
	''::text as service_group, ''::text as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(b.closed_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN  (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0
		THEN (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim)
		ELSE 0.00 - (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) END
	ELSE 
		CASE WHEN  (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0
		THEN (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt)
		ELSE 0.00 - (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) END 
	END AS gross_amount,
	0.00 AS round_off_amount,
	0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN 'Write Off A/C'::text ELSE 'Counter Receipts'::text END 
	ELSE
		CASE WHEN (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN 'Write Off A/C'::text ELSE 'Counter Receipts'::text END
	END as debit_account,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN 'Counter Receipts'::text
		ELSE 'Write Off A/C'::text END 
	ELSE 
		CASE WHEN (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN 'Counter Receipts'::text
		ELSE 'Write Off A/C'::text END 
	END as credit_account,
	0 AS tax_amount,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) >= 0 THEN
		(b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) ELSE
		0.00 - (b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim) END 
	ELSE 
		CASE WHEN (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) >= 0 THEN
		(b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) ELSE
		0.00 - (b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt) END
	END AS net_amount,
	doc.doctor_name as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, ''::text as payee_doctor,
	''::text as outhouse_name, ''::text as incoimng_hospital,
	d.dept_name as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, b.closed_date as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		b.total_amount - b.total_claim - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim
	ELSE
		b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt
	END as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM bill b

	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)

	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim FROM bill_credit_notes bcn
				LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  )  AS creditNote ON(creditNote.bill_no = b.bill_no)
WHERE
	bill_type in ('P', 'C') AND b.restriction_type in ('N') AND b.is_tpa = true
	AND b.total_amount + b.total_tax != b.total_claim + b.total_claim_tax and b.total_amount - b.total_claim + b.total_tax - b.total_claim_tax - b.deposit_set_off - b.total_receipts - b.points_redeemed_amt != 0
	AND b.status = 'C' AND b.total_amount >= 0
UNION ALL
-- insurance portion of tpa bills
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	''::text as charge_group, ''::text as charge_head, b.account_group,
	''::text as service_group, ''::text as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(b.closed_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) >= 0 THEN
		(b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) ELSE
		0.00 - (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) END
	ELSE
		CASE WHEN (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) >= 0 THEN
		(b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) ELSE
		0.00 - (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) END 
	END AS gross_amount,
	0.00 AS round_off_amount,
	0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) >= 0 THEN 'Write Off A/C'::text ELSE tpa.tpa_name END 
	ELSE 
		CASE WHEN (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) >= 0 THEN 'Write Off A/C'::text ELSE tpa.tpa_name END
	END as debit_account,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) >= 0 THEN tpa.tpa_name ELSE 'Write Off A/C'::text	END 
	ELSE
		CASE WHEN (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) >= 0 THEN tpa.tpa_name ELSE 'Write Off A/C'::text END
	END as credit_account,
	0 AS tax_amount,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		CASE WHEN (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) >= 0 THEN
		(b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) ELSE
		0.00 - (b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim) END
	ELSE 
		CASE WHEN (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) >= 0 THEN
		(b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) ELSE
		0.00 - (b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) END 
	END AS net_amount,
	doc.doctor_name as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, b.closed_date as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN
		b.total_claim-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts + creditNote.total_credit_claim
	ELSE 
		b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts
	END as transaction_amount, 
	tpa.tpa_name as sponsor_name, tpa.sponsor_type, ''::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM bill b
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN ( SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim FROM bill_credit_notes bcn
				LEFT JOIN bill cn ON(bcn.credit_note_bill_no = cn.bill_no ) GROUP BY bcn.bill_no  )  AS creditNote ON(creditNote.bill_no = b.bill_no)
WHERE
	bill_type in ('P', 'C') AND b.restriction_type in ('N') AND b.is_tpa = true AND
	(b.total_claim+b.total_claim_tax-b.claim_recd_amount-b.primary_total_sponsor_receipts-b.secondary_total_sponsor_receipts ) != 0
	AND b.status = 'C' AND b.total_amount >= 0
ORDER BY bill_no ;


-- Adjustment vouchers when insuranace / sponsor is changed for Hospital Items

DROP VIEW IF EXISTS acc_all_insuarnce_adjustments_view CASCADE;

CREATE VIEW acc_all_insuarnce_adjustments_view AS
SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, cg.chargegroup_name AS charge_group, 
	cc.chargehead_name AS charge_head, b.account_group, sg.service_group_name AS service_group, 
	ssg.service_sub_group_name AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'HOSPBILLS'::text AS voucher_type, date(v.new_posted_date) AS voucher_date, mbc.act_description_id::text AS item_code, 
	mbc.act_description::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, mbc.act_quantity AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
   	CASE
    	WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
       	ELSE 0.00 - bc.insurance_claim_amount
   	END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
   	0.00 AS points_redeemed_rate, COALESCE(b.points_redeemed_amt, 0::numeric) AS points_redeemed_amt, 
   	0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
   	0.00 AS sales_vat_percent, 
   	CASE
    	WHEN bc.insurance_claim_amount >= 0::numeric THEN 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
        ELSE tpa.tpa_name
    END AS debit_account, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN tpa.tpa_name
        ELSE 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
    END AS credit_account, 0 AS tax_amount, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS net_amount, doc.doctor_name AS admitting_doctor, pdoc.doctor_name AS prescribing_doctor, 
    cdoc.doctor_name AS conductiong_doctor, COALESCE(refdoc.doctor_name, ref.referal_name, ''::character varying) AS referral_doctor, 
    cdoc.doctor_name AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    cdept.dept_name AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, v.new_mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	bc.insurance_claim_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '')::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN acc_all_changed_sponsor_charges v ON (v.bill_charge_adjustment_id = bc.bill_charge_adjustment_id and v.charge_id = bc.charge_id)
	JOIN bill_charge mbc ON mbc.charge_id::text = bc.charge_id::text
	JOIN chargehead_constants cc ON cc.chargehead_id::text = bc.charge_head::text
	JOIN chargegroup_constants cg ON cg.chargegroup_id::text = bc.charge_group::text
	JOIN bill_account_heads bah ON cc.account_head_id = bah.account_head_id
	JOIN bill b ON bc.bill_no::text = b.bill_no::text
	LEFT JOIN service_sub_groups ssg ON ssg.service_sub_group_id = bc.service_sub_group_id
	LEFT JOIN service_groups sg ON sg.service_group_id = ssg.service_group_id
	LEFT JOIN bill_account_heads sbah ON ssg.account_head_id = sbah.account_head_id
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN department cdept ON cdept.dept_id::text = mbc.act_department_id::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON v.primary_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
	LEFT JOIN doctors refdoc ON pr.reference_docto_id::text = refdoc.doctor_id::text
	LEFT JOIN referral ref ON pr.reference_docto_id::text = ref.referal_no::text
	LEFT JOIN doctors pdoc ON mbc.prescribing_dr_id::text = pdoc.doctor_id::text
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON mbc.payee_doctor_id::text = cdoc.doctor_id::text
WHERE (bc.charge_head NOT IN ('PHMED', 'PHRET', 'PHCMED', 'PHCRET', 'INVITE', 'INVRET')) 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true AND bc.insurance_claim_amount <> 0.00

UNION ALL 

SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, cg.chargegroup_name AS charge_group, 
	cc.chargehead_name AS charge_head, b.account_group, sg.service_group_name AS service_group, 
 	ssg.service_sub_group_name AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'HOSPBILLS'::text AS voucher_type, date(v.new_posted_date) AS voucher_date, mbc.act_description_id::text AS item_code, 
	mbc.act_description::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, mbc.act_quantity AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
    0.00 AS points_redeemed_rate, COALESCE(b.points_redeemed_amt, 0::numeric) AS points_redeemed_amt, 
    0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
    0.00 AS sales_vat_percent, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN tpa.tpa_name
        ELSE 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
    END AS debit_account, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
        ELSE tpa.tpa_name
    END AS credit_account, 0 AS tax_amount, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS net_amount, doc.doctor_name AS admitting_doctor, pdoc.doctor_name AS prescribing_doctor, 
    cdoc.doctor_name AS conductiong_doctor, COALESCE(refdoc.doctor_name, ref.referal_name, ''::character varying) AS referral_doctor, 
    cdoc.doctor_name AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    cdept.dept_name AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, v.new_mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	bc.insurance_claim_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '')::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN acc_all_changed_sponsor_charges v ON (v.bill_charge_adjustment_id = bc.bill_charge_adjustment_id and v.charge_id = bc.charge_id)           
	JOIN bill_charge mbc ON mbc.charge_id::text = bc.charge_id::text
	JOIN chargehead_constants cc ON cc.chargehead_id::text = bc.charge_head::text
	JOIN chargegroup_constants cg ON cg.chargegroup_id::text = bc.charge_group::text
	JOIN bill_account_heads bah ON cc.account_head_id = bah.account_head_id
	JOIN bill b ON bc.bill_no::text = b.bill_no::text
	LEFT JOIN service_sub_groups ssg ON ssg.service_sub_group_id = bc.service_sub_group_id
	LEFT JOIN service_groups sg ON sg.service_group_id = ssg.service_group_id
	LEFT JOIN bill_account_heads sbah ON ssg.account_head_id = sbah.account_head_id
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN department cdept ON cdept.dept_id::text = mbc.act_department_id::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON v.new_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
	LEFT JOIN doctors refdoc ON pr.reference_docto_id::text = refdoc.doctor_id::text
	LEFT JOIN referral ref ON pr.reference_docto_id::text = ref.referal_no::text
	LEFT JOIN doctors pdoc ON mbc.prescribing_dr_id::text = pdoc.doctor_id::text
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON mbc.payee_doctor_id::text = cdoc.doctor_id::text
WHERE (bc.charge_head NOT IN ('PHMED', 'PHRET', 'PHCMED', 'PHCRET', 'INVITE', 'INVRET')) 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true AND bc.insurance_claim_amount <> 0.00
ORDER BY bill_no;



-- Adjustment vouchers when insuranace / sponsor is changed for Pharmacy/Inventory Items

DROP VIEW IF EXISTS acc_all_stores_insuarnce_adjustments_view CASCADE;

CREATE VIEW acc_all_stores_insuarnce_adjustments_view AS
SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, cg.chargegroup_name AS charge_group, 
	cc.chargehead_name AS charge_head, b.account_group, sg.service_group_name AS service_group, 
	ssg.service_sub_group_name AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'PHBILLS'::text AS voucher_type, date(v.new_posted_date) AS voucher_date, mbc.act_description_id::text AS item_code, 
	mbc.act_description::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, mbc.act_quantity AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
   	CASE
    	WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
       	ELSE 0.00 - bc.insurance_claim_amount
   	END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
   	0.00 AS points_redeemed_rate, COALESCE(b.points_redeemed_amt, 0::numeric) AS points_redeemed_amt, 
   	0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
   	0.00 AS sales_vat_percent, 
   	CASE
    	WHEN bc.insurance_claim_amount >= 0::numeric THEN 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
        ELSE tpa.tpa_name
    END AS debit_account, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN tpa.tpa_name
        ELSE 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
    END AS credit_account, 0 AS tax_amount, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS net_amount, doc.doctor_name AS admitting_doctor, pdoc.doctor_name AS prescribing_doctor, 
    cdoc.doctor_name AS conductiong_doctor, COALESCE(refdoc.doctor_name, ref.referal_name, ''::character varying) AS referral_doctor, 
    cdoc.doctor_name AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    cdept.dept_name AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, v.new_mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	bc.insurance_claim_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '')::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN acc_all_changed_sponsor_charges v ON (v.bill_charge_adjustment_id = bc.bill_charge_adjustment_id and v.charge_id = bc.charge_id)
	JOIN bill_charge mbc ON mbc.charge_id::text = bc.charge_id::text
	JOIN chargehead_constants cc ON cc.chargehead_id::text = bc.charge_head::text
	JOIN chargegroup_constants cg ON cg.chargegroup_id::text = bc.charge_group::text
	JOIN bill_account_heads bah ON cc.account_head_id = bah.account_head_id
	JOIN bill b ON bc.bill_no::text = b.bill_no::text
	LEFT JOIN service_sub_groups ssg ON ssg.service_sub_group_id = bc.service_sub_group_id
	LEFT JOIN service_groups sg ON sg.service_group_id = ssg.service_group_id
	LEFT JOIN bill_account_heads sbah ON ssg.account_head_id = sbah.account_head_id
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN department cdept ON cdept.dept_id::text = mbc.act_department_id::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON v.primary_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
	LEFT JOIN doctors refdoc ON pr.reference_docto_id::text = refdoc.doctor_id::text
	LEFT JOIN referral ref ON pr.reference_docto_id::text = ref.referal_no::text
	LEFT JOIN doctors pdoc ON mbc.prescribing_dr_id::text = pdoc.doctor_id::text
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON mbc.payee_doctor_id::text = cdoc.doctor_id::text
WHERE (bc.charge_head IN ('PHMED', 'PHRET', 'PHCMED', 'PHCRET', 'INVITE', 'INVRET')) 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true AND bc.insurance_claim_amount <> 0.00

UNION ALL 

SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, cg.chargegroup_name AS charge_group, 
	cc.chargehead_name AS charge_head, b.account_group, sg.service_group_name AS service_group, 
 	ssg.service_sub_group_name AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'PHBILLS'::text AS voucher_type, date(v.new_posted_date) AS voucher_date, mbc.act_description_id::text AS item_code, 
	mbc.act_description::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, mbc.act_quantity AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
    0.00 AS points_redeemed_rate, COALESCE(b.points_redeemed_amt, 0::numeric) AS points_redeemed_amt, 
    0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
    0.00 AS sales_vat_percent, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN tpa.tpa_name
        ELSE 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
    END AS debit_account, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN 
        CASE
            WHEN sbah.account_head_name IS NULL THEN bah.account_head_name
            ELSE sbah.account_head_name
        END
        ELSE tpa.tpa_name
    END AS credit_account, 0 AS tax_amount, 
    CASE
        WHEN bc.insurance_claim_amount >= 0::numeric THEN bc.insurance_claim_amount
        ELSE 0.00 - bc.insurance_claim_amount
    END AS net_amount, doc.doctor_name AS admitting_doctor, pdoc.doctor_name AS prescribing_doctor, 
    cdoc.doctor_name AS conductiong_doctor, COALESCE(refdoc.doctor_name, ref.referal_name, ''::character varying) AS referral_doctor, 
    cdoc.doctor_name AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    cdept.dept_name AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, v.new_mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	bc.insurance_claim_amount as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	coalesce(sbah.account_head_name, bah.account_head_name, '')::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_adjustment bc
	JOIN acc_all_changed_sponsor_charges v ON (v.bill_charge_adjustment_id = bc.bill_charge_adjustment_id and v.charge_id = bc.charge_id)           
	JOIN bill_charge mbc ON mbc.charge_id::text = bc.charge_id::text
	JOIN chargehead_constants cc ON cc.chargehead_id::text = bc.charge_head::text
	JOIN chargegroup_constants cg ON cg.chargegroup_id::text = bc.charge_group::text
	JOIN bill_account_heads bah ON cc.account_head_id = bah.account_head_id
	JOIN bill b ON bc.bill_no::text = b.bill_no::text
	LEFT JOIN service_sub_groups ssg ON ssg.service_sub_group_id = bc.service_sub_group_id
	LEFT JOIN service_groups sg ON sg.service_group_id = ssg.service_group_id
	LEFT JOIN bill_account_heads sbah ON ssg.account_head_id = sbah.account_head_id
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN department cdept ON cdept.dept_id::text = mbc.act_department_id::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON v.new_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
	LEFT JOIN doctors refdoc ON pr.reference_docto_id::text = refdoc.doctor_id::text
	LEFT JOIN referral ref ON pr.reference_docto_id::text = ref.referal_no::text
	LEFT JOIN doctors pdoc ON mbc.prescribing_dr_id::text = pdoc.doctor_id::text
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON mbc.payee_doctor_id::text = cdoc.doctor_id::text
WHERE (bc.charge_head IN ('PHMED', 'PHRET', 'PHCMED', 'PHCRET', 'INVITE', 'INVRET')) 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true AND bc.insurance_claim_amount <> 0.00
ORDER BY bill_no;
-- Adjustment vouchers when sponsor amount has a approval limit

DROP VIEW IF EXISTS acc_all_insurance_limits_adjustments_view CASCADE;

CREATE VIEW acc_all_insurance_limits_adjustments_view AS
SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, ''::text AS charge_group, 
	''::text AS charge_head, b.account_group, ''::text AS service_group, 
	''::text AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'HOSPBILLS'::text AS voucher_type, date(bca.mod_time) AS voucher_date, ''::text AS item_code, 
	''::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, 0 AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN bca.primary_total_claim
       	ELSE 0.00 - bca.primary_total_claim
   	END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
   	0.00 AS points_redeemed_rate, 0::numeric AS points_redeemed_amt, 
   	0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
   	0.00 AS sales_vat_percent, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN tpa.tpa_name
    	ELSE 'Sponsor Adjustment A/C'::text
    END AS debit_account, 
    CASE
        WHEN bca.primary_total_claim >= 0::numeric THEN 'Sponsor Adjustment A/C'::text 
        ELSE tpa.tpa_name
    END AS credit_account, 0 AS tax_amount, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN bca.primary_total_claim
       	ELSE 0.00 - bca.primary_total_claim
    END AS net_amount, doc.doctor_name AS admitting_doctor, ''::text AS prescribing_doctor, 
    ''::text AS conductiong_doctor, ''::text AS referral_doctor, 
    ''::text AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    ''::text AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, bca.mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, bca.primary_total_claim as transaction_amount,
	tpa.tpa_name as sponsor_name, tpa.sponsor_type, ''::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM bill_claim_adjustment bca
	JOIN bill b ON bca.bill_no::text = b.bill_no::text
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN patient_details pd ON pr.mr_no::text = pd.mr_no::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON pr.primary_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
WHERE coalesce(bca.approval_amount, 0) != 0 and bca.primary_total_claim != 0 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true
	
UNION ALL

SELECT hcm.center_id, hcm.center_name, b.visit_type, pr.mr_no, b.visit_id, ''::text AS charge_group, 
	''::text AS charge_head, b.account_group, ''::text AS service_group, 
	''::text AS service_sub_group, b.bill_no, b.audit_control_number, b.bill_no AS voucher_no, 
	'HOSPBILLS'::text AS voucher_type, date(bca.mod_time) AS voucher_date, ''::text AS item_code, 
	''::text AS item_name, ''::text AS receipt_store, ''::text AS issue_store, ''::text AS currency, 
	0.00 AS currency_conversion_rate, 0 AS quantity, ''::text AS unit, 0.00 AS unit_rate, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN bca.primary_total_claim
       	ELSE 0.00 - bca.primary_total_claim
   	END AS gross_amount, 0.00 AS round_off_amount, 0.00 AS discount_amount, 0 AS points_redeemed, 
   	0.00 AS points_redeemed_rate, 0::numeric AS points_redeemed_amt, 
   	0 AS item_category_id, 0.00 AS purchase_vat_amount, 0.00 AS purchase_vat_percent, 0.00 AS sales_vat_amount, 
   	0.00 AS sales_vat_percent, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN 'Sponsor Adjustment A/C'::text 
    	ELSE 'Counter Receipts'::text
    END AS debit_account, 
    CASE
        WHEN bca.primary_total_claim >= 0::numeric THEN 'Counter Receipts'::text  
        ELSE 'Sponsor Adjustment A/C'::text
    END AS credit_account, 0 AS tax_amount, 
   	CASE
    	WHEN bca.primary_total_claim >= 0::numeric THEN bca.primary_total_claim
       	ELSE 0.00 - bca.primary_total_claim
    END AS net_amount, doc.doctor_name AS admitting_doctor, ''::text AS prescribing_doctor, 
    ''::text AS conductiong_doctor, ''::text AS referral_doctor, 
    ''::text AS payee_doctor, ''::text AS outhouse_name, ''::text AS incoimng_hospital, d.dept_name AS admitting_department, 
    ''::text AS conducting_department, 0.00 AS cost_amount, ''::text AS supplier_name, ''::text AS invoice_no, 
    NULL::timestamp without time zone AS invoice_date, ''::text AS voucher_ref, b.remarks, bca.mod_time AS mod_time, 
    ''::text AS counter_no, b.open_date AS bill_open_date, b.finalized_date AS bill_finalized_date, 
    CASE
        WHEN b.is_tpa THEN 'Y'::text
        ELSE 'N'::text
    END AS is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'R'::character as transaction_type, 0.00-bca.primary_total_claim as transaction_amount,
	''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM bill_claim_adjustment bca
	JOIN bill b ON bca.bill_no::text = b.bill_no::text
	LEFT JOIN patient_registration pr ON pr.patient_id::text = b.visit_id::text
	LEFT JOIN patient_details pd ON pr.mr_no::text = pd.mr_no::text
	LEFT JOIN department d ON d.dept_id::text = pr.dept_name::text
	LEFT JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id
	LEFT JOIN tpa_master tpa ON pr.primary_sponsor_id::text = tpa.tpa_id::text
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON pr.doctor::text = doc.doctor_id::text
WHERE coalesce(bca.approval_amount, 0) != 0 and bca.primary_total_claim != 0 
	AND (b.bill_type = ANY (ARRAY['P'::bpchar, 'C'::bpchar])) 
	AND b.restriction_type = 'N'::bpchar AND b.is_tpa = true;


---======================================================= HOSPITAL BILLS TAX VOUCHERS ===================================================
-- dropping the old view in case it is there, already.
DROP VIEW IF EXISTS acc_all_hospital_bills_tax_view CASCADE ;

DROP VIEW IF EXISTS acc_all_hospital_bills_new_tax_view CASCADE ;
CREATE VIEW acc_all_hospital_bills_new_tax_view AS
-- non-insurance bills -> new record inserted
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.tax_amt) >= 0 THEN (bc.tax_amt) ELSE 0.00 - (bc.tax_amt) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.tax_amt) >= 0 THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.tax_amt) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, CASE WHEN bc.tax_amt >= 0 THEN bc.tax_amt ELSE 0.00 - bc.tax_amt END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name,	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	bc.mod_time as mod_time,	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN bc.tax_amt >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	bc.tax_amt as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true AND bc.old_tax_sub_group_id is null

UNION ALL

-- insurance portion of tpa bills -> when new entries are inserted
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN bc.sponsor_tax_amount
	ELSE 0.00 - bc.sponsor_tax_amount END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN tpa.tpa_name ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE tpa.tpa_name END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount) ELSE 0.00 - (bc.sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_tax_sub_group_id is null AND bc.sponsor_tax_amount != 0;
	

DROP VIEW IF EXISTS acc_all_hospital_bills_tax_adjustment_view CASCADE ;
CREATE VIEW acc_all_hospital_bills_tax_adjustment_view AS
-- non-insurance bills -> when tax subgroup id changed, post a regular for the new amount and new tax group
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (COALESCE(bc.tax_amt, 0)) >= 0 THEN (bc.tax_amt) ELSE 0.00 - (COALESCE(bc.tax_amt, 0.00)) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.tax_amt) >= 0 THEN 'Counter Receipts'::text ELSE 'Tax Liabiity A/C'::text END as debit_account,
	CASE WHEN (bc.tax_amt) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, CASE WHEN (COALESCE(bc.tax_amt, 0)) >= 0 THEN (bc.tax_amt) ELSE 0.00 - (COALESCE(bc.tax_amt, 0)) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name,	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	bc.mod_time as mod_time,	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (COALESCE(bc.tax_amt, 0)) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(COALESCE(bc.tax_amt, 0)) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true AND bc.old_tax_sub_group_id is NOT NULL AND 
	bc.tax_sub_group_id != bc.old_tax_sub_group_id AND COALESCE(bc.tax_amt, 0) != 0

UNION ALL

-- non-insurance bills -> when tax subgroup id changed, post a contra for the old amount and old tax group
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (COALESCE(bc.old_tax_amt, 0)) >= 0 THEN (bc.old_tax_amt) ELSE 0.00 - (COALESCE(bc.old_tax_amt, 0.00)) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.old_tax_amt) >= 0 THEN 'Tax Liabiity A/C'::text ELSE 'Counter Receipts'::text END as debit_account,
	CASE WHEN (bc.old_tax_amt) >= 0 THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as credit_account,
	0 AS tax_amount, CASE WHEN (COALESCE(bc.old_tax_amt, 0)) >= 0 THEN (bc.old_tax_amt) ELSE 0.00 - (COALESCE(bc.old_tax_amt, 0)) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name,	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	bc.mod_time as mod_time,	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (COALESCE(bc.old_tax_amt, 0)) >= 0.00 THEN 'R'::character ELSE 'N'::character END as transaction_type,
	0.00 - (COALESCE(bc.old_tax_amt, 0)) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.old_tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true AND bc.old_tax_sub_group_id is NOT NULL AND 
	bc.tax_sub_group_id != bc.old_tax_sub_group_id AND COALESCE(bc.old_tax_amt, 0) != 0

UNION ALL

-- non-insurance bills -> when tax amount changed but the tax sub group is not changed, post the accrual
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.tax_amt-bc.old_tax_amt) >= 0 THEN (bc.tax_amt-bc.old_tax_amt) ELSE 0.00 - (bc.tax_amt-bc.old_tax_amt) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.tax_amt-bc.old_tax_amt) >= 0 THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.tax_amt-bc.old_tax_amt) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, CASE WHEN bc.tax_amt-bc.old_tax_amt >= 0 THEN bc.tax_amt-bc.old_tax_amt ELSE 0.00 - (bc.tax_amt-bc.old_tax_amt) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
	''::text as outhouse_name,	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks as remarks,
	bc.mod_time as mod_time,	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
	CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.tax_amt-bc.old_tax_amt) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.tax_amt-bc.old_tax_amt) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true AND bc.old_tax_amt is NOT NULL
	AND bc.tax_amt != bc.old_tax_amt AND COALESCE(bc.old_tax_sub_group_id, -1) = COALESCE(bc.tax_sub_group_id, -1);

DROP VIEW IF EXISTS acc_all_hospital_bills_tpa_tax_adjustment_view CASCADE ;
CREATE VIEW acc_all_hospital_bills_tpa_tax_adjustment_view AS

-- insurance portion of tpa bills -> when tax subgroup id changed, post a regular for the new sponsor tax and new sub group
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount)
	ELSE 0.00 - (bc.sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN tpa.tpa_name ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE tpa.tpa_name END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount) ELSE 0.00 - (bc.sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_tax_sub_group_id is NOT null 
	AND (bc.tax_sub_group_id != bc.old_tax_sub_group_id OR COALESCE(bc.claim_id, '') != COALESCE(bc.old_claim_id))
	AND COALESCE(bc.sponsor_tax_amount, 0) != 0
	
UNION ALL

-- insurance portion of tpa bills -> when tax subgroup id changed, post a contra for the old sponsor tax and old sub group
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.old_sponsor_tax_amount) >= 0 THEN (bc.old_sponsor_tax_amount)
	ELSE 0.00 - (bc.old_sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.old_sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE tpa.tpa_name END as debit_account,
	CASE WHEN (bc.old_sponsor_tax_amount) >= 0 THEN  tpa.tpa_name ELSE 'Tax Liability A/C'::text END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.old_sponsor_tax_amount) >= 0 THEN (bc.old_sponsor_tax_amount) ELSE 0.00 - (bc.old_sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.old_sponsor_tax_amount) >= 0.00 THEN 'R'::character ELSE 'N'::character END as transaction_type,
	(bc.old_sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.old_claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.old_tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_tax_sub_group_id is NOT null AND 
	(bc.tax_sub_group_id != bc.old_tax_sub_group_id OR COALESCE(bc.old_claim_id, '') != COALESCE(bc.claim_id, '')) 
	AND COALESCE(bc.old_sponsor_tax_amount, 0) != 0

UNION ALL

-- insurance portion of tpa bills -> when tax amount is changed, but neither the sub group nor the tpa changed
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)
	ELSE 0.00 - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) >= 0 THEN tpa.tpa_name ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE tpa.tpa_name END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) ELSE 0.00 - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.sponsor_tax_amount-bc.old_sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM bill_charge_details_adjustment bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_sponsor_tax_amount is NOT null AND bc.sponsor_tax_amount != bc.old_sponsor_tax_amount
	AND COALESCE(bc.old_claim_id, '') = COALESCE(bc.claim_id, '') AND COALESCE(bc.old_tax_sub_group_id, -1) = COALESCE(bc.tax_sub_group_id, -1)
	AND COALESCE((bc.sponsor_tax_amount-bc.old_sponsor_tax_amount), 0) != 0
ORDER BY bill_no ;

DROP VIEW IF EXISTS acc_all_hospital_bills_patient_tax_adjustment_view CASCADE ;
CREATE VIEW acc_all_hospital_bills_patient_tax_adjustment_view AS
-- cash portion of tpa bills, on new inserts
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN bc.tax_amt - bc.sponsor_tax_amount
	ELSE 0.00 - (bc.tax_amt - bc.sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN 'Counter Receipts' ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts' END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN (bc.tax_amt - bc.sponsor_tax_amount) ELSE 0.00 - (bc.tax_amt - bc.sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.tax_amt - bc.sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_consolidated_tax_adjustments_view bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true 
	AND (bc.tax_amt - bc.sponsor_tax_amount) != 0.00
	AND bc.old_tax_sub_group_id is null AND bc.tax_amt != bc.sponsor_tax_amount

UNION ALL

-- cash portion of tpa bills -> when tax subgroup / tpa id changed, post a regular for the new tax amt and new sub group / tpa
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.tax_amt- bc.sponsor_tax_amount) >= 0 THEN (bc.tax_amt- bc.sponsor_tax_amount)
	ELSE 0.00 - (bc.tax_amt- bc.sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.tax_amt- bc.sponsor_tax_amount) >= 0 THEN 'Counter Receipts' ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (bc.tax_amt- bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts' END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.tax_amt- bc.sponsor_tax_amount) >= 0 THEN (bc.tax_amt- bc.sponsor_tax_amount) ELSE 0.00 - (bc.tax_amt- bc.sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.tax_amt- bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(bc.tax_amt- bc.sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_consolidated_tax_adjustments_view bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true 
	AND (bc.tax_amt - bc.sponsor_tax_amount) != 0.00
	AND bc.old_tax_sub_group_id is NOT null 
	AND (bc.tax_sub_group_id != bc.old_tax_sub_group_id OR COALESCE(bc.claim_id, '') != COALESCE(bc.old_claim_id))

UNION ALL

-- insurance portion of tpa bills -> when tax subgroup id changed, post a contra for the old sponsor tax and old sub group
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) >= 0 THEN (bc.old_tax_amt - bc.old_sponsor_tax_amount)
	ELSE 0.00 - (bc.old_tax_amt - bc.old_sponsor_tax_amount) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as debit_account,
	CASE WHEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) >= 0 THEN  'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as credit_account,
	0 AS tax_amount, 
	CASE WHEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) >= 0 THEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) ELSE 0.00 - (bc.old_tax_amt - bc.old_sponsor_tax_amount) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (bc.old_tax_amt - bc.old_sponsor_tax_amount) >= 0.00 THEN 'R'::character ELSE 'N'::character END as transaction_type,
	(bc.old_tax_amt - bc.old_sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_consolidated_tax_adjustments_view bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.old_claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.old_tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_tax_sub_group_id is NOT null AND 
	(bc.tax_sub_group_id != bc.old_tax_sub_group_id OR COALESCE(bc.old_claim_id, '') != COALESCE(bc.claim_id, '')) 

UNION ALL

-- insurance portion of tpa bills -> when tax amount is changed, but neither the sub group nor the tpa changed
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
	cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
	sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
	b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
	mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) >= 0 THEN 
		((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount))
	ELSE 0.00 - ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) END AS gross_amount,
	0.00 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) >= 0 
		THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) >= 0 
		THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0 AS tax_amount, 
	CASE WHEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) >= 0 
		THEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) 
		ELSE 0.00 - ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) END AS net_amount,
	doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
	coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
	''::text as incoimng_hospital,
	d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, b.remarks, bc.mod_time as mod_time,
	''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN ((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) >= 0.00 
		THEN 'N'::character ELSE 'R'::character END as transaction_type,
	((bc.tax_amt - bc.old_tax_amt) - (bc.sponsor_tax_amount-bc.old_sponsor_tax_amount)) as transaction_amount, 
	tpa.tpa_name as sponsor_name, tpa.sponsor_type, 
	isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
	''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_consolidated_tax_adjustments_view bc
	LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
	JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
   	JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
   	JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON (mbc.bill_no=b.bill_no)
	-- left join required on sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
	--this has to be left join, for retail credit bills we can add round offs.
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
	LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
	LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
	LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
	LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
	LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
	LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
	LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
	AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
	AND bc.old_sponsor_tax_amount > 0 AND (bc.sponsor_tax_amount != bc.old_sponsor_tax_amount OR bc.tax_amt != bc.old_tax_amt)
	AND COALESCE(bc.old_claim_id, '') = COALESCE(bc.claim_id, '') AND COALESCE(bc.old_tax_sub_group_id, -1) = COALESCE(bc.tax_sub_group_id, -1)
ORDER BY bill_no ;
	
---======================================================= HOSPITAL BILLS TAX VOUCHERS END ===============================================

-- ================================================= PHARMACY SALES BILLS ====================================================

DROP VIEW IF EXISTS acc_all_pharmacy_bills_view CASCADE ;
CREATE VIEW acc_all_pharmacy_bills_view AS
-- non-tpa bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0 THEN amount + item_discount
	ELSE -amount - item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount + item_discount) >= 0 THEN 'Counter Receipts'::text
	ELSE account_head_name END as debit_account,
	CASE WHEN (amount + item_discount) >= 0 THEN account_head_name
	ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, account_head_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,cust_item_code, ''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type='P' and is_tpa != true
UNION ALL
-- cash portion of tpa bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	
	CASE WHEN  ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END )-insurance_claim_amt) >= 0
		THEN ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END )-insurance_claim_amt)
		ELSE 0.00 - ((amount + CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END )-insurance_claim_amt) END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (insurance_claim_amt) != amount THEN item_discount ELSE 0.00 END  as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount  - insurance_claim_amt) >= 0 THEN 'Counter Receipts'::text
	ELSE account_head_name END as debit_account,
	CASE WHEN (amount  - insurance_claim_amt) >= 0 THEN account_head_name
	ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN (amount - insurance_claim_amt) > 0.00 THEN (amount  - insurance_claim_amt) ELSE -(amount - insurance_claim_amt) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	amount-insurance_claim_amt as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type='P' and is_tpa = true and (amount != insurance_claim_amt )
UNION ALL
-- insurance portion of insurance bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	
	CASE WHEN (insurance_claim_amt) >= 0 
	THEN 
		CASE WHEN (insurance_claim_amt) = amount 
		THEN insurance_claim_amt + item_discount 
		ELSE (insurance_claim_amt) END 
	ELSE 
		CASE WHEN (insurance_claim_amt) = amount
		THEN 0.00 - (insurance_claim_amt) - item_discount
		ELSE 0.00 - insurance_claim_amt END
	END AS gross_amount,
	0.00 AS round_off_amount,
	CASE WHEN (insurance_claim_amt) = amount THEN item_discount ELSE 0.00 END as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (insurance_claim_amt) >= 0 THEN primary_sponsor_name
	ELSE account_head_name END as debit_account,
	CASE WHEN (insurance_claim_amt) >= 0 THEN account_head_name
	ELSE primary_sponsor_name END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN (insurance_claim_amt) > 0.00 THEN (insurance_claim_amt) ELSE -(insurance_claim_amt) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	insurance_claim_amt as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	account_head_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where restriction_type='P' and is_tpa = true and insurance_claim_amt != 0.00
UNION ALL
-- All pharmacy discounts
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0.00 THEN amount + item_discount
	ELSE -amount-item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Discounts A/C'::text
	ELSE 'Counter Receipts'::text END as debit_account,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Counter Receipts'::text
	ELSE 'Discounts A/C'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	-amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_discounts where restriction_type='P'
UNION ALL
-- All pharmacy roundoffs
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN (amount + item_discount) >= 0.00 THEN amount + item_discount
	ELSE -amount-item_discount END AS gross_amount, round_off AS round_off_amount,
	item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Counter Receipts'::text
	ELSE 'Roundoff A/C' END as debit_account,
	CASE WHEN (amount + item_discount) >= 0.00 THEN 'Roundoff A/C'::text
	ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN amount > 0.00 THEN amount ELSE -amount END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_roundoffs where restriction_type='P'
ORDER BY bill_no;

--================================================ COST ACCOUNTING ===========================================================

DROP VIEW IF EXISTS acc_all_stock_transactions_view CASCADE;
CREATE VIEW acc_all_stock_transactions_view AS
-- Cost accounting for sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'INVTRANS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	abs(cost_value) as gross_amount, 0.00 AS round_off_amount,
	0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (cost_value) >= 0 THEN
		CASE WHEN (type = 'S') THEN 'COGS A/C'::text ELSE 'Inventory A/C'::text END
	ELSE
		CASE WHEN (type = 'S') THEN 'Inventory A/C'::text ELSE 'COGS A/C'::text END
	END as debit_account,
	CASE WHEN (cost_value) >= 0 THEN
		CASE WHEN (type = 'S') THEN 'Inventory A/C'::text ELSE 'COGS A/C'::text END
	ELSE
		CASE WHEN (type = 'S') THEN 'COGS A/C'::text ELSE 'Inventory A/C'::text END
	END as credit_account,
	0.00 AS tax_amount, CASE WHEN cost_value > 0.00 THEN cost_value ELSE -cost_value END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills where cost_value != 0
UNION ALL
-- Cost accounting for issues
SELECT visit_center_id, visit_center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'INVTRANS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate, 
	abs(cost_value) as gross_amount, round_off as round_off_amount, item_discount as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (cost_value) >= 0 THEN 'COGS A/C'::text ELSE 'Inventory A/C'::text END as debit_account,
	CASE WHEN (cost_value) >= 0 THEN 'Inventory A/C'::text ELSE 'COGS A/C'::text END as credit_account,
	0.00 AS tax_amount, abs(cost_value) as net_amount,
	''::text as admitting_department, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, 
	''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (charge_head = 'Inventory Item') THEN 'N'::character ELSE 'R'::character END as transaction_type,
	cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code ,''::text as prescribing_doctor_dept_name
FROM acc_all_stock_issues where cost_value != 0
UNION ALL
-- Cost accounting for inventory returns
SELECT visit_center_id, visit_center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'INVTRANS'::text as voucher_type, voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	qty as quantity, ''::text as unit, 0.00 as unit_rate, 
	abs(cost_value) as gross_amount, round_off as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (cost_value) >= 0 THEN 'Inventory A/C'::text ELSE 'COGS A/C'::text END as debit_account,
	CASE WHEN (cost_value) >= 0 THEN 'COGS A/C'::text ELSE 'Inventory A/C'::text END as credit_account,
	0.00 AS tax_amount, abs(cost_value) as net_amount,
	admitting_doctor, prescribing_doctor, conductiong_doctor, referral_doctor, payee_doctor, 
	''::text as outhouse_name, ''::text as incoimng_hospital,
	admitting_department, conducting_department, cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN (charge_head = 'Inventory Item') THEN 'N'::character ELSE 'R'::character END as transaction_type,
	cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_stock_returns where cost_value != 0;

--======================================= STOCK TRANSFERS ===============================================================

DROP VIEW IF EXISTS acc_intra_center_stock_transfers_view CASCADE ;

DROP VIEW IF EXISTS acc_all_stock_transfers_view CASCADE ;
CREATE VIEW acc_all_stock_transfers_view AS
-- All store transfers
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, ts.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	stm.transfer_no as voucher_no, 'STOCKTRANSFER'::text as voucher_type, stm.date_time as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ts.dept_name::text as receipt_store, fs.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	std.qty - std.qty_rejected as quantity, ''::text as unit, 0.00 as unit_rate,
	std.cost_value as gross_amount, 0.00 as round_off_amount,
	0.00 as discount_amount, 0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Inventory A/C'::text as debit_account, 'Inventory A/C'::text as credit_account,
	0.00 as tax_amount, std.cost_value as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, std.cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, stm.reason as remarks, stm.date_time as mod_time,
	''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, fhcm.center_name as issue_store_center, hcm.center_name as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	std.cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_transfer_details std
JOIN store_transfer_main stm on (std.transfer_no = stm.transfer_no)
JOIN store_item_details sid ON (sid.medicine_id=std.medicine_id)
JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
JOIN stores ts ON (stm.store_to=ts.dept_id)
JOIN stores fs on (stm.store_from = fs.dept_id)
--	JOIN supplier_master sm ON (supplier_code = supplier_id)
JOIN hospital_center_master hcm on (ts.center_id = hcm.center_id)
JOIN hospital_center_master fhcm on (fs.center_id = fhcm.center_id)
WHERE ts.center_id = fs.center_id and ts.account_group = fs.account_group and std.qty - std.qty_rejected != 0;

--==================================================== CONSUMPTION ACCOUNTS ========================================================

DROP VIEW IF EXISTS acc_all_reagents_consumption_view CASCADE ;
CREATE VIEW acc_all_reagents_consumption_view AS
-- Cost accounting for consumptions
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	srum.reagent_usage_seq as voucher_no, 'STKCONSUMPTION'::text as voucher_type, srum.date_time as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	srud.qty as quantity, ''::text as unit, 0.00 as unit_rate,
	srud.cost_value as gross_amount, 0.00 as round_off_amount,
	0.00 as discount_amount, 0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Internal Consumption A/C'::text as debit_account, 'Inventory A/C'::text as credit_account,
	0.00 as tax_amount, srud.cost_value as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, srud.cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, srum.date_time as mod_time,
	''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	srud.cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_reagent_usage_details srud
JOIN store_reagent_usage_main srum on (srud.reagent_usage_seq = srum.reagent_usage_seq)
JOIN store_item_batch_details sibd ON (sibd.item_batch_id=srud.item_batch_id)
JOIN store_item_details sid ON (sid.medicine_id=sibd.medicine_id)
JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
JOIN stores s ON (srum.store_id=s.dept_id)
--	JOIN supplier_master sm ON (supplier_code = supplier_id)
JOIN hospital_center_master hcm on (s.center_id = hcm.center_id)
WHERE srud.qty != 0;

-- Consumption against user / dept issues
DROP VIEW IF EXISTS acc_all_user_issues_returns_view CASCADE ;
CREATE VIEW acc_all_user_issues_returns_view AS
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	sism.user_issue_no as voucher_no, 'USERISSUE'::text as voucher_type, sism.date_time as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	sisd.qty as quantity, ''::text as unit, 0.00 as unit_rate,
	sisd.cost_value as gross_amount, 0.00 as round_off_amount,
	sisd.discount as discount_amount, 0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Internal Consumption A/C'::text as debit_account, 'Inventory A/C'::text as credit_account,
	0.00 as tax_amount, sisd.cost_value as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, sisd.cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, sism.date_time as mod_time,
	''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type,
	sisd.cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM stock_issue_details sisd
JOIN stock_issue_main sism on (sisd.user_issue_no = sism.user_issue_no)
JOIN store_item_batch_details sibd ON (sibd.item_batch_id=sisd.item_batch_id)
JOIN store_item_details sid ON (sid.medicine_id=sibd.medicine_id)
JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
JOIN stores s ON (sism.dept_from=s.dept_id)
--	JOIN supplier_master sm ON (supplier_code = supplier_id)
JOIN hospital_center_master hcm on (s.center_id = hcm.center_id)
WHERE sisd.qty != 0 and sism.user_type = 'Hospital'

UNION ALL
-- user returns, swap the cr/dr accounts
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	sism.user_issue_no as voucher_no, 'USERISSUE'::text as voucher_type, sism.date_time as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	sisd.qty as quantity, ''::text as unit, 0.00 as unit_rate,
	sisd.cost_value as gross_amount, 0.00 as round_off_amount,
	sisd.discount as discount_amount, 0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	'Inventory A/C'::text as debit_account, 'Internal Consumption A/C'::text as credit_account,
	0.00 as tax_amount, sisd.cost_value as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, sisd.cost_value as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, sism.date_time as mod_time,
	''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'R'::character as transaction_type,
	sisd.cost_value as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_issue_returns_details sisd
JOIN store_issue_returns_main sism on (sisd.user_return_no = sism.user_return_no)
JOIN store_item_batch_details sibd ON (sibd.item_batch_id=sisd.item_batch_id)
JOIN store_item_details sid ON (sid.medicine_id=sibd.medicine_id)
JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
JOIN stores s ON (sism.dept_to=s.dept_id)
--	JOIN supplier_master sm ON (supplier_code = supplier_id)
JOIN hospital_center_master hcm on (s.center_id = hcm.center_id)
LEFT JOIN stock_issue_main sim on (sim.user_issue_no = sism.user_issue_no)
WHERE sisd.qty != 0 and coalesce(sim.user_type, '') = 'Hospital';

--===================================DEPOSIT SET OFF FOR ACCOUNTING ==========================================================

DROP VIEW IF EXISTS acc_all_deposits_setoff_view CASCADE;
CREATE VIEW acc_all_deposits_setoff_view AS
-- deposit setoff
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, pds.mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, b.account_group as account_group,
	''::text as service_group, ''::text as service_sub_group, pds.bill_no, ''::text as audit_control_number,
	pds.bill_no as voucher_no, 'RECEIPT'::text as voucher_type, date(pds.created_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	pds.amount as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN (pds.deposit_for = 'B') THEN
		CASE WHEN pds.is_multi_pkg is TRUE THEN 'Package Deposit Liability Account'::text ELSE 'General Deposit Liability Account'::text END
	ELSE 'IP Deposit Liability Account'::text END as debit_account,
	'Counter Receipts'::text as credit_account,
	0.00 as tax_amount, pds.amount as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, pds.mod_time, ''::text counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, -- voucher type is payment so tx type is normal
	pds.amount as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, ''::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,''::text as cust_item_code ,''::text as prescribing_doctor_dept_name 
FROM patient_deposits_setoff_adjustments pds
	LEFT JOIN bill b ON (pds.bill_no=b.bill_no)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON (pds.mr_no = pd.mr_no)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id);

--=================================== TAX related pharmacy views =======================================
-- Pharmacy Bills Tax views

DROP VIEW IF EXISTS acc_all_pharmacy_bills_tax_view CASCADE ;
CREATE VIEW acc_all_pharmacy_bills_tax_view AS
-- non-tpa bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(sstd.tax_amt,0) >= 0 THEN COALESCE(sstd.tax_amt,0) ELSE -sstd.tax_amt END AS gross_amount, 0 AS round_off_amount,
	0 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sstd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(sstd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(sstd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN COALESCE(sstd.tax_amt,0) > 0.00 THEN COALESCE(sstd.tax_amt,0) ELSE -sstd.tax_amt END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(sstd.tax_amt,0) ELSE COALESCE(sstd.tax_amt,0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, isg.item_subgroup_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date,cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb 
LEFT JOIN store_sales_tax_details sstd ON (aapb.sale_item_id = sstd.sale_item_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id)
where restriction_type='P' and is_tpa != true and COALESCE(sstd.tax_amt,0) != 0
UNION ALL
-- cash portion of tpa bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(ptv.tax_amt, 0) >= 0 THEN COALESCE(ptv.tax_amt,0) ELSE -ptv.tax_amt END AS gross_amount,
	0.00 AS round_off_amount, 0.00  as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, COALESCE(ptv.tax_rate, 0) as sales_vat_percent,
	CASE WHEN (COALESCE(ptv.tax_amt, 0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(ptv.tax_amt, 0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN COALESCE(ptv.tax_amt, 0) > 0.00 THEN COALESCE(ptv.tax_amt,0) ELSE -ptv.tax_amt END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(ptv.tax_amt,0) ELSE COALESCE(ptv.tax_amt,0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb
LEFT JOIN acc_all_sub_group_sponsor_tax_amt_view ptv ON(ptv.sale_item_id = aapb.sale_item_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = ptv.item_subgroup_id)
where restriction_type='P' and is_tpa = true and COALESCE(ptv.tax_amt, 0) != 0 and aapb.type='S'
UNION ALL
-- cash portion of tpa bills sales returns
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END AS gross_amount,
	0.00 AS round_off_amount, 0.00  as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, COALESCE(sstd.tax_rate, 0) as sales_vat_percent,
	CASE WHEN (COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) > 0.00 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' 
		then -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb
LEFT JOIN 
	(SELECT ssd.sale_item_id, ssd.tax - sum(coalesce(scd.tax_amt, 0)) as tax_amt 
	FROM store_sales_details ssd 
	LEFT JOIN sales_claim_details scd ON (ssd.sale_item_id = scd.sale_item_id)
	GROUP BY ssd.sale_item_id, ssd.tax) AS apprm ON (apprm.sale_item_id = aapb.sale_item_id) 
LEFT JOIN store_sales_tax_details sstd ON (apprm.sale_item_id = sstd.sale_item_id)
LEFT JOIN 
	(SELECT sale_item_id, item_subgroup_id, 
	coalesce(tax_amt,0) / (sum(coalesce(tax_amt, 0)) OVER (PARTITION BY sale_item_id)) AS subgroup_ratio
	FROM store_sales_tax_details 
	WHERE coalesce(tax_amt, 0) != 0) as taxratio
	ON (taxratio.sale_item_id = sstd.sale_item_id AND taxratio.item_subgroup_id = sstd.item_subgroup_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id) 
where restriction_type='P' and is_tpa = true and COALESCE(apprm.tax_amt, 0) != 0 and aapb.type='R'
UNION ALL
-- insurance portion of insurance bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(sctd.tax_amt,0) >= 0 THEN COALESCE(sctd.tax_amt,0) ELSE -sctd.tax_amt END AS gross_amount,
	0.00 AS round_off_amount, 0.00 AS discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sctd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(sctd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN primary_sponsor_name ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(sctd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE primary_sponsor_name END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN COALESCE(sctd.tax_amt,0) > 0.00 THEN COALESCE(sctd.tax_amt,0) ELSE -sctd.tax_amt END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(sctd.tax_amt,0) ELSE COALESCE(sctd.tax_amt,0) END) as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb LEFT JOIN sales_claim_tax_details sctd ON(aapb.sale_item_id = sctd.sale_item_id) 
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sctd.item_subgroup_id)
where restriction_type='P' and is_tpa = true and COALESCE(sctd.tax_amt,0) != 0 and aapb.type='S'
UNION ALL
-- insurance portion of insurance bills sales returns
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) END AS gross_amount,
	0.00 AS round_off_amount, 0.00 AS discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sstd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN primary_sponsor_name ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE primary_sponsor_name END as credit_account,
	0.00 AS tax_amount,
	CASE WHEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) > 0.00 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN type = 'S' THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END) as transaction_amount, 
		primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb 
LEFT JOIN 
	(SELECT ssd.sale_item_id, sum(coalesce(scd.tax_amt, 0)) as tax_amt 
	FROM store_sales_details ssd 
	LEFT JOIN sales_claim_details scd ON (ssd.sale_item_id = scd.sale_item_id)
	GROUP BY ssd.sale_item_id) AS apprm ON (apprm.sale_item_id = aapb.sale_item_id) 
LEFT JOIN store_sales_tax_details sstd ON (apprm.sale_item_id = sstd.sale_item_id)
LEFT JOIN 
	(SELECT sale_item_id, item_subgroup_id, 
	coalesce(tax_amt,0) / (sum(coalesce(tax_amt, 0)) OVER (PARTITION BY sale_item_id)) AS subgroup_ratio
	FROM store_sales_tax_details 
	WHERE coalesce(tax_amt, 0) != 0) as taxratio
	ON (taxratio.sale_item_id = sstd.sale_item_id AND taxratio.item_subgroup_id = sstd.item_subgroup_id)
--LEFT JOIN (SELECT sum(scd.tax_amt) as tax_amt,sstd.item_subgroup_id,scd.sale_item_id,sstd.tax_rate FROM sales_claim_details scd 
--			JOIN store_sales_tax_details sstd USING(sale_item_id) group by sale_item_id ,sstd.item_subgroup_id,sstd.tax_rate) sctd ON(aapb.sale_item_id = sctd.sale_item_id) 
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id)
where  restriction_type='P' and is_tpa = true and COALESCE(apprm.tax_amt,0) !=0 and aapb.type='R'
ORDER BY bill_no;

-- Hospital Bills Tax views

DROP VIEW IF EXISTS acc_all_hospital_pharma_bills_tax_view CASCADE ;
CREATE VIEW acc_all_hospital_pharma_bills_tax_view AS
-- All pharmacy bills added to hospital bill, cash
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(sstd.tax_amt,0) >= 0 THEN COALESCE(sstd.tax_amt,0) ELSE -COALESCE(sstd.tax_amt,0) END AS gross_amount, 0.00 AS round_off_amount,
	0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sstd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(sstd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(sstd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts' END as credit_account,
	0.00 AS tax_amount, CASE WHEN COALESCE(sstd.tax_amt,0) > 0.00 THEN COALESCE(sstd.tax_amt,0) ELSE -COALESCE(sstd.tax_amt,0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN COALESCE(sstd.tax_amt,0) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(sstd.tax_amt,0) ELSE COALESCE(sstd.tax_amt,0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, isg.item_subgroup_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb
LEFT JOIN store_sales_tax_details sstd ON (aapb.sale_item_id = sstd.sale_item_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id)
where restriction_type IN ('N', 'T') and is_tpa != true and COALESCE(sstd.tax_amt,0) != 0
UNION ALL
-- All pharmacy bills added to hospital bills - cash portion of tpa bills sales
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN  COALESCE(ptv.tax_amt, 0) >= 0 THEN COALESCE(ptv.tax_amt,0) ELSE -COALESCE(ptv.tax_amt,0) END AS gross_amount,
	0.00 AS round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, COALESCE(ptv.tax_rate, 0) as sales_vat_percent,
	CASE WHEN (COALESCE(ptv.tax_amt, 0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(ptv.tax_amt, 0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, 
	CASE WHEN COALESCE(ptv.tax_amt, 0) > 0.00 THEN COALESCE(ptv.tax_amt,0) ELSE -COALESCE(ptv.tax_amt, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN COALESCE(ptv.tax_amt, 0) > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(ptv.tax_amt,0) ELSE COALESCE(ptv.tax_amt,0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, isg.item_subgroup_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb
LEFT JOIN acc_all_sub_group_sponsor_tax_amt_view ptv ON(ptv.sale_item_id = aapb.sale_item_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = ptv.item_subgroup_id) 
where restriction_type IN ('N', 'T') and is_tpa = true AND COALESCE(ptv.tax_amt, 0) != 0 and aapb.type='S'
UNION ALL
-- All pharmacy bills added to hospital bills - cash portion of tpa bills sales return
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type, sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN  COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) END AS gross_amount,
	0.00 AS round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, COALESCE(sstd.tax_rate, 0) as sales_vat_percent,
	CASE WHEN (COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
	0.00 AS tax_amount, 
	CASE WHEN COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) > 0.00 
		THEN COALESCE(apprm.tax_amt,0) * COALESCE(taxratio.subgroup_ratio, 0) 
		ELSE -COALESCE(apprm.tax_amt, 0) * COALESCE(taxratio.subgroup_ratio, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN COALESCE(apprm.tax_amt, 0) > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(apprm.tax_amt,0) ELSE COALESCE(apprm.tax_amt,0) END) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, isg.item_subgroup_name::text as account_name,
	''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb 
--LEFT JOIN (SELECT (sstd.tax_amt+sum(scd.tax_amt)) as tax_amt,sstd.item_subgroup_id,sstd.sale_item_id,sstd.tax_rate FROM store_sales_tax_details sstd JOIN sales_claim_details scd 
--ON(sstd.sale_item_id = scd.sale_item_id) JOIN store_sales_details ssd ON(ssd.sale_item_id = sstd.sale_item_id) 
--JOIN store_sales_main ssm ON (ssm.sale_id=ssd.sale_id) WHERE ssm.type = 'R'  group by sstd.item_subgroup_id, sstd.tax_amt, sstd.tax_rate,sstd.sale_item_id) apprm ON (aapb.sale_item_id = apprm.sale_item_id)
LEFT JOIN 
	(SELECT ssd.sale_item_id, sum(coalesce(scd.tax_amt, 0)) as tax_amt 
	FROM store_sales_details ssd 
	LEFT JOIN sales_claim_details scd ON (ssd.sale_item_id = scd.sale_item_id)
	GROUP BY ssd.sale_item_id) AS apprm ON (apprm.sale_item_id = aapb.sale_item_id) 
LEFT JOIN store_sales_tax_details sstd ON (apprm.sale_item_id = sstd.sale_item_id)
LEFT JOIN 
	(SELECT sale_item_id, item_subgroup_id, 
	coalesce(tax_amt,0) / (sum(coalesce(tax_amt, 0)) OVER (PARTITION BY sale_item_id)) AS subgroup_ratio
	FROM store_sales_tax_details 
	WHERE coalesce(tax_amt, 0) != 0) as taxratio
	ON (taxratio.sale_item_id = sstd.sale_item_id AND taxratio.item_subgroup_id = sstd.item_subgroup_id)
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id) 
where restriction_type IN ('N', 'T') and is_tpa = true AND COALESCE(apprm.tax_amt, 0) != 0 and aapb.type='R'
-- All pharmacy bills added to hospital bill, insurance sales
UNION ALL
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(sctd.tax_amt,0) >= 0 THEN COALESCE(sctd.tax_amt,0) ELSE -sctd.tax_amt END AS gross_amount,
	0.00 AS round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sctd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(sctd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN primary_sponsor_name::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(sctd.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE primary_sponsor_name::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN COALESCE(sctd.tax_amt,0) > 0.00 THEN COALESCE(sctd.tax_amt,0) ELSE -COALESCE(sctd.tax_amt, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN COALESCE(sctd.tax_amt,0) > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then -COALESCE(sctd.tax_amt,0) ELSE COALESCE(sctd.tax_amt,0) END) as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb LEFT JOIN sales_claim_tax_details sctd ON(aapb.sale_item_id = sctd.sale_item_id) 
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sctd.item_subgroup_id) 
where restriction_type IN ('N', 'T') and is_tpa = true AND COALESCE(sctd.tax_amt,0) != 0 and aapb.type='S'
UNION ALL
-- All pharmacy bills added to hospital bill, insurance sales return 
SELECT visit_center_id as center_id, visit_center_name as center_name, visit_type, mr_no, visit_id,
	charge_group, charge_head, account_group,
	service_group_name as service_group, service_sub_group_name as service_sub_group, bill_no, audit_control_number,
	bill_no as voucher_no, 'PHBILLS'::text as voucher_type,sale_date as voucher_date,
	item_code, item_name, ''::text as receipt_store, store_name::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(apprm.tax_amt,0) >= 0 THEN COALESCE(apprm.tax_amt,0) ELSE -apprm.tax_amt END AS gross_amount,
	0.00 AS round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	category_id as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
	0.00 as sales_vat_amount, sstd.tax_rate as sales_vat_percent,
	CASE WHEN (COALESCE(apprm.tax_amt,0) >= 0 AND aapb.type != 'R') THEN primary_sponsor_name::text ELSE 'Tax Liability A/C'::text END as debit_account,
	CASE WHEN (COALESCE(apprm.tax_amt,0) >= 0 AND aapb.type != 'R') THEN 'Tax Liability A/C'::text ELSE primary_sponsor_name::text END as credit_account,
	0.00 AS tax_amount, CASE WHEN COALESCE(apprm.tax_amt,0) > 0.00 THEN COALESCE(apprm.tax_amt,0) ELSE -COALESCE(apprm.tax_amt, 0) END AS net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, ''::text as remarks, mod_time,
	''::text as counter_no, open_date as bill_open_date, finalized_date as bill_finalized_date, CASE WHEN is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
	primary_insurance_co as insurance_co, oldmrno as old_mr_no, store_center as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 
	CASE WHEN COALESCE(apprm.tax_amt,0) > 0 THEN 'N'::character ELSE 'R'::character END as transaction_type,
	(CASE WHEN aapb.type = 'R' then COALESCE(apprm.tax_amt,0) ELSE -COALESCE(apprm.tax_amt,0) END) as transaction_amount, primary_sponsor_name as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date, cust_item_code,''::text as prescribing_doctor_dept_name
FROM acc_all_pharmacy_bills aapb 
LEFT JOIN 
	(SELECT ssd.sale_item_id, sum(coalesce(scd.tax_amt, 0)) as tax_amt 
	FROM store_sales_details ssd 
	LEFT JOIN sales_claim_details scd ON (ssd.sale_item_id = scd.sale_item_id)
	GROUP BY ssd.sale_item_id) AS apprm ON (apprm.sale_item_id = aapb.sale_item_id) 
LEFT JOIN store_sales_tax_details sstd ON (apprm.sale_item_id = sstd.sale_item_id)
LEFT JOIN 
	(SELECT sale_item_id, item_subgroup_id, 
	coalesce(tax_amt,0) / (sum(coalesce(tax_amt, 0)) OVER (PARTITION BY sale_item_id)) AS subgroup_ratio
	FROM store_sales_tax_details 
	WHERE coalesce(tax_amt, 0) != 0) as taxratio
	ON (taxratio.sale_item_id = sstd.sale_item_id AND taxratio.item_subgroup_id = sstd.item_subgroup_id)
--LEFT JOIN (SELECT sum(scd.tax_amt) as tax_amt,sstd.item_subgroup_id,scd.sale_item_id,sstd.tax_rate FROM sales_claim_details scd JOIN store_sales_tax_details sstd USING(sale_item_id) group by sale_item_id ,sstd.item_subgroup_id,sstd.tax_rate) sctd ON(aapb.sale_item_id = sctd.sale_item_id) 
LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sstd.item_subgroup_id) 
where restriction_type IN ('N', 'T') and is_tpa = true AND COALESCE(apprm.tax_amt,0) != 0 and aapb.type='R'
ORDER BY bill_no;

-- Supplier invoices tax
DROP VIEW IF EXISTS acc_all_invoices_tax_view CASCADE;
CREATE VIEW acc_all_invoices_tax_view AS
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	i.invoice_no||'/'||gm.grn_no as voucher_no, 'PURCHASE'::text as voucher_type, i.invoice_date as voucher_date,
	pmd.medicine_id::text as item_code, pmd.medicine_name as item_name, s.dept_name as receipt_store, ''::text as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	g.billed_qty as quantity, g.grn_pkg_size as unit, 0 as unit_rate,
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN COALESCE(gt.tax_amt, g.tax, 0) ELSE -COALESCE(gt.tax_amt, g.tax, 0) END as gross_amount, 0.00 as round_off_amount, 0.00 as discount_amount,
	0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	icm.category_id as item_category_id, 0.00 as purchase_vat_amount, gt.tax_rate as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN 'Tax Paid A/C'::text ELSE sup.supplier_name END as debit_account, 
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN sup.supplier_name ELSE 'Tax Paid A/C'::text END as credit_account,
	0.00 as tax_amount, CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN COALESCE(gt.tax_amt, g.tax, 0) ELSE -COALESCE(gt.tax_amt, g.tax, 0) END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
	i.invoice_no as invoice_no, i.invoice_date as invoice_date, gm.grn_no::text as voucher_ref, ''::text as remarks, i.date_time as mod_time, ''::text as counter_no,
	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
	i.po_no::text as po_number, p.po_date::date as po_date, 'N'::character as transaction_type,
	COALESCE(gt.tax_amt, g.tax, 0) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name, sup.cust_supplier_code as cust_supplier_code,gm.grn_date as grn_date,
	pmd.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_invoice i
	JOIN store_grn_main gm USING (supplier_invoice_id)
	JOIN store_grn_details g USING (grn_no)
	JOIN store_item_details pmd using (medicine_id)
	LEFT JOIN store_grn_tax_details gt ON (gt.grn_no = g.grn_no AND gt.item_batch_id = g.item_batch_id AND gt.medicine_id = g.medicine_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id=gt.item_subgroup_id)
	JOIN stores s on s.dept_id=gm.store_id
	JOIN hospital_center_master hcm ON s.center_id=hcm.center_id
	JOIN supplier_master sup ON (i.supplier_id = sup.supplier_code)
	JOIN store_category_master icm ON (pmd.med_category_id=icm.category_id)
	LEFT JOIN store_po_main p ON (p.po_no = i.po_no) 
WHERE i.consignment_stock=false and i.status!='O';

-- Supplier invoices consigment tax 
DROP VIEW IF EXISTS acc_all_cs_issued_tax_view CASCADE;
CREATE VIEW acc_all_cs_issued_tax_view AS
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, si.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	si.invoice_no||'/'||sgm.grn_no as voucher_no, 'CSISSUE'::text as voucher_type, date(sci.con_invoice_date) as voucher_date,
	''::text as item_code, ''::text as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	0.00 as quantity, ''::text as unit, 0.00 as unit_rate,
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN COALESCE(gt.tax_amt, g.tax, 0) ELSE -COALESCE(gt.tax_amt, g.tax, 0) END as gross_amount, 0.00 as round_off_amount,
	0.00 as discount_amount, 0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, gt.tax_rate as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN 'Tax Paid A/C'::text ELSE sm.supplier_name END as debit_account, 
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN sm.supplier_name ELSE 'Tax Paid A/C'::text END as credit_account, 0.00 as tax_amount, 
	CASE WHEN COALESCE(gt.tax_amt, g.tax, 0) >= 0 THEN COALESCE(gt.tax_amt, g.tax, 0) ELSE -COALESCE(gt.tax_amt, g.tax, 0) END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, sm.supplier_name as supplier_name,
	si.invoice_no as invoice_no, si.invoice_date as invoice_date, ''::text as voucher_ref,
	si.remarks as remarks, si.date_time as mod_time, ''::text as counter_no, null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	si.po_no::text as po_number, p.po_date::date as po_date, 'N'::character as transaction_type,
	COALESCE(gt.tax_amt, g.tax, 0) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,sm.cust_supplier_code as cust_supplier_code,sgm.grn_date,
	pmd.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_consignment_invoice sci
	JOIN store_invoice si USING (supplier_invoice_id)
	JOIN store_grn_main sgm ON (sci.grn_no = sgm.grn_no)
	JOIN store_grn_details g ON (sgm.grn_no=g.grn_no and sci.medicine_id=g.medicine_id and sci.batch_no=g.batch_no)
	JOIN store_item_details pmd ON (pmd.medicine_id=g.medicine_id)
	LEFT JOIN store_grn_tax_details gt ON (gt.grn_no = g.grn_no AND gt.item_batch_id = g.item_batch_id AND gt.medicine_id = g.medicine_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id=gt.item_subgroup_id)
	JOIN stock_issue_details sid ON (sid.user_issue_no=sci.issue_id
		and sid.medicine_id=sci.medicine_id and sid.batch_no=sci.batch_no)
	JOIN stock_issue_main sim ON (sim.user_issue_no=sci.issue_id)
	JOIN supplier_master sm ON (si.supplier_id=sm.supplier_code)
	JOIN store_category_master scm ON (pmd.med_category_id=scm.category_id)
	JOIN stores s ON (s.dept_id = sgm.store_id)
	LEFT JOIN store_po_main p ON (p.po_no = si.po_no)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
WHERE si.consignment_stock=true;

-- supplier returns with debit note tax
DROP VIEW IF EXISTS acc_all_returns_with_debitnote_tax_view CASCADE ;
CREATE VIEW acc_all_returns_with_debitnote_tax_view AS
SELECT hcm.center_id, hcm.center_name, ''::text as visit_type, ''::text as mr_no, ''::text as visit_id,
	''::text as charge_group, ''::text as charge_head, s.account_group,
	''::text as service_group, ''::text as service_sub_group, ''::text as bill_no, ''::text as audit_control_number,
	sdn.debit_note_no as voucher_no, 'STORERETURNS'::text as voucher_type, sdn.debit_note_date as voucher_date,
	sid.medicine_id::text as item_code, sid.medicine_name as item_name, ''::text as receipt_store, s.dept_name as issue_store,
	''::text as currency, 0.00 as currency_conversion_rate,
	g.billed_qty as quantity, g.grn_pkg_size as unit, 0 as unit_rate, 
	CASE WHEN gt.tax_amt >= 0 THEN gt.tax_amt ELSE -gt.tax_amt END as gross_amount, 0.00 as round_off_amount,
	0.00 as discount_amount,0 as points_redeemed, 0.00 as points_redeemed_rate, 0.00 as points_redeemed_amt,
	scm.category_id as item_category_id, 0.00 as purchase_vat_amount, gt.tax_rate as purchase_vat_percent,
	0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
	CASE WHEN gt.tax_amt >= 0 THEN sm.supplier_name ELSE 'Tax Paid A/C'::text END as debit_account, 
	CASE WHEN gt.tax_amt >= 0 THEN 'Tax Paid A/C'::text ELSE sm.supplier_name END as credit_account,
	0.00 as tax_amount, CASE WHEN gt.tax_amt >= 0 THEN gt.tax_amt ELSE -gt.tax_amt END as net_amount,
	''::text as admitting_doctor, ''::text as prescribing_doctor, ''::text as conductiong_doctor,
	''::text as referral_doctor, ''::text as payee_doctor, ''::text as outhouse_name, ''::text as incoimng_hospital,
	''::text as admitting_department, ''::text as conducting_department, 0.00 as cost_amount, sm.supplier_name as supplier_name,
	''::text as invoice_no, NULL::timestamp as invoice_date, ''::text as voucher_ref, sdn.remarks as remarks, sdn.date_time as mod_time,
	''::text as counter_no,	null::date as bill_open_date, null::date as bill_finalized_date, ''::text as is_tpa,
	''::text as insurance_co, ''::text as old_mr_no, hcm.center_name as issue_store_center, ''::text as receipt_store_center,
	''::text as po_number, null::date as po_date, 'N'::character as transaction_type, -- voucher type is store returns, so transaction type is N
	gt.tax_amt as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type, 
	isg.item_subgroup_name::text as account_name,sm.cust_supplier_code as cust_supplier_code,NULL::timestamp as grn_date,
	sid.cust_item_code as cust_item_code,''::text as prescribing_doctor_dept_name
FROM store_debit_note sdn
	JOIN store_grn_main sgm ON (sgm.debit_note_no = sdn.debit_note_no)
	JOIN store_grn_details g ON (g.grn_no=sgm.grn_no)
	JOIN store_item_details sid ON (sid.medicine_id=g.medicine_id)
	LEFT JOIN store_grn_tax_details gt ON (gt.grn_no = g.grn_no AND gt.item_batch_id = g.item_batch_id AND gt.medicine_id = g.medicine_id)
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id=gt.item_subgroup_id)
	JOIN store_category_master scm ON (sid.med_category_id=scm.category_id)
	JOIN stores s ON (sgm.store_id=s.dept_id)
	JOIN supplier_master sm ON (supplier_code = supplier_id)
	JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
WHERE sdn.status='C'
ORDER BY sdn.debit_note_no;

--=================================== VIEWS TO EXTRACT THE MISSING TAX VOUCHERS ======================================

DROP VIEW IF EXISTS acc_missing_tax_adjustments_view CASCADE;
CREATE OR REPLACE VIEW acc_missing_tax_adjustments_view AS
select bct.charge_id, bct.tax_sub_group_id, bct.tax_rate, bct.tax_amount as tax_amt, bcct.claim_id, coalesce(bcct.sponsor_tax_amount, 0.00) as sponsor_tax_amount from bill_charge_tax bct left join bill_charge_claim_tax bcct on (bct.charge_id = bcct.charge_id and bct.tax_sub_group_id = bcct.tax_sub_group_id) where bct.charge_id not in (select distinct charge_id from bill_charge_details_adjustment);

DROP VIEW IF EXISTS acc_missing_patient_tax_adjustments_view CASCADE;
CREATE OR REPLACE VIEW acc_missing_patient_tax_adjustments_view AS
select bct.charge_id, bct.tax_sub_group_id, bct.tax_rate, bct.tax_amount as tax_amt, sum(coalesce(bcct.sponsor_tax_amount, 0.00)) as sponsor_tax_amount from bill_charge_tax bct left join bill_charge_claim_tax bcct on (bct.charge_id = bcct.charge_id and bct.tax_sub_group_id = bcct.tax_sub_group_id) where bct.charge_id not in (select distinct charge_id from bill_charge_details_adjustment) group by bct.charge_id, bct.tax_sub_group_id, bct.tax_rate, bct.tax_amount;

DROP VIEW IF EXISTS acc_all_missing_bills_tax_view CASCADE ;
CREATE VIEW acc_all_missing_bills_tax_view AS
-- non-insurance bills -> new record inserted
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
        cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
        sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
        b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
        mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
        ''::text as currency, 0.00 as currency_conversion_rate,
        mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
        CASE WHEN (bc.tax_amt) >= 0 THEN (bc.tax_amt) ELSE 0.00 - (bc.tax_amt) END AS gross_amount,
        0.00 AS round_off_amount,
        0 as discount_amount,
        0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
        0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
        0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
        CASE WHEN (bc.tax_amt) >= 0 THEN 'Counter Receipts'::text ELSE 'Tax Liability A/C'::text END as debit_account,
        CASE WHEN (bc.tax_amt) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts'::text END as credit_account,
        0 AS tax_amount, CASE WHEN bc.tax_amt >= 0 THEN bc.tax_amt ELSE 0.00 - bc.tax_amt END AS net_amount,
        doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
        coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor,
        ''::text as outhouse_name,      ''::text as incoimng_hospital,
        d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
        ''::text as invoice_no, NULL::timestamp as invoice_date, bc.charge_id::text as voucher_ref, b.remarks as remarks,
        mbc.mod_time as mod_time,        ''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date,
        CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
        ''::text as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
        ''::text as po_number, null::date as po_date,
        CASE WHEN bc.tax_amt >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
        bc.tax_amt as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type,
        isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
        ''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_missing_tax_adjustments_view bc
        JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
        JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
        JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
        JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
        JOIN bill b ON (mbc.bill_no=b.bill_no)
        -- left join required on sub groups, for ex: round off's there will be no service sub group.
        LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
        LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
        LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
        LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
        LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
        LEFT JOIN department d ON (d.dept_id=pr.dept_name)
        LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
        --this has to be left join, for retail credit bills we can add round offs.
        LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
        LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
        LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
        LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
        LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
        LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
        LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
        LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
        AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa != true

UNION ALL

-- insurance portion of tpa bills -> when new entries are inserted
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
        cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
        sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
        b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
        mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
        ''::text as currency, 0.00 as currency_conversion_rate,
        mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
        CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN bc.sponsor_tax_amount
        ELSE 0.00 - bc.sponsor_tax_amount END AS gross_amount,
        0.00 AS round_off_amount,
        0 as discount_amount,
        0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
        0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
        0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
        CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN tpa.tpa_name ELSE 'Tax Liability A/C'::text END as debit_account,
        CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE tpa.tpa_name END as credit_account,
        0 AS tax_amount,
        CASE WHEN (bc.sponsor_tax_amount) >= 0 THEN (bc.sponsor_tax_amount) ELSE 0.00 - (bc.sponsor_tax_amount) END AS net_amount,
        doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
        coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
        ''::text as incoimng_hospital,
        d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
        ''::text as invoice_no, NULL::timestamp as invoice_date, bc.charge_id::text as voucher_ref, b.remarks, mbc.mod_time as mod_time,
        ''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
        icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
        ''::text as po_number, null::date as po_date,
        CASE WHEN (bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
        (bc.sponsor_tax_amount) as transaction_amount, tpa.tpa_name as sponsor_name, tpa.sponsor_type,
        isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
        ''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_missing_tax_adjustments_view bc
        LEFT JOIN bill_charge_claim bcl ON (bcl.claim_id=bc.claim_id AND bcl.charge_id=bc.charge_id)
        JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
        JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
        JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
        JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
        JOIN bill b ON (mbc.bill_no=b.bill_no)
        -- left join required on sub groups, for ex: round off's there will be no service sub group.
        LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
        LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
        LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
        LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
        LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
        LEFT JOIN department d ON (d.dept_id=pr.dept_name)
        LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
        --this has to be left join, for retail credit bills we can add round offs.
        LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
        LEFT JOIN tpa_master tpa ON (bcl.sponsor_id = tpa.tpa_id)
        LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
        LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
        LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
        LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
        LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
        LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
        LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
        LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
        AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
        AND bc.sponsor_tax_amount != 0

UNION ALL

-- cash portion of tpa bills, on new inserts
SELECT hcm.center_id as center_id, hcm.center_name as center_name, b.visit_type, pr.mr_no, b.visit_id,
        cg.chargegroup_name as charge_group, cc.chargehead_name as charge_head, b.account_group,
        sg.service_group_name as service_group, ssg.service_sub_group_name as service_sub_group, b.bill_no, b.audit_control_number,
        b.bill_no as voucher_no, 'HOSPBILLS'::text as voucher_type, date(mbc.posted_date) as voucher_date,
        mbc.act_description_id::text as item_code, mbc.act_description::text as item_name, ''::text as receipt_store, ''::text as issue_store,
        ''::text as currency, 0.00 as currency_conversion_rate,
        mbc.act_quantity as quantity, ''::text as unit, 0.00 as unit_rate,
        CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN bc.tax_amt - bc.sponsor_tax_amount
        ELSE 0.00 - (bc.tax_amt - bc.sponsor_tax_amount) END AS gross_amount,
        0.00 AS round_off_amount,
        0 as discount_amount,
        0 as points_redeemed, 0.00 as points_redeemed_rate, 0 as points_redeemed_amt,
        0 as item_category_id, 0.00 as purchase_vat_amount, 0.00 as purchase_vat_percent,
        0.00 as sales_vat_amount, 0.00 as sales_vat_percent,
        CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN 'Counter Receipts' ELSE 'Tax Liability A/C'::text END as debit_account,
        CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN 'Tax Liability A/C'::text ELSE 'Counter Receipts' END as credit_account,
        0 AS tax_amount,
        CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0 THEN (bc.tax_amt - bc.sponsor_tax_amount) ELSE 0.00 - (bc.tax_amt - bc.sponsor_tax_amount) END AS net_amount,
        doc.doctor_name as admitting_doctor, pdoc.doctor_name as prescribing_doctor, cdoc.doctor_name as conductiong_doctor,
        coalesce(refdoc.doctor_name, ref.referal_name, '') as referral_doctor, cdoc.doctor_name as payee_doctor, ''::text as outhouse_name,
        ''::text as incoimng_hospital,
        d.dept_name as admitting_department, cdept.dept_name as conducting_department, 0.00 as cost_amount, ''::text as supplier_name,
        ''::text as invoice_no, NULL::timestamp as invoice_date, bc.charge_id::text as voucher_ref, b.remarks, mbc.mod_time as mod_time,
        ''::text as counter_no, b.open_date as bill_open_date, b.finalized_date as bill_finalized_date, CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END as is_tpa,
        icm.insurance_co_name as insurance_co, pd.oldmrno as old_mr_no, ''::text as issue_store_center, ''::text as receipt_store_center,
        ''::text as po_number, null::date as po_date,
        CASE WHEN (bc.tax_amt - bc.sponsor_tax_amount) >= 0.00 THEN 'N'::character ELSE 'R'::character END as transaction_type,
        (bc.tax_amt - bc.sponsor_tax_amount) as transaction_amount, ''::text as sponsor_name, ''::text as sponsor_type,
        isg.item_subgroup_name as account_name,''::text as cust_supplier_code,NULL::timestamp as grn_date,
        ''::text as cust_item_code,pdocdept.dept_name as prescribing_doctor_dept_name
FROM acc_missing_patient_tax_adjustments_view bc
        JOIN bill_charge mbc ON (mbc.charge_id=bc.charge_id)
        JOIN chargehead_constants cc ON (cc.chargehead_id=mbc.charge_head)
        JOIN chargegroup_constants cg ON (cg.chargegroup_id=mbc.charge_group)
        JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
        JOIN bill b ON (mbc.bill_no=b.bill_no)
        -- left join required on sub groups, for ex: round off's there will be no service sub group.
        LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=mbc.service_sub_group_id)
        LEFT JOIN service_groups sg ON (sg.service_group_id=ssg.service_group_id)
        LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
        LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
        LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
        LEFT JOIN department d ON (d.dept_id=pr.dept_name)
        LEFT JOIN department cdept ON (cdept.dept_id=mbc.act_department_id)
        --this has to be left join, for retail credit bills we can add round offs.
        LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
        LEFT JOIN insurance_company_master icm on (pr.primary_insurance_co = icm.insurance_co_id)
        LEFT JOIN doctors doc ON (pr.doctor = doc.doctor_id)
        LEFT JOIN doctors refdoc ON (pr.reference_docto_id = refdoc.doctor_id)
        LEFT JOIN referral ref ON (pr.reference_docto_id = ref.referal_no)
        LEFT JOIN doctors pdoc On (mbc.prescribing_dr_id = pdoc.doctor_id)
        LEFT JOIN department pdocdept ON (pdocdept.dept_id = pdoc.dept_id)
        LEFT JOIN doctors cdoc ON (mbc.payee_doctor_id = cdoc.doctor_id)
        LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = bc.tax_sub_group_id)
WHERE mbc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET')
        AND bill_type in ('P', 'C') AND restriction_type in ('N') AND b.is_tpa = true AND mbc.insurance_claim_amount != 0.00
        AND bc.tax_amt != bc.sponsor_tax_amount;

--==================== FUNCTION TO POST THE MISSING TAX VOUCHERS =====================================

CREATE OR REPLACE FUNCTION post_missing_tax_accounting_vouchers()
  RETURNS integer AS $BODY$

DECLARE
	rec RECORD;
	accountingOn integer;
BEGIN
	SELECT 1 WHERE EXISTS 
	(SELECT table_name FROM information_schema.tables 
	WHERE table_schema = current_schema() AND table_catalog = current_database() AND table_name = 'hms_accounting_info') INTO accountingOn;
	IF accountingOn IS NOT null
	THEN
		INSERT INTO hms_accounting_info (SELECT * FROM acc_all_missing_bills_tax_view) ;
	END IF;

	RETURN 1;
END;
$BODY$ LANGUAGE 'plpgsql';

--========== FUNCTIONS used in accounting data export ==========================

DROP FUNCTION IF EXISTS export_accounting_vouchers(timestamp without time zone, timestamp without time zone) CASCADE;

CREATE OR REPLACE FUNCTION export_accounting_vouchers(start_time timestamp without time zone, end_time timestamp without time zone)
	RETURNS integer
	LANGUAGE plpgsql
	AS $function$
BEGIN

	insert into hms_accounting_info
	select * from acc_all_receipts_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_deposits_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_consolidate_sponsor_receipts_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_payment_vouchers_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_payments_due_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_invoices_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_returns_with_debitnote_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_cs_issued_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hospital_bills_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hospital_bills_new_tax_view where mod_time > start_time
	AND mod_time <= end_time;

	insert into hms_accounting_info
	select * from acc_all_hospital_bills_tax_adjustment_view where mod_time > start_time
	AND mod_time <= end_time;

	insert into hms_accounting_info
	select * from acc_all_hospital_bills_tpa_tax_adjustment_view where mod_time > start_time
	AND mod_time <= end_time;

	insert into hms_accounting_info
	select * from acc_all_hospital_bills_patient_tax_adjustment_view where mod_time > start_time
	AND mod_time <= end_time;

	insert into hms_accounting_info
	select * from acc_all_incoming_sample_bills_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_pharmacy_bills_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hosp_issue_returns_bills_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hospital_pharma_bills_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hosp_bills_writeoff_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_stock_transfers_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_reagents_consumption_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_stock_transactions_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_remittance_receipts_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_user_issues_returns_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_insuarnce_adjustments_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_stores_insuarnce_adjustments_view where mod_time > start_time
	AND mod_time <= end_time;
	
	
	insert into hms_accounting_info
	select * from acc_all_stores_insuarnce_adjustments_view where mod_time > start_time
	AND mod_time <= end_time;
	
	
	insert into hms_accounting_info
	select * from acc_all_insurance_limits_adjustments_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_deposits_setoff_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_pharmacy_bills_tax_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_hospital_pharma_bills_tax_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_invoices_tax_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_cs_issued_tax_view where mod_time > start_time
	AND mod_time <= end_time;
	
	insert into hms_accounting_info
	select * from acc_all_returns_with_debitnote_tax_view where mod_time > start_time
	AND mod_time <= end_time;
	
	return 1;
	
END;
$function$;

DROP FUNCTION IF EXISTS export_accounting_vouchers() CASCADE;

-- Fucntion to export the vouchers from the views to the tables between 2 given time stamps
CREATE OR REPLACE FUNCTION export_accounting_vouchers()
	RETURNS integer
	LANGUAGE plpgsql
	AS $BODY$
DECLARE
	lastUpdated timestamp without time zone;
	start_time timestamp without time zone;
	end_time timestamp without time zone;
	timediff text;
	ret integer;
BEGIN
	lastUpdated := NULL;
	timediff := '1 day';
	start_time := NULL;
	end_time := NULL;
	ret := 0;
	
	SELECT max(mod_time) FROM hms_accounting_info
		INTO lastUpdated;
	
	SELECT date_trunc('day', current_timestamp::timestamp without time zone)
		INTO end_time;

	IF lastUpdated IS NOT NULL THEN
		SELECT date_trunc('day', lastUpdated + timediff::interval)
			INTO start_time;
	END IF;

	IF start_time IS NOT NULL THEN
		IF start_time < end_time THEN
			ret := export_accounting_vouchers(start_time, end_time);
		END IF;
	ELSE
		ret := init_accounting_vouchers(end_time);
	END IF;
	return ret;
END;
$BODY$;

DROP FUNCTION IF EXISTS init_accounting_vouchers(timestamp without time zone) CASCADE;

CREATE OR REPLACE FUNCTION init_accounting_vouchers(end_time timestamp without time zone)
	RETURNS integer
	LANGUAGE plpgsql

	AS $function$
DECLARE 
	recs integer;
	ret integer;

BEGIN
	recs := 0;
	ret := 0;
	
	SELECT count(*) FROM hms_accounting_info INTO recs;
	
	IF recs IS NULL OR recs = 0 THEN

		insert into hms_accounting_info
		select * from acc_all_receipts_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_deposits_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_consolidate_sponsor_receipts_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_payment_vouchers_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_payments_due_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_invoices_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_returns_with_debitnote_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_cs_issued_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_bills_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_bills_new_tax_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_bills_tax_adjustment_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_bills_tpa_tax_adjustment_view where mod_time <= end_time;

		insert into hms_accounting_info
		select * from acc_all_hospital_bills_patient_tax_adjustment_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_incoming_sample_bills_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_pharmacy_bills_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hosp_issue_returns_bills_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_pharma_bills_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hosp_bills_writeoff_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_stock_transfers_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_reagents_consumption_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_stock_transactions_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_remittance_receipts_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_user_issues_returns_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_insuarnce_adjustments_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_stores_insuarnce_adjustments_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_insurance_limits_adjustments_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_deposits_setoff_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_pharmacy_bills_tax_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_hospital_pharma_bills_tax_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_invoices_tax_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_cs_issued_tax_view where mod_time <= end_time;
		
		insert into hms_accounting_info
		select * from acc_all_returns_with_debitnote_tax_view where mod_time <= end_time;

		ret := 1;
	ELSE	
		RAISE NOTICE 'hms_accounting_info already has % records, skipping init', recs;
		ret := 0;
	END IF;
	
	return ret;
	
END;
$function$;

ALTER TABLE hms_accounting_info ALTER COLUMN guid SET DEFAULT generate_id('ACCOUNTING_VOUCHER'::text);