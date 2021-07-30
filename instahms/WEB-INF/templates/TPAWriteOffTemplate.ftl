<html>
<head>
	<title>TPA Write Off Summary - Insta HMS</title>
	<#include "ReportStyles.ftl">
</head>

<#setting date_format="dd-MM-yyyy">
<#assign empty = {}>

<body>
	<div align="center">
		<p>TPA Write Off Dashboard</p>
		<p>${fromDate} - ${toDate} <span style="font-size: 8pt">(as of ${curDateTime})</span></p>
	</div>

	<table align="center" border="0" class="report">
		<tr>
			<th></th>
			<th colspan="2">IP</th>
			<th colspan="2">OP</th>
		</tr>
		<tr>
			<th></th>
			<th>Count</th>
			<th>Amount</th>
			<th>Count</th>
			<th>Amount</th>
		</tr>

		<#list tpaWriteOffList as tpa>
			<#if tpa != "_total">
				<tr>
					<td>${tpa?html}</td>
					<td class="number">${((tpaWriteOffCountMap[tpa]!empty)["i"]!0)?string('#')}</td>
					<td class="number">${(tpaWriteOffAmountMap[tpa]!empty)["i"]!0}</td>
					<td class="number">${((tpaWriteOffCountMap[tpa]!empty)["o"]!0)?string('#')}</td>
					<td class="number">${(tpaWriteOffAmountMap[tpa]!empty)["o"]!0}</td>
				</tr>
			</#if>
		</#list>

		<tr class="total">
			<th>Total</th>
			<td class="number">${((tpaWriteOffCountMap["_total"]!empty)["i"]!0)?string('#')}</td>
			<td class="number">${(tpaWriteOffAmountMap["_total"]!empty)["i"]!0}</td>
			<td class="number">${((tpaWriteOffCountMap["_total"]!empty)["o"]!0)?string('#')}</td>
			<td class="number">${(tpaWriteOffAmountMap["_total"]!empty)["o"]!0}</td>
		</tr>
	</table>

	<#if format == "screen">
		<div align="center" style="margin-top: 2em">
			<form method="GET" action="" target="_blank">
				<input type="submit" value="Print"/>
				<input type="hidden" name="method" value="tpaWriteOffReport"/>
				<input type="hidden" name="format" value="pdf"/>
				<input type="hidden" name="fromDate" value="${fromDate}"/>
				<input type="hidden" name="toDate" value="${toDate}"/>
				<input type="hidden" name="groupBy" value="${groupBy?html}"/>
			    <input type="hidden" name="filterBy" value="${filterBy}"/>
			    <input type="hidden" name="filterValue" value="${filterValue}"/>
			    <input type="hidden" name="reportType" value="dashboard"/>
			</form>
		</div>
	</#if>

</body>
</html>

