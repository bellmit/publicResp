<#function get obj key1 key2='' key3=''>
	<#if !(obj??)><#return 0></#if>
	<#if !(obj[key1]??)><#return 0></#if>

	<#if key2 == ''><#return obj[key1]></#if>
	<#if !(obj[key1][key2]??)><#return 0></#if>

	<#if key3 == ''><#return obj[key1][key2]></#if>
	<#if !(obj[key1][key2][key3]??)><#return 0></#if>

	<#return obj[key1][key2][key3]>
</#function>
<#assign empty={}>
<#assign daily_data_count=0>
<#assign monthly_data_count=0>
<#assign avg_count=0>
<#assign daily_data_rev=0>
<#assign monthly_data_rev=0>
<#assign avg_rev=0>
<#setting number_format="#,##0.000">
<#macro outputRow bean format='#,##0.000' islastrow=false>
	<#assign daily_data_count=daily_data_count+(bean.daily_data_count!0)>
	<#assign monthly_data_count=monthly_data_count+(bean.monthly_data_count!0)>
	<#assign avg_count=avg_count+(bean.avg_count!0)>
	<#assign daily_data_rev=daily_data_rev+(bean.daily_data_rev!0)>
	<#assign monthly_data_rev=monthly_data_rev+(bean.monthly_data_rev!0)>
	<#assign avg_rev=avg_rev+(bean.avg_rev!0)>
	<#assign style="">
	<#if islastrow>
		<#assign style="style='border-bottom: 1px solid'">
	</#if>
	<#if bean?has_content>
		<tr>
			<td >${bean.particulars}</td>
			<td ${style} class="number">${(bean.daily_data_count!0)?string(format)}</td>
			<td ${style} class="number">${(bean.daily_data_rev!0)}</td>
			<td ${style} class="number">${(bean.monthly_data_count!0)?string(format)}</td>
			<td ${style} class="number">${(bean.monthly_data_rev!0)}</td>
			<td ${style} class="number">${(bean.avg_count!0)}</td>
			<td ${style} class="number">${(bean.avg_rev!0)}</td>
		</tr>
	</#if>
	<#if islastrow>
		<tr>
			<td style="text-align: right; border-bottom: 1px solid; font-weight: bold">Total:</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${daily_data_count?string(format)}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${daily_data_rev!}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${monthly_data_count?string(format)}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${monthly_data_rev!}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${avg_count!}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${avg_rev}</td>
		</tr>
		<#assign daily_data_count=0>
		<#assign monthly_data_count=0>
		<#assign avg_count=0>
		<#assign daily_data_rev=0>
		<#assign monthly_data_rev=0>
		<#assign avg_rev=0>
	</#if>
</#macro>
<#function getQuery subquery >
	<#assign fields="SELECT ord, particulars, main_particulars, daily_data_count,
       		monthly_data_count,
       		daily_data_rev::numeric(10,2),monthly_data_rev::numeric(10,2),
       		(monthly_data_count /(SELECT extract(day FROM ?::date)))::numeric(10,2) AS avg_count,
       		(monthly_data_rev /(SELECT extract(day FROM ?::date)))::numeric(10,2) AS avg_rev FROM"/>
	<#return fields + "( " + subquery + " ) as foo" >
</#function>

<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Daily MIS Report - Insta HMS</title>
	<style>
		@page {
			size: A4 landscape;
			margin: 36pt 36pt 36pt 36pt;
		}
		body {
			font-family: Arial, sans-serif;
			font-size: 9pt;
		}
		td.number {
			text-align: right;
		}
	</style>
</head>
<#setting date_format="dd-MM-yyyy">
<body>
<#escape x as x?html>
<div align="center">
	<span style="font-size: 14pt;font-weight: bold;margin-top:10px;">${prefMap.hospital_name!?upper_case}<br/></span>
	<span style="font-size: 10pt;font-weight: bold;margin-top:10px;">MIS Accrual Report</span>
	<p style="margin-top:2px;">${reportDate}</p>
