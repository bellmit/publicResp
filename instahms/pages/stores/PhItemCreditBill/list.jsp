<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title><insta:ltext key="salesissues.pendingbillscolection.details.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<insta:js-bundle prefix="billing.salucro"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var popurl = '${pageContext.request.contextPath}';
		var doctor = '${ifn:cleanJavaScript(param._doctor)}';
		var receiptNo = '${ifn:cleanJavaScript(param._receiptNo)}';
		var billNo = '${ifn:cleanJavaScript(param._billNo)}';
		var customerId = '${ifn:cleanJavaScript(param._customerId)}';
		var paymentType = '${ifn:cleanJavaScript(param._paymentType)}';
		var printType = '${ifn:cleanJavaScript(param._printType)}';
	</script>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/pend_crdt_bills.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>

	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<jsp:include page="/pages/Common/BillNoPrefix.jsp" />

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty billList}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="S" value="Settled"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<body onload="init(); showFilterActive(document.pendingCrdtBillSearchForm); ajaxForPrintUrls();filterPaymentModes();">
<c:set var="visitid">
<insta:ltext key="salesissues.pendingbillscolection.details.visitid"/>
</c:set>
<c:set var="hosp.billno">
<insta:ltext key="salesissues.pendingbillscolection.details.hosp.billno"/>
</c:set>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="patientname">
<insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var="statusOptions">
<insta:ltext key="salesissues.pendingbillscolection.details.open"/>,
<insta:ltext key="salesissues.pendingbillscolection.details.finalized"/>,
<insta:ltext key="salesissues.pendingbillscolection.details.settled"/>,
<insta:ltext key="salesissues.pendingbillscolection.details.closed"/>,
<insta:ltext key="salesissues.pendingbillscolection.details.cancelled"/>
</c:set>

<h1><insta:ltext key="salesissues.pendingbillscolection.details.pendingcreditbills"/></h1>

<insta:feedback-panel/>

<form name="pendingCrdtBillSearchForm" method="GET">
	<input type="hidden" name="_method" value="getCreditBillsList">
	<input type="hidden" name="_searchMethod" value="getCreditBillsList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="pendingCrdtBillSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.pendingbillscolection.details.billno"/></div>
					<div class="sboFieldInput">
						<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}" onkeypress="onKeyPressBillNo(event);"onblur="onChangeBillNo()">
					</div>
		    </div>
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/></div>
					<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no"  id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
							    <div id="mrnoContainer" style="width: 300px"></div>
					        </div>
			        </div>
		    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="ui.label.patient.name"/></div>
						<div class="sfField">
							<input type="text" name="patient_name"  value="${ifn:cleanHtmlAttribute(param.patient_name)}"/>
								<input type="hidden" name="patient_name@op" value="ilike" />
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pendingbillscolection.details.billstatus"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,F,S,C,X" optexts="${statusOptions}"/>
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
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="visit_id" title="${visitid}"/>
			    <insta:sortablecolumn name="patient_name" title="${patientname}"/>
				<insta:sortablecolumn name="bill_no" title="${hosp.billno}"/>
				<th><insta:ltext key="salesissues.pendingbillscolection.details.status"/></th>
			</tr>
            <c:forEach var="bill" items="${billList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{billno:'${bill.bill_no}',visitid:'${bill.visit_id}',mr_no:'${bill.mr_no}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${bill.mr_no}</td>
					<td>${bill.visit_id}</td>
					<td>${bill.patient_name}</td>
					<td>${bill.bill_no}</td>
					<td>${statusDisplay[bill.status]}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getCreditBillsList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

</form>
</body>
</html>