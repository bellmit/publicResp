<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="salesissues.pendingbilllists.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="billing.billlist"/>
	
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
	</script>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/pending_sales_bills.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty billList}"/>

<body onload="init(); showFilterActive(document.pendingSalesSearchForm); ajaxForPrintUrls();">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="billno">
<insta:ltext key="salesissues.pendingbilllists.list.billno"/>
</c:set>
<c:set var="saledate">
<insta:ltext key="salesissues.pendingbilllists.list.saledate"/>
</c:set>
<h1><insta:ltext key="salesissues.pendingbilllists.list.pendingbilllists"/></h1>

<insta:feedback-panel/>

<form name="pendingSalesSearchForm" method="GET">
	<input type="hidden" name="_method" value="getPendingSaleBillsList">
	<input type="hidden" name="_searchMethod" value="getPendingSaleBillsList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="pendingSalesSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.pendingbilllists.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.pendingbilllists.list.pharmacybillno"/></div>
					<div class="sboFieldInput">
						<input type="text" name="sale_id" value="${ifn:cleanHtmlAttribute(param.sale_id)}" >
					</div>
		    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pendingbilllists.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="sale_date" id="sale_date0" value="${paramValues.sale_date[0]}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pendingbilllists.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="sale_date" id="sale_date1" value="${paramValues.sale_date[1]}"/>
							<input type="hidden" name="sale_date@op" value="ge,le"/>
							<input type="hidden" name="sale_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="ui.label.patient.name"/></div>
						<div class="sfField">
							<input type="text" name="patient_full_name"  value="${ifn:cleanHtmlAttribute(param.patient_full_name)}"/>
								<input type="hidden" name="patient_full_name@op" value="ilike" />
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
				<insta:sortablecolumn name="sale_id" title="${billno}"/>
				<insta:sortablecolumn name="sale_date" title="${saledate}"/>
				<th><insta:ltext key="salesissues.pendingbilllists.list.visitid"/></th>
				<th><insta:ltext key="ui.label.patient.name"/></th>
			</tr>
            <c:forEach var="sale" items="${billList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{saleId:'${sale.sale_id}',billNo:'${sale.bill_no}',
						 visitId:'${sale.visit_id}', mrno:'${sale.mr_no}',saleType:'${sale.type}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${sale.mr_no}</td>
					<td>${sale.sale_id}</td>
					<td><fmt:formatDate value="${sale.sale_date}" pattern="dd-MM-yyyy"/></td>
					<td>${sale.visit_id}</td>
					<td>${sale.patient_full_name}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getPendingSaleBillsList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

</form>
</body>
</html>