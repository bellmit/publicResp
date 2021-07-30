<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Paid Payments Report - Insta HMS</title>
	<insta:link type="script" file="/payments/paidpayments.js"/>
</head>

<html>
	<body onload="init()">
		<div class="pageHeader">Paid Payments Report</div>
		<form name="inputform" method="GET" action="${cpath}/payment/PaidPayments.do"/>
			<input type="hidden" name="method" value="paidPaymentsReport">
			<input type="hidden" name="methodPdf" value="paidPaymentsReport">
			<input type="hidden" name="format" value="screen">
			<input type="hidden" name="docFilter" value="all">
			<input type="hidden" id="payeeTypeValues" name="payeeTypeValues" value="">
			<c:set var="payee" value="${requestScope.payeeList}"/>

			<div class="tipText">
				This report gives the paid payments of conducting doctor / prescribing doctor / referral payments/ out house payments / supplier payments/ miscellaneous payments and payment reversals .
			</div>

			<table align="center">
				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>

				<tr>
					<td>Group By:</td>
					<td>
							<select name="groupBy">
								<option selected value="">...Select...</option>
								<c:forEach var="field" items="${fieldNames}">
								<option value="${field}">${fieldDisplayNamesMap[field]}</option>
								</c:forEach>
					</td>
				</tr>
					<tr>
							<td>Counter By:</td>
							<td>
									 <insta:selectoptions name="counter" value="All" opvalues="*,${billingcounterId},${pharmacyCounterId}" optexts="..(All)..,${billingcounterName},${pharmacyCounterName}"/>
							</td>
					</tr>
				<tr>
						<td>Filter By:</td>
						<td>
							<select name="filterBy" onchange="onChangeFilterBy()">
									<option selected value="">...(No Filter)...</option>
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
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="P" onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button>
					</td>
				</tr>
			</table>
		</form>
		<script>
			var gGroupList = {};
			gGroupList.payeename={list:${payeeList},addNull:false, column:"payee_name"};
			gGroupList.payment_type_names = {column:"type", addNull:false, list:[
			{type:"Miscellaneous Payments"},
			{type:"Condcucting Doctor Payments"},
			{type:"Referral Doctor Payments"},
			{type:"Prescribed Doctor Payments"},
			{type:"OutHouse Payments"},
			{type:"Supplier Payments"}
			]};

			gGroupList.voucher_category = {column:"type", addNull:false, list:[
			{type:"Payments"},
			{type:"Payment Reversal"}
			]};

			gGroupList.payment_mode_name = {column:"type", addNull:false, list:[
			{type:"(All)"},
			{type:"Cash"},
			{type:"Cheque"},
			{type:"Demand Draft"},
			{type:"Credit Card"},
			{type:"Debit Card"}
			]};


		</script>
	</body>
</html>


