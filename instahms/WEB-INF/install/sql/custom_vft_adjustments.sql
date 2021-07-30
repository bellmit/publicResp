DROP VIEW IF EXISTS deposits_receipts_view CASCADE;
CREATE OR REPLACE VIEW deposits_receipts_view AS
SELECT case when pdepo.receipt_type = 'R' then 'DR'  when pdepo.receipt_type = 'F' then 'DF'
	else ''  end as payment_type, pdepo.receipt_id AS receipt_no, pdepo.amount as amount,0.00 AS tds_amt,
	pdepo.display_date AS display_date,
	pdepo.modified_at as mod_time, pdepo.counter as counter, pdepo.payment_mode_id,pdepo.card_type_id,pm.payment_mode,
	ctm.card_type, pdepo.bank_name as bank_name, pdepo.reference_no as reference_no,
	pdepo.created_by as username, pdepo.remarks as remarks,
	pdepo.bank_batch_no, pdepo.card_auth_code, pdepo.card_holder_name, pdepo.currency_id, pdepo.exchange_rate,
	pdepo.exchange_date, pdepo.currency_amt, pdepo.card_expdate, pdepo.card_number, fc.currency,
	case when pdepo.receipt_type = 'R' then 'DR'  when pdepo.receipt_type = 'F' then 'DF'  else ''  end AS status,
	p.mr_no as mr_no, s.salutation, p.patient_name, p.middle_name, p.last_name,
	get_patient_full_name(s.salutation, p.patient_name, p.middle_name, p.last_name) AS patient_full_name,
	COALESCE(p.dateofbirth, p.expected_dob) AS dob, p.patient_gender, counter_type,c.counter_no,
	spl_account_name as payment_mode_account,pdepo.paid_by, c.center_id, pm.ref_required, pm.bank_required,
	-- bank_name referenced as bank in tally(which is replaced with template matcher)
	pdepo.bank_name as bank, fc.conversion_rate
FROM patient_deposits_view pdepo
	LEFT JOIN counters c on pdepo.counter=counter_id
	LEFT JOIN patient_details p ON pdepo.mr_no::text = p.mr_no::text
	LEFT JOIN salutation_master s ON s.salutation_id::text = p.salutation::text
	JOIN payment_mode_master pm ON (pm.mode_id = pdepo.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = pdepo.currency_id)
	LEFT JOIN card_type_master ctm ON (ctm.card_type_id = pdepo.card_type_id) WHERE pdepo.is_deposit ;

DROP VIEW IF EXISTS aev_deposit_receipts CASCADE;
CREATE VIEW aev_deposit_receipts AS
SELECT drv.receipt_no, drv.display_date as receipt_date, drv.remarks as description,
	NULL::varchar as bill_no, drv.mr_no AS cust_id,
	drv.amount, drv.payment_mode_id, drv.payment_mode as payment_mode_name, drv.reference_no,
	drv.currency_id, drv.currency as currency_name,
	drv.counter as counter_id, drv.counter_no as counter_name, drv.center_id, hcm.center_name,
	drv.mod_time
FROM deposits_receipts_view drv
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = drv.center_id);

-- trigger on bill to track the bill when it is finalized for the first time

CREATE OR REPLACE FUNCTION bill_finalization_trigger() RETURNS trigger AS $BODY$
DECLARE
	finalized numeric;
BEGIN
	finalized := 0;
	IF (NEW.status != OLD.status AND NEW.status = 'F') THEN
		SELECT 1 into finalized WHERE EXISTS (SELECT bill_no from bills_finalized WHERE bill_no = NEW.bill_no);
		-- raise notice '%', finalized;
		IF (finalized IS NULL OR finalized = 0) THEN
			INSERT INTO bills_finalized (bill_no) VALUES (NEW.bill_no);
		END IF;
	END IF;
RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_finalization_trigger ON bill CASCADE;
CREATE TRIGGER bill_finalization_trigger
	AFTER UPDATE ON bill
	FOR EACH ROW EXECUTE PROCEDURE bill_finalization_trigger();

-- trigger on bill charge to post an adjustment entry in bill_adjustment and bill_charge_adjustment when
-- bill amounts are edited after finalization

CREATE OR REPLACE FUNCTION y_bill_charge_adjustment_trigger() RETURNS trigger AS $BODY$
DECLARE
	finalized numeric;
	patient_id varchar;
	tpa boolean;
	seq_no integer;
