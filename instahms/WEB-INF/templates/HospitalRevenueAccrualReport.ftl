<#assign query = "
SELECT account_group, opened_by, patient_type, sum(amount) as amount, sum(ins_claim_amount) as claim_amount
FROM (SELECT coalesce(b.opened_by, 'None') as opened_by,
	CASE WHEN al.field_name = 'amount' THEN al.new_value::numeric - COALESCE(al.old_value::numeric, 0)
		WHEN (al.field_name = 'status' AND al.new_value = 'X'::text)  THEN 0 - bc.amount
		ELSE 0 END AS amount,
	CASE WHEN al.field_name = 'insurance_claim_amount' THEN al.new_value::numeric - COALESCE(al.old_value::numeric, 0)
		WHEN (al.field_name = 'status' and al.new_value = 'X') THEN 0 - bc.insurance_claim_amount
	ELSE 0 END AS ins_claim_amount,
	CASE WHEN b.is_tpa='f' and (pr.primary_insurance_co is null or pr.primary_insurance_co='') then 'Cash'
     when  b.is_tpa='t' and (pr.primary_insurance_co is not null or pr.primary_insurance_co!='')
     	and  pcm.category_name ='General' then 'Insurance'
     when b.is_tpa='t' and (pr.primary_insurance_co is null or pr.primary_insurance_co='') then 'Corporate'
     when  b.is_tpa='t' and (pr.primary_insurance_co is not null or pr.primary_insurance_co!='')
     and  pcm.category_name !='General' then 'Copay_Credit' else 'Cash' end as patient_type,
	CASE WHEN bc.charge_head in ('PHMED', 'PHCMED', 'PHRET', 'PHCRET') then 'Pharmacy'
     ELSE 'Hospital' END AS account_group
	FROM bill_charge_audit_log al
		JOIN bill_charge bc on (bc.charge_id = al.charge_id)
		JOIN bill b on (b.bill_no = bc.bill_no)
		LEFT JOIN patient_registration pr on (pr.patient_id = b.visit_id)
		LEFT JOIN patient_category_master pcm on (pcm.category_id = pr.patient_category_id)
		WHERE date(al.mod_time) BETWEEN ? and ?
			AND (al.operation = 'UPDATE' OR al.operation = 'INSERT')
			AND (al.field_name = 'amount' OR al.field_name = 'insurance_claim_amount'
	 			OR al.field_name = 'status')
) AS foo
WHERE patient_type IS NOT NULL AND account_group IS NOT NULL
GROUP BY account_group, opened_by, patient_type
">

<#assign result=queryToDynaList(query, fromDate, toDate)>
<#assign amtMap=listBeanToMapMapMapNumeric(result, "account_group", "opened_by", "patient_type", "amount")>
<#assign claimMap=listBeanToMapMapMapNumeric(result, "account_group", "opened_by", "patient_type", "claim_amount")>
<#assign acGroups=amtMap?keys>



<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Revenue Report - Insta HMS</title>
	<style>
		@page {size: 595pt 842pt; margin: 36pt 36pt 36pt 36pt; }
		body { font-family: Arial, sans-serif; }
		table.report { empty-cells: show; font-size: 9pt; }
		table.report { border-collapse: collapse; border: 1px solid black; }
		table.report th { border: 1px solid black; padding: 2px 8px 2px 3px; }
		table.report td { padding: 2px 4px 2px 4px; border: 1px solid black;}
		table.report td.number { text-align: right; }
		table.report td.heading { font-weight: bold; }
		table.report th {text-align: right}
		table.report td.sub {padding-left: 1em;}
		p.noresult { font-weight: bold; }
	</style>
</head>

<#setting date_format="dd-MM-yyyy">
<#assign empty= {}>
<body>
<div align="center">
	<span style="font-size: 12pt;font-weight: bold;margin-top:10px;">Revenue Report</span>
	<p style="margin-top:2px;">${fromDate} - ${toDate} </p>
</div>

<#if acGroups?size == 0>
	<div align="center"><p class="noresult">No data for the given date range</p></div>
<#else>

<#assign users=amtMap._total?keys>

