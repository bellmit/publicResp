<#-- Revenue Report

Custom report being used by Unity.

Based on revenue accrual, distributed over departments, where departments is 
defined very specific to this hospital.

Parameters : fromDate (The date on which this report is being taken)
 -->
<#assign empty= {}>

<#assign queryMainPart="
SELECT count(distinct charge_id), top_level, sub_level,visit_type,sum(incr_amount)
FROM (
SELECT
CASE
WHEN charge_head ='IPREG'
THEN 'ADMISSION CHARGE'
WHEN charge_head IN ('OPDOC','ROPDOC','SPODOC','IPDOC','NIPDOC','WIPDOC','DDODOC','DDRDOC')
THEN 'CONSULTATION'
WHEN charge_head='LTDIA'
THEN 'LABORATORY'
WHEN charge_head='RTDIA'
THEN 'RADIOLOGY'
WHEN dept_name iLIKE 'DIALYSIS'
THEN 'DIALYSIS'
WHEN dept_name iLIKE 'DIET'
THEN 'DIET'
WHEN dept_name iLIKE'GASTROENTEROLOGY'
THEN 'GASTROENTEROLOGY'
WHEN charge_group ilike 'ICU'
THEN 'ICU'
WHEN service_name ilike 'LABOUR THEATER CHARGES'
THEN 'LABOUR THEATER CHARGES'
WHEN charge_head = 'LTAX'
THEN 'LUXURY TAX'
WHEN service_name ilike '%emergency day care%'
THEN 'EMERGENCY DAY CARE'
WHEN charge_head = 'MLREG'
THEN 'MLC CHARGES'
WHEN charge_head IN('NCBED','NCICU')
THEN 'NURSING CARE CHARGES'
WHEN charge_group = 'OPE' AND charge_head = 'TCOPE'
THEN 'OPERATION THEATRE'
WHEN service_name iLIKE '%PHYSIOTHERAPY%'
THEN 'PHYSIOTHERAPY'
WHEN service_name ilike 'POR %'
THEN 'POR'
WHEN chargehead_name ilike 'Professional Charge%'
THEN 'PROFESSIONAL CHARGES'
WHEN dept_name ilike 'PULMONOLOGY'
THEN 'PULMONOLOGY'
WHEN service_name ilike '%Room charge%'
THEN 'ROOM CHARGES'
WHEN service_name ilike '%SERVICE & MISCELLANEOUS%'
THEN 'SERVICE AND MISCELLANEOUS'
WHEN dept_name ='ENT'
THEN 'ENT'
WHEN service_name ilike 'TELEPHONE CHARGES'
THEN 'TELEPHONE CHARGES'
WHEN dept_name ilike 'Transport'
THEN 'TRANSPORTATION'
WHEN service_name ilike '%Angiogram%' AND ssg.service_sub_group_name ilike 'Cardiology'
THEN 'CARDIOLOGY'
WHEN service_name ilike '%Angioplasty%' AND ssg.service_sub_group_name ilike 'Cardiology'
THEN 'CARDIOLOGY'
WHEN charge_head IN ('INVITE', 'INVRET','CONMED','CONOPE')
THEN 'INVENTORY'
WHEN dept_name = 'RHEUMATOLOGY'
THEN 'RHEUMATOLOGY'
WHEN dept_name = 'UROLOGY'
THEN 'UROLOGY'
WHEN charge_head ='PKGPKG'
THEN
	CASE
	WHEN act_description ilike 'UNI% CHECKUP'
		OR  act_description ilike 'UNITY% CHECKUP'
	THEN 'UNICHECKUP PACKAGES'
	ELSE 'MIGRANT PACKAGES'
	END
ELSE ''
END AS top_level,
CASE
	WHEN charge_head = 'LTDIA'
	THEN COALESCE (treating_dept,dept_name)
	WHEN charge_head ='RTDIA'
	THEN COALESCE (treating_dept,dept_name)
	WHEN charge_group = 'OPE' AND charge_head = 'TCOPE'
	THEN ssg.service_sub_group_name--tm.theatre_name
	WHEN charge_head IN ('INVITE', 'INVRET','CONMED','CONOPE')
	THEN
		CASE
		WHEN dept_name ilike '%SURGERY%'
		THEN 'OT'
		WHEN dept_name ilike 'CARDIOLOGY'
		THEN 'CARDIOLOGY'
		WHEN dept_name ilike 'Radiology'
		THEN 'RADIOLOGY'
		WHEN dept_name ilike 'LABORATORY'
		THEN 'LABORATORY'
		ELSE 'OTHER'
	END
	WHEN service_name ilike '%Angiogram%' AND ssg.service_sub_group_name ilike 'Cardiology'
	THEN 'ANGIOGRAM'
	WHEN service_name ilike '%Angioplasty%' AND ssg.service_sub_group_name ilike 'Cardiology'
	THEN 'ANGIOPLASTY'
	ELSE ''
END AS sub_level,incr_amount,r.charge_id,r.visit_type
FROM rpt_revenue_accrual_report_view r
LEFT JOIN (SELECT service_sub_group_id,charge_id FROM bill_charge) AS bc ON bc.charge_id = r.charge_id
LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
">

<#assign queryDayPart= "
WHERE incr_date::DATE BETWEEN ? AND ?
--WHERE charge_head IN ('INVITE', 'INVRET','CONMED','CONOPE')
) AS foo
WHERE top_level!=''
GROUP BY top_level, sub_level, visit_type
">


