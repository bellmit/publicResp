<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<head>
	<title>OP Trend - Insta HMS</title>

</head>

<html>
	<body onload="setDateRangeFinancialYear(document.inputform.fromDate,document.inputform.toDate)">
		<div class="pageHeader">OP Trend Report</div>
		<form name="inputform"  target="_blank" action="OPTrend.do" method="get">
		<input type="hidden" name="report" value="OutPatientMonthlyTrend">
		<input type="hidden" name="method" value="getReport" />

		<div class="tipText">
				This report shows a trend of the number of OP patients and revenue
				on a monthly basis.
		</div>

		<table align="center">
			<tr>
				<td colspan="2">Select a date range for report</td>
			</tr>
			<tr>
				<td valign="top">
					<input type="radio" name="selectDate" checked
						onclick="setDateRangeFinancialYear(document.inputform.fromDate,document.inputform.toDate)">
						<label>This Financial Year</label>
					<br/>
					<input type="radio" name="selectDate"
						onclick="setDateRangeYear(document.inputform.fromDate,document.inputform.toDate)">
						<label>This Calender Year</label>
					<br/>
				</td>
				<td valign="top" style="padding-left: 2em;">
					<table>
						<tr>
							<td align="right">From</td>
							<td><insta:datewidget name="fromDate" value="today"/></td>
						</tr><tr>
							<td align="right">To</td>
							<td><insta:datewidget name="toDate" value="today"/> </td>

						</tr>
					</table>

				</td>
			</tr>
		</table>
		<table align="center" style="margin-top: 1em;">
			<tr>
				<td>
					<button type="submit" accesskey="G"
						onclick="return validateFromToDate(document.inputform.fromDate,document.inputform.toDate)">
						<b><u>G</u></b>enerate Report</button>
				</td>
			</tr>

		</table>
		<div id="doctordiv" style="display:none">
			<table class="formtable" style="width: 100">
				<tr><td>Doctor Name :</td></tr>
				<tr>
					<td>
						<select>

						</select>
					</td>
				</tr>
			</table>
		</div>


		</form>
	</body>
</html>
