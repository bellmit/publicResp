<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Out Patient Report - Insta HMS</title>



	<insta:link type="script" file="reports/outpatientreport.js"/>

</head>

<html>
	<body >
		<div class="pageHeader">Out Patient Details Report</div>
		<form name="inputform" method="GET" target="_blank"
			action="OutPatientReport.do">
		<input type="hidden" name="method" value="getReport">
		<input type="hidden" name="filterBy" value=""/>
		<input type="hidden" name="filterValue" id="filterValue" value=""/>


			<div align="center" class="tipText">
				This report lists Out Patients given time period.

			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select a date range for the report</td>
				</tr>
				<tr>
					<td valign="top">
						<input type="radio" id="today" name="selectDate" checked
								onclick="setDateRangeToday(document.inputform.fromDate, document.inputform.toDate)">
						<label for="today">Today</label>
						<br/>

						<input type="radio" id="week" name="selectDate"
								onclick="setDateRangeWeek(document.inputform.fromDate, document.inputform.toDate)">
						<label for="week">This week</label>
						<br/>

						<input type="radio" id="month" name="selectDate"
								onclick="setDateRangeMonth(document.inputform.fromDate, document.inputform.toDate)">
						<label for="month">This month</label>
						<br/>

						<input type="radio" id="year" name="selectDate"
								onclick="setDateRangeYear(document.inputform.fromDate, document.inputform.toDate)">
						<label for="year">This year</label>
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
					<td align="center">Group By:</td>
					<td><select name="groupBy">
							<option value="dept_name">Department</option>
							<option value="doctor">Doctor</option>
						</select>
					</td>
				</tr>


				<tr>
					<td align="center">Filter By:</td>
					<td>
						<select name="filterBy" id="filterBy" onchange="disableFilter();">
							<option selected value="dummy">--(No Filter)--</option>
							<option value="dept_name">Department</option>
							<option value="doctor">Doctor</option>
							<option value="complaint">Complaint</option>
						</select>
					</td>


					<td>=</td>
					<td >
						<select name="dummy" value="*" id="dummy"><option value="*">..(All)..</option></select>
						<insta:selectdb table="department" valuecol="dept_id" displaycol="dept_name" name="dept_name" dummyvalue="(All)" id="dept_name" style="display:none"  onchange="getFilterValue(this.value)" />
						<insta:selectdb  table="doctors" valuecol="doctor_id" displaycol="doctor_name" name="doctor" dummyvalue="(All)" id="doctor" style="display:none"  onchange="getFilterValue(this.value)"/>
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

				  </tr>
			</table>

		</form>
	</body>
</html>

