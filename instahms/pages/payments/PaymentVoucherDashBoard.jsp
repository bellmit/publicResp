<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
	<head>
		<title>Payment Vouchers List - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="dashboardsearch.js" />
		<insta:link type="js" file="payments/paymentdashboard.js" />
	</head>
	<body onload="init(), printVoucher('${ifn:cleanJavaScript(voucherNo)}','${ifn:cleanJavaScript(printType)}','${ifn:cleanJavaScript(paymentType)}');">
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<c:set var="payee" value="${requestScope.payeeList}"/>
		<c:set var="voucherList" value="${paymentDues.dtoList}"/>
		<div class="pageHeader">${ifn:cleanHtml(screen)} Voucher Dashboard</div>
		<div>
				<insta:feedback-panel/>
		</div>

		<c:set var="actionUrl"
			value="/pages/payments/PaymentDashboard.do"/>
		<form method="GET" action="${cpath}${actionUrl}" name="VoucherDashboard">
				<input type="hidden" name="_method" value="getPaymentDues"/>
				<input type="hidden" name="_searchMethod" value="getPaymentDues"/>
				<c:set var="hasResult" value="${not empty voucherList}"/>
				<insta:search form="VoucherDashboard" optionsId="optionalFilter" closed="${hasResult}" clearFunction="clearPayeeName">
				<div class="searchBasicOpts">
					<div class="sboField">
							<div class="sboFieldLabel">Payee Name:</div>
							<div class="sboFieldInput">
									<input type="text" name="_payeeName" id="payeesName" value="${ifn:cleanHtmlAttribute(param._payeeName)}"/>
									<input type="hidden" name="payee_name" id="payeeId" value="${ifn:cleanHtmlAttribute(param.payee_name)}"/>
									<div id="payeeNameContainer" class="autoComplete"/>
							</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear:both; display: ${hasResult ? 'none': 'block'}">
					<table class="searchFormTable" >
						<tr>
							<td>
									<div class="sfLabel" style="width:180px">Payment Type:</div>
									<div class="sfField">
										<insta:checkgroup name="payment_type" selValues="${paramValues.payment_type}" opvalues="D,P,R,F,O,S,C" optexts="Doctor,Prescribing Doctor,Hospital Referral Doctor,Outside Referral Doctor ,Outgoing Tests, Supplier,Miscellaneous"/>
									</div>
								</td>
								<td>
									<div class="sfLabel">Date:</div>
									<div class="sfField">
										<div class="sfFieldSub">From:</div>
										<insta:datewidget name="posted_date" valid="past" id="posted_date0" btnPos="left"  value="${paramValues.posted_date[0]}"/>
									</div>
									<div class="sfField">
										<div class="sfFieldSub">To:</div>
										  <insta:datewidget name="posted_date" valid="past" id="posted_date1" btnPos="left"  value="${paramValues.posted_date[1]}"/>
											<input type="hidden" name="posted_date@op" value="ge,le"/>
											<input type="hidden" name="posted_date@cast" value="y"/>
									</div>
								</td>
								<td>
									<div class="sfLabel">Description</div>
									<div class="sfField">
										<input type="text" name="description" id="description" value="${ifn:cleanHtmlAttribute(param.description)}"/>
										<div id="descriptionDiv" />
									</div>
								</div>
								</td>
								<td class="last">
									<div class="sfLabel">Category</div>
									<div class="sfField">
									<input type="text" name="category" id="category" value="${ifn:cleanHtmlAttribute(param.category)}"/>
									<div id="categoryDiv" />
									</div>
								</div>

								</td>
								<td class="last">&nbsp;</td>
							</tr>
					</table>
				</div>
				</insta:search>
				<c:set var="centerId" value="${sessionScope['centerId']}" />
				<insta:paginate curPage="${paymentDues.pageNumber}" numPages="${paymentDues.numPages}"	pageNumParam="pageNum" totalRecords="${paymentDues.totalRecords}"/>
				<c:choose>
					<c:when test ="${not empty voucherList}" >
					<table class="resultList" cellpadding="0" cellspacing="0" width="100%" align="center"
						id="resultTable">
								<tr onmouseover="hideToolBar();">
										<th>#</th>
										<th>Payee Name</th>
										<c:if test="${centerId eq 0}"><th>Center</th></c:if>
										<th>Payment Type</th>
										<insta:sortablecolumn name="posted_date" title="Posted Date"/>
										<th>Description</th>
										<th>Category</th>
										<th class="number">Amount</th>
								</tr>
								<c:forEach var="p" items="${voucherList}" varStatus="st">
									<c:set var="paymentType" value="${p.map.payment_type}"/>
								<tr class="${st.index == 0 ? 'firstRow' :''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
										onclick="showToolbar(${st.index}, event, 'resultTable', {payeeId: '${p.map.payeeid}',paymentType : '${p.map.payment_type}',screen: '${ifn:cleanJavaScript(screen)}', totalAmount: '${p.map.amount}'},
											[	${(urlRightsMap.pay_paymentscreen_combined eq 'A' or roleId eq 1 or roleId eq 2) and (paymentType eq 'D' or paymentType eq 'R' or paymentType eq 'F' or paymentType eq 'P')},
												${(urlRightsMap.pay_paymentscreen_ip eq 'A' or roleId eq 1 or roleId eq 2) and (paymentType eq 'D' or paymentType eq 'R' or paymentType eq 'F' or paymentType eq 'P')},
												${(urlRightsMap.pay_paymentscreen_op eq 'A' or roleId eq 1 or roleId eq 2) and (paymentType eq 'D' or paymentType eq 'R' or paymentType eq 'F' or paymentType eq 'P')},
												${(urlRightsMap.pay_paymentscreen_others eq 'A' or roleId eq 1 or roleId eq 2) and (paymentType eq 'O' or paymentType eq 'S' or paymentType eq 'C')}
											])"
											onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

										<td>${st.index+1}</td>
										<td>${p.map.payee_name}</td>
										<c:if test="${centerId eq 0}"><td>${p.map.center_name}</td></c:if>
										<td>${p.map.paymenttype}</td>
										<td>
											<fmt:formatDate type="both" value="${p.map.posted_date}" pattern="dd-MM-yyyy hh:mm a"/>
										</td>
										<td>${p.map.description}</td>
										<td>${p.map.category}</td>
										<td class="number">${p.map.amount}</td>
								</tr>
								</c:forEach>
					</table>
					</c:when>
			</c:choose>
		</form>
		<script>
				var screenType='${ifn:cleanJavaScript(screen)}';
				var payeeNamesList =${payeeList};
				var contextPath = '${cpath}';
		</script>
	</body>
</html>
