<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>IP List - Insta HMS</title>
</head>


	<body >
		<div class="pageHeader">In Patient List</div>
		<form name="inputform" method="GET" target="_blank"
			action="${cpath}/InPatientReport.do">
			<input type="hidden" name="report" value="InPatientChart">

			<div class="tipText">
						This report gives number of IP patients in a ward  between the given dates based on
			a. Date of Admission or b. Dates when patient was in the hospital.
                        The report displays Patient's Name, Age/Gender, MR no, Patient Visit Id, Doctors Name, Referral Doctor Name, Bed Name
			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select a type based on  which report has to be genrated</td>
				</tr>
				<tr>
					<td><input checked type="radio" name="basedon" value="admitDate" id="admit_date" ><label>Based on Date of Admission</label></td>
				</tr>
				<tr>
					<td><input type="radio"  name="basedon" value="stayDate" id="stay_date"><label>Based on dates when patient was in hospital</label></td>
				</tr>
				<tr>
					<td colspan="2">Select a date range for the report (or select From and To dates manually)</td>
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

