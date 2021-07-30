<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OP Statistcs HMS</title>

<style type="text/css">
	@page {
		size: A4 ;
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

	table.report td.totnumber {
		text-align: right;

	}

	p.noresult {
		font-weight: bold;
	}

	p.heading {
		font-size: 12pt;
		font-weight: bold;
	}
</style>



</head>

<#setting number_format="####0">
<#setting date_format="dd-MM-yyyy">
<#assign trendDisplay = {"month":"Monthly", "week":"Weekly", "day":"Daily"}>
<#assign empty= {}>
<#if report="ops">
	<#assign wise="Consultant wise">
</#if>
<#if report="opsd">
	<#assign wise="Department Wise">
</#if>

<body>
<!-- <#escape x as x?html> -->
	<div align="center">
		<p class="heading">${hospital}</p>
		<p class="heading">  OP Statistics (${wise})</p>
		<p>${fromDate} - ${toDate} </p>
	</div>

	<#if list?size == 0>
		<div align="center"><p class="noresult">No data for the given date range</p></div>
	<#else>

	<table align="center">
		<tr>
			<td>
				<table class="report" border="0" align="center">

					<tr>
						<th rowspan="2">Unit Name</th>
						<th colspan="3">Registration</th>
						<th colspan="3">Revisit</th>
						<th colspan="3">Referred</th>
						<th rowspan="2">Total</th>
					</tr>
					<tr>
						<th>M</th>
						<th>F</th>
						<th>T</th>
						<th>M</th>
						<th>F</th>
						<th>T</th>
						<th>M</th>
						<th>F</th>
						<th>T</th>
					</tr>

					<#assign tot_m_first_visit= 0>
					<#assign tot_f_first_visit= 0>
					<#assign tot_m_revisit= 0>
					<#assign tot_f_revisit= 0>
					<#assign tot_m_referred= 0>
					<#assign tot_f_referred= 0>

					<#assign tot_first_visit= 0>
					<#assign tot_revisit= 0>
					<#assign tot_referred = 0>

					<#list list as l>
					<tr>

						<td>${l.dept_name!''}</td>


						<td class="number">${l.m_first_visit!0}</td>
						<td class="number">${l.f_first_visit!0}</td>
						<td class="number">${l.m_first_visit + l.f_first_visit}</td>
						<td class="number">${l.m_revisit}</td>
						<td class="number">${l.f_revisit}</td>
						<td class="number">${l.m_revisit+l.f_revisit}</td>

						<td class="number">${l.m_referred!0}</td>
						<td class="number">${l.f_referred!0}</td>
						<td class="number">${l.m_referred +l.f_referred}</td>

						<#assign tot_m_first_visit = tot_m_first_visit + l.m_first_visit>
						<#assign tot_f_first_visit = tot_f_first_visit + l.f_first_visit>
						<#assign tot_m_revisit = tot_m_revisit + l.m_revisit>
						<#assign tot_f_revisit = tot_f_revisit + l.f_revisit>
						<#assign tot_m_referred = tot_m_referred + l.m_referred>
						<#assign tot_f_referred = tot_f_referred + l.f_referred>

						<#assign tot_first_visit = tot_first_visit + l.m_first_visit + l.f_first_visit>
						<#assign tot_revisit = tot_revisit + l.m_revisit + l.f_revisit>
						<#assign tot_referred = tot_referred + l.m_referred + l.f_referred>
						<td class="number">${l.m_first_visit + l.f_first_visit +  l.m_revisit + l.f_revisit + l.m_referred + l.f_referred}</td>

					</tr>
					</#list>
					<tr>
						<td >Grand Total:</td>
						<td class="number">${tot_m_first_visit}</td>
						<td class="number">${tot_f_first_visit}</td>
						<td class="number">${tot_m_first_visit+tot_f_first_visit}</td>
						<td class="number">${tot_m_revisit}</td>
						<td class="number">${tot_f_revisit}</td>
						<td class="number">${tot_m_revisit+tot_f_revisit}</td>
						<td class="number">${tot_m_referred}</td>
						<td class="number">${tot_f_referred}</td>
						<td class="number">${tot_m_referred+tot_f_referred}</td>
						<td class="number">${tot_m_first_visit+tot_f_first_visit+tot_m_revisit+tot_f_revisit+tot_m_referred+tot_f_referred}</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				<table align="left">
					<tr>

					</tr>
				</table>
			</td>
		</tr>
		</table>

	</#if>
<!-- </#escape> -->
</body>
</html>