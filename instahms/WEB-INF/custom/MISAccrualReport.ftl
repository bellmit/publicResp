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
<#assign daily_data_tot=0>
<#assign monthly_data_tot=0>
<#assign avg_tot=0>
<#setting number_format="#,##0.00">
<#macro outputRow bean format='#,##0.00' islastrow=false>

	<#if (bean.main_particulars!'') != 'Bed Occupancy Details'>
		<#assign daily_data_tot=daily_data_tot+(bean.daily_data!0)>
		<#assign monthly_data_tot=monthly_data_tot+(bean.monthly_data!0)>
		<#assign avg_tot=avg_tot+(bean.average!0)>
	</#if>
	<#assign style="">
	<#if islastrow>
		<#assign style="style='border-bottom: 1px solid'">
	</#if>
	<#if bean?has_content>
		<tr>
			<td >${bean.particulars}</td>
			<td ${style} class="number">${(bean.daily_data!0)?string(format)}</td>
			<td ${style} class="number">${(bean.percentage!0)?string(format)}</td>
			<td ${style} class="number">${(bean.monthly_data!0)?string(format)}</td>
			<td ${style} class="number">${(bean.average!0)?string(format)}</td>
		</tr>
	</#if>
	<#if islastrow && (bean.main_particulars!'') != 'Bed Occupancy Details'>
		<tr>
			<td style="text-align: right; border-bottom: 1px solid; font-weight: bold">Total:</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${daily_data_tot?string(format)}</td>
			<td style="border-bottom: 1px solid"></td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${monthly_data_tot?string(format)}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">${avg_tot?string(format)}</td>
		</tr>
		<#assign daily_data_tot=0>
		<#assign monthly_data_tot=0>
		<#assign avg_tot=0>
	</#if>
</#macro>

<#function getQuery subquery >
	<#assign fields="SELECT ord, particulars, main_particulars, daily_data,
       		ROUND( (daily_data::numeric/pc_den::numeric)*100,2) AS percentage,
       		monthly_data,
       		(monthly_data /(SELECT extract(day FROM ?::date)))::numeric(10,2) AS average FROM"/>
	<#return fields + "( " + subquery + " ) as foo" >
</#function>

