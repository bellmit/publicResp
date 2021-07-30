<#--
   * Sample FTL report, to exercise various options / techniques.
	 *  Parameters : fromDate, toDate
	 *  Output : Revenue report Tabular summary. Horiz group: Visit Type. Vert Group: Charge Group.
	 *   Vertical Sub group: data fields (Amount, Discount, net).
-->

<#assign query = "
SELECT chargegroup_name, visit_type_name, sum(amount) as amount, sum(discount) as discount,
  sum(net_amount) as net_amount FROM (
	SELECT (bc.discount+bc.amount) AS amount, bc.amount AS net_amount, bvn.visit_type_name,
	  date(b.finalized_date) AS finalized_date, cgc.chargegroup_name, b.bill_no, bc.discount
	FROM bill_charge bc
		JOIN bill b ON (bc.bill_no = b.bill_no)
		JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
		JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
		JOIN visit_type_names bvn ON (bvn.visit_type = b.visit_type)
	WHERE b.status != 'X' AND bc.status != 'X'
) AS query
WHERE (finalized_date >= ?) AND (finalized_date <= ?)
GROUP BY chargegroup_name, visit_type_name
ORDER BY chargegroup_name ASC 
">

<#assign dlist = queryToDynaList(query, fromDate, toDate)>
<#assign amt = listBeanToMapMapNumeric(dlist, 'chargegroup_name', 'visit_type_name', 'amount')>
<#assign disc= listBeanToMapMapNumeric(dlist, 'chargegroup_name', 'visit_type_name', 'discount')>
<#assign net = listBeanToMapMapNumeric(dlist, 'chargegroup_name', 'visit_type_name', 'net_amount')>

<#-- convenience to return 0 if value is null -->
<#function getNumber obj key>
	<#if obj??>
		<#if obj[key]??>
			<#return obj[key]>
		</#if>
	</#if>
	<#return 0>
</#function>

<#macro outputRowCsv dataMap title newrow=true>
${title}<#t>
<#list ['IP','OP','Retail','Test','_total'] as vtype>
	<#assign value=getNumber(dataMap, vtype)>,${value?string('#')}<#t>
</#list>
</#macro>

<#if _format == 'csv'>
MIS Report
"(${fromDate} - ${toDate})"

Charge Group,Data,IP,OP,Retail,Test,Total
<#list amt?keys as gr>
"${gr}",<@outputRowCsv amt[gr] 'Amount'/>
,<@outputRowCsv disc[gr] 'Discount'/>
,<@outputRowCsv net[gr] 'Net Amount'/>
</#list>
<#else>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>MIS Report - Insta HMS</title>
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
		}
	
		table.report th {
			border: 1px solid black;
			padding: 2px 8px 2px 3px;
			text-align: right;
		}
	
		table.report td {
			border: 1px solid black;
			padding: 2px 4px 2px 4px;
			text-align: right;
		}
	</style>
</head>

<body>

<#macro outputRowHtml dataMap title>
	<td>${title}</td>
	<#list ['IP','OP','Retail','Test','_total'] as vtype>
		<#assign value=getNumber(dataMap, vtype)>
		<td>${value}</td>
	</#list>
</#macro>

<#escape x as x?html>
<div class="header" align="center">
	<span style="font-size: 10pt;font-weight: bold;margin-top:10px;">MIS Report</span>
	<p style="margin-top:2px;">(${fromDate} - ${toDate})</p>
</div>

<table class="report">
	<tr>
		<th>Charge Group</th>
		<th>Data</th>
		<th>IP</th>
		<th>OP</th>
		<th>Retail</th>
		<th>Test</th>
		<th>Total</th>
	</tr>

	<#list amt?keys as gr>
		<tr><td rowspan="3">${gr}</td><@outputRowHtml amt[gr] 'Amount'/></tr>
		<tr><@outputRowHtml disc[gr] 'Discount'/></tr>
		<tr><@outputRowHtml net[gr] 'Net Amount'/></tr>
	</#list>
</table>
</body>
</html>
</#escape>
</#if>

