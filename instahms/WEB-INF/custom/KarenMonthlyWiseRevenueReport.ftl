<#function getNumber obj key>
	<#if obj??>
		<#if obj[key]??>
			<#return obj[key]>
		</#if>
	</#if>
	<#return 0>
</#function>
<#assign revAllTotal=0>
<#assign consRevAllTotal=0>
<#macro outputRow title beanMap format='#,##,###' islastrow=false>
	<tr>
		<th>${title?html}</th>
		<#assign total=0>
		<#list dateRange as month>
			<#assign value=getNumber(beanMap, month)>
			<td>${value?string(format)}</td>
			<#assign total = total+ value>
		</#list>
		<td>${total?string(format)}</td>
		<#assign revAllTotal = revAllTotal+total>
	</tr>
</#macro>

<#macro outputHeading title>
	<tr>
		<th colspan="${1 + dateRange?size}" style="font-style: italic">${title}</th>
		<th></th>
	</tr>
</#macro>

<#assign dateRange = getDatesInRange(fromDate, toDate, 'month')>
<#assign empty= {}>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Monthly MIS Report - Insta HMS</title>
	<style>
		@page {
			size: A4 landscape;
			margin: 36pt 36pt 36pt 36pt;
		}
	body {
		font-family: Arial, sans-serif;
	}
	table.report {
		empty-cells: show;
		font-size: 9pt;
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.report th {
		border: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.report td {
		padding: 2px 4px 2px 4px;
		border: 1px solid black;
	}

	table.report td.number {
		text-align: right;
	}

	table.report td.heading {
		font-weight: bold;
	}
		table.report th {text-align: left}
		table.report td {text-align: right}
	</style>
</head>

<#assign revenueByCategoryQuery = "SELECT sum(amount) as amount,
to_char(date_trunc('month', posted_date), 'Mon yyyy') AS month_dt,category
FROM(select (bc.amount) as amount,bc.posted_date,'A&E'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND  hcm.center_id = 1 AND tdep.dept_name IN ('A & E','A&E')
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'A&E'::text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_id = 2 AND hcm.center_id = 1
UNION ALL
SELECT (total_item_amount - ssm.discount + round_off) as amount,date_time::date as posted_date,'Cafeteria'::text AS category
FROM store_sales_main ssm
JOIN stores s ON(s.dept_id = ssm.store_id)
JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
JOIN bill_activity_charge bac ON (bac.activity_code='PHS' AND bac.activity_id = ssm.sale_id)
JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
where dept_id = 34 AND hcm.center_id = 1
UNION ALL
select (bc.amount) as amount,bc.posted_date,'Cath Lab'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1 AND tdep.dept_name IN ('CATH LAB')
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Cath Lab'::text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_id = 5
UNION ALL
SELECT (bc.amount) as amount,bc.posted_date,'Consultant Clinic'::text as category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN consultation_types ct on (bc.consultation_type_id=ct.consultation_type_id )
LEFT JOIN doctors cdoc ON(cdoc.doctor_id=bc.payee_doctor_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND consultation_type IN('OP Consultation','OP Revisit Consultation','OP Follow Up Consultation','Doctor Procedure Charges - OP') AND hcm.center_id =1 AND doctor_name NOT IN('DMO','Dr Anthony Gikonyo','Dr. Betty M Gikonyo', 'Dr Daniel Mugendi', 'Dr. Dan K Gikonyo', 'Dr Daniel Nduiga Keiro', 'Dr.Deya', 'Dr D K Gikonyo', 'Dr Elfenesh Dribsa', 'Dr George Nyale', 'Dr. Joseph Lelo', 'Dr. Joseph Wambugu Wachira', 'Dr Kaaria Mwirigi', 'Dr Muthoni Mburu', 'Dr Paul Kipngetich Lagat', 'Dr Samuel Deya', 'Dr Suleiman Bakari', 'Dr. Suleiman Bakari', 'Dr Wabomba', 'Peter Biu')
UNION ALL
select (bc.amount) as amount,bc.posted_date,'Consultant Clinic'::text as category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1 AND tdep.dept_name IN ('CONSULTANT CLINIC','CONSULTANT CLINICS')
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Consultant Clinic'::text as category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_id = 7
UNION ALL
select (bc.amount) as amount,bc.posted_date,'Dental':: text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1 AND tdep.dept_name IN ('Dental','DENTAL')
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Dental':: text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_id IN(8,9) AND hcm.center_id = 1
UNION ALL
select (bc.amount) as amount,bc.posted_date,'Endoscopy'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1 AND tdep.dept_name IN ('ENDOSCOPY')
UNION ALL
SELECT (bc.amount) as amount,bc.posted_date,'Icu & Hdu'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1
AND (chargegroup_name = 'ICU Charges' OR (tdep.dept_name IN('ICU','ICU & HDU')))
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Icu & Hdu':: text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_name = 'ICU SUB STORE' AND hcm.center_id = 1
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Imaging'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_id = 1 AND chargehead_name = 'Radiology Tests'
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Karatina'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_name ='Karatina'
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Laboratory'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_id = 1 AND chargehead_name = 'Lab Tests'
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Meru Clinic':: text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_name ='Meru'
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Nakuru Clinic'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_name ='Nakuru'
UNION ALL
select (bc.amount) as amount,bc.posted_date,'Nutrition'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND hcm.center_id = 1 AND tdep.dept_name IN ('Nutrition','NUTRITION')
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Nyeri'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_name ='Nyeri'
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Pediatric Clinic'::text AS categoty
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND tdep.dept_name IN ('Paediatrics', 'Paediatric Surgery', 'PEADIATRIC') AND bhcm.center_id =1
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Pediatric Clinic':: text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_name = 'PAEDIATRIC SUB STORE' AND hcm.center_id = 1
UNION ALL
SELECT (total_item_amount - ssm.discount + round_off) as amount,sale_date::date as posted_date,'Pharmacy'::text AS category
FROM store_sales_main ssm
JOIN stores s ON(s.dept_id = ssm.store_id)
JOIN bill_activity_charge bac ON (bac.activity_code='PHS' AND bac.activity_id = ssm.sale_id)
JOIN bill b ON (b.bill_no = ssm.bill_no)
JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
WHERE s.dept_name NOT IN('CAFETERIA') AND hcm.center_id = 1
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Rehabilitation'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND tdep.dept_name IN ('REHABILITATION') AND hcm.center_id = 1
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Rehabilitation'::text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE st.dept_name = 'REHABILITATION MAIN STORE' AND hcm.center_id = 1
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Renal'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND tdep.dept_name IN ('Nephrology','RENAL UNIT') AND hcm.center_id = 1
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Renal':: text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE dept_name = 'RENAL SUB STORE' AND hcm.center_id = 1
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Theatre'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND tdep.dept_name IN ('THEATRE', 'Theatre Equipments', 'THEATRE PROCEDURES', 'THEATRE TIME FEE')  AND hcm.center_id = 1
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Theatre'::text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE st.dept_name = 'OPERATINGTHEATRE SUB STORE' AND hcm.center_id = 1
UNION ALL
select (bc.amount) AS amount,bc.posted_date,'Town Clinic'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND bhcm.center_name = 'Town Clinic'
UNION ALL
SELECT (bc.amount) AS amount,bc.posted_date,'Wards'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND (tdep.dept_name IN ('WARD') OR chargegroup_name IN('Ward Charges')) AND bhcm.center_id = 1
UNION ALL
SELECT (bc.amount)  AS amount, bc.posted_date,'Wards'::text AS category
FROM bill_charge bc
JOIN bill b ON (bc.bill_no = b.bill_no)
JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN doctors cdoc ON(cdoc.doctor_id=bc.payee_doctor_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
LEFT JOIN visit_type_names vn ON (vn.visit_type = pr.visit_type)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN hospital_center_master bhcm ON (pr.center_id=bhcm.center_id or isr.center_id=bhcm.center_id or prc.center_id=bhcm.center_id)
WHERE b.status != 'X' AND bc.status != 'X' AND vn.visit_type_name = 'IP' AND chargehead_name != 'OP Consultation' AND bhcm.center_id = 1
AND cdoc.doctor_name IN('DMO','Dr Anthony Gikonyo','Dr. Betty M Gikonyo', 'Dr Benard Gituma','Dr. Catherine Gichangi','Dr Daniel Mugendi',
'Dr. Dan K Gikonyo', 'Dr.Deya', 'Dr Elfenesh Dribsa', 'Dr George Nyale', 'Dr Gladwell Gichuru Kiarie', 'Dr Hellen Nguchu', 'Dr. Joseph Lelo',
'Dr Karogo Mwangi', 'Dr Kaaria Mwirigi', 'Dr Kezia W Nginya', 'Dr Moses Ngugi', 'Dr Paul Kipngetich Lagat', 'Dr Suleiman Bakari',
'Dr. Suleiman Bakari', 'Dr Warugongo Jeneffer')
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,'Wards'::text AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE st.dept_name = 'WARD SUBSTORE' AND hcm.center_id = 1
) as foo WHERE date(posted_date) >= ? AND date(posted_date) <= ?
group by month_dt, category
">

<#assign consumablesRevenueByCategoryQuery = "SELECT sum(amount) as amount,
to_char(date_trunc('month', posted_date), 'Mon yyyy') AS month_dt,category
FROM(SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date AS posted_date,
CASE WHEN dept_id IN(2) THEN 'AE'::text
WHEN dept_id IN(5)	THEN 'Cath Lab'::text
WHEN dept_id IN(7) 	THEN 'Consultant Clinic'::text
WHEN dept_id IN(8,9)  	THEN 'Dental':: text
WHEN dept_id IN(28)  	THEN 'Rehabilitation'::text
WHEN dept_id IN(25)  	THEN 'Theatre'::text
WHEN dept_id IN(13)    	THEN 'Icu & Hdu'
WHEN dept_id IN(30)    	THEN 'Renal'
WHEN dept_id IN(33)    	THEN 'Wards'
WHEN dept_id IN(26)    	THEN 'Pediatric Clinic' END AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
WHERE  hcm.center_id = 1
UNION ALL
SELECT (total_item_amount - ssm.discount + round_off) as amount,sale_date::date as posted_date,
CASE WHEN hcm.center_name ='The Karen Hospital' AND s.dept_name NOT IN('CAFETERIA') THEN 'Pharmacy'::text
WHEN hcm.center_name ='The Karen Hospital' AND  s.dept_name IN('CAFETERIA') THEN 'Cafeteria'::text
WHEN hcm.center_name = 'Meru' AND s.dept_name = 'MERU CLINIC SUB STORE' THEN 'Meru Clinic'::text
WHEN hcm.center_name = 'Nakuru' AND s.dept_name = 'NAKURU CLINIC SUB STORE' THEN 'Nakuru Clinic'::text
WHEN hcm.center_name = 'Nyeri' AND s.dept_name = 'NYERI CLINIC SUB STORE' THEN 'Nyeri'::text
WHEN hcm.center_name = 'Town Clinic' AND s.dept_name = 'TOWN CLINIC SUB STORE' THEN 'Town Clinic'::text
WHEN hcm.center_name = 'Karatina' AND s.dept_name = 'KARATINA CLINIC SUB STORE' THEN 'Karatina'::text END AS category
FROM store_sales_main ssm
JOIN stores s ON(s.dept_id = ssm.store_id)
JOIN bill_activity_charge bac ON (bac.activity_code='PHS' AND bac.activity_id = ssm.sale_id)
JOIN bill b ON (b.bill_no = ssm.bill_no)
JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
UNION ALL
SELECT COALESCE(sid.amount,pkg_mrp) as amount,date_time::date as posted_date,
CASE WHEN hcm.center_name = 'Meru' AND st.dept_name = 'MERU CLINIC SUB STORE' THEN 'Meru Clinic'::text
WHEN hcm.center_name = 'Nakuru' AND st.dept_name = 'NAKURU CLINIC SUB STORE' THEN 'Nakuru Clinic'::text
WHEN hcm.center_name = 'Nyeri' AND st.dept_name = 'NYERI CLINIC SUB STORE' THEN 'Nyeri'::text
WHEN hcm.center_name = 'Town Clinic' AND st.dept_name = 'TOWN CLINIC SUB STORE' THEN 'Town Clinic'::text
WHEN hcm.center_name = 'Karatina' AND st.dept_name = 'KARATINA CLINIC SUB STORE' THEN 'Karatina'::text END AS category
FROM stock_issue_main sim
LEFT JOIN stock_issue_details sid ON sid.user_issue_no=sim.user_issue_no
LEFT JOIN stores st on st.dept_id = dept_from
LEFT JOIN hospital_center_master hcm ON(hcm.center_id=st.center_id)
) as foo WHERE date(posted_date) >= ? AND date(posted_date) <= ?
group by month_dt,category
">

<#assign revenueByCategoryResult = queryToDynaList(revenueByCategoryQuery, fromDate, toDate)>
<#assign revenueByCategory = listBeanToMapMapNumeric(revenueByCategoryResult,'category','month_dt','amount')>
<#assign consumablesRevenueByCategoryResult = queryToDynaList(consumablesRevenueByCategoryQuery, fromDate, toDate)>
<#assign consumablesRevenueByCategory = listBeanToMapMapNumeric(consumablesRevenueByCategoryResult,'category','month_dt','amount')>
<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>
<#setting date_format="dd-MM-yyyy">
<body>
<div align="center">
	<span style="font-size: 14pt;font-weight: bold;margin-top:10px;">${prefMap.hospital_name!?upper_case}<br/></span>
	<span style="font-size: 10pt;font-weight: bold;margin-top:10px;">Month-Wise Revenue Report</span>
	<p style="margin-top:2px;">(${fromDate} - ${toDate})</p>
</div>

<div align="center">
<table class="report">
	<tr>
		<th>Revenue</th>
		<#list dateRange as month>
			<th>${month}</th>
		</#list>
		<th>Total</th>
	</tr>
	<#list ["A&E", "Cafeteria", "Cath Lab", "Consultant Clinic", "Dental",
	    "Endoscopy", "Icu & Hdu", "Imaging", "Karatina", "Laboratory", "Meru Clinic", "Nakuru Clinic", "Nutrition",
	    "Nyeri","Pediatric Clinic","Pharmacy","Rehabilitation","Renal","Theatre","Town Clinic","Wards"]
      as category>
		<@outputRow title=category beanMap=(revenueByCategory[category])!empty />
	</#list>
	<tr>
		<td>
			Total Revenue
		</td>
		<#list dateRange as month>
			<#assign revTotal = 0>
			<#list ["A&E", "Cafeteria", "Cath Lab", "Consultant Clinic", "Dental",
		    	"Endoscopy", "Icu & Hdu", "Imaging", "Karatina", "Laboratory", "Meru Clinic", "Nakuru Clinic", "Nutrition",
		    	"Nyeri","Pediatric Clinic","Pharmacy","Rehabilitation","Renal","Theatre","Town Clinic","Wards"]
	      		as category>
	      		<#assign beanMap=(revenueByCategory[category])!empty />
	      		<#assign value=getNumber(beanMap, month)>
				<#assign revTotal = revTotal+ value>
			</#list>
			<td>${revTotal?string("#,##,###")}</td>
		</#list>
		<td>${revAllTotal?string("#,##,###")}</td>
	</tr>
</table>
</div>
<div style="width:100px; height:100px;"> </div>
<div align="center">COST OF SALES AND CONSUMABLES (INCLUDED IN THE ABOVE TOTAL)
<table class="report">
	<tr>
		<th>Revenue</th>
		<#list dateRange as month>
			<th>${month}</th>
		</#list>
		<th>Total</th>
	</tr>
	<#list ["AE", "Cafeteria", "Cath Lab", "Consultant Clinic", "Dental",
	    "Endoscopy", "Icu & Hdu", "Imaging", "Karatina", "Laboratory", "Meru Clinic", "Nakuru Clinic", "Nutrition",
	    "Nyeri","Pediatric Clinic","Pharmacy","Rehabilitation","Renal","Theatre","Town Clinic","Wards"]
      as category>
		<@outputRow title=category beanMap=(consumablesRevenueByCategory[category])!empty />
	</#list>
	<tr>
		<td>
			Total Direct cost And Consumables
		</td>
		<#list dateRange as month>
			<#assign revTotal = 0>
			<#list ["AE"?html, "Cafeteria", "Cath Lab", "Consultant Clinic", "Dental",
		    	"Endoscopy", "Icu AND Hdu"?html, "Imaging", "Karatina", "Laboratory", "Meru Clinic", "Nakuru Clinic", "Nutrition",
		    	"Nyeri","Pediatric Clinic","Pharmacy","Rehabilitation","Renal","Theatre","Town Clinic","Wards"]
	      		as category>
	      		<#assign beanMap=(consumablesRevenueByCategory[category])!empty />
	      		<#assign value=getNumber(beanMap, month)>
				<#assign revTotal = revTotal+ value>
			</#list>
			<td>${revTotal?string("#,##,###")}</td>
			<#assign consRevAllTotal = consRevAllTotal+revTotal>
		</#list>
		<td>${consRevAllTotal?string("#,##,###")}</td>
	</tr>
</table>
</div>
</body>
</html>