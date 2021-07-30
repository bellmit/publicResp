<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Dietary Summary Report -Insta HMS</title>
<link type="text/css" rel="stylesheet" href="/instahms/css/hmsNew.css"/>

<style type="text/css">
	@page {
		size: 595pt 842pt;
		margin: 36pt 36pt 36pt 36pt;
	}

	body {
		font-family: Arial, sans-serif;
	}

	table.report {
		empty-cells: show;
		font-size: 9pt;
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

	p.noresult {
		font-weight: bold;
	}

	p.heading {
		font-size: 12pt;
		font-weight: bold;
	}
	p {
		margin: 4pt;
	}

	.total {
		font-weight: bold;
	}
</style>

	</head>
<#function printPage >
	<#if format="screen">
	</#if>

</#function>
	<#setting date_format="dd-MM-yyyy">
	<#assign mealTimings = {"BF" :"BF","Lunch":"Lunch","Dinner":"Dinner","Spl":"Spl"}>
	<#assign timings =mealTimings?keys >
	<#assign subType = ["C","A"] >

	<body>
		<div align="center">
			<p class="heading">Meals Summary Report</p>
			<p>${fromDate}- ${toDate} <span style="font-size: 8pt">(as of ${curDateTime})</span></p>
		</div>
	<#if mealTimes?size=0>
		<div align="center"><p class="noresult">No data for the given date range</p> </div>
	<#else>
		<table align="center" border="0" class="report" style="font-size: 8pt">
			<tr>
				<th></th>
				<#list mealTimes as mealtime>
					<#if mealtime != "_total">
						<th colspan="2">${mealtime}</th>
						<#else>
							<th colspan="2">Total</th>
					</#if>
				</#list>
			</tr>
			<tr>
				<th></th>
				<#list mealTimes as mealtime>
					<#list subType as type>
						<th>${type}</th>
					</#list>
				</#list>
			</tr>
			<#list mealNames as mealname>
				<tr>
					<#if mealname != "_total">
						<td>${mealname}</td>
					<#list mealTimes as melatime>
						<#if melatime != "_total">
						<#list timings as timing>
							<#if timing == melatime>
								<td class="number">${countMap[melatime][mealname]!0}</td>
								<td class="number">${amountMap[mealname][melatime]!0}</td>

							</#if>
						</#list>
						<#else>
							<td class="number">${countMap._total[mealname]!0}</td>
							<td class="number">${amountMap[mealname]._total!0}</td>
						</#if>
					</#list>
				</#if>
				</tr>
			</#list>
			<tr>
				<th>Total</th>
				<#list mealNames as mealname>
					<#if mealname == "_total">
						<td class="number"><b>${countMap._total[mealname]!0}</b></td>
						<td class="number"><b>${amountMap._total[mealname]!0}</b></td>
					</#if>
					<#list mealTimes as mealtime>
						<#list timings as timing>
							<#if timing == mealtime && mealname == "_total">
									<td class="number"><b>${countMap[mealtime]._total!0}</b></td>
									<td class="number"><b>${amountMap._total[mealtime]!0}</b></td>
							</#if>
						</#list>
					</#list>
				</#list>
			</tr>
		</table>
		<table align="center">
			<tr>
				<td>Legend :</td>
				<td>C=Count</td>
				<td>A=Amount</td>
			</tr>
		</table>
	<#if format == "screen">
		<div align="center" style="margin-top: 2em">
			<form method="GET" action="" target="_blank">
				<input type="submit" value="Print"/>
				<input type="hidden" name="method" value="getTrendReport"/>
				<input type="hidden" name="format" value="pdf"/>
				<input type="hidden" name="fromDate" value="${fromDate}"/>
				<input type="hidden" name="toDate" value="${toDate}"/>
			</form>
		</div>
	</#if>
</#if>
	</body>
</html>