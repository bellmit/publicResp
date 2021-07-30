<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="salesissues.retailcreditpendingbills.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var popurl = '${ifn:cleanJavaScript(pageContext.request.contextPath)}';
		var doctor = '${ifn:cleanJavaScript(param._doctor)}';
		var receiptNo = '${ifn:cleanJavaScript(param._receiptNo)}';
		var billNo = '${ifn:cleanJavaScript(param._billNo)}';
		var customerId = '${ifn:cleanJavaScript(param._customerId)}';
		var paymentType = '${ifn:cleanJavaScript(param._paymentType)}';
		var no_of_credit_debit_card_digits = 0;
		
	</script>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/ret_crdt_pend_sales_bills.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	

	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<jsp:include page="/pages/Common/BillNoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty billList}"/>
<body onload="init(); showFilterActive(document.RetCrdtpendingSalesSearchForm);filterPaymentModes(); ">
<c:set var="customername">
<insta:ltext key="salesissues.retailcreditpendingbills.list.customername"/>
</c:set>
<c:set var="saledate">
<insta:ltext key="salesissues.retailcreditpendingbills.list.saledate"/>
</c:set>
<c:set var="hospbillno">
<insta:ltext key="salesissues.retailcreditpendingbills.list.hosp.billno"/>
</c:set>
<h1><insta:ltext key="salesissues.retailcreditpendingbills.list.retailcreditpendingbills"/></h1>

<insta:feedback-panel/>

<form name="RetCrdtpendingSalesSearchForm" method="GET">
	<input type="hidden" name="_method" value="getRetailPendingSaleBillsList">
	<input type="hidden" name="_searchMethod" value="getRetailPendingSaleBillsList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="RetCrdtpendingSalesSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.retailcreditpendingbills.list.billno"/></div>
					<div class="sboFieldInput">
						<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}" onkeypress="onKeyPressBillNo(event);"onblur="onChangeBillNo()">
					</div>
		    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.retailcreditpendingbills.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="open_date" id="open_date0" value="${paramValues.open_date[0]}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.retailcreditpendingbills.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="open_date" id="open_date1" value="${paramValues.open_date[1]}"/>
							<input type="hidden" name="open_date@op" value="ge,le"/>
							<input type="hidden" name="open_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.retailcreditpendingbills.list.customername"/></div>
						<div class="sfField">
							<input type="text" name="customer_name"  value="${ifn:cleanHtmlAttribute(param.customer_name)}"/>
								<input type="hidden" name="customer_name@op" value="ilike" />
					    </div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="customer_name" title="${customername}"/>
			    <th><insta:ltext key="salesissues.retailcreditpendingbills.list.phonenumber"/> </th>
			    <th><insta:ltext key="salesissues.retailcreditpendingbills.list.creditlimit"/> </th>
			    <th><insta:ltext key="salesissues.retailcreditpendingbills.list.salebillno"/></th>
			    <insta:sortablecolumn name="sale_date" title="${saledate}"/>
				<insta:sortablecolumn name="bill_no" title="${hospbillno}"/>
				<th style="text-align: right"><insta:ltext key="salesissues.retailcreditpendingbills.list.billamount"/></th>
			</tr>
            <c:forEach var="sale" items="${billList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{billno:'${sale.bill_no}',customerid:'${sale.customer_id}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${sale.customer_name}</td>
					<td>${sale.phone_no}</td>
					<td>${sale.credit_limit}</td>
					<td>
						<c:forTokens items="${sale.saleids}" delims="," var="saleId" varStatus="i">
							${ifn:cleanHtml(saleId)} ${ifn:cleanHtml(i.last ? '' :',')}
							<c:if test="${((i.index+1) % 2) == 0 && (not i.first)}">
								</br>
							</c:if>
						</c:forTokens>
					</td>
					<td>
						<c:forTokens items="${sale.sale_date}" delims="," var="saledt" varStatus="i">
						${saledt } ${i.last ? '' :',' }
						<c:if test="${((i.index+1) % 2) == 0 && (not i.first)}">
							</br>
						</c:if>
						</c:forTokens>
					 </td>
					<td> ${sale.bill_no}</td>
					<td style="text-align: right">${sale.current_due}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getRetailPendingSaleBillsList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

</form>
</body>
</html>
