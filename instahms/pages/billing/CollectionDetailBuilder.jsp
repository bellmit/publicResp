<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Collection Detail Report - Insta HMS</title>
	<insta:link type="script" file="billing/collection_report.js"/>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Collection Report</div>
		<form name="inputform" method="GET" action="${cpath}/billing/CollectionReport.do" target="_blank">
			<input type="hidden" name="method" value="showReport">
			<input type="hidden" name="format" value="pdf">
			<input type="hidden" name="groupByName" value="">

			<div class="tipText">
				This report gives you the details of all the patient related receipts/refunds between two dates.
				This excludes any payments made to doctors etc. made out of counters.
			</div>

			<table align="center">
				<tr>
					<td colspan="2"> Select the type of output:</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="sum" name="reportType" onchange="onChangeReportType()" value="summary"
						checked>
						<label for="sum">Tabular Summary: Total collection against each item in the Group By</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="bill" name="reportType" onchange="onChangeReportType()" value="detail">
						<label for="bill">Detailed: Receipt amount against each bill/patient (no preview)</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="csv" name="reportType" onchange="onChangeReportType()" value="csv">
						<label for="csv">Spreadsheet: All receipts/refunds as a CSV/Spreadsheet (no preview)</label>
				</tr>
				<tr>
					<td colspan="2" style="padding-bottom: 1em">
						<input type="radio" id="trend" name="reportType" onchange="onChangeReportType()" value="trend">
						<label for="trend">Trend: Total collection against each item in Group By, trend by:</label>
						<select name="trendPeriod" style="width: 6em">
							<option value="day" selected>Day</option>
							<option value="week">Week</option>
							<option value="month">Month</option>
						</select>
					</td>
				</tr>

				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>

				<tr>
					<td colspan="2" style="padding-top: 1em">Select the grouping criteria and the filter criteria:</td>
				</tr>
			</table>

			<table align="center">
				<tr>
					<td align="right">Group By:</td>
					<td>
						<select name="groupBy">
							<c:forEach var="field" items="${fieldNames}">
								<option value="${field}">${fieldDisplayNamesMap[field]}</option>
							</c:forEach>
						</select>
					</td>
				</tr>

				<tr>
					<td align="right">Filter By:</td>
					<td>
						<select name="filterBy" onchange="onChangeFilterBy()">
							<option selected value="">(No Filter)</option>
							<c:forEach var="field" items="${fieldNames}">
								<c:if test="${field !='bill_no'}">
										<option value="${field}">${fieldDisplayNamesMap[field]}</option>
								</c:if>
							</c:forEach>
						</select>
					</td>
					<td>=
						<select name="filterValue">
							<option value="*">..(All)..</option>
						</select>
					</td>
				</tr>

				<tr>
					<td align="right">Exclude:</td>
					<td>
						<select name="exclude" onchange="onChangeExclude()">
							<option selected value="">(None)</option>
							<c:forEach var="field" items="${fieldNames}">
								<c:if test="${field != 'bill_no'}">
										<option value="${field}">${fieldDisplayNamesMap[field]}</option>
									</c:if>
							</c:forEach>
						</select>
					</td>
					<td>=
						<select name="excludeValue">
							<option value="*">..(None)..</option>
						</select>
					</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<input type="submit" id="viewButton"value="Preview" onclick="return onSubmit('screen')">
					</td>
					<td>
						<input type="submit" value="Print" onclick="return onSubmit('pdf')">
					</td>
				</tr>
			</table>

		</form>

		<script>
			var gGroupList = {};
			gGroupList.payment_mode_name = {list: ${paymentModesJSON}, column: "payment_mode_name", addNull: true};
			gGroupList.main_type = {column: "type", addNull: false, list: [
				{type: "Receipt"},
				{type: "Refund"},
				{type: "Deposit"},
				{type: "Payments"},
				{type: "Payment Reversal"}
			]};
			gGroupList.combo_type = {column: "type", addNull: false, list: [
				{type: "Bill Later Advance"},
				{type: "Bill Later Settlement"},
				{type: "Bill Now Payment"},
				{type: "Bill Later Refund"},
				{type: "Bill Now Refund"},
				{type: "Patient Deposit"},
				{type: "Patient Deposit Refund"},
				{type: "Payments"},
				{type: "Payment Reversal"}
			]};
			gGroupList.visit_type_name = {list: ${visitTypesJSON}, column: "visit_type_name", addNull: true};
			gGroupList.counter_no = {list: ${countersJSON}, column: "counter_no", addNull: false};
			gGroupList.username = {list: ${usersJSON}, column: "emp_username", addNull: false};
			gGroupList.bill_type={column:"type", addNull: false, list:[
				{type: "Bill Now"},
				{type: "Bill Later"},
				{type: "Pharmacy Bills"}
			]};
		</script>
	</body>
</html>


