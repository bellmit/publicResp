<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Patient Stats Trends - Insta HMS</title>

<style type="text/css">
	@page {
		size: A4 landscape;
		margin: 15pt 15pt 15pt 15pt;
	}

	body {
		font-family: Arial, sans-serif;
		font-size: 8pt;
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
<#assign trendDisplay = {"month":"Monthly", "week":"Weekly", "day":"Daily"}>
<#assign empty= {}>

<body>
	<div align="center" style="margin-top: 1em; margin-bottom: 0.5em">
		<div class="heading">${trendDisplay[trendPeriod]} Patient Stats Trend: by ${groupByName}</div>
		<#if centerName != ''><div class="heading">Center : ${centerName}</div></#if>
		<div>${fromDate} - ${toDate} <span style="font-size: 7pt">(as of ${curDateTime})</span></div>
	</div>

	<#if categories?size == 0>
		<div align="center"><p class="noresult">No data for the given date range</p></div>
	<#else>
	<table align="center">
		<tr>
			<td>
				<table class="report" border="0" align="center">
					<tr>
						<th colspan="2"></th>

						<#list periods as period>
							<#if period != "_total">
								<th >
									<#if trendPeriod == "month">
										${period?date("yyyy-MM-dd")?string("MMM yyyy")}
									<#else>
										${period?date("yyyy-MM-dd")?string("dd-MM-yyyy")}
									</#if>
								</th>
							</#if>
						</#list>
						<th colspan="4">Total</th>
					</tr>

					<#list categories as cat>
						<tr class="primary">
							<#if cat != "_total">
								<th rowspan="4">${cat?html}</th>
								<th>M</th>
								<#list periods as period>
									<td class="number">${((result[cat]!empty)[period]!empty).M!0}</td>
								</#list>
							</#if>
						</tr>

						<tr class="secondary">
							<#if cat != "_total">
								<th>F</th>
								<#list periods as period>
									<td class="number">${((result[cat]!empty)[period]!empty).F!0}</td>
								</#list>
							</#if>
						</tr>

						<tr class="secondary">
							<#if cat != "_total">
								<th>O</th>
								<#list periods as period>
									<td class="number">${((result[cat]!empty)[period]!empty).O!0}</td>
								</#list>
							</#if>
						</tr>

						<tr>
							<#if cat != "_total">
								<th>T</th>
								<#list periods as period>
									<td class="number">${((result[cat]!empty)[period]!empty)._total!0}</td>
								</#list>
							</#if>
						</tr>

					</#list>
					<tr class="total">
						<th rowspan="4">Total</th>
						<th>M</th>
						<#list periods as period>
							<td class="number">${(result._total[period]!empty).M!0}</td>
						</#list>
					</tr>
					<tr class="total">
						<th>F</th>
						<#list periods as period>
							<td class="number">${(result._total[period]!empty).F!0}</td>
						</#list>
					</tr>
					<tr class="total">
						<th>O</th>
						<#list periods as period>
							<td class="number">${(result._total[period]!empty).O!0}</td>
						</#list>
					</tr>
					<tr class="total">
						<th>T</th>
						<#list periods as period>
							<td class="number">${(result._total[period]!empty)._total!0}</td>
						</#list>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				<table align="left">
					<tr>
						<td>Legend:</td><td></td><td>M=Male</td><td></td><td>F=Female</td><td>O=Others</td><td>T=Total</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>
		<#if format == "screen">
			<div align="center" style="margin-top: 2em">
				<form method="GET" action="" target="_blank">
					<input type="submit" value="Print"/>
					<input type="hidden" name="method" value="trendReport"/>
					<input type="hidden" name="trendPeriod" value="${trendPeriod}"/>
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

