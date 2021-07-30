<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OP/IP Statistcs HMS</title>

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

<#if report="ipsc" || report="ipscg">
	<#assign wise="Consultant wise">
</#if>
<#if report="ipsd" || report="ipsdg" || report="ipsdabo">
	<#assign wise="(Department Wise)">
</#if>

<body>

	<div align="center">
		<p class="heading">${hospital?html}</p>
		<p class="heading">  IP Statistics ${wise!''}</p>
		<p>${fromDate} - ${toDate} </p>
	</div>

	<#if departments?size == 0>
		<div align="center"><p class="noresult">No data for the given date range</p></div>
	<#else>

	<table align="center">
		<tr>
			<td>
				<table class="report" border="0" align="center">
					<#if report="ipscg" || report="ipsdg">
					<tr>
						<th rowspan="2">Unit Name</th>
						<th colspan="4">Admission</th>
						<th colspan="4">Discharge</th>
						<th colspan="4">TO</th>
					</tr>
					<tr>
						<th>M</th>
						<th>F</th>
						<th>C</th>
						<th>T</th>
						<th>M</th>
						<th>F</th>
						<th>C</th>
						<th>T</th>
						<th>M</th>
						<th>F</th>
						<th>C</th>
						<th>T</th>
					</tr>
					<#elseif report="ipsc" || report="ipsd" || report="ipsdabo">
						<tr>
							<th>Unit Name</th>
							<#if report!="ipsdabo">
							<th>PO</th>
							</#if>
							<th>Admission</th>
							<th>Discharge</th>
							<th>Death</th>
							<th>IP Days</th>
							<#if report="ipsdabo">
								<th>MLC</th>
								<th>ABO</th>
								<th>BEDS</th>
								<th>BO Rate</th>
							<#else>
								<th>Hosp Days</th>
							</#if>
						</tr>
					</#if>

					<#list departments as dep>

					<tr>
						<#if dep != "_total">
						<td>${dep?html}</td>

						<#if report="ipscg" || report="ipsdg">

							<td class="number">
								<#if GAdmitsMap[dep].M??>${GAdmitsMap[dep].M}<#else>0</#if>
							</td>
							<td class="number">
								<#if GAdmitsMap[dep].F??>${GAdmitsMap[dep].F}<#else>0</#if>
							</td>
							<td class="number">
								<#if GAdmitsMap[dep].C??>${GAdmitsMap[dep].C}<#else>0</#if>
							</td>
							<td class="number">
								<#if GAdmitsMap[dep]._total??>${GAdmitsMap[dep]._total}<#else>0</#if>
							</td>

							<td class="number">
								<#if GDischargeMap[dep].M??>${GDischargeMap[dep].M}<#else>0</#if>
							</td>
							<td class="number">
								<#if GDischargeMap[dep].F??>${GDischargeMap[dep].F}<#else>0</#if>
							</td>
							<td class="number">
								<#if GDischargeMap[dep].C??>${GDischargeMap[dep].C}<#else>0</#if>
							</td>
							<td class="number">
								<#if GDischargeMap[dep]._total??>${GDischargeMap[dep]._total}<#else>0</#if>
							</td>

							<td class="number">
								<#if GTotalOccupancyMap[dep].M??>${GTotalOccupancyMap[dep].M}<#else>0</#if>
							</td>
							<td class="number">
								<#if GTotalOccupancyMap[dep].F??>${GTotalOccupancyMap[dep].F}<#else>0</#if>
							</td>
							<td class="number">
								<#if GTotalOccupancyMap[dep].C??>${GTotalOccupancyMap[dep].C}<#else>0</#if>
							</td>
							<td class="number">
								<#if GTotalOccupancyMap[dep]._total??>
									${GTotalOccupancyMap[dep]._total}
								<#else>0
								</#if>
							</td>

						<#elseif report="ipsc" || report="ipsd" || report="ipsdabo">
						<#if report!="ipsdabo">
							<td class="number">${POMap[dep]}</td>
						</#if>
							<td class="number">${AdmitsMap[dep]}</td>
							<td class="number">${DischargeMap[dep]}</td>
							<td class="number">${DeathMap[dep]}</td>

							<td class="number">${IPDaysMap[dep]}</td>
							<#if report="ipsdabo">
							<td class="number">${MlcMap[dep]}</td>
							<td class="number">${AboMap[dep]?string("0.###")}</td>
							<td class="number">${BedsMap[dep]}</td>
							<td class="number">${BoRateMap[dep]?string("0.###")}</td>
							<#else>
								<td class="number">${DurStayMap[dep]}</td>
							</#if>
						</#if>
						</#if>
					</tr>
					</#list>
					<tr>
						<td >Grand Total:</td>
					<#if report="ipscg" || report="ipsdg">

						<td class="number">${(GAdmitsMap._total!empty).M!0}</td>
						<td class="number">${(GAdmitsMap._total!empty).F!0}</td>
						<td class="number">${(GAdmitsMap._total!empty).C!0}</td>
						<td class="number">
							<#assign tot_m_visit = ((GAdmitsMap._total!empty).M)!0>
							<#assign tot_f_visit = ((GAdmitsMap._total!empty).F)!0>
							<#assign tot_c_visit = ((GAdmitsMap._total!empty).C)!0>
							<#assign tot_visit = 0+tot_m_visit+tot_f_visit+tot_c_visit>
							${tot_visit}
						</td>
						<td class="number">${(GDischargeMap._total!empty).M!0}</td>
						<td class="number">${(GDischargeMap._total!empty).F!0}</td>
						<td class="number">${(GDischargeMap._total!empty).C!0}</td>
						<td class="number">
							<#assign tot_m_discharge = ((GDischargeMap._total!empty).M)!0>
							<#assign tot_f_discharge = ((GDischargeMap._total!empty).F)!0>
							<#assign tot_c_discharge = ((GDischargeMap._total!empty).C)!0>
							<#assign tot_discharge = 0+tot_m_discharge+tot_f_discharge+tot_c_discharge>
							${tot_discharge}
						</td>
						<td class="number">${(GTotalOccupancyMap._total!empty).M!0}</td>
						<td class="number">${(GTotalOccupancyMap._total!empty).F!0}</td>
						<td class="number">${(GTotalOccupancyMap._total!empty).C!0}</td>
						<td class="number">
							<#assign tot_m_occupancy = ((GTotalOccupancyMap._total!empty).M)!0>
							<#assign tot_f_occupancy = ((GTotalOccupancyMap._total!empty).F)!0>
							<#assign tot_c_occupancy = ((GTotalOccupancyMap._total!empty).C)!0>
							<#assign tot_occupancy = 0+tot_m_occupancy+tot_f_occupancy+tot_c_occupancy>
							${tot_occupancy}
						</td>

					<#elseif report="ipsc" || report="ipsd" || report="ipsdabo">
						<#if report!="ipsdabo">
						<td class="number">${POMap._total}</td>
						</#if>
						<td class="number">${AdmitsMap._total}</td>
						<td class="number">${DischargeMap._total}</td>
						<td class="number">${DeathMap._total}</td>
						<td class="number">${IPDaysMap._total}</td>
						<#if report="ipsdabo">
							<td class="number">${MlcMap._total}</td>
							<td class="number">${AboMap._total?string("0.###")}</td>
							<td class="number">${BedsMap._total}</td>
							<td class="number">${BoRateMap._total?string("0.###")}</td>
						<#else>
						<td class="number">${DurStayMap._total}</td>
						</#if>
					</#if>
					</tr>

				</table>
			</td>
		</tr>
		<tr>
			<td>
				<table align="left">
					<tr><td>
						PO-Previous Occupancy <#if report="ipscg" || report="ipsdg"> M-Male F-Female C-Child TO-Total Occupancy </#if>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>

	</#if>

</body>
</html>