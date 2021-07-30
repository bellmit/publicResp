<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Insta HMS</title>



</head>

<html>
	<body onload="setDateRangeFinancialYear(document.inputform.fromDate, document.inputform.toDate)">
		<div class="pageHeader">IP Trend Report</div>
		<form name="inputform" method="GET" target="_blank"	action="PatientDeptTrend.do">
			<input type="hidden" name="report" value="PatientDeptMonthlyTrend">
			<input type="hidden" name="method" value="getReport" />
			<div class="tipText">
				This report shows a trend of the number of IP patients admitted and revenue on a monthly basis,
				grouped by departments.Patient count is based on registration date and revenue is
				based on finalized date
			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select a date range for the report</td>
				</tr>
				<tr>
					<td valign="top">
						<input type="radio" id="year" name="selectDate" checked
								onclick="setDateRangeFinancialYear(document.inputform.fromDate, document.inputform.toDate)">
						<label for="year">This Financial Year</label>

						<br/>
						<input type="radio" id="year" name="selectDate"
								onclick="setDateRangeYear(document.inputform.fromDate, document.inputform.toDate)">
						<label for="year">This Calendar Year</label>
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
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<button type="submit" accesskey="G"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
							<b><u>G</u></b>enerate Report</button>
					</td>
			</table>

		</form>
	</body>
</html>

