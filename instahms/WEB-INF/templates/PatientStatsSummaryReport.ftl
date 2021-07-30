<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Patient Stats - Insta HMS</title>

<style type="text/css">
	@page {
			size: A4 landscape;
			margin: 36pt 36pt 36pt 36pt;
	}

	body {
		font-family: Arial, sans-serif;
		font-size: 7pt;
	}

	table.report {
		empty-cells: show;
	}

	table.report {
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

	table.report td.total {
		text-align: right;
		font-weight: bold;
	}

	p.noresult {
		font-weight: bold;
	}

	.heading {
		font-size: 12pt;
		font-weight: bold;
	}
</style>

<#if format == "screen">
<link type="text/css" rel="stylesheet" href="../css/hmsNew.css"/>
</#if>

</head>

<#setting number_format="####0">
<#setting date_format="dd-MM-yyyy">

<#assign types = ["OPN", "OPY", "IPN","IPY", "_total"]>
<#assign typeNames = ["OP New", "OP Revisit", "IP New", "IP Revisit", "Total"]>
<#assign gender = ["Male","Female","Others","Total"]>
<#assign empty= {}>

<body>
	<div align="center" style="margin-top: 1em; margin-bottom: 0.5em">
		<div class="heading">Patient Stats Summary: by ${groupByName}</div>
		<#if centerName != ''><div class="heading">Center : ${centerName}</div></#if>
		<div>${fromDate} - ${toDate} <span style="font-size: 8pt">(as of ${curDateTime})</span></div>
	</div>

	<#if categories?size == 0>
		<div align="center"><p class="noresult">No data for the given date range</p></div>
	<#else>
		<table class="report" border="0" align="center">

			<tr>
				<th></th>
				<#list typeNames as name>
						<th align="center" colspan="4">${name}</th>
				</#list>
			</tr>
			<tr>
			<th></th>
				<#list typeNames as name>
					<#list gender as gen>
							<th align="center">${gen}</th>
					</#list>
				</#list>
			</tr>

			<#escape x as x?html>
			<#list categories as cgl>
				<#if cgl !="_total">
					<tr>
						<td>${cgl}</td>
						<#list types as type>
								<td class="number">${((result[cgl]!empty)[type]!empty).M!0}</td>
								<td class="number">${((result[cgl]!empty)[type]!empty).F!0}</td>
								<td class="number">${((result[cgl]!empty)[type]!empty).O!0}</td>
								<td class="number">${((result[cgl]!empty)[type]!empty)._total!0}</td>
						</#list>
					</tr>
				</#if>
			</#list>
			</#escape>

			<tr class="total">
				<th>Total</th>
				<#list types as type>
						<td class="total">${(result._total[type]!empty).M!0}</td>
						<td class="total">${(result._total[type]!empty).F!0}</td>
						<td class="total">${(result._total[type]!empty).O!0}</td>
						<td class="total">${(result._total[type]!empty)._total!0}</td>
				</#list>
			</tr>

		</table>
		<#if format == "screen">
			<div align="center" style="margin-top: 2em">
				<form method="GET" action="" target="_blank">
					<input type="submit" value="Print"/>
					<input type="hidden" name="method" value="summaryReport"/>
					<input type="hidden" name="format" value="pdf"/>
					<input type="hidden" name="fromDate" value="${fromDate}"/>
					<input type="hidden" name="toDate" value="${toDate}"/>
					<input type="hidden" name="groupBy" value="${groupBy}"/>
					<input type="hidden" name="groupByName" value="${groupByName}"/>
					<input type="hidden" name="centerClause" value="${centerClause}"/>
					<input type="hidden" name="centerName" value="${centerName}"/>
				</form>
			</div>
		</#if>
	</#if>
</body>
</html>