<#assign queryMonthPart= "
WHERE incr_date::DATE BETWEEN date_trunc('MONTH', ?::date) AND (date_trunc('MONTH', ?::date) + INTERVAL '1 MONTH - 1 day')
--WHERE charge_head IN ('INVITE', 'INVRET','CONMED','CONOPE')
) AS foo
WHERE top_level!=''
GROUP BY top_level, sub_level, visit_type
">


<#assign queryLast30DaysPart= "
WHERE incr_date::DATE BETWEEN ( ?::date - interval '30 days')  AND ?::date
--WHERE charge_head IN ('INVITE', 'INVRET','CONMED','CONOPE')
) AS foo
WHERE top_level!=''
GROUP BY top_level, sub_level, visit_type
">

<#assign daysRevenueQuery = queryMainPart+queryDayPart>
<#assign monthsRevenueQuery = queryMainPart+queryMonthPart>
<#assign last30DaysRevenueQuery = queryMainPart+queryLast30DaysPart>

<#assign daysRevenueBeans= queryToDynaList(daysRevenueQuery, fromDate, fromDate)>
<#assign monthsRevenueBeans= queryToDynaList(monthsRevenueQuery, fromDate, fromDate)>
<#assign last30DaysRevenueBeans= queryToDynaList(last30DaysRevenueQuery, fromDate, fromDate)>

<#assign daysRevenue= listBeanToMapMapMapNumeric(daysRevenueBeans, "top_level", "sub_level", "visit_type", "sum")>
<#assign monthsRevenue= listBeanToMapMapMapNumeric(monthsRevenueBeans, "top_level", "sub_level", "visit_type", "sum")>
<#assign last30DaysRevenue= listBeanToMapMapMapNumeric(last30DaysRevenueBeans, "top_level", "sub_level", "visit_type", "sum")>

<#assign daysCount = listBeanToMapMapMapNumeric(daysRevenueBeans, "top_level", "sub_level", "visit_type", "count")>
<#assign monthsCount = listBeanToMapMapMapNumeric(monthsRevenueBeans, "top_level", "sub_level", "visit_type", "count")>
<#assign last30DaysCount = listBeanToMapMapMapNumeric(last30DaysRevenueBeans, "top_level", "sub_level", "visit_type", "count")>

<#assign topLevelList = ["ADMISSION CHARGE","CONSULTATION",
"LABORATORY","RADIOLOGY","DIALYSIS","DIET","GASTROENTEROLOGY",
"ICU","LABOUR THEATER CHARGES","LUXURY TAX","EMERGENCY DAY CARE","MLC CHARGES"
"NURSING CARE CHARGES","OPERATION THEATRE", "PHYSIOTHERAPY","POR","PROFESSIONAL CHARGES",
"PULMONOLOGY","ROOM CHARGES","SERVICE AND MISCELLANEOUS","ENT","TELEPHONE CHARGES","TRANSPORTATION"
"CARDIOLOGY","RHEUMATOLOGY","UROLOGY","UNICHECKUP PACKAGES","MIGRANT PACKAGES","INVENTORY"
]>

<#assign diagSubLevelListQuery = "SELECT * FROM treating_departments_view ORDER BY dept_category" >
<#assign diagSubLevelList = queryToDynaList(diagSubLevelListQuery) >
<#assign diagSubLevelMap = listBeanToMapBean(diagSubLevelList, "dept_name")>
<#assign theatreSubLevelListQuery = "SELECT * FROM service_sub_groups  ORDER BY service_sub_group_name" >
<#assign theatreSubLevelList = queryToDynaList(theatreSubLevelListQuery) >
<#assign theatreSubLevelMap = listBeanToMapBean(theatreSubLevelList, "service_sub_group_name")>

<#assign inventorySubLevel = ["OT","CARDIOLOGY","RADIOLOGY","LABORATORY","OTHER"]>
<#assign cardiologySubLevel = ["ANGIOGRAM","ANGIOPLASTY"]>
<#assign theatreSubLevel=theatreSubLevelMap?keys>
<#assign diagSubLevel=diagSubLevelMap?keys>

