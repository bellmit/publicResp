<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.ComplaintsLog.ComplaintsLogDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Complaints Log - Insta HMS</title>


	<script>
		function csvReport() {
			document.inputform.action="${cpath}/master/ComplaintsLog/ComplaintsLogReport.do"
			document.inputform.submit();
		}
	</script>
</head>


	<body>
		<div class="pageHeader">Complaints Log Report</div>
		<form name="inputform" method="GET" target="_blank"
			action="${cpath}/DirectReport.do">
			<input type="hidden" name="method" value="complaintsLogExportCSV">

			<div class="infoPanel">
				<div class="img"><img src="${cpath}/images/information.png"/></div>
				<div class="txt">This report gives a list of all the complaints for the selected date period based on the updated date</div>
				<div style="clear: both"></div>
			</div>

			<fieldset class="fieldSetBorder">
				<table class="formtable">
					<tr>
						<td colspan="2">Select a date range for the report</td>
					</tr>
					<tr>
						<td >
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
						<td style="white-space:nowrap;">
							From: <insta:datewidget name="fromDate" value="today"/><br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;
							To:<insta:datewidget name="toDate" value="today"/>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					</tr>
					<tr>
						<td class="formlabel">Status:</td>
						<td>
							<select name="complaint_status" class="dropdown">
								<option value="*">All</option>
								<option value="Open">Open</option>
								<option value="Clarify">Clarify</option>
								<option value="Pending">Pending</option>
								<option value="Fixed">Fixed</option>
								<option value="NotInScope">Not In Scope</option>
								<option value="ProdEnh">Prod Enh</option>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</fieldset>

			<table class="screenActions">
				<tr>
					<td>
						<button type="submit" accesskey="E"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate),csvReport();">
							<b><u>E</u></b>xport to CSV</button>
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>

