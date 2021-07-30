<%@page import="com.bob.hms.common.RequestContext"%>
<%@page import="com.insta.hms.stores.PharmacymasterDAO"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<head>
	<title>Supplier Payments Report - Insta HMS</title>


</head>

<html>
	<body onload="setDateRangeFinancialYear(document.inputform.fromDate,document.inputform.toDate);">
		<div class="pageHeader">Pharmacy Supplier Payments Report</div>
		<form name="inputform"  target="_blank"	 method="get">
		<input type="hidden" name="report" value="SupplierPaymentsReport">
		<input type="hidden" name="method" value="getReport">

		<div class="tipText">

		</div>

		<table align="center">
			<tr>
				<td>Select the date type :</td>
				<td>
					<input type="radio" name="dateType" id="postedDate" value="invoice_date">Posted Date
					<input type="radio" name="dateType" id="dueDate" value="due_date">Due Date
				</td>
			</tr>
			<tr>
				<td colspan="2">Select a date range for report</td>
			</tr>
			<tr>
				<td valign="top">
					<input type="radio" name="selectDate"
						onclick="setDateRangePreviousMonth(document.inputform.fromDate,document.inputform.toDate)">
						<label>Previous Month</label>
					<br/>

					<input type="radio" name="selectDate"
						onclick="setDateRangeMonth(document.inputform.fromDate,document.inputform.toDate)">
						<label>This Month</label>
					<br/>



					<input type="radio" name="selectDate"
						onclick="setDateRangePreviousFinancialYear(document.inputform.fromDate,document.inputform.toDate)">
						<label>Previous Financial Year</label>
					<br/>

					<input type="radio" name="selectDate" checked
						onclick="setDateRangeFinancialYear(document.inputform.fromDate,document.inputform.toDate)">
						<label>This Financial Year</label>
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

			<tr>
			<td>Supplier Name</td>
			<td align="right"><insta:selectdb name="supplier" table="supplier_master" 
			valuecol="supplier_code" displaycol="supplier_name" dummyvalue="All"></insta:selectdb>
			</tr>

			<tr>
			<td>Invoice Status</td>
			<td align="right"><select name="invstatausArray" multiple="multiple">
				<option value="O" >Open</option>
				<option value="F" >Finalized</option>
				<option value="C" >Closed</option>
			</select>
			</td>
			</tr>


		</table>
		<table align="center" style="margin-top: 1em;">
			<tr>
				<td>
					<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
				</td>
				<td>

				<button type="submit" accesskey="G" onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
				<b><u>G</u></b>enerate Report</button>

				</td>
			</tr>

		</table>

		</form>
	</body>
</html>