<#assign subLevelList = {"diag":diagSubLevel}+{"cardio":cardiologySubLevel}+{"theatre":theatreSubLevel}+{"inv":inventorySubLevel}+{"all":[""]}>

<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>

<#assign noOfDaysQuery = "
 SELECT EXTRACT (DAYS  FROM (DATE_TRUNC('month', ?::date) + INTERVAL '1 MONTH') - DATE_TRUNC('month', ?::date))::int AS days_count
 ">
<#assign noOfDays = queryToDynaBean(noOfDaysQuery, fromDate, fromDate)>

<#-- Report Queries(End)------------------------------------------------------------------------------------------------------>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Consolidated Revenue Report - Insta HMS</title>
	<style>
		@page {size: 595pt 842pt; margin: 36pt 36pt 36pt 36pt; }
		body { font-family: Arial, sans-serif; }

		table.CFDreport {
			empty-cells: show;
			font-size: 9pt;
			vertical-align:top;
			white-space: nowrap;
			width:100%;
			border: none;
			border-collapse: collapse;
		}

		table.CFDreport {
			vertical-align:top;
		}

		table.CFDreport th {
			border: 1px solid black;
			padding: 2px 0px 2px 0px;
			vertical-align:top;
		}

		table.CFDreport td {
			padding: 2px 0px 2px 0px;
			border: 1px solid black;
			vertical-align:top;
			text-align: right;
			height:15px;
		}

		table.CFDreport td.norm {
			padding: 2px 0px 2px 0px;
			vertical-align:top;
			border: 1px solid black;
		}

		table.CFDreport td.number {
			text-align: right;
			vertical-align:top;
		}

		table.CFDreport td.heading {
			vertical-align:top;
			text-wrap:none;
			word-wrap:normal;
			text-align: left;
			height:15px;
		}

		table.CFDreport tr.total {
			border: 1px solid black;
			padding: 2px 0px 2px 0px;
			vertical-align:top;
			text-align: right;
			height:15px;
			font-style:italic;
			font-weight:bold;
		}
}
	</style>
</head>
<#setting date_format="dd-MM-yyyy">
<body>
<#escape x as x?html>
<div align="center">
	<div style="font-size: 12pt;font-weight: bold;margin-top:10px;">${prefMap.hospital_name!?upper_case}</div>
	<div style="font-size: 10pt;font-weight: bold;margin-top:10px;">CONSOLIDATED REVENUE REPORT FOR</div>
	<p style="margin-top:2px;font-size: 10pt;font-weight: bold;">${fromDate}</p>
</div>

<#assign isDayGTotRev = (((daysRevenue["_total"]!empty)["_total"]!empty)["_total"]!0)?int != 0>
<#assign isMonthGTotRev = (((monthsRevenue["_total"]!empty)["_total"]!empty)["_total"]!0)?int != 0>
<#assign isLastMonthGTotRev = (((last30DaysRevenue["_total"]!empty)["_total"]!empty)["_total"]!0)?int != 0>

