<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Diagnostic Progressive Report Department Wise: Insta HMS</title>
        <#include "ReportStyles.ftl">
		<style type="text/css">
		@page {
			size: A3 landscape;
			margin: 36pt 36pt 36pt 36pt;
		}
		table.report {
		empty-cells: show;
		font-size: 10pt;
		}
	</style>
</head>
<#setting date_format="dd-MM-yyyy">
<#assign Prgs=0>
<body>
	<div align="center">
		<p class="heading">Diagnostic Progressive Report Department Wise</p>
		<p>${fromDate} - ${toDate}  <span style="font-size: 8pt">(as of ${curDateTime})</span></p>
	</div>
<#if categories?size == 0>
		<div align="center"><p class="noresult">No data for the given date range</p></div>
	<#else>
	<table  align="center" class="report" border="0" style="align:center">
              <tr>
				<th></th>
				<#list categories as cat>
					<#if cat != "_total">
					<th colspan="2">
				    	${cat?html}
					</th>
					</#if>
				</#list>
				<th colspan="3">Total</th>
			</tr>
			<tr>
				<th> </th>
				<#list categories as cat>
					<th align="right">Nos</th>
					<th align="right">Prgs</th>
				</#list>
			</tr>
				<#assign periodIndex = 0>
			   <#list periods as period>
			   	<#assign periodIndex = periodIndex+1>
				  <tr>
                        <#if period != "_total">
						<td>${period?date("yyyy-MM-dd")?string("dd-MM-yyyy")}</td>
							<#assign tot_prgValue=0>
							<#list categories as cat>
							<#if cat != "_total">
						       <td class="number">${diagProgressiveTestCountResult[cat][period]!"0"}</td>

						       <td class="number">
						       <#if diagProgressiveResult[cat][period]?exists>
						       		${diagProgressiveResult[cat][period]}
						       	<#else>
						       		<#assign prgValue = 0>
						       		<#assign prdIndex=0>
						       		<#list periods as prd>
						       			<#assign prdIndex = prdIndex+1>
						       			<#if prdIndex < periodIndex >
						       				<#if diagProgressiveResult[cat][prd]?exists>
						       				<#if diagProgressiveTestCountResult[cat][prd]?exists>
						       				<#assign prgValue = prgValue+diagProgressiveTestCountResult[cat][prd]>
						       				</#if>
						       				</#if>
						       			</#if>
						       		</#list>
						       		${prgValue}
						       		<#assign tot_prgValue = tot_prgValue+prgValue>
							   </#if>

						       </td>
						        </#if>
							</#list>
							<#list categories as cat>
							<#if cat == "_total">
						       <td class="number">${diagProgressiveTestCountResult[cat][period]!"0"}</td>
						       <td class="number">
						       <#if diagProgressiveResult[cat][period]?exists>
						       <#assign tot_prgValue = tot_prgValue + diagProgressiveResult[cat][period]>
						       </#if>
						       ${tot_prgValue!"0"}</td>
						      </#if>
							</#list>
                        </#if>
			    	</tr>
			</#list>
			<tr class="total">
				<th>Total</th>
					<#list categories as cat>
					<#if cat != "_total">
					    <td class="number">${diagProgressiveTestCountResult[cat]._total!"0"}</td>
					     <td class="number"></td>
					     </#if>
					</#list>
					<#list categories as cat>
					<#if cat == "_total">
					    <td class="number">${diagProgressiveTestCountResult._total[cat]!"0"}</td>
					      <td class="number"></td>
					     </#if>
					</#list>
				</tr>
          </table>
       </#if>
  </body>
</html>
