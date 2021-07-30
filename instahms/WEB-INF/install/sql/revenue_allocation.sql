-- DROP TABLE IF EXISTS revenue_allocation_map CASCADE;
CREATE TABLE revenue_allocation_map (
	charge_head varchar,
	item_id varchar,
	allocation_department varchar,
	allocation_percent numeric(10, 2)
);

CREATE INDEX ram_charge_head_idx ON revenue_allocation_map (charge_head);
CREATE INDEX ram_item_idx ON revenue_allocation_map (item_id);

-- INSERT INTO revenue_allocation_map (charge_head, allocation_percent) (SELECT distinct chargehead_id, 1 from chargehead_constants);

DROP VIEW IF EXISTS aev_revenue_allocation;

CREATE VIEW aev_revenue_allocation AS
SELECT pr.center_id as org_id, 'Revenue'::character varying as transaction_type, bc.bill_no, b.remarks,
b.finalized_date, 'AED'::character varying as currency_code, bc.charge_id as line_number, bc.act_description as line_description,
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
WHERE b.status = 'F' and bc.status != 'X';