<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Daily MIS Report - Insta HMS</title>
	<style>
		@page {
			size: A4 portrait;
			margin: 04pt 36pt 04pt 36pt;
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
		<th style="border-bottom : 1px solid; text-align: right">Daily Sale</th>
		<th style="border-bottom : 1px solid; text-align: right">Percentage</th>
		<th style="border-bottom : 1px solid; text-align: right">Monthly Sale as on Date</th>
		<th style="border-bottom : 1px solid; text-align: right">Average</th>
	</tr>
	<#assign Admissions = getQuery("
			SELECT 1 as ord, 'Admission' AS Particulars,'Bed Occupancy Details' AS main_Particulars,
   			(SELECT count(*) AS daily FROM  patient_registration  WHERE visit_type='i' AND
    			reg_date::date =?::date ) AS daily_data,
   			(SELECT null::numeric  AS pc_den ) AS pc_den,
   			(SELECT count(*) AS monthly FROM  patient_registration  WHERE visit_type='i' AND
    			reg_date::date BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data")>
	<#assign admBean = queryToDynaBean(Admissions, reportDate, reportDate, reportDate, reportDate)!empty>

	<tr>
		<td colspan="5"><b>${admBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=admBean format="0"/>

	<#assign discharges = getQuery(" SELECT 2 as ord, 'Discharge' AS Particulars,'Bed Occupancy Details' AS main_Particulars,
   		(SELECT COUNT(*) AS daily FROM patient_registratiON
    			WHERE discharge_flag='D' AND visit_type='i' AND
    			discharge_date::DATE=?::date) AS daily_data ,
   		(SELECT null::numeric  AS pc_den ) AS pc_den,
   		(SELECT COUNT(*) AS monthly FROM patient_registratiON
    			WHERE discharge_flag='D' AND visit_type='i' AND
    			discharge_date::DATE BETWEEN date_trunc('month', ?::date) AND ?::date) AS monthly_data")>

	<#assign dischargeBean = queryToDynaBean(discharges, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=dischargeBean format="#"/>

	<#assign curBedOccupancy = getQuery(" SELECT 3 as ord, 'Current Bed Occupancy' AS Particulars,'Bed Occupancy Details' AS main_Particulars,
   		(SELECT count(*) AS daily FROM bed_status_report bsr WHERE bsr.status in ('A','C','R') ) as daily_data,
		(SELECT count(*) AS denominator FROM bed_names bn LEFT JOIN ward_names wn  USING (ward_no)
             		WHERE wn.status='A' AND bn.status='A' )  AS pc_den,
   		(SELECT null::numeric AS monthly_data) as monthly_data")>

	<#assign bedOccupancyBean = queryToDynaBean(curBedOccupancy, reportDate)!empty>
	<@outputRow bean=bedOccupancyBean format="0"/>

	<#assign activeIP = getQuery(" SELECT 4 as ord, 'Active IP' AS Particulars,'Bed Occupancy Details' AS main_Particulars,
		(SELECT count(*) AS daily FROM patient_registratiON WHERE status='A' AND visit_type='i' ) AS daily_data,
		(SELECT null::numeric  AS pc_den ) AS pc_den,
		(SELECT null::numeric AS monthly) AS monthly_data")>

	<#assign activeIPBean = queryToDynaBean(activeIP, reportDate)!empty>
	<@outputRow bean=activeIPBean islastrow=true format="0"/>

	<#assign saleAmtorDisc="select
		extract(day from ?::date) as day,
		sum(case when incr_date=?::date and visit_type='IP' and charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then
			coalesce(incr_amount+incr_discount, 0) else 0 end) as ip_daily_amt,

		sum(case when incr_date=?::date and visit_type='OP' and charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then
			coalesce(incr_amount+incr_discount, 0) else 0 end) as op_daily_amt,

		sum(case when incr_date=?::date and charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_discount,0) else 0 end) as 				hosp_daily_disc,
		sum(case when incr_date=?::date and charge_head IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_discount) else 0 end) as 				ph_daily_disc,

		sum(case when visit_type = 'IP' and charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_amount+incr_discount, 0) 				else 0 end) as ip_monthly_amt,

		sum(case when visit_type = 'OP' and charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_amount+incr_discount, 0) 				else 0 end) as op_monthly_amt,

		sum(case when charge_head not IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_discount, 0) else 0 end) as hosp_monthly_disc,

		sum(case when charge_head IN ('PHCMED','PHMED','PHCRET','PHRET') then coalesce(incr_discount, 0) else 0 end) as ph_monthly_disc


		FROM rpt_revenue_accrual_report_view WHERE incr_date BETWEEN date_trunc('month', ?::date) AND ?::date">

	<#assign saleorDiscbean = queryToDynaBean(saleAmtorDisc, reportDate, reportDate, reportDate, reportDate, reportDate, reportDate,reportDate)>
	<#if saleorDiscbean.ip_daily_amt?has_content || saleorDiscbean.ip_monthly_amt?has_content || saleorDiscbean.op_daily_amt?has_content
		|| saleorDiscbean.op_monthly_amt?has_content>
		<tr>
			<td colspan="5"><b>Hospital Sales</b></td>
		</tr>
		<tr>
			<td >IP Sales</td>
			<td class="number">
				<#assign ip_daily_amt = saleorDiscbean.ip_daily_amt!0>
				<#assign op_daily_amt = saleorDiscbean.op_daily_amt!0>
				<#assign ip_monthly_amt = saleorDiscbean.ip_monthly_amt!0>
				<#assign op_monthly_amt = saleorDiscbean.op_monthly_amt!0>
				${ip_daily_amt}
			</td>
			<td class="number">
				<#if ip_daily_amt != 0>
					<#assign percentage = (ip_daily_amt/(ip_daily_amt+op_daily_amt))*100>
				</#if>
				${(percentage!0)?round}
			</td>
			<td class="number">${ip_monthly_amt}</td>
			<td class="number">
				<#assign ipavg = 0>
				<#if ip_monthly_amt != 0>
					<#assign ipavg = ip_monthly_amt/saleorDiscbean.day>
				</#if>
				${ipavg}
			</td>
		</tr>
		<tr>
			<td >OP Sales</td>
			<td style="border-bottom: 1px solid" class="number">
				${op_daily_amt}</td>
			<td style="border-bottom: 1px solid" class="number">
				<#if op_daily_amt != 0>
					<#assign percentage = (op_daily_amt/(op_daily_amt+op_daily_amt))*100>
				</#if>
				${(percentage!0)?round}</td>
			<td style="border-bottom: 1px solid" class="number">
				${op_monthly_amt}</td>
			<td style="border-bottom: 1px solid" class="number">
				<#assign opavg = 0>
				<#if op_monthly_amt != 0>
					<#assign opavg = op_monthly_amt/saleorDiscbean.day>
				</#if>
				${opavg}
			</td>
		</tr>
		<tr>
			<td style="text-align: right; border-bottom: 1px solid; font-weight: bold">Total:</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(ip_daily_amt+op_daily_amt)}</td>
			<td style="border-bottom: 1px solid"></td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(ip_monthly_amt+op_monthly_amt)}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(ipavg+opavg)}</td>
		</tr>
	</#if>

	<#assign cashSales = getQuery(" SELECT 7 as ord, 'Cash Sales' AS Particulars ,'Pharmacy Sales' AS main_Particulars,
   		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type ='r' AND type ='S' and
			date(sale_date) = ?::date  ) AS daily_data,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE type ='S' and date(sale_date) = ?::date  )  AS pc_den,
		(SELECT sum(total_item_amount+round_off-discount) AS monthly FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type ='r' AND type ='S' and
 			date(sale_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS monthly_data")>

	<#assign cashSalesBean = queryToDynaBean(cashSales, reportDate, reportDate, reportDate, reportDate, reportDate)!empty>
	<tr>
		<td colspan="5"><b>${cashSalesBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=cashSalesBean />

	<#assign creditSales = getQuery(" SELECT 8 as ord, 'Credit Sales' AS Particulars ,'Pharmacy Sales' AS main_Particulars,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type in ('o','i') AND type ='S' and
			date(sale_date) = ?::date  ) AS daily_data,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE type ='S' and date(sale_date) = ?::date  )  AS pc_den,
		(SELECT sum(total_item_amount+round_off-discount) AS monthly FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type in ('o','i') AND type ='S' and
			date(sale_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS monthly_data")>

	<#assign creditSalesBean = queryToDynaBean(creditSales, reportDate, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=creditSalesBean />

	<#assign cashReturns = getQuery(" SELECT 9 as ord, 'Cash Returns' AS Particulars ,'Pharmacy Sales' AS main_Particulars,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type ='r' AND type ='R' and date(sale_date) = ?::date  ) AS daily_data,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE type ='S' and date(sale_date) = ?::date  )  AS pc_den,
		(SELECT sum(total_item_amount+round_off-discount) AS monthly FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type ='r' AND type ='R'
			and date(sale_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS monthly_data")>

	<#assign cashReturnsBean = queryToDynaBean(cashReturns, reportDate, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=cashReturnsBean />

	<#assign creditReturns = getQuery(" SELECT 10 as ord, 'Credit Returns' AS Particulars ,'Pharmacy Sales' AS main_Particulars,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type in ('o','i') AND type ='R' and date(sale_date) = ?::date  ) AS daily_data,
		(SELECT sum(total_item_amount+round_off-discount) AS daily FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE type ='S' and date(sale_date) = ?::date  )   AS pc_den,
		(SELECT sum(total_item_amount+round_off-discount) AS monthly FROM store_sales_main sm LEFT JOIN bill b using (bill_no)
			WHERE visit_type in ('o','i') AND type ='R' and
			date(sale_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS monthly_data")>

	<#assign creditReturnsBean = queryToDynaBean(creditReturns, reportDate, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=creditReturnsBean islastrow=true/>

	<#assign ipSetlmnt = getQuery(" SELECT 11 as ord, 'IP Settlement' AS Particulars ,'Hospital Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='R' AND
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='R' AND
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign ipSetlmntBean = queryToDynaBean(ipSetlmnt, reportDate, reportDate, reportDate, reportDate)!empty>
	<tr>
		<td colspan="5"><b>${ipSetlmntBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=ipSetlmntBean />

	<#assign ipAdv = getQuery(" SELECT 12 as ord, 'IP Advances' AS Particulars ,'Hospital Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='R' AND 
		        date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='R' AND 
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign ipAdvBean = queryToDynaBean(ipAdv, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=ipAdvBean />

	<#assign ipRef = getQuery(" SELECT 13 as ord, 'IP Refunds' AS Particulars ,'Hospital Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='F' and
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='i' AND counter_type='B' AND receipt_type='F' AND
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign ipRefBean = queryToDynaBean(ipRef, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=ipRefBean />

	<#assign ipColl = getQuery(" SELECT 14 as ord, 'OP Collection' AS Particulars ,'Hospital Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM receipts r JOIN bill_receipts br LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type in ('o','t')  AND counter_type='B' AND receipt_type in ('R','F') and
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM receipts r JOIN bill_receipts br LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type in ('o','t')  AND counter_type='B' AND receipt_type in ('R','F') AND
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign ipCollBean = queryToDynaBean(ipColl, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=ipCollBean />

	<#assign tpaColl = getQuery(" SELECT 15 as ord, 'TPA Collections' AS Particulars ,'Hospital Collection' AS main_Particulars,
		(SELECT sum(foo.daily) FROM
			(SELECT sum (amount) AS daily FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no 
				JOIN bill b USING (bill_no)
				WHERE (CASE WHEN br.sponsor_index = 'P' THEN primary_claim_status in ('S','R') ELSE secondary_claim_status in ('S', 'R') END)
					AND r.receipt_type='R' AND r.tpa_id IS NOT NULL AND
				date(r.display_date) = ?::date
			UNION ALL
			SELECT sum(amount) AS daily FROM bill_sponsor_receipts sr
				JOIN bill_sponsor sb USING (sponsor_bill_no)
				WHERE sb.status in ('S','R') AND sb.sponsor_type ='S' AND
				date(sr.display_date) = ?::date
			) AS foo
		) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(foo1.monthly) FROM
			(SELECT sum(amount) AS monthly FROM bill b
				JOIN bill_receipts br USING (bill_no)
				JOIN receipts r ON br.receipt_no = r.receipt_id 
				WHERE (CASE WHEN br.sponsor_index = 'P' THEN primary_claim_status in ('S','R') ELSE secondary_claim_status in ('S', 'R') END)
				AND r.receipt_type='R' AND r.tpa_id IS NOT NULL AND
				date(br.display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date
			UNION ALL
			SELECT sum(amount) AS monthly FROM bill_sponsor_receipts sr
				JOIN bill_sponsor sb USING (sponsor_bill_no)
				WHERE sb.status IN ('S','R') AND sb.sponsor_type ='S' AND
				date(sr.display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date
		) as foo1) AS monthly_data")>

	<#assign tpaCollBean = queryToDynaBean(tpaColl, reportDate, reportDate, reportDate, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=tpaCollBean  islastrow=true/>

	<#assign pCashSales = getQuery(" SELECT 16 as ord, 'Cash Sales' AS Particulars ,'Pharmacy Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='r'  AND counter_type='P' AND receipt_type ='R' AND
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM receipts r JOIN bill_receipts br ON r.receipt_id = br.receipt_no LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type ='r' AND counter_type='P' AND receipt_type ='R' and
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign pCashSalesBean = queryToDynaBean(pCashSales, reportDate, reportDate, reportDate, reportDate)!empty>
	<tr>
		<td colspan="5"><b>${pCashSalesBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=pCashSalesBean />

	<#assign phAdv = getQuery(" SELECT 17 as ord, 'Pharmacy Advance' AS Particulars ,'Pharmacy Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type !='r'  AND counter_type='P' AND receipt_type ='R' and
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON (r.counter=c.counter_id) WHERE visit_type !='r' AND counter_type='P' AND receipt_type ='R' and
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign phAdvBean = queryToDynaBean(phAdv, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=phAdvBean />

	<#assign phSet = getQuery(" SELECT 18 as ord, 'Pharmacy Settlement' AS Particulars ,'Pharmacy Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON 				(r.counter=c.counter_id) WHERE visit_type !='r'  AND counter_type='P' AND receipt_type ='R' AND is_settlement and
			date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON 				(r.counter=c.counter_id) WHERE visit_type !='r' AND counter_type='P' AND receipt_type ='R' AND is_settlement and
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign phSetBean = queryToDynaBean(phSet, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=phSetBean />

	<#assign phRet = getQuery(" SELECT 19 as ord, 'Pharmacy Returns' AS Particulars ,'Pharmacy Collection' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON 				(r.counter=c.counter_id) WHERE counter_type='P' AND receipt_type ='F' AND date(display_date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT r.amount FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no) LEFT JOIN bill b using (bill_no) left JOIN counters c ON 				(r.counter=c.counter_id) WHERE counter_type='P' AND receipt_type ='F' AND
			date(display_date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign phRetBean = queryToDynaBean(phRet, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=phRetBean islastrow=true/>

	<#if saleorDiscbean.hosp_daily_disc?has_content || saleorDiscbean.hosp_monthly_disc?has_content || saleorDiscbean.ph_daily_disc?has_content
		|| saleorDiscbean.ph_monthly_disc?has_content>
		<tr>
			<td colspan="5"><b>Discounts</b></td>
		</tr>
		<tr>
			<td >Hospital Discounts</td>
			<td class="number">
				<#assign hosp_daily_disc=saleorDiscbean.hosp_daily_disc!0>
				<#assign ph_daily_disc=saleorDiscbean.ph_daily_disc!0>
				<#assign hosp_monthly_disc=saleorDiscbean.hosp_monthly_disc!0>
				<#assign ph_monthly_disc=saleorDiscbean.ph_monthly_disc!0>
				${hosp_daily_disc}
			</td>
			<td class="number"></td>
			<td class="number">${hosp_monthly_disc}</td>
			<td class="number">
				<#assign ipdiscavg = 0>
				<#if hosp_monthly_disc != 0>
					<#assign ipdiscavg = hosp_monthly_disc/saleorDiscbean.day>
				</#if>
				${ipdiscavg}
			</td>
		</tr>
		<tr>
			<td >Pharmacy Discounts</td>
			<td style="border-bottom: 1px solid" class="number">
				${ph_daily_disc}
			</td>
			<td style="border-bottom: 1px solid" class="number"></td>
			<td style="border-bottom: 1px solid" class="number">
				${ph_monthly_disc}
			</td>
			<td style="border-bottom: 1px solid" class="number">
				<#assign opdiscavg = 0>
				<#if ph_monthly_disc != 0>
					<#assign opdiscavg = ph_monthly_disc/saleorDiscbean.day>
				</#if>
				${opdiscavg}
			</td>
		</tr>
		<tr>
			<td style="text-align: right; border-bottom: 1px solid; font-weight: bold">Total:</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(hosp_daily_disc+ph_daily_disc)}</td>
			<td style="border-bottom: 1px solid"></td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(hosp_monthly_disc+ph_monthly_disc)}</td>
			<td style="border-bottom: 1px solid; font-weight: bold" class="number">
				${(ipdiscavg+opdiscavg)}</td>
		</tr>
	</#if>

	<#assign docPay = getQuery(" SELECT 22 as ord, 'Doctor Payments' AS Particulars ,'Payments' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type in ('D','P','R','F') and date(date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type in ('D','P','R','F')
			and date(date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign docPayBean = queryToDynaBean(docPay, reportDate, reportDate, reportDate, reportDate)!empty>
	<tr>
		<td colspan="5"><b>${docPayBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=docPayBean />

	<#assign supPay = getQuery(" SELECT 23 as ord, 'Supplier Payments' AS Particulars ,'Payments' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type ='S' and date(date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type='S' and
			date(date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign supPayBean = queryToDynaBean(supPay, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=supPayBean />

	<#assign miscPay = getQuery(" SELECT 24 as ord, 'Misc Payments' AS Particulars ,'Payments' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type ='C' and date(date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type ='C' and
			date(date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign miscPayBean = queryToDynaBean(miscPay, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=miscPayBean />

	<#assign outhousePay = getQuery(" SELECT 25 as ord, 'Outhouse Payments' AS Particulars ,'Payments' AS main_Particulars,
		(SELECT sum(amount) AS daily FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type ='O' and date(date) = ?::date  ) AS ph_sales) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT sum(amount) AS monthly FROM (SELECT amount FROM payments p LEFT JOIN counters c ON (p.counter=c.counter_id)
			WHERE payment_type ='O' and
			date(date) BETWEEN date_trunc('month', ?::date) AND  ?::date  ) AS ph_sales) AS monthly_data")>

	<#assign outhousePayBean = queryToDynaBean(outhousePay, reportDate, reportDate, reportDate, reportDate)!empty>
	<@outputRow bean=outhousePayBean islastrow=true/>

	<#assign purchaseDet = getQuery(" SELECT 26 as ord, mt.Particulars, 'Purchase Details' AS main_Particulars,
		daily::numeric(10,2) as daily_data, null::numeric as pc_den, monthly::numeric(10,2) as monthly_data
		FROM (SELECT dept_name as Particulars, sum(grn_total_amount + invoice_level_amount) as monthly
			FROM (SELECT gm.store_id, gm.grn_no, sum(g.billed_qty/id.issue_base_unit*g.cost_price - g.discount + g.tax) AS grn_total_amount,
				CASE WHEN supplier_invoice_id IS NULL THEN 0-(dn.other_charges - dn.discount + dn.round_off)
				ELSE i.other_charges - i.discount + i.round_off + i.cess_tax_amt END AS invoice_level_amount
        			FROM store_grn_details g
					JOIN store_grn_main gm USING (grn_no)
					JOIN store_item_details id USING (medicine_id)
					LEFT JOIN store_invoice i USING (supplier_invoice_id)
					LEFT JOIN store_debit_note dn USING (debit_note_no)
				WHERE date(gm.grn_date) BETWEEN date_trunc('month', ?::date) AND ?::date
				GROUP BY gm.store_id, gm.grn_no, invoice_level_amount
				) as inv
				JOIN stores s ON (inv.store_id = s.dept_id)
			GROUP BY dept_name
    		) AS mt
    		LEFT JOIN (SELECT dept_name as Particulars, sum(grn_total_amount + invoice_level_amount) as daily
				FROM (SELECT gm.store_id, gm.grn_no,
			    		sum(g.billed_qty/id.issue_base_unit*g.cost_price - g.discount + g.tax) AS grn_total_amount,
					CASE WHEN supplier_invoice_id IS NULL THEN 0-(dn.other_charges - dn.discount + dn.round_off)
					ELSE i.other_charges - i.discount + i.round_off + i.cess_tax_amt END AS invoice_level_amount
					FROM store_grn_details g
						JOIN store_grn_main gm USING (grn_no)
						JOIN store_item_details id USING (medicine_id)
						LEFT JOIN store_invoice i USING (supplier_invoice_id)
						LEFT JOIN store_debit_note dn USING (debit_note_no)
					WHERE date(gm.grn_date) = ?::date
					GROUP BY gm.store_id, gm.grn_no, invoice_level_amount
				) as inv
				        JOIN stores s ON (inv.store_id = s.dept_id)
				GROUP BY dept_name
    			) as dt ON (dt.Particulars = mt.Particulars)")>
	<#assign purchaseDetList = queryToDynaList(purchaseDet, reportDate, reportDate, reportDate, reportDate)!empty>
	<#if purchaseDetList?has_content>
		<tr>
			<td colspan="5"><b>Purchase Details</b></td>
		</tr>
	</#if>
	<#list purchaseDetList as plist>
		<@outputRow bean=plist islastrow=(purchaseDetList?seq_index_of(plist) == purchaseDetList?size-1)/>
	</#list>

	<#assign tpaDues = getQuery(" SELECT 27 as ord, 'Current TPA Dues' AS Particulars ,'TPA Dues' AS main_Particulars,
		(SELECT sum(COALESCE(total_claim,0) - (CASE WHEN claim_recd_amount is not null THEN claim_recd_amount
			ELSE COALESCE(primary_total_sponsor_receipts,0) end)::NUMERIC) AS tpa_due
		 FROM bill WHERE primary_claim_status ='S' ) AS daily_data,
		(SELECT null::numeric ) AS pc_den,
		(SELECT null::numeric ) AS monthly_data")>

	<#assign tpaDuesBean = queryToDynaBean(tpaDues, reportDate)>
	<tr>
		<td colspan="5"><b>${tpaDuesBean.main_particulars!''}</b></td>
	</tr>
	<@outputRow bean=tpaDuesBean islastrow=true/>

</table>
</#escape>
</body>
</html>