BEGIN
	finalized := 0;
	SELECT 1 into finalized WHERE EXISTS (SELECT bill_no from bills_finalized WHERE bill_no = NEW.bill_no);

	IF (finalized = 1 AND NEW.status != 'X' AND (NEW.amount != OLD.amount OR NEW.insurance_claim_amount != OLD.insurance_claim_amount
		OR NEW.return_insurance_claim_amt != OLD.return_insurance_claim_amt)) THEN

		SELECT visit_id from bill where bill_no = NEW.bill_no INTO patient_id;
		SELECT is_tpa from bill where bill_no = NEW.bill_no INTO tpa;
		SELECT nextval('bill_adjustment_seq') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_no, charge_id, amount, insurance_claim_amount, return_insurance_claim_amt,
			act_description, ref_bill_no, ref_charge_id, posted_date, status, charge_head, act_description_id,
			act_department_id, prescribing_dr_id, charge_group)
		VALUES ('AD' || NEW.bill_no || '-' || seq_no, 'AD' || NEW.charge_id || '-' || seq_no, NEW.amount - OLD.amount, NEW.insurance_claim_amount - OLD.insurance_claim_amount,
			NEW.return_insurance_claim_amt - OLD.return_insurance_claim_amt,
			NEW.act_description, NEW.bill_no, NEW.charge_id, current_timestamp, 'A', NEW.charge_head, NEW.act_description_id,
			NEW.act_department_id, NEW.prescribing_dr_id, NEW.charge_group);

		INSERT INTO bill_adjustment (bill_no, visit_id, total_amount, total_claim, total_claim_return, finalized_date, status, ref_bill_no, is_tpa)
			VALUES ('AD' || NEW.bill_no || '-' || seq_no, patient_id, NEW.amount - OLD.amount, NEW.insurance_claim_amount - OLD.insurance_claim_amount,
			NEW.return_insurance_claim_amt - OLD.return_insurance_claim_amt, current_timestamp, 'F', NEW.bill_no, tpa);
	END IF;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS y_bill_charge_adjustment_trigger ON bill_charge CASCADE;
CREATE TRIGGER y_bill_charge_adjustment_trigger
	AFTER UPDATE ON bill_charge
	FOR EACH ROW EXECUTE PROCEDURE y_bill_charge_adjustment_trigger();

-- convenience view for bills with adjustment amounts included

DROP VIEW IF EXISTS aev_bill_adjustment cascade;
CREATE OR REPLACE VIEW aev_bill_adjustment AS
SELECT b.bill_no, b.bill_label_id, b.visit_id, b.is_tpa, b.remarks, b.finalized_date, b.total_amount, b.total_claim, b.last_receipt_no, b.status,
	sum(coalesce(ba.total_amount, 0)) as amount_adjustment,
	sum(coalesce(ba.total_claim, 0)) as claim_adjustment,
	sum(coalesce(ba.total_claim_return, 0)) as claim_return_adjustment
FROM bill b LEFT JOIN bill_adjustment ba ON (b.bill_no = ba.ref_bill_no)
GROUP BY b.bill_no, b.bill_label_id, b.visit_id, b.is_tpa, b.remarks, b.finalized_date, b.total_amount, b.total_claim, b.last_receipt_no, b.status;

-- convenience view for bill_charge with adjustment amounts included

DROP VIEW IF EXISTS aev_bill_charge_adjustment cascade;
CREATE OR REPLACE VIEW aev_bill_charge_adjustment AS
SELECT bc.bill_no, bc.charge_id, bc.act_description, bc.charge_head, bc.amount, bc.act_department_id,
	bc.payee_doctor_id, bc.service_sub_group_id, bc.act_description_id, bc.status,
	sum(coalesce(bca.amount, 0)) as amount_adjustment,
	sum(coalesce(bca.insurance_claim_amount, 0)) as claim_adjustment,
	sum(coalesce(bca.return_insurance_claim_amt, 0)) as claim_return_adjustment, bc.prescribing_dr_id,
	bc.insurance_claim_amount
FROM bill_charge bc LEFT JOIN bill_charge_adjustment bca ON (bc.bill_no = bca.ref_bill_no AND bc.charge_id = bca.ref_charge_id)
GROUP BY bc.bill_no, bc.charge_id, bc.act_description, bc.charge_head, bc.amount, bc.act_department_id, bc.payee_doctor_id,
	bc.service_sub_group_id, bc.act_description_id, bc.status, bc.prescribing_dr_id,bc.insurance_claim_amount;

-- adjustments end ---

DROP VIEW IF EXISTS aev_customer_master CASCADE;
CREATE VIEW aev_customer_master AS
select tpa_id as customer_number, tpa_name as customer_name,
	tm.status, tpa_id as customer_reference, sponsor_type as customer_type,
	coalesce(address, '-NA-')::character varying as address1,
	null::character varying as address2, null::character varying as address3,
	null::character varying as address4, city, state, pincode as postal_code,
	country, null::character varying as contact_type, null::character varying as phone_area_code, phone_no as phone_number,
	null::character varying as phone_extension, email_id as email_address, contact_name as contact_person_first_name,
	null::character varying as contact_person_last_name, contact_designation as contact_person_job_title,
	0 as due_days, tcm.center_id::character varying as org_id,
	null::character varying as process_flag, coalesce(updated_timestamp, created_timestamp) as mod_time
from tpa_master tm
	join tpa_center_master tcm using (tpa_id);

DROP VIEW IF EXISTS aev_plan_master CASCADE;
CREATE VIEW aev_plan_master AS
SELECT ipm.plan_id, ipm.plan_name, ipm.status, mod_time,
	(select center_id from hospital_center_master where center_name='DIP (DXB)')::character varying as org_id
 FROM insurance_plan_main ipm ;


DROP VIEW IF EXISTS aev_bill_header CASCADE;
CREATE VIEW aev_bill_header AS
-- All bills with unadjusted amounts
SELECT b.bill_no as trx_number, date(b.finalized_date) as trx_date,
	'INV'::character varying as trx_type,
	'AED'::character varying as currency_code, coalesce(conversion_rate, 1) as conversion_rate,
	(b.total_amount - coalesce(b.amount_adjustment, 0)) as amount, 0 as cust_trx_type_name, 0 as due_days,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END as customer_number, b.remarks as comments,
	null::character varying as reference, COALESCE(pr.center_id, prc.center_id)::character varying as org_id,
	insurance_co_name as insurance_co, ipm.plan_name, b.finalized_date as mod_time,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
		|| case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end,
		prc.customer_name, isr.patient_name)::character varying AS patient_name,
		(b.total_claim - coalesce(b.claim_adjustment, 0)) as sponsor_amt
