<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
		<head>
			<title>Payment Voucher Screen - Insta HMS</title>
			<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >

			<insta:link type="js" file="widgets.js" />
			<insta:link type="js" file="ajax.js"/>
			<insta:link type="css" file="widgets.css"/>
			<insta:link type="js" file="dashboardsearch.js" />
			<insta:link type="js" file="payments/paymentvoucher.js"/>
		</head>

		<body onload="init();hidePaymentModeForPaymentVoucher();">
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<c:set var="payee" value="${requestScope.payeeList}"/>
		<c:set var="voucherlist" value="${pagedList.dtoList}"/>
		<c:set var="printVoucherBreakup" value="${cpath}/pages/payments/PaymentDashboard.do?method=printVoucherBreakup"/>
		<div><!-${vouchers}-></div>
		<c:set var="actionUrl"
		value="/pages/payments/PaymentVoucher.do"/>
		<form method="GET" action="${cpath}${actionUrl}" name="PaidPaymentForm">
		<c:set var="actionMethod" value="getPaymentVouchers"/>
			<input type="hidden" name="_method" value="${actionMethod}"/>
			<input type="hidden" name="_searchMethod" value="${actionMethod}"/>
			<input type="hidden" name="_screen" value="${ifn:cleanHtmlAttribute(screen)}"/>
			<input type="hidden" name="sortOrder" value="voucher_no" />
				<h1>	${ifn:cleanHtml(screen)} Vouchers</h1>
			<c:set var="hasResult" value="${not empty voucherlist}"/>
			<insta:search form="PaidPaymentForm" optionsId="optionalFilter" closed="${hasResult}"
			validateFunction="validateForm()" clearFunction="clearPayeeName">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Payee Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="_payeeName" id="payeesName" value="${ifn:cleanHtmlAttribute(param._payeeName)}"/>
						<div id="payeeListContainer"/>
					</div>
				</div>
				<input type="hidden" name="payee_name" id="payeeId" value="${ifn:cleanHtmlAttribute(param.payee_name)}"/>
			</div>
			<div id="optionalFilter" style="clear: both ; display :${hasResult ? 'none': 'block'}">
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Payment Category:</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:selectoptions name="voucher_category" opvalues="P,R" dummyvalue="Select Category"
									value="${param.voucher_category}"	optexts="Payments,Payment Reversal" />
								</div>
							</div>
						</td>
						<td>
								<div class="sfLabel">Payment Type:</div>
								<div class="sfField">
									 <insta:checkgroup name="payment_type" selValues="${paramValues.payment_type}" opvalues="D,P,F,O,S,C" optexts="Doctor,Prescribing Doctor,Referral Doctor,Outgoing Tests, Supplier,Miscellaneous"/>
								</div>
						</td>
						<td class="last">
								<div class="sfLabel">Date:</div>
								<div class="sfField">
									<div class="sfFieldsub">From</div>
									<insta:datewidget name="date" id="date0" value="${paramValues.date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To</div>
									<insta:datewidget name="date" id="date1" value="${paramValues.date[1]}"/>
									<input type="hidden" name="date@op" value="ge,le" id="date@op"/>
									<input type="hidden" name="date@cast" value="y" id="date@cast"/>
								</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
			</insta:search>
			<insta:paginate numPages="${pagedList.numPages}" curPage="${pagedList.pageNumber}" pageNumParam="pageNum" totalRecords="${pagedList.totalRecords}"/>
			<div>
				<c:choose>
					<c:when test="${not empty voucherlist}">
					<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable">
						<tr onmouseout="hideToolBar()">
							<th>#</th>
							<insta:sortablecolumn name="voucher_no" title="Voucher No"/>
							<insta:sortablecolumn name="payee_name" title="Payee Name" />
							<th class="number">Amount</th>
							<insta:sortablecolumn name="date" title="Paid Date" />
							<th>Payment Mode</th>
						</tr>
						<c:forEach var="v" items="${voucherlist}" varStatus="st">
						<tr class="${st.index == 0 ? 'firstRow': ''} ${st.index % 2 == 0 ?'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{voucherno: '${v.map.voucher_no}', voucher_no:'${v.map.voucher_no}', screen: '${ifn:cleanJavaScript(screen)}'})" onmouseover="hideToolBar(${st.index})"
							id ="toolbarRow${st.index}">
							<td>${st.index+1}</td>
							<td>${v.map.voucher_no}</td>
							<td>${v.map.payee_name}</td>
							<td class="number">${v.map.amount}</td>
							<td><fmt:formatDate value="${v.map.date}" pattern="dd-MM-yyyy"/></td>
							<td>${v.map.payment_mode}</td>
						</tr>
						</c:forEach>
					</table>
					</c:when>
				</c:choose>
				<c:if test="${not empty voucherlist}">
						 <table class="screenActions">
							 <tr>
								 <td>
									 <button type="button" name="ExportCSV" accesskey="E" class="button" onclick="return exportToCSV();"/>
									 	<b><u>E</u></b>xport To Csv</button>
									 <button type="button" name="print" accesskey="P"  class="button" onclick="return getVoucherReport()"/>
									 	<b><u>P</u></b>rint</button>
								 </td>
							 </tr>
						 </table>
				 </c:if>
							</div>
							</form>
							<script>
									var screenType ='${ifn:cleanJavaScript(screen)}';
									var payeeNamesList = ${payeeList};
							</script>
						</body>
</html>
