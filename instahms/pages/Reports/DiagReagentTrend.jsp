<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Reagent Usage Report- Insta HMS</title>



	<script type="text/javascript">
	function setOrgname(){
		document.forms[0].org_name.value = document.forms[0].orgId.options[document.forms[0].orgId.selectedIndex].text;
	}
	</script>
</head>

<html>
	<body >
		<div class="pageHeader">Reagent Usage Report</div>
		<form name="inputform" method="GET" target="_blank" action="${cpath}/DirectReport.do" onsubmit="setOrgname();">
			<input type="hidden" name="report" value="ReagentUsageReport">

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
			<td>Select Desired Rate plan</td>
			<td><insta:selectdb name="orgId" table="Organization_Details" valuecol="org_id"
										displaycol="org_name" filtered="true" filtercol="status" filtervalue="A"/></td>
				<input type="hidden" name="org_name" id="org_name"/></tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<input type="submit" value="Generate Report"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
					</td>
				</td>
			</table>

		</form>
	</body>
</html>

