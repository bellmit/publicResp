<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Pharmacy Purchase (by Invoice) Report Builder - Insta HMS</title>
	<insta:link type="script" file="pharmacy/purchase_report.js"/>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Pharmacy Purchase (by Invoice) Report Builder </div>
		<form name="inputform" method="GET" action="${cpath}/pages/pharmacy/PurchaseInvoiceReportBuilder.do" target="_blank">
			<input type="hidden" name="method" value="showReport">
			<input type="hidden" name="format" value="pdf">
			<input type="hidden" name="groupByName" value="">

			<div class="tipText">
				This report builder allows you to to generate various reports that show the Pharmacy Purchases
				based on Invoices whose Invoice date is between the chosen dates.
				This includes debit notes but does not include returns and replacements.
			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select the type of output:</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="item" name="reportType" onchange="onChangeReportType()" value="itemWise"
							checked>
						<label for="sum">Detailed: details of each invoice, including items purchased. (no preview)</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="bill" name="reportType" onchange="onChangeReportType()" value="invoiceWise">
						<label for="bill">Invoice-wise: one row per invoice (no preview)</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="sum" name="reportType" onchange="onChangeReportType()" value="summary">
						<label for="sum">Tabular Summary: total amounts against each item in Group By</label>
			    </td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="csv" name="reportType" onchange="onChangeReportType()" value="csv">
						<label for="csv">Spreadsheet: invoice wise details output as CSV/Spreadsheet (no preview)</label>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="trend" name="reportType" onchange="onChangeReportType()" value="trend">
						<label for="trend">Trend: total invoice count and amounts against each item in Group By, trend by:</label>
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
					<td colspan="2" style="padding-top: 1em">Select the grouping criteria and the filter criteria and invoice due date criteria:</td>
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
					<td align="right">
				       <label>Invoice Due Date:From:</label>
				    </td>
				    <td>
					    <insta:datewidget name="due_fromDate" valid="" value="" btnPos="left"/>
					</td>
   					<td align="left">
						<label>To:</label>
						<insta:datewidget name="due_toDate" valid="" value="" btnPos="left"/>
					</td>
				</tr>
				<tr>
					<td align="right">Filter By:</td>
					<td>
						<select name="filterBy" onchange="onChangeFilterBy()">
							<option selected value="">(No Filter)</option>
							<c:forEach var="field" items="${fieldNames}">
								<option value="${field}">${fieldDisplayNamesMap[field]}</option>
							</c:forEach>
						</select>
					</td>
					<td>=
						<select name="filterValue">
							<option value="*">..(All)..</option>
						</select>
					</td>
					</tr>

					</table>

				<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em"/>
					</td>
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
			gGroupList.supplier_name = {list: ${supplierNamesJSON}, column: "supplier_name", addNull: false};
			gGroupList.invoice_status = {column:"type", addNull:false, list:[
				{type:"Open"},
				{type:"Finalized"},
				{type:"Closed"}
				]};
			gGroupList.purchase_type = {column:"type", addNull:false, list:[
				{type:"Purchase"},
				{type:"Debit Note"}
				]};
			gGroupList.tax_name = {column:"type", addNull:false, list:[
				{type:"VAT"},
				{type:"CST"}
				]};


		</script>

		</body>
  </html>
