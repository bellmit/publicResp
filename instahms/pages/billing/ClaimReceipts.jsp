<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Claim Receipts</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/claim_receipts.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
<script>
	var companyList = ${insCompList};
	var categoryList = ${insCategoryList};
	var tpaList = ${tpaList};
</script>
</head>

<jsp:useBean id="modeDisplay" class="java.util.HashMap"/>
<c:set target="${modeDisplay}" property="Q" value="Cheque"/>
<c:set target="${modeDisplay}" property="D" value="Draft"/>

<c:set var="method_name" value= "getScreen"/>
<c:set var="receiptList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty receiptList}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>
<body onload="init();">

<div class="pageHeader">Claim Receipts</div>

<insta:feedback-panel/>

<form name="ReceiptSearchForm" method="GET">
	<input type="hidden" name="_method" value="getClaimReceipts">
	<input type="hidden" name="_searchMethod" value="getClaimReceipts"/>
	<insta:search form="ReceiptSearchForm" optionsId="optionalFilter" closed="${hasResults}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel">Receipt No:</div>
			<div class="sboFieldInput">
				<input type="text" name="receipt_no" id="receipt_no" value="${ifn:cleanHtmlAttribute(param.receipt_no)}"/>
			</div>
		</div>
	</div>

<div id="optionalFilter" style="clear: both; display : ${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Receipt Date:</div>
				<div class="sfField">
					<div class="sfFieldSub">From:</div>
					<insta:datewidget name="display_date" id="display_date0" value="${paramValues.display_date[0]}"/>
				</div>
				<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="display_date" id="display_date1" value="${paramValues.display_date[1]}"/>
						<input type="hidden" name="display_date@op" value="ge,le"/>
						<input type="hidden" name="display_date@cast" value="y"/>
				</div>
				<div class="sfLabel">Counter:</div>
				<div class="sfField">
					<insta:selectdb name="counter" table="counters"  dummyvalue="ALL COUNTERS"
					valuecol="counter_id" displaycol="counter_no" value="${param.counter}" />
				</div>
			</td>
			<td>
				<div class="sfLabel">Counter Type</div>
				<div class="sfField">
					<insta:checkgroup name="counter_type" selValues="${paramValues.counter_type}"
					opvalues="B,P" optexts="Billing counter,Pharmacy counter"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Ins. Company Name:</div>
				<div class="sfField">
					<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
				</div>
			</td>
			<td>
				
				<%-- Ins30: Corporate Insurance Begin --%>
				<div class="sfLabel">Sponsor Name:</div>
				<%--Ins30: Corporate Insurance End --%>
				
				<%-- Ins30: Corporate Insurance Begin
				<c:choose>
					<c:when test="${corpInsurance eq 'Y'}">
						<div class="sfLabel">Sponsor Name:</div>
					</c:when>
					<c:otherwise>
						<div class="sfLabel">TPA Name:</div>
					</c:otherwise>
				</c:choose>
				Ins30: Corporate Insurance End --%>
				
				<div class="sfField">
					<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA();"/>
				</div>
			</td>
		</tr>
	</table>
</div>
</insta:search>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
<c:choose>
<c:when test="${empty (receiptList) && (param._method!=method_name)}">
	<insta:noresults hasResults="${hasResults}"/>
</c:when>
<c:otherwise>

	<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
		onmouseover="hideToolBar('');">
		<tr>
			<insta:sortablecolumn name="receipt_no" title="Receipt No"/>

			<%-- Ins30: Corporate Insurance Begin --%>
			<insta:sortablecolumn name="insurance_co_name" title="Ins. Company Name"/>
			<%--Ins30: Corporate Insurance End --%>
				
			<%-- Ins30: Corporate Insurance Begin
			<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
					<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
				</c:when>
				<c:otherwise>
					<insta:sortablecolumn name="tpa_name" title="TPA Name"/>
				</c:otherwise>
			</c:choose>
			Ins30: Corporate Insurance End --%>
			
			<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
			<insta:sortablecolumn name="display_date" title="Receipt Date"/>
			<insta:sortablecolumn name="amount" title="Amount"/>
			<insta:sortablecolumn name="payment_mode" title="Mode"/>
			<insta:sortablecolumn name="bank_name" title="Bank"/>
			<insta:sortablecolumn name="counter_no" title="Counter"/>
			<th>User Name</th>
			<th>Remittance Total Amt.</th>
		</tr>

		<c:forEach var="r" items="${receiptList}" varStatus="st">
			<c:set var="i" value="${st.index}"/>
			<c:set var="flagColor" value="empty"/>

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
				{ receipt_no:'${r.receipt_no}'},
				[true])" onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
				<td>${r.receipt_no}</td>
				<td>${r.insurance_co_name}</td>
				<td>${r.tpa_name}</td>
				<td><fmt:formatDate value="${r.display_date}" pattern="dd-MM-yyyy"/></td>
				<td style="text-align: right">${r.amount}</td>
				<td>${r.payment_mode}</td>
				<td><insta:truncLabel value="${r.bank_name}" length="30"/></td>
				<td>${r.counter_no}</td>
				<td>${r.username}</td>
				<td style="text-align: right">${r.remittance_total_amount}</td>
			</tr>
		</c:forEach>
	</table>
</c:otherwise>
</c:choose>

<c:if test="${not empty billingcounterId || not empty pharmacyCounterId}">
<div class="screenActions">
	<a href="./claimReceipts.do?_method=add">New Receipt</a>
</div>
</c:if>

<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
	<div class="flag"><img src="${cpath}/images/empty_flag.gif"/></div>
	<div class="flagText">Receipt</div>
</div>
</body>
</html>