<#list acGroups as acgrp>
	<#if acgrp != "_total">
		<table><tr></tr><tr></tr></table>
			<b>${acgrp}</b>
			<#assign tot_amt = 0>
		<#list users as user>
			<#if user != "_total">
				<#assign cash = ((amtMap[acgrp]!empty)[user]!empty).Cash!0>
				<#assign ins_pat = (((amtMap[acgrp]!empty)[user]!empty).Insurance!0) - (((claimMap[acgrp]!empty)[user]!empty).Insurance!0)>
				<#assign ins_due = ((claimMap[acgrp]!empty)[user]!empty).Insurance!0>
				<#assign corporate = ((amtMap[acgrp]!empty)[user]!empty).Corporate!0>
				<#assign copay_ins = ((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0>
				<#assign copay_corporate = (((amtMap[acgrp]!empty)[user]!empty).Copay_Credit!0) - (((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0)>
				<#assign tot_amt = tot_amt + cash + ins_pat + ins_due + corporate + copay_ins + copay_corporate>
			</#if>
		</#list>
					
		
	<#if tot_amt gt 1000000000>			
		<table class="report" border="0" style="font-size:6pt">
	<#else>
		<table class="report" border="0">
	</#if>
			<tr>
			<th>User Name</th>
			<th>Cash</th>
			<th style="text-align:center" colspan="2" >Insurance</th>
			<th>Corporate</th>
			<th colspan="2" style="text-align:center">Copay_Credit</th>
			<th>Total</th>
			</tr>
			<tr>
			<th></th>
			<th></th>
			<th>Co-pay</th>
			<th>Due</th>
			<th></th>
			<th>Insurance</th>
			<th>Corporate</th>
			<th></th>
			</tr>
			<#assign tot_amt = 0>
			<#list users as user>
				<#if user != "_total">
					<#assign cash = ((amtMap[acgrp]!empty)[user]!empty).Cash!0>
					<#assign ins_pat = (((amtMap[acgrp]!empty)[user]!empty).Insurance!0) - (((claimMap[acgrp]!empty)[user]!empty).Insurance!0)>
					<#assign ins_due = ((claimMap[acgrp]!empty)[user]!empty).Insurance!0>
					<#assign corporate = ((amtMap[acgrp]!empty)[user]!empty).Corporate!0>
					<#assign copay_ins = ((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0>
					<#assign copay_corporate = (((amtMap[acgrp]!empty)[user]!empty).Copay_Credit!0) - (((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0)>
					<#if cash != 0 || ins_pat != 0 || ins_due != 0 || corporate != 0 || copay_ins != 0 || copay_corporate != 0>
						<tr>
						<td>${user}</td>
						<td style="text-align:right">${cash}</td>
						<td style="text-align:right">${ins_pat}</td>
						<td style="text-align:right">${ins_due}</td>
						<td style="text-align:right">${corporate}</td>
						<td style="text-align:right">${copay_ins}</td>
						<td style="text-align:right">${copay_corporate}</td>
						<td style="text-align:right">${cash + ins_pat + ins_due + corporate + copay_ins + copay_corporate}</td>
						<#assign tot_amt = tot_amt + cash + ins_pat + ins_due + corporate + copay_ins + copay_corporate>
						</tr>
					</#if>
				</#if>
			</#list>
			<#list users as user>
				<#if user == "_total">
					<tr>
					<th>Total</th>
					<td style="text-align:right">${((amtMap[acgrp]!empty)[user]!empty).Cash!0}</td>
					<td style="text-align:right">${(((amtMap[acgrp]!empty)[user]!empty).Insurance!0) - (((claimMap[acgrp]!empty)[user]!empty).Insurance!0)}</td>
					<td style="text-align:right">${((claimMap[acgrp]!empty)[user]!empty).Insurance!0}</td>
					<td style="text-align:right">${((amtMap[acgrp]!empty)[user]!empty).Corporate!0}</td>
					<td style="text-align:right">${((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0}</td>
					<td style="text-align:right">${(((amtMap[acgrp]!empty)[user]!empty).Copay_Credit!0)- (((claimMap[acgrp]!empty)[user]!empty).Copay_Credit!0)}</td>
					<td style="text-align:right">${tot_amt}</td>
					</tr>
				</#if>
			</#list>
		</table>
	</#if>
</#list>
</#if>
</body>
</html>

