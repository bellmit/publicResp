<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Revenue Detail Reports - Insta HMS</title>
	<insta:link type="script" file="billing/revenue_report.js"/>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Revenue Report</div>
		<form name="inputform" method="GET" action="${cpath}/billing/RevenueReport.do" target="_blank">
			<input type="hidden" name="method" value="showReport">
			<input type="hidden" name="format" value="pdf">
			<input type="hidden" name="groupByName" value="">

			<div class="tipText">
				This report shows the revenue recognized through bills between the chosen dates.
				The Group By can be used to split the revenue, or group it according to each item
				in that group.
				Only finalized bills are considered in this report.
			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select the type of output:</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="sum" name="reportType" onchange="onChangeReportType()" value="summary"
							checked>
						<label for="sum">Tabular Summary: total revenue against each item in the Group By</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="bill" name="reportType" onchange="onChangeReportType()" value="billWise">
						<label for="bill">Bill-wise: billed amount against each bill (no preview)</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="item" name="reportType" onchange="onChangeReportType()" value="itemWise">
						<label for="item">Item-wise: amount against each charge item (no preview)</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="csv" name="reportType" onchange="onChangeReportType()" value="csv">
						<label for="csv">Spreadsheet: all charge item details as CSV/Spreadsheet (no preview)</label>
				</tr>
				<tr>
					<td colspan="2" style="padding-bottom: 1em">
						<input type="radio" id="trend" name="reportType" onchange="onChangeReportType()" value="trend">
						<label for="trend">Trend: total revenue against each item in Group By, trend by:</label>
						<select name="trendPeriod" style="width: 6em">
							<option value="day" selected>Day</option>
							<option value="week">Week</option>
							<option value="month">Month</option>
						</select>
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
							<option selected value="">--Select--</option>
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
								<c:if test="${field != 'patient_name_id' && field != 'act_description'}">
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
								<c:if test="${field != 'patient_name_id' && field != 'act_description'}">
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
						<input id="viewButton" type="submit" value="Preview" onclick="return onSubmit('screen')">
					</td>
					<td>
						<input type="submit" value="Print" onclick="return onSubmit('pdf')">
					</td>
				</tr>
			</table>
		</form>

		<script>
			var gGroupList = {};
			gGroupList.visit_type_name = {list: ${visitTypesJSON}, column: "visit_type_name", addNull: false};
			gGroupList.chargehead_name = {list: ${chargeHeadsJSON}, column: "chargehead_name", addNull: false};
			gGroupList.chargegroup_name = {list: ${chargeGroupsJSON}, column: "chargegroup_name", addNull: false};
			gGroupList.ac_head = {list: ${accountHeadsJSON}, column: "account_head_name", addNull: false};
			gGroupList.account_group_name={list: ${accountGroupsJSON}, column: "account_group_name", addNull: false};
			gGroupList.dept_name = {list: ${departmentsJSON}, column: "dept_name", addNull: true};
			gGroupList.store_name = {list: ${storesJSON}, column: "store_name", addNull: false};
			gGroupList.doctor_name = {list: ${doctorsJSON}, column: "doctor_name", addNull: true};
			gGroupList.conducting_doctor = {list: ${doctorsJSON}, column: "doctor_name", addNull: true};
			gGroupList.referer = {list: ${referersJSON}, column: "referer_name", addNull: true};
			gGroupList.ward_name = {list: ${wardsJSON}, column: "ward_name", addNull: true};
			gGroupList.tpa_name = {list: ${tpasJSON}, column: "tpa_name", addNull: true};
			gGroupList.org_name = {list: ${orgsJSON}, column: "org_name", addNull: true};
			gGroupList.category_name = {list: ${categoriesJSON}, column: "category_name", addNull: true};
			gGroupList.unit_name = {list: ${unitsJSON}, column: "unit_name", addNull: true};
			gGroupList.bill_type = {column:"type", addNull:false, column2:"btype", list:[
				{type:"Bill Now", btype:"P"},
				{type:"Bill Later", btype:"C"},
				{type:"Pharmacy Bill", btype:"M"}
			]};
		</script>
	</body>
</html>