FROM aev_bill_adjustment b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pr.patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details pnd ON
		(pnd.patient_national_sponsor_id = pr.patient_national_sponsor_id)
	LEFT JOIN patient_policy_details spd ON (spd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details scd ON (scd.patient_corporate_id = pr.secondary_patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details snd ON
		(snd.patient_national_sponsor_id = pr.secondary_patient_national_sponsor_id)
	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
	LEFT JOIN insurance_company_master icm ON (ipm.insurance_co_id=icm.insurance_co_id)
	LEFT JOIN bill_label_master lbl ON (lbl.bill_label_id = b.bill_label_id)
	-- these two joins are used to get the currency details for last receipt of the bill.
	LEFT JOIN bill_receipts r ON (r.receipt_no=b.last_receipt_no)
	JOIN receipts rpt ON (r.receipt_no=rpt.receipt_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
WHERE b.status NOT IN ('A','X') AND b.is_tpa=true
-- add all adjustments
UNION ALL

SELECT b.bill_no as trx_number, date(b.finalized_date) as trx_date,
	CASE WHEN b.total_amount < 0 THEN 'CM'::character varying ELSE 'DM'::character varying END as trx_type,
	'AED'::character varying as currency_code, coalesce(conversion_rate, 1) as conversion_rate,  b.total_amount as amount,
	0 as cust_trx_type_name, 0 as due_days,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END as customer_number, b.remarks as comments,
	ref_bill_no as reference, COALESCE(pr.center_id, prc.center_id)::character varying as org_id,
	insurance_co_name as insurance_co, ipm.plan_name, b.finalized_date as mod_time,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| case when coalesce(pd.middle_name, '') = '' then '' else (' ' || pd.middle_name) end
		|| case when coalesce(pd.last_name, '') = '' then '' else (' ' || pd.last_name) end,
		prc.customer_name, isr.patient_name)::character varying AS patient_name,
		b.total_claim as sponsor_amt
FROM bill_adjustment b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pr.patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details pnd ON
		(pnd.patient_national_sponsor_id = pr.patient_national_sponsor_id)
	LEFT JOIN patient_policy_details spd ON (spd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details scd ON (scd.patient_corporate_id = pr.secondary_patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details snd ON
		(snd.patient_national_sponsor_id = pr.secondary_patient_national_sponsor_id)
	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
	LEFT JOIN insurance_company_master icm ON (ipm.insurance_co_id=icm.insurance_co_id)
	-- these two joins are used to get the currency details for last receipt of the bill.
	LEFT JOIN bill_receipts r ON (r.receipt_no=b.last_receipt_no)
    JOIN receipts rpt ON (r.receipt_no=rpt.receipt_id)
    LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
WHERE b.status NOT IN ('A','X') AND b.is_tpa=true;


DROP VIEW IF EXISTS aev_bill_trx_lines CASCADE;
CREATE VIEW aev_bill_trx_lines AS
-- hospital items (lab, rad, issues, returns etc., )
SELECT b.bill_no as invoice_number, bc.charge_id as line_number,
	case when coalesce(act_description, '')='' then chc.chargehead_name else act_description end as service_name,
	bc.insurance_claim_amount - bc.claim_adjustment as amount, tdep.dept_name AS department, doc.doctor_name,
	COALESCE(pr.center_id, prc.center_id)::character varying as org_id,
	b.finalized_date as mod_time
FROM aev_bill_charge_adjustment bc
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN doctors doc ON (doc.doctor_id = bc.payee_doctor_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)

	LEFT JOIN bill_activity_charge ibac ON (ibac.activity_code = 'PHI' AND ibac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVITE')
	LEFT JOIN stock_issue_details id ON (ibac.activity_id::integer = id.item_issue_no)
	LEFT JOIN stock_issue_main im ON (id.user_issue_no = im.user_issue_no)

	LEFT JOIN bill_activity_charge rbac ON (rbac.activity_code = 'PHI' AND rbac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVRET')
	LEFT JOIN store_issue_returns_details rd ON (rbac.activity_id::integer = rd.item_return_no)
	LEFT JOIN store_issue_returns_main rm ON (rd.user_return_no = rm.user_return_no)

	LEFT JOIN store_item_details sid ON (sid.medicine_id::text = bc.act_description_id
		AND bc.charge_head IN ('INVITE','INVRET'))
	LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)
WHERE bc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET')
	AND b.status NOT IN ('A','X') AND bc.status != 'X' AND b.is_tpa=true

UNION ALL
SELECT bc.bill_no as invoice_number, bc.charge_id as line_number,
	case when coalesce(bc.act_description, '')='' then chc.chargehead_name else bc.act_description end as service_name,
	bc.insurance_claim_amount as amount, tdep.dept_name AS department, doc.doctor_name,
	COALESCE(pr.center_id, prc.center_id)::character varying as org_id,
	b.finalized_date as mod_time
FROM bill_charge_adjustment bc
	JOIN bill b ON (b.bill_no = bc.ref_bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN doctors doc ON (doc.doctor_id = bc.payee_doctor_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)

	LEFT JOIN bill_activity_charge ibac ON (ibac.activity_code = 'PHI' AND ibac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVITE')
	LEFT JOIN stock_issue_details id ON (ibac.activity_id::integer = id.item_issue_no)
	LEFT JOIN stock_issue_main im ON (id.user_issue_no = im.user_issue_no)

	LEFT JOIN bill_activity_charge rbac ON (rbac.activity_code = 'PHI' AND rbac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVRET')
	LEFT JOIN store_issue_returns_details rd ON (rbac.activity_id::integer = rd.item_return_no)
	LEFT JOIN store_issue_returns_main rm ON (rd.user_return_no = rm.user_return_no)

	LEFT JOIN store_item_details sid ON (sid.medicine_id::text = bc.act_description_id
		AND bc.charge_head IN ('INVITE','INVRET'))
	LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)
WHERE bc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET')
	AND b.status NOT IN ('A','X') AND bc.status != 'X' AND b.is_tpa=true

UNION ALL
-- pharmacy items
SELECT sm.bill_no, 'PI' || s.sale_item_id, m.medicine_name as service_name, s.insurance_claim_amt as amount,
	'PHARMACY' as department, NULL AS doctor_name, st.center_id::character varying as org_id,
	b.finalized_date as mod_time

FROM store_sales_details s
	JOIN store_sales_main sm ON (s.sale_id = sm.sale_id)
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN store_item_details m ON (s.medicine_id = m.medicine_id)
	JOIN store_category_master scm ON (m.med_category_id = scm.category_id)
	JOIN bill_charge bc ON (bc.charge_id= sm.charge_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = m.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE b.is_tpa=true

/*
UNION ALL
-- pharmacy discounts

-- discounts and roundoff's will not apply on sponsor amounts so commenting them.

SELECT sm.bill_no, 'PD' || sm.sale_id, 'Pharmacy Bill Level Discounts' as service_name,
	(0-sm.discount) as amount, 'PHARMACY' as department, NULL AS doctor_name,
	st.center_id::character varying as org_id, b.finalized_date as mod_time

FROM store_sales_main sm
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = 'BIDIS')
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = 'DIS')
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = 0)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE sm.discount != 0 AND b.is_tpa=true

UNION ALL
-- pharmacy round offs
SELECT sm.bill_no, 'RO' || sm.sale_id, 'Pharmacy Round Offs' as service_name, sm.round_off as amount,
	'PHARMACY' as department, NULL as doctor_name, st.center_id::character varying as org_id,
	b.finalized_date as mod_time

FROM store_sales_main sm
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = 'ROF')
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = 'DIS')
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = 0)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE sm.round_off != 0 AND b.is_tpa=true
*/
;


DROP VIEW IF EXISTS aev_ar_receipts CASCADE;
CREATE VIEW aev_ar_receipts AS
SELECT r.receipt_no, r.display_date as receipt_date, 'AED'::character varying as currency_code, r.amount,
	coalesce(conversion_rate, 1) as conversion_rate, (case when b.is_tpa and (b.total_amount - b.primary_total_claim - b.secondary_total_claim) > 0 and
		(b.primary_total_claim + b.secondary_total_claim) > 0 then 'COPAY' else 'CASH' end)::character varying cash_copay_flag,
	(CASE WHEN rpt.receipt_type='R' AND rpt.is_settlement AND rpt.tpa_id IS null THEN 'Normal' 
		WHEN rpt.receipt_type='R' AND NOT rpt.is_settlement AND rpt.tpa_id IS null THEN 'Advance' 
		WHEN rpt.receipt_type='F' THEN 'Advance Refund' END)::character varying AS receipt_type, pmm.payment_mode as receipt_method_name,
	null::character varying as bank_account,
	c.center_id::character varying as org_id, r.display_date as mod_time

FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no)
	JOIN bill b ON (b.bill_no = r.bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN counters c ON (c.counter_id = rpt.counter)
	LEFT JOIN payment_mode_master pmm ON (pmm.mode_id = rpt.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)

UNION ALL
SELECT rr.receipt_no, rr.display_date as receipt_date, 'AED'::character varying as currency_code, rr.amount,
	1 as conversion_rate, (case when b.is_tpa and (b.total_amount - b.primary_total_claim - b.secondary_total_claim) > 0 and
		(b.primary_total_claim + b.secondary_total_claim) > 0 then 'COPAY' else 'CASH' end)::character varying cash_copay_flag,
	(case when recpt_type='S' or recpt_type = 'R' then 'Normal' when recpt_type='A' then 'Advance' when recpt_type='F'
		then 'Advance Refund' end)::character varying as receipt_type, pmm.payment_mode as receipt_method_name,
	null::character varying as bank_account,
	coalesce(pr.center_id, 0)::character varying as org_id, rr.display_date as mod_time

FROM remittance_receipts rr
	JOIN bill b ON (b.bill_no = rr.bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN counters c ON (c.counter_id = rr.counter)
	LEFT JOIN payment_mode_master pmm ON (pmm.mode_id = rr.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rr.currency_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)
UNION ALL

SELECT drv.receipt_no, drv.display_date as receipt_date, 'AED'::character varying as currency_code, drv.amount,
	coalesce(conversion_rate, 1) as conversion_rate, 'CASH'::character varying as cash_copay_flag,
	(case when payment_type='DR' then 'Deposit Receipt' when payment_type='DF' then 'Deposit Refund' end)::character varying as receipt_type,
	drv.payment_mode as receipt_method_name,
	null::character varying as bank_account,
	hcm.center_id::character varying as org_id, drv.display_date as mod_time
FROM deposits_receipts_view drv
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = drv.center_id);
;


DROP VIEW IF EXISTS aev_ar_receipts_summary CASCADE;
CREATE VIEW aev_ar_receipts_summary AS
SELECT c.center_id::character varying as org_id, date(r.display_date) as display_date,
	(case when b.is_tpa and (b.total_amount - b.primary_total_claim - b.secondary_total_claim) > 0 and
		(b.primary_total_claim + b.secondary_total_claim) > 0 then 'COPAY' else 'CASH' end)::character varying as cash_copay_flag,
	(CASE WHEN rpt.receipt_type='R' AND rpt.is_settlement AND rpt.tpa_id IS null THEN 'Normal' 
		WHEN rpt.receipt_type='R' AND NOT rpt.is_settlement AND rpt.tpa_id IS null THEN 'Advance' 
		WHEN rpt.receipt_type='F' THEN 'Advance Refund' END)::character varying as receipt_type, pmm.payment_mode as receipt_method_name,
	sum(r.amount) as amount, 'AED'::character varying as currency_code, coalesce(conversion_rate, 1) as conversion_rate

FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no)
	JOIN bill b ON (b.bill_no = r.bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN counters c ON (c.counter_id = rpt.counter)
	LEFT JOIN payment_mode_master pmm ON (pmm.mode_id = rpt.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)
WHERE date(r.display_date) <= current_date-1
group by 1, 2, 3, 4, 5, 8

UNION ALL

SELECT hcm.center_id::character varying as org_id,
	date(drv.display_date) as display_date,
	'CASH'::character varying as cash_copay_flag,
	(case when payment_type='DR' then 'Deposit Receipt' when payment_type='DF' then 'Deposit Refund' end)::character varying as receipt_type,
	drv.payment_mode as receipt_method_name,
	sum(drv.amount) as amount, 'AED'::character varying as currency_code, coalesce(conversion_rate, 1) as conversion_rate

FROM deposits_receipts_view drv
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = drv.center_id)
WHERE date(drv.display_date) <= current_date-1
group by 1, 2, 3, 4, 5, 8
;


DROP VIEW IF EXISTS aev_sponsor_writeoffs CASCADE;
CREATE VIEW aev_sponsor_writeoffs AS
SELECT ('ADJ-'||b.bill_no)::character varying as adjustment_number, date(b.finalized_date) as adjustment_date,
	(total_claim - (b.claim_recd_amount +
		b.primary_total_sponsor_receipts + b.secondary_total_sponsor_receipts)) as amount,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END as customer_number,
	b.bill_no as trx_reference, null::character varying as trx_line_number,
	coalesce(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id, 'AED'::character varying as currency_code,
	coalesce(conversion_rate, 1) as conversion_rate,
	null::character varying as adjustment_reason, b.closed_date as mod_time

FROM bill b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)

	-- these two joins are used to get the currency details for last receipt of the bill.
	LEFT JOIN bill_receipts r ON (r.receipt_no=b.last_receipt_no)
	JOIN receipts rpt ON (rpt.receipt_id=r.receipt_no)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
WHERE b.status='C' AND b.is_tpa=true AND (total_claim - (b.deposit_set_off + b.total_receipts + b.claim_recd_amount +
	b.primary_total_sponsor_receipts + b.secondary_total_sponsor_receipts)) != 0
;


DROP VIEW IF EXISTS aev_patient_writeoffs CASCADE;
CREATE VIEW aev_patient_writeoffs AS
-- patient writeoff for non-insurance bills.
SELECT coalesce(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id,
	'Patient Write-Off'::character varying as transactio_type,
	b.bill_no as bill_number, b.remarks as bill_remarks,
	date(b.finalized_date) as bill_finalisatio_date,
	'AED'::character varying as currency_code,
	null::character varying as line_number,
	null::character varying as line_description,
	(total_amount - (b.deposit_set_off + b.total_receipts)) as amount,
	coalesce(conversion_rate, 1) as conversion_rate,
	(CASE WHEN b.is_tpa THEN 'Credit' ELSE 'Cash' END)::character varying as cash_credit,
	pr.dept_name as department, b.closed_date as mod_time

FROM bill b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)

	-- these two joins are used to get the currency details for last receipt of the bill.
	LEFT JOIN bill_receipts r ON (r.receipt_no=b.last_receipt_no)
	JOIN receipts rpt ON (r.receipt_no=rpt.receipt_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
WHERE b.status='C' AND b.is_tpa=false AND (total_amount - (b.deposit_set_off + b.total_receipts)) != 0

UNION ALL
-- patient writeoff for insurnace bills
SELECT coalesce(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id,
	'Patient Write-Off'::character varying as transactio_type,
	b.bill_no as bill_number, b.remarks as bill_remarks,
	date(b.finalized_date) as bill_finalisatio_date,
	'AED'::character varying as currency_code,
	null::character varying as line_number,
	null::character varying as line_description,
	(total_amount - total_claim - (b.deposit_set_off + b.total_receipts)) as amount,
	coalesce(conversion_rate, 1) as conversion_rate,
	(CASE WHEN b.is_tpa THEN 'Credit' ELSE 'Cash' END)::character varying as cash_credit,
	pr.dept_name as department, b.closed_date as mod_time

FROM bill b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)

	-- these two joins are used to get the currency details for last receipt of the bill.
	LEFT JOIN bill_receipts r ON (r.receipt_no=b.last_receipt_no)
	JOIN receipts rpt ON (r.receipt_no=rpt.receipt_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
WHERE b.status='C' AND b.is_tpa=true AND (total_amount - total_claim - (b.deposit_set_off + b.total_receipts)) != 0
;

-- trigger on bill to post a receipt automatically when a remittance is received
DROP VIEW IF EXISTS aev_patient_dues CASCADE;
CREATE VIEW aev_patient_dues AS
SELECT COALESCE(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id,
	'Patient Due'::character varying as transactio_type, b.bill_no as bill_number,
	b.remarks as bill_remarks, date(current_date-extract(day from current_date)::integer) as bill_finalizatio_date,
	'AED'::character varying as currency_code, 0 as line_number, null::character varying as line_description,
	foo.amount, coalesce(foo.conversion_rate, 1) as conversion_rate,
	(CASE WHEN b.is_tpa THEN 'Credit' ELSE 'Cash' END)::character varying as cash_credit,
	date(current_date-extract(day from current_date)::integer) as mod_time
FROM bill b
	JOIN (SELECT b.bill_no, fc.conversion_rate, (b.total_amount - b.deposit_set_off - b.primary_total_claim - b.secondary_total_claim) - sum(coalesce(r.amount,0)) as amount
			FROM bill b
				LEFT JOIN bill_receipts r using (bill_no)
	            JOIN receipts rpt ON (r.receipt_no=rpt.receipt_id)
				LEFT JOIN foreign_currency fc ON (fc.currency_id = rpt.currency_id)
			WHERE b.status NOT IN ('A', 'X') AND date(b.finalized_date) <= date(current_date-extract(day from current_date)::integer)
				AND case when r.display_date is null then true else date(r.display_date)  <= date(current_date-extract(day from current_date)::integer) end
				AND coalesce(rpt.receipt_type, 'R')='R'
			GROUP BY b.bill_no, b.total_amount, b.primary_total_claim, b.secondary_total_claim, b.deposit_set_off, fc.conversion_rate) as foo using (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
WHERE amount > 0;

DROP VIEW IF EXISTS aev_receipt_application CASCADE;
CREATE VIEW aev_receipt_application AS
SELECT r.receipt_no as receipt_number, r.display_date as application_date, b.bill_no as trx_reference,
	r.amount as application_amount,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END as customer_number,
	COALESCE(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id,
	r.modified_at as mod_time
FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no)
	JOIN bill b using (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
WHERE rpt.tpa_id IS NOT NULL and b.status IN ('C', 'F')
UNION ALL
SELECT rr.receipt_no as receipt_number, rr.display_date as application_date, b.bill_no as trx_reference,
	rr.amount as application_amount,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END as customer_number,
	COALESCE(pr.center_id, prc.center_id, isr.center_id)::character varying as org_id,
	rr.display_date as mod_time
FROM remittance_receipts rr
	JOIN bill b using (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
WHERE payment_type='S' and b.status IN ('C', 'F');

-- INSERT INTO revenue_allocation_map (charge_head, allocation_percent) (SELECT distinct chargehead_id, 1 from chargehead_constants);

DROP VIEW IF EXISTS aev_special_allocation ;
CREATE OR REPLACE VIEW aev_special_allocation AS
select foo.status, foo.act_department_id, foo.act_description, foo.act_description_id, foo.prescribing_dr_id, foo.bill_no, foo.charge_id, foo.charge_head, foo.amount, foo.alloc_priority, foo.allocation_group, foo.priority_allocation,
foo.amount * ((foo.bill_amount - (sum(foo.priority_allocation) OVER (PARTITION BY foo.bill_no ORDER BY foo.alloc_priority)))
/ (foo.bill_amount - foo.margin - (sum(foo.consumed_charge) OVER (PARTITION BY foo.bill_no ORDER BY foo.alloc_priority)))) as adj_allocation,
foo.bill_amount from
(
select coalesce(ab.priority, 100) as alloc_priority, bch.*, sb.charge_head as allocation_group,
coalesce(ab.allocation_percent, 0) * (bch.amount - bch.amount_adjustment) as priority_allocation,
CASE WHEN (coalesce(ab.allocation_percent, 0) != 0) THEN bch.amount - bch.amount_adjustment ELSE 0 END as consumed_charge,
sb.bill_amount, sb.margin from aev_bill_charge_adjustment bch
JOIN
	(select distinct b.bill_no, bc.charge_head, bc.amount - bc.amount_adjustment as margin, b.total_amount - b.amount_adjustment as bill_amount
	from aev_bill_adjustment b JOIN aev_bill_charge_adjustment bc ON (b.bill_no = bc.bill_no)
	JOIN revenue_allocation_block rab ON (bc.charge_head = rab.indicator_charge_head)) as sb
ON (bch.bill_no = sb.bill_no and bch.charge_head != sb.charge_head)
LEFT JOIN revenue_allocation_block ab ON (bch.charge_head = ab.charge_head AND sb.charge_head = ab.indicator_charge_head)
ORDER BY alloc_priority
) as foo order by bill_no;

DROP VIEW IF EXISTS aev_special_adjustment_allocation;
CREATE OR REPLACE VIEW aev_special_adjustment_allocation AS
select foo.status, foo.act_department_id, foo.act_description, foo.act_description_id, foo.prescribing_dr_id, foo.adj_bill_no as bill_no, foo.charge_id,
foo.charge_head, foo.alloc_priority, foo.allocation_group, foo.priority_allocation, foo.bill_no as ref_bill_no,
foo.amount - foo.amount_adjustment as amount, sum(foo.amount - foo.amount_adjustment) OVER (PARTITION BY foo.bill_no) as alloc_total,
(foo.amount - foo.amount_adjustment) / (sum(foo.amount - foo.amount_adjustment) OVER (PARTITION BY foo.bill_no)) * foo.bill_amount as adj_allocation,
foo.bill_amount from
(
select 100 as alloc_priority, bch.*, sb.charge_head as allocation_group, 0 as priority_allocation, 0 as consumed_charge, sb.bill_amount,
sb.margin, sb.adj_bill_no from aev_bill_charge_adjustment bch
JOIN
	(select distinct b.ref_bill_no as bill_no, b.bill_no as adj_bill_no, bc.charge_head, bc.amount as margin, b.total_amount as bill_amount
	from bill_adjustment b JOIN bill_charge_adjustment bc ON (b.bill_no = bc.bill_no)
	JOIN revenue_allocation_block rab ON (bc.charge_head = rab.indicator_charge_head)) as sb
ON (bch.bill_no = sb.bill_no and bch.charge_head != sb.charge_head)
WHERE bch.charge_head not in (select charge_head from revenue_allocation_block ab where sb.charge_head = ab.indicator_charge_head)
ORDER BY alloc_priority
) as foo order by bill_no;

DROP VIEW IF EXISTS aev_revenue_allocation;

CREATE VIEW aev_revenue_allocation AS
SELECT pr.center_id as org_id, 'Revenue'::character varying as transaction_type, bc.bill_no, b.remarks,
	b.finalized_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	round(((bc.amount -bc.amount_adjustment) * (coalesce(rami.allocation_percent, ramc.allocation_percent))), 2) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM aev_bill_charge_adjustment bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head and ramc.item_id IS null)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')
WHERE b.status NOT IN ('A','X') and bc.status != 'X' AND pr.use_drg = 'N' and pr.use_perdiem = 'N' and b.dyna_package_id = 0
UNION ALL
SELECT pr.center_id as org_id, 'Revenue'::character varying as transaction_type, bc.bill_no, b.remarks,
	bc.posted_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	round(((bc.amount) * (coalesce(rami.allocation_percent, ramc.allocation_percent))), 2) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM bill_charge_adjustment bc
	JOIN bill b ON (bc.ref_bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head and ramc.item_id IS null)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')
WHERE b.status NOT IN ('A','X') and bc.status != 'X' AND pr.use_drg = 'N' and pr.use_perdiem = 'N' and b.dyna_package_id = 0
UNION ALL
SELECT pr.center_id as org_id, 'Revenue'::character varying as transaction_type, bc.bill_no, b.remarks,
	b.finalized_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	round((CASE WHEN bc.priority_allocation != 0 THEN priority_allocation ELSE bc.adj_allocation END), 2) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM aev_special_allocation bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head AND ramc.item_id IS NULL)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')
WHERE b.status NOT IN ('A','X') and bc.status != 'X'
UNION ALL
SELECT pr.center_id as org_id, 'Revenue'::character varying as transaction_type, bc.bill_no, b.remarks,
	b.finalized_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	round((CASE WHEN bc.priority_allocation != 0 THEN priority_allocation ELSE bc.adj_allocation END), 2) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM aev_special_adjustment_allocation bc
	JOIN bill b ON (bc.ref_bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head AND ramc.item_id IS NULL)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')
WHERE b.status NOT IN ('A','X') and bc.status != 'X';


DROP VIEW IF EXISTS aev_unfinalized_bill;
CREATE VIEW aev_unfinalized_bill AS
SELECT pr.center_id as org_id, 'UNFINALIZED'::character varying as transaction_type, bc.bill_no, b.remarks,
	date(current_date-extract(day from current_date)::integer) as bill_finalization_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	amount * (coalesce(rami.allocation_percent, ramc.allocation_percent)) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM bill_charge bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')

WHERE b.status NOT IN ('A','X') AND bc.status != 'X' AND b.open_date < date(current_date-extract(day from current_date)::integer)
	AND date(b.finalized_date) between date(current_date-extract(day from current_date)::integer) and current_date
	AND bc.posted_date < date(current_date-extract(day from current_date)::integer)

UNION ALL

SELECT pr.center_id as org_id, 'UNFINALIZED'::character varying as transaction_type, bc.bill_no, b.remarks,
	date(current_date-extract(day from current_date)::integer) as bill_finalization_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
	amount * (coalesce(rami.allocation_percent, ramc.allocation_percent)) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	coalesce(dit.dept_name, dch.dept_name, dpdoc.dept_name, dadoc.dept_name, dac.dept_name, rami.allocation_department, ramc.allocation_department, '-NA-') as department
FROM bill_charge bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN revenue_allocation_map ramc ON (bc.charge_head = ramc.charge_head)
	LEFT JOIN department dch ON (dch.dept_id = ramc.allocation_department)
	LEFT JOIN revenue_allocation_map rami ON (bc.act_description_id = rami.item_id)
	LEFT JOIN department dit ON (dit.dept_id = rami.allocation_department)
	LEFT JOIN department dac ON (dac.dept_id = bc.act_department_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'CDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dpdoc
		ON (bc.prescribing_dr_id = dpdoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) ='PDEPT')
	LEFT JOIN
		(SELECT doc.doctor_id, doc.doctor_name, doc.dept_id, dep.dept_name FROM doctors doc JOIN department dep ON (doc.dept_id = dep.dept_id)) as dadoc
		ON (pr.doctor = dadoc.doctor_id AND coalesce(rami.allocation_department, ramc.allocation_department) = 'ADEPT')
WHERE b.status='A' AND bc.status!='X' AND date(b.open_date) < date(current_date-extract(day from current_date)::integer)
	AND bc.posted_date < date(current_date-extract(day from current_date)::integer);

DROP VIEW IF EXISTS aev_deposit_application CASCADE;
CREATE VIEW aev_deposit_application AS
SELECT c.center_id::character varying as org_id, 'Advance'::character varying as transaction_type, b.bill_no, b.finalized_date,
	'AED'::character varying as currency_code, null::character varying as line_number, null::character varying as line_description,
	least(sum(r.amount), b.total_amount) as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	null::character varying as department, b.finalized_date as mod_time
FROM receipts rpt JOIN bill_receipts r ON (rpt.receipt_id = r.receipt_no)
	JOIN bill b ON (b.bill_no = r.bill_no)
	LEFT JOIN counters c ON (c.counter_id = rpt.counter)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id)
WHERE (rpt.receipt_type = 'R' AND NOT rpt.is_settlement AND rpt.tpa_id IS NOT NULL) AND b.status NOT IN ('A','X')
GROUP BY c.center_id, b.bill_no, b.finalized_date, b.is_tpa, b.total_amount

UNION ALL

SELECT hcm.center_id::character varying as org_id, 'Deposit'::character varying as transaction_type, b.bill_no, b.finalized_date,
	'AED'::character varying as currency_code, null::character varying as line_number, null::character varying as line_description,
	b.deposit_set_off as amount, 1 as conversion_rate,
	CASE WHEN b.is_tpa THEN 'Credit'::character varying ELSE 'Cash'::character varying END as cash_credit,
	null::character varying as department, b.finalized_date as mod_time
FROM bill b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.deposit_set_off > 0 and b.status NOT IN ('A','X');


CREATE OR REPLACE FUNCTION auto_post_sponsor_receipts() RETURNS trigger AS $BODY$
DECLARE
	amount_diff numeric;
	auto_post character(1);
BEGIN
	amount_diff := 0;

-- check if the generic preferences is set
-- check if the claim_recd_amount changed
-- if there is a positive change post a receipt
	IF OLD.claim_recd_amount < NEW.claim_recd_amount THEN
		SELECT auto_post_sponsor_receipts INTO auto_post FROM generic_preferences LIMIT 1;
		IF (auto_post = 'Y') THEN

			amount_diff := NEW.claim_recd_amount - OLD.claim_recd_amount;

			INSERT INTO remittance_receipts(  receipt_no, bill_no, recpt_type, amount,
				display_date, counter, payment_mode_id , card_type_id,
				bank_name, reference_no, username, remarks,
				payment_type,tds_amt,paid_by, bank_batch_no,
				card_auth_code, card_holder_name, currency_id, exchange_rate,
				exchange_date,  currency_amt, card_number, card_expdate,
				sponsor_index)
			VALUES (
				generate_id('SPONSOR_RECEIPT_DEFAULT'), NEW.bill_no, 'S', amount_diff,
				current_timestamp, NULL, -1, 0,
				NULL, NULL, NEW.username, 'auto posted',
				'S', 0, '', NULL,
				NULL, NULL, 0, NULL,
				NULL, NULL, NULL, NULL, 'P');
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS auto_post_sponsor_receipts ON bill;

CREATE TRIGGER auto_post_sponsor_receipts
	AFTER UPDATE ON bill
	FOR EACH ROW EXECUTE PROCEDURE auto_post_sponsor_receipts();

DROP TRIGGER IF EXISTS auto_post_credit_memo_trigger ON insurance_claim CASCADE;
DROP FUNCTION IF EXISTS auto_post_credit_memo_trigger() CASCADE;

-- This is in addition to the auto_post_credit_memo_trigger to post credit memo and debit memo on
-- insurance claim amount edit (claim correction and resubmission)

DROP TRIGGER IF EXISTS auto_post_adjustment_trigger ON bill_charge CASCADE;
DROP FUNCTION IF EXISTS auto_post_adjustment_trigger() CASCADE;

DROP VIEW IF EXISTS aev_perdiem_amounts_view;
CREATE VIEW aev_perdiem_amounts_view AS
SELECT bc.bill_no, bc.charge_id,
	round((sum(coalesce(insurance_claim_amount, 0.00)) OVER (PARTITION BY bc.bill_no) / sum(coalesce(bc.amount, 0.00)) OVER (PARTITION BY bc.bill_no)), 2)
	as claim_percentage
FROM bill_charge bc
	JOIN service_sub_groups ssg ON (bc.service_sub_group_id = ssg.service_sub_group_id)
	JOIN (select per_diem_code, regexp_split_to_table(service_groups_incl, ',') as pdiem_sgroup_id from per_diem_codes_master) as ps
	ON ((ssg.service_group_id::text = ps.pdiem_sgroup_id) OR (bc.act_rate_plan_item_code = ps.per_diem_code))
WHERE bill_no in (select bill_no from bill_charge where charge_head = 'MARPDM' and status != 'X');
