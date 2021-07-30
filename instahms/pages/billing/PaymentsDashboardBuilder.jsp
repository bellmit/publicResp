<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Payments Summary Report - Insta HMS</title>
	<insta:link type="script" file="billing/paymentbuilder.js"/>

</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Payments Tabular Summary Report</div>
		<form name="inputform" method="GET" action="${cpath}/billing/PaymentsReport.do">
			<input type="hidden" name="method" value="paymentsDashboardReport">
			<input type="hidden" name="format" value="screen" id="format">
			<input type="hidden" name="docFilter" value="all">
			<input type="hidden" id="docName" name="docName" value=""/>

			<div class="tipText">
				This report gives you a tabular summary of the Doctor Payments between two dates.
			</div>
			<table align="center">
				<tr>
					<td colspan="2">Select the type of output:</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="sum" name="reportType" onchange="onChangeReportType()" value="dashboard"
						checked>
						<label for="sum">Tabular Summary: total payments against each doctor/referral in the Group By</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="bill" name="reportType" onchange="onChangeReportType()" value="summary">
						<label for="bill">Charge-wise: payments against each bill to doctors(no preview)</label>
					</td>
				</tr>

				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
				<tr>

				</tr>
				<tr>
					<td colspan="2" style="padding-top: 1em;">Select the grouping criteria and the filter criteria:</td>
				</tr>
			</table>

			<table align="center">
				<tr>
					<td>Report Type:</td>
					<td>
					<select name="groupBy" onchange="onChangeReportType()">
						<option value="">...Select...</option>
						<option value="All">All Eligible Payments</option>
						<option value="doctor_name">Payment Eligible Not Posted</option>
						<option value="payment_id">Payment Posted Not Paid</option>
						<option value="voucher_no">Paid Payments</option>
					</select>
				</td>
				</tr>
				<tr>
					<td>Filter By:</td>
					<td>
						<select name="filterBy" onchange="onChangeFilterBy()">
							<option value="">All</option>
							<option value="doctor_id">Conducting Doctor Charges</option>
							<option value="reference_docto_id">Referral Doctor Charges</option>
							<option value="prescribing_dr_id">Prescribing Doctor Charges</option>
						</select>
					</td>
					<td> =</td>
					<td>
						<select name="filterValue" id="filterValue">
							<option value="*">..(All)..</option>
						</select>

					</td>
				</tr>
				<tr>
					<td>Insurance Filter :</td>
					<td>
						<input type="checkbox" name="insurance" id="insurance" checked="checked" onclick="setInsuranceFilter()">Insurance
						<input type="checkbox" name="nonInsurance" id="nonInsurance" checked="checked" onclick="setInsuranceFilter()">Non Insurance
					</td>

				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<button type="submit" id="viewButton" accesskey="V" onclick="return onSubmit('screen')">
						Pre<b><u>v</u></b>iew</button>
					</td>
					<td>
						<button type="submit" accesskey="P" onclick="return onSubmit('pdf')">
						<b><u>P</u></b>rint</button>
					</td>
					<td>
						<button type="submit" id="exportButton" accesskey="E" onclick="return onSubmit('csv')">
						<b><u>E</u></b>xport to CSV</button>
					</td>
				</tr>
			</table>
			<script>
				var jDocList = ${allDoctorsJSON};
				var jRefDocList = ${refdoctorJSON};
				var jPresDocList = ${presDoctorJSON};
			</script>

		</form>
	</body>
</html>


