<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Reagent/Consumable Detail Report- Insta HMS</title>

</head>

<html>
	<body>
		<div class="pageHeader">Reagent/Consumable Detail Report</div>
		<form name="inputform" method="GET" target="_blank" action="ReagentDetailReport.do" >
			<input type="hidden" name="report" value="ReagentDetailReport">
			<input type="hidden" name="method" value="getReport" />

			<div class="tipText">
				This report lists the total quantity of each reagent that has been used for test
				between specified dates.
			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select a date range for the report</td>
				</tr>
				<tr>
					<td valign="top">
									<input checked type="radio" id="pd" name="_sel" onclick="setDateRangeYesterday(fromDate, toDate)">
									<label for="pd">Yesterday</label>
									<br/>

									<input type="radio" id="td" name="_sel" checked onclick="setDateRangeToday(fromDate, toDate)">
									<label for="td">Today</label>
									<br/>

									<input type="radio" id="pm" name="_sel" onclick="setDateRangePreviousMonth(fromDate, toDate)">
									<label for="pm">Previous Month</label>
									<br/>

									<input type="radio" id="tm" name="_sel" onclick="setDateRangeMonth(fromDate, toDate)">
									<label for="tm">This Month</label>
									<br/>

									<input type="radio" id="pfy"
										name="_sel" onclick="setDateRangePreviousFinancialYear(fromDate, toDate)">
									<label for="pfy">Previous Financial Year</label>
									<br/>

									<input type="radio" id="tfy" name="_sel" onclick="setDateRangeFinancialYear(fromDate, toDate)">
									<label for="tfy">This Financial Year</label>
									<br/>
					</td>

					<td valign="top" style="padding-left: 2em">
						<table>
							<tr>
								<td align="right">From:</td>
								<td><insta:datewidget name="fromDate" value="today"/></td>
							</tr>
							<tr>
								<td align="right">To:</td>
								<td><insta:datewidget name="toDate" value="today"/></td>
							</tr>
						</table>
					</td>
				</tr>

				<tr>
					<tr><td></td></tr>
			<td>Report Type</td>
			<td><insta:selectoptions name="store" value="-1" opvalues="-1,-2" optexts="Laboratory/Radiology Reagent,Service Consumable" style="width:18em"/></td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<button type="submit" accesskey="G"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
							<b><u>G</u></b>enerate Report</button>
					</td>
				</td>
			</table>

		</form>
	</body>
</html>