<#if (isDayGTotRev || isMonthGTotRev ||  isLastMonthGTotRev)>
<table class="CFDreport" style="border-width:0px;">
	<#-- IP Rows-->
	<#list ["IP","OP"] as visittype>
	<tr>
		<td style="border-right:none;border-left:none;text-align:left;font-size:11pt;border-top:none;"> <b>${visittype} DEPARTMENT WISE</b> <br/> </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
		<td style="border-right:none;border-left:none;border-top:none;"> <br/>  </td>
	</tr>
	<tr>
		<th style="width:20%;text-align:left;border-right:none;">PARTICULARS</th>
		<th style="border-right:none;border-left:none;text-align:right;">COUNT</th>
		<th style="border-right:none;border-left:none;text-align:right;">DAY AMT.</th>
		<th style="border-right:none;border-left:none;text-align:right;">(%)</th>
		<th style="border-right:none;border-left:none;text-align:right;">MONTH AMT.</th>
		<th style="border-right:none;border-left:none;text-align:right;">(%)</th>
		<th style="border-right:none;border-left:none;text-align:right;">AVG AMT.</th>
		<th style="border-right:none;border-left:none;text-align:right;">LAST 30 DAYS AMT.</th>
		<th style="border-left:none;text-align:right;">(%)</th>
	</tr>
	<#list topLevelList as toplevel>
	<#if toplevel == "LABORATORY">
		<#assign subLevs = subLevelList.diag>
	<#elseif toplevel == "RADIOLOGY">
		<#assign subLevs = subLevelList.diag>
	<#elseif toplevel == "OPERATION THEATRE">
		<#assign subLevs = subLevelList.theatre>
	<#elseif toplevel == "INVENTORY">
		<#assign subLevs = subLevelList.inv>
	<#elseif toplevel == "CARDIOLOGY">
		<#assign subLevs = subLevelList.cardio>
	<#else>
		<#assign subLevs = subLevelList.all>
	</#if>

	<#assign isDayTotRev = (((daysRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0) != 0>
	<#assign isMonthTotRev = (((monthsRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0) != 0>
	<#assign isLastMonthTotRev = (((last30DaysRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0) != 0>

	<#if subLevs[0] != "" && (isDayTotRev || isMonthTotRev || isLastMonthTotRev)>
		<tr>
			<td style="text-align:left;border-right:none;border-top:none;border-bottom:none;"><b><u>${toplevel}</u></b></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border:none;"></td>
			<td style="border-left:none;border-top:none;border-bottom:none;"></td>
		</tr>
	</#if>
	<#list subLevs as sublevel>
	<#assign isDayRev = (((daysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0) != 0>
	<#assign isMonthRev = (((monthsRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0) != 0>
	<#assign isLastMonthRev = (((last30DaysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0) != 0>



	<#if ((sublevel != "") && (isDayRev || isMonthRev || isLastMonthRev)) || (sublevel == "") >
	<tr>
		<td style="text-align:left;border-right:none;border-top:none;border-bottom:none;">
		<#if sublevel != "">
			${"\xA0"}${"\xA0"}${"\xA0"}${sublevel}
		<#else>
			${toplevel}
		</#if>
		</td>
		<td style="border:none;">${(((daysCount[toplevel]!empty)[sublevel]!empty)[visittype]!0)?string("#")}</td>
		<td style="border:none;">${(((daysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)}</td>
		<td style="border:none;">${(((((daysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)/(((daysRevenue["_total"]!empty)[""]!empty)[visittype]!1))?number * 100)?string("#")}</td>
		<td style="border:none;">${(((monthsRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)}</td>
		<td style="border:none;">${(((((monthsRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)/(((monthsRevenue["_total"]!empty)[""]!empty)[visittype]!1))?number * 100)?string("#")}</td>
		<td style="border:none;">${(((monthsRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0) / (noOfDays.days_count?number)}</td>
		<td style="border:none;">${(((last30DaysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)}</td>
		<td style="border-left:none;border-top:none;border-bottom:none;">${(((((last30DaysRevenue[toplevel]!empty)[sublevel]!empty)[visittype]!0)/(((last30DaysRevenue["_total"]!empty)[""]!empty)[visittype]!1))?number * 100)?string("#")}</td>
	</tr>
	</#if>
	</#list>

	<#if subLevs[0] != "" && (isDayTotRev || isMonthTotRev || isLastMonthTotRev)>
		<tr>
			<td style="text-align:left;border-right:none"><b>TOTAL</b></td>
			<td style="border-right:none;border-left:none;"><b>${(((daysCount[toplevel]!empty)["_total"]!empty)[visittype]!0)?string("#")}</b></td>
			<td style="border-right:none;border-left:none;"><b>${(((daysRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0)}</b></td>
			<td style="border-right:none;border-left:none;"></td>
			<td style="border-right:none;border-left:none;"><b>${(((monthsRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0)}</b></td>
			<td style="border-right:none;border-left:none;"></td>
			<td style="border-right:none;border-left:none;"><b>${(((monthsRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0) / (noOfDays.days_count?number)}</b></td>
			<td style="border-right:none;border-left:none;"><b>${(((last30DaysRevenue[toplevel]!empty)["_total"]!empty)[visittype]!0)}</b></td>
			<td style="border-left:none;"></td>
		</tr>
	 </#if>
	</#list>
			<#--Grand Total Rows-->
			<tr style="font-size:10pt;font-weight:bold;">
				<td style="text-align:left;border-right:none"> GRAND TOTAL</td>
				<td style="border-right:none;border-left:none;">${(((daysCount["_total"]!empty)["_total"]!empty)[visittype]!0)?string("#")}</td>
				<td style="border-right:none;border-left:none;">${(((daysRevenue["_total"]!empty)["_total"]!empty)[visittype]!0)}</td>
				<td style="border-right:none;border-left:none;"></td>
				<td style="border-right:none;border-left:none;">${(((monthsRevenue["_total"]!empty)["_total"]!empty)[visittype]!0)}</td>
				<td style="border-right:none;border-left:none;"></td>
				<td style="border-right:none;border-left:none;">${(((monthsRevenue["_total"]!empty)["_total"]!empty)[visittype]!0) / (noOfDays.days_count?number)}</td>
				<td style="border-right:none;border-left:none;">${(((last30DaysRevenue["_total"]!empty)["_total"]!empty)[visittype]!0)}</td>
				<td style="border-left:none;"></td>
			</tr>
	</#list>
</table>
<#else>
	<div align="center">
		No Data Found For The Given Date Range
	</div>
</#if>
</#escape>
</body>
</html>