</div>
<table width="100%">
	<tr>
		<th style="border-bottom : 1px solid">Perticulars</th>
		<th style="border-bottom : 1px solid; text-align: right">Daily Count</th>
		<th style="border-bottom : 1px solid; text-align: right">Daily Rev</th>
		<th style="border-bottom : 1px solid; text-align: right">Monthly Count</th>
		<th style="border-bottom : 1px solid; text-align: right">Monthly Rev</th>
		<th style="border-bottom : 1px solid; text-align: right">Avg Count</th>
		<th style="border-bottom : 1px solid; text-align: right">Avg Rev</th>
	</tr>
	<#assign Cash = getQuery("
			SELECT 1 as ord, 'Cash' AS Particulars,'Total Collection' AS main_Particulars,
   			(SELECT count(*) AS daily FROM  bill_receipts br
   				LEFT JOIN bill b USING(bill_no)
   				LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   				WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND
    			display_date::date =?::date ) AS daily_data_count,
   			(SELECT count(*) AS monthly FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data_count,
			(SELECT sum(amount)  FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND
    			display_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(amount)  FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign cashBean = queryToDynaBean(Cash, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<tr>
		<td colspan="5"><b>${cashBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=cashBean format="#"/>

	<#assign CorporateCash = getQuery("
			SELECT 2 as ord, 'Corporate Cash' AS Particulars,'Total Collection' AS main_Particulars,
   			(SELECT count(*) AS daily  FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name= 'Corporate' AND
    			display_date::date =?::date ) AS daily_data_count,
   			(SELECT count(*) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name= 'Corporate' AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data_count,
			(SELECT sum(amount) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name= 'Corporate' AND
    			display_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(amount) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name= 'Corporate' AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign corporateCashBean = queryToDynaBean(CorporateCash, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=corporateCashBean format="#"/>

	<#assign InsuranceCash = getQuery("
			SELECT 3 as ord, 'Insurance Cash' AS Particulars,'Total Collection' AS main_Particulars,
   			(SELECT count(*) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate' AND
    			display_date::date =?::date ) AS daily_data_count,
   			(SELECT count(*) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate' AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data_count,
			(SELECT sum(amount) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate' AND
    			display_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(amount) AS daily FROM  bill_receipts br
			LEFT JOIN bill b USING(bill_no)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate' AND
    			display_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign insuranceCashBean = queryToDynaBean(InsuranceCash, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=insuranceCashBean format="#"  islastrow=true/>

	<#assign CorporateCredit = getQuery("
			SELECT 4 as ord, 'Corporate Credit' AS Particulars,'Total Credit' AS main_Particulars,
   			(SELECT count(*) AS daily FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate' AND
    			finalized_date::date =?::date) AS daily_data_count,
   			(SELECT count(*) AS daily FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
    			AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data_count,
			(SELECT sum(COALESCE(COALESCE(b.total_claim,0) - COALESCE(b.insurance_deduction,0),0)) AS daily
			FROM bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
    			AND finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(COALESCE(COALESCE(b.total_claim,0) - COALESCE(b.insurance_deduction,0),0)) AS daily
    			FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
    			AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign corporateCreditBean = queryToDynaBean(CorporateCredit, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<tr>
		<td colspan="5"><b>${corporateCreditBean.main_particulars!''}</b></td>
	</tr>

	<@outputRow bean=corporateCreditBean format="#"/>

	<#assign InsuranceCredit = getQuery("
			SELECT 5 as ord, 'Insurance Credit' AS Particulars,'Total Credit' AS main_Particulars,
   			(SELECT count(*) AS daily FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate' AND
    			finalized_date::date =?::date) AS daily_data_count,
   			(SELECT count(*) AS daily FROM bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
    			AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data_count,
			(SELECT sum(COALESCE(COALESCE(b.total_claim,0) - COALESCE(b.insurance_deduction,0),0)) AS daily
			FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
    			AND finalized_date::date =?::date ) AS daily_data_rev,
    			(SELECT sum(COALESCE(COALESCE(b.total_claim,0) - COALESCE(b.insurance_deduction,0),0)) AS daily
    			FROM  bill b
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
    			AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign insuranceCreditBean = queryToDynaBean(InsuranceCredit, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=insuranceCreditBean format="#" islastrow=true/>

	<#assign HospitalCash = getQuery("
			SELECT 6 as ord, 'Cash Patient' AS Particulars,'Hospital Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date group by b.bill_no) AS foo) AS daily_data_count,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND  agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) AS foo) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign hospitalCashBean = queryToDynaBean(HospitalCash, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<tr>
		<td colspan="5"><b>${hospitalCashBean.main_particulars!''}</b></td>
	</tr>

	<@outputRow bean=hospitalCashBean format="#"/>

	<#assign HospitalCorporate = getQuery("
			SELECT 7 as ord, 'Corporate Patient' AS Particulars,'Hospital Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date group by b.bill_no) AS foo) AS daily_data_count,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) AS foo) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign hospitalCorporateBean = queryToDynaBean(HospitalCorporate, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=hospitalCorporateBean format="#"/>

	<#assign HospitalInsurance = getQuery("
			SELECT 8 as ord, 'Insurance Patient' AS Particulars,'Hospital Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) as daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date group by b.bill_no) as foo) AS daily_data_count,
   			(SELECT count(bill_no) as daily FROM(SELECT b.bill_no  FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) as foo ) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Hospital' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign hospitalInsuranceBean = queryToDynaBean(HospitalInsurance, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=hospitalInsuranceBean format="#" islastrow=true/>

	<#assign PharmacyCash = getQuery("
			SELECT 6 as ord, 'Cash Patient' AS Particulars,'Pharmacy Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date group by b.bill_no) AS foo) AS daily_data_count,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND  agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) AS foo) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id = 0  OR pr.category_id IS NULL) AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign pharmacyCashBean = queryToDynaBean(PharmacyCash, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<tr>
		<td colspan="5"><b>${pharmacyCashBean.main_particulars!''}</b></td>
	</tr>

	<@outputRow bean=pharmacyCashBean format="#"/>

	<#assign PharmacyCorporate = getQuery("
			SELECT 7 as ord, 'Corporate Patient' AS Particulars,'Pharmacy Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date group by b.bill_no) AS foo) AS daily_data_count,
   			(SELECT count(bill_no) AS daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) AS foo) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign pharmacyCorporateBean = queryToDynaBean(PharmacyCorporate, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=pharmacyCorporateBean format="#"/>

	<#assign PharmacyInsurance = getQuery("
			SELECT 8 as ord, 'Insurance Patient' AS Particulars,'Pharmacy Finalized Revenue' AS main_Particulars,
   			(SELECT count(bill_no) as daily FROM(SELECT b.bill_no FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date group by b.bill_no) as foo) AS daily_data_count,
   			(SELECT count(bill_no) as daily FROM(SELECT b.bill_no  FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date group by b.bill_no) as foo ) AS monthly_data_count,
			(SELECT sum(bc.amount) AS daily
			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date =?::date ) AS daily_data_rev,
    		(SELECT sum(bc.amount) AS daily
    			FROM  bill_charge bc
			JOIN bill b USING(bill_no)
			LEFT JOIN account_group_master agm ON(agm.account_group_id = b.account_group)
			LEFT JOIN patient_registration  pr ON(b.visit_id = pr.patient_id)
   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
   			WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
			AND agm.account_group_name = 'Pharmacy' AND
    			finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev ")>
	<#assign pharmacyInsuranceBean = queryToDynaBean(PharmacyInsurance, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>

	<@outputRow bean=pharmacyInsuranceBean format="#" islastrow=true/>


	<#assign OpCashPatients = getQuery("SELECT 9 as ord, 'Cash Patient' AS Particulars,'No Of Out Patients' AS main_Particulars,
				  (SELECT count(*) as daily FROM patient_registration pr
				  WHERE pr.visit_type = 'o' AND (pr.category_id = 0 OR pr.category_id IS NULL)
				AND reg_date::date =?::date) AS daily_data_count,
				(SELECT count(*) as daily FROM patient_registration pr
				WHERE pr.visit_type = 'o' AND (pr.category_id = 0 OR pr.category_id IS NULL)
				AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
				'0.000'::numeric AS daily_data_rev,
				'0.000'::numeric AS monthly_data_rev")>

	<#assign opCashPatBean = queryToDynaBean(OpCashPatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<tr>
		<td colspan="5"><b>${opCashPatBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=opCashPatBean format="#"/>

	<#assign OpCorporatePatients = getQuery("SELECT 10 as ord, 'Corporate Patient' AS Particulars,'No Of Out Patients' AS main_Particulars,
						  (SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
						  	WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
							AND pr.visit_type = 'o'
						  	AND reg_date::date =?::date ) AS daily_data_count,
						(SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
							WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate' 								AND pr.visit_type = 'o'
							AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
							'0.000'::numeric AS daily_data_rev,
							'0.000'::numeric AS monthly_data_rev")>

	<#assign opCorporatePatBean = queryToDynaBean(OpCorporatePatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=opCorporatePatBean format="#"/>

	<#assign OpInsurancePatients = getQuery("SELECT 11 as ord, 'Insurance Patient' AS Particulars,'No Of Out Patients' AS main_Particulars,
						  (SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
							WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
						  	AND  pr.visit_type = 'o'
						  	AND reg_date::date =?::date ) AS daily_data_count,
						(SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
							WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
							AND pr.visit_type = 'o'
 							AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
							'0.000'::numeric AS daily_data_rev,
							'0.000'::numeric AS monthly_data_rev")>

	<#assign opInsurancePatBean = queryToDynaBean(OpInsurancePatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=opInsurancePatBean format="#" islastrow=true/>

	<#assign IpCashPatients = getQuery("SELECT 12 as ord, 'Cash Patient' AS Particulars,'No of IP Admissions' AS main_Particulars,
						  (SELECT count(*) as daily FROM patient_registration pr
						  WHERE pr.visit_type = 'i' AND (pr.category_id = 0 AND pr.category_id IS NULL)
						AND reg_date::date =?::date ) AS daily_data_count,
						(SELECT count(*) as daily FROM patient_registration pr
						WHERE pr.visit_type = 'i' AND (pr.category_id = 0 AND pr.category_id IS NULL)
						 AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						'0.000'::numeric AS daily_data_rev,
						'0.000'::numeric AS monthly_data_rev")>

	<#assign ipCashPatBean = queryToDynaBean(IpCashPatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<tr>
		<td colspan="5"><b>${ipCashPatBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=ipCashPatBean format="#"/>

	<#assign IpCorporatePatients = getQuery("SELECT 13 as ord, 'Corporate Patient' AS Particulars,'No of IP Admissions' AS main_Particulars,
						  (SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
						  	WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
						  	AND pr.visit_type = 'i'
						  	AND reg_date::date =?::date ) AS daily_data_count,
						(SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
						  	WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name = 'Corporate'
						  	AND pr.visit_type = 'i'
						 	AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
							'0.000'::numeric AS daily_data_rev,
							'0.000'::numeric AS monthly_data_rev")>

	<#assign ipCorporatePatBean = queryToDynaBean(IpCorporatePatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=ipCorporatePatBean format="#"/>

	<#assign IpInsurancePatients = getQuery("SELECT 14 as ord, 'Insurance Patient' AS Particulars,'No of IP Admissions' AS main_Particulars,
						  (SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
						  	WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
						  	AND pr.visit_type = 'i'
						  	AND reg_date::date =?::date ) AS daily_data_count,
						(SELECT count(*) as daily FROM  patient_registration pr
				   			LEFT JOIN insurance_category_master icm ON(icm.category_id = pr.category_id)
						  	WHERE (pr.category_id != 0  OR pr.category_id IS NOT NULL) AND icm.category_name != 'Corporate'
						  	AND pr.visit_type = 'i'
						  	AND reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
							'0.000'::numeric AS daily_data_rev,
							'0.000'::numeric AS monthly_data_rev")>

	<#assign ipInsurancePatBean = queryToDynaBean(IpInsurancePatients, reportDate, reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=ipInsurancePatBean format="#" islastrow=true/>

	<#assign RadCtScans = getQuery("SELECT 15 as ord, 'CT Scans' AS Particulars,'Tests' AS main_Particulars,
						(SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE pres_date::date =?::date AND ddep.category='DEP_RAD'
						  AND ddep.ddept_name = 'CT SCAN') AS daily_data_count,
						(SELECT count(*) as daily FROM tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE ddep.category='DEP_RAD' AND ddep.ddept_name = 'CT SCAN'
						  AND pres_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'CT SCAN'
						AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'CT SCAN'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign radCtScansBean = queryToDynaBean(RadCtScans, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<tr>
		<td colspan="5"><b>${radCtScansBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=radCtScansBean format="#"/>

	<#assign MriScans = getQuery("SELECT 16 as ord, E'MRI\\'s' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE pres_date::date =?::date AND ddep.category='DEP_RAD'
						  AND ddep.ddept_name = 'MRI') AS daily_data_count,
						(SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE ddep.category='DEP_RAD' AND ddep.ddept_name = 'MRI' AND
						  pres_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'MRI' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'MRI'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign mriScanBean = queryToDynaBean(MriScans, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=mriScanBean format="#"/>


	<#assign UltraSounds = getQuery("SELECT 17 as ord, 'Ultra Sounds' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE pres_date::date =?::date AND ddep.category='DEP_RAD'
						 AND ddep.ddept_name = 'ULTRASONOGRAPHY') AS daily_data_count,
						(SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE ddep.category='DEP_RAD' AND ddep.ddept_name = 'ULTRASONOGRAPHY' AND
						  pres_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'ULTRASONOGRAPHY' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'ULTRASONOGRAPHY'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign ultraSoundBean = queryToDynaBean(UltraSounds, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=ultraSoundBean format="#"/>


	<#assign Xrays = getQuery("SELECT 18 as ord, 'X-Rays' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE pres_date::date =?::date AND ddep.category='DEP_RAD'
						  AND ddep.ddept_name = 'X-RAY') AS daily_data_count,
						(SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE ddep.category='DEP_RAD' AND ddep.ddept_name = 'X-RAY' AND
						  pres_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'X-RAY' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_RAD' AND dd.ddept_name = 'X-RAY'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign xrayBean = queryToDynaBean(Xrays, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=xrayBean format="#"/>


	<#assign Labs = getQuery("SELECT 19 as ord, 'Lab' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE pres_date::date =?::date AND ddep.category='DEP_LAB') AS daily_data_count,
						(SELECT count(*) as daily FROM  tests_prescribed tp
						  JOIN diagnostics diag ON (tp.test_id=diag.test_id)
	  					  JOIN diagnostics_departments ddep ON (ddep.ddept_id = diag.ddept_id)
						  WHERE ddep.category='DEP_LAB' AND
						  pres_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_LAB' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN diagnostics_departments dd ON(dd.ddept_id = bc.act_department_id)
						WHERE dd.category='DEP_LAB'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign labBean = queryToDynaBean(Labs, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=labBean format="#"/>

	<#assign ServiceVisaMedicals = getQuery("SELECT 20 as ord, 'Visa Medicals' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE presc_date::date =?::date AND ssg.service_sub_group_name='Visa Medical') AS daily_data_count,
						(SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE ssg.service_sub_group_name='Visa Medical' AND
						  presc_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Visa Medical' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Visa Medical'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign visaMedBean = queryToDynaBean(ServiceVisaMedicals, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=visaMedBean format="#"/>

	<#assign ServiceMinorSurgeries = getQuery("SELECT 20 as ord, 'Minor Sugeries' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE presc_date::date =?::date AND ssg.service_sub_group_name='Minor Surgery') AS daily_data_count,
						(SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE ssg.service_sub_group_name='Minor Surgery' AND
						  presc_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Minor Surgery' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Minor Surgery'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign minorSurgeriesBean = queryToDynaBean(ServiceMinorSurgeries, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=minorSurgeriesBean format="#"/>

	<#assign ServiceMajorSurgery = getQuery("SELECT 20 as ord, 'Major Surgeries' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE presc_date::date =?::date AND ssg.service_sub_group_name='Major Surgery') AS daily_data_count,
						(SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE ssg.service_sub_group_name='Major Surgery' AND
						  presc_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Major Surgery' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Major Surgery'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign majorSurgeryBean = queryToDynaBean(ServiceMajorSurgery, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=majorSurgeryBean format="#"/>

	<#assign ServiceNoramlDeliveries = getQuery("SELECT 20 as ord, 'Normal Deliveries' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE presc_date::date =?::date AND ssg.service_sub_group_name='Normal Delivery') AS daily_data_count,
						(SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE ssg.service_sub_group_name='Normal Delivery' AND
						  presc_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Normal Delivery'
						AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Normal Delivery'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign normalDelveriesBean = queryToDynaBean(ServiceNoramlDeliveries, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=normalDelveriesBean format="#"/>

	<#assign ServiceCaesarianDeliveries = getQuery("SELECT 20 as ord, 'Caesarian Deliveries' AS Particulars,'Tests' AS main_Particulars,
						  (SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE presc_date::date =?::date
						AND ssg.service_sub_group_name='Caesarian Delivery') AS daily_data_count,
						(SELECT count(*) as daily FROM  services_prescribed sb
						  JOIN services ser ON(ser.service_id = sb.service_id)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = ser.service_sub_group_id)
						  WHERE ssg.service_sub_group_name='Caesarian Delivery' AND
						  presc_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_count,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Caesarian Delivery' AND finalized_date::date =?::date ) AS daily_data_rev,
						(SELECT sum(bc.amount) as daily FROM bill b
						  JOIN bill_charge bc ON(b.bill_no=bc.bill_no)
						  JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = bc.service_sub_group_id)
						WHERE ssg.service_sub_group_name='Caesarian Delivery'
						AND finalized_date::date BETWEEN date_trunc('month', ?::date) AND ?::date ) AS monthly_data_rev")>

	<#assign caesarianDeliveriesBean = queryToDynaBean(ServiceCaesarianDeliveries, reportDate, reportDate, reportDate, reportDate,reportDate, reportDate, reportDate,reportDate)!empty>
	<@outputRow bean=caesarianDeliveriesBean format="#" islastrow=true/>


</table>
</#escape>
</body>
</html>