<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<html>
<head>
	<title><insta:ltext key="storemgmt.stockconsumptionlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var addEditRights = '${urlRightsMap.store_stock_consumption_new}';
		var toolbar = {}
			toolbar.Edit= {
				title:toolbarOptions["editconsumptions"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'pages/stores/stockconsumtionnew.do?_method=showStockConsumption',
				onclick: null,
				description: toolbarOptions["editconsumptions"]["description"],
				show : (!empty(addEditRights) && addEditRights == 'A')
			};

		/*	Cancel: {
				title: "Cancel",
				imageSrc: "icons/Edit.png",
				href: 'pages/stores/stockconsumtion.do?_method=cancelConsumptionTransaction',
				onclick: '',
				description: "Cancel Stock Consumption Details."
			},

			Finalize: {
				title: "Finalize",
				imageSrc: "icons/Edit.png",
				href: 'pages/stores/stockconsumtion.do?_method=finalizeConsumptionDetails',
				onclick: null,
				description: "Finalize Consumption  Details/Stock Qty will be Reduced."
			}*/


		function init() {
			createToolbar(toolbar);
			if(empty(document.getElementById('store_id').value)){
				showMessage("js.stores.mgmt.usernothave.assignedstore");
				return false;
			}
		}

		function checkStore() {
			if(empty(document.stockConsumptionSearchForm.store_id.value)) {
				showMessage("js.stores.mgmt.selectstore.search");
				document.stockConsumptionSearchForm.store_id.focus();
				return false;
			}
			return true;
		}
	</script>


</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stockConsumptionList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty stockConsumptionList}"/>
<body onload="init();showFilterActive(document.stockConsumptionSearchForm)">
<c:set var="grumstatus">
<insta:ltext key="storemgmt.stockconsumptionlist.list.open"/>,
<insta:ltext key="storemgmt.stockconsumptionlist.list.finalized"/>,
<insta:ltext key="storemgmt.stockconsumptionlist.list.cancel"/>
</c:set>
<c:set var="consumer">
<insta:ltext key="storemgmt.stockconsumptionlist.list.consumer"/>
</c:set>
<c:set var="dummyvalue">
<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<h1><insta:ltext key="storemgmt.stockconsumptionlist.list.stockconsumptionlist"/></h1>

<insta:feedback-panel/>

<form name="stockConsumptionSearchForm" method="GET">
	<input type="hidden" name="_method"  id = "_method" value="getStoreStockConsumptionList">
	<input type="hidden" name="_searchMethod" value="getStoreStockConsumptionList"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="stockConsumptionSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="checkStore()">
	  <div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.stockconsumptionlist.list.store"/> :</div>
			<c:choose>
			   	<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
				<insta:userstores username="${userid}" elename="store_id" id="store_id" val="${param.store_id}"/>
				</c:when>
				<c:otherwise>
					<input type="hidden" name="store_id" id="store_id" value="${pharmacyStoreId}" />
					<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
				</c:otherwise>
			</c:choose>
			<input type="hidden" name="store_id@type" value="integer">
		</div>
	</div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockconsumptionlist.list.status"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="grum.status" opvalues="O,F,X" optexts="${grumstatus}" selValues="${paramValues['grum.status']}"/>
							<input type="hidden" name="grum.status@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockconsumptionlist.list.opendate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.stockconsumptionlist.list.from"/>:</div>
							<insta:datewidget name="open_date" id="open_date0" value="${paramValues.open_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.stockconsumptionlist.list.to"/>:</div>
							<insta:datewidget name="open_date" id="open_date1" value="${paramValues.open_date[1]}"/>
							<input type="hidden" name="open_date@op" value="ge,le">
							<input type="hidden" name="open_date@type" value="date">
							<input type="hidden" name="open_date@cast" value="Y">
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockconsumptionlist.list.finalizeddate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.stockconsumptionlist.list.from"/>:</div>
							<insta:datewidget name="finalized_date" id="finalized_date0" value="${paramValues.finalized_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storemgmt.stockconsumptionlist.list.to"/>:</div>
							<insta:datewidget name="finalized_date" id="finalized_date1" value="${paramValues.finalized_date[1]}"/>
							<input type="hidden" name="finalized_date@op" value="ge,le">
							<input type="hidden" name="finalized_date@type" value="date">
							<input type="hidden" name="finalized_date@cast" value="Y">
						</div>
					</td>
				</tr>
		</table>
	  </div>
</insta:search>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('')";>
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="consumption_id" title="${consumer}"/>
				<th><insta:ltext key="storemgmt.stockconsumptionlist.list.store"/></th>
				<th><insta:ltext key="storemgmt.stockconsumptionlist.list.status"/></th>
				<th><insta:ltext key="storemgmt.stockconsumptionlist.list.opendate"/></th>
				<th><insta:ltext key="storemgmt.stockconsumptionlist.list.finalizeddate"/></th>
			</tr>
			<c:forEach var="consumptions" items="${stockConsumptionList}" varStatus="status">
				<c:set var="index" value="${status.index}"/>
				<c:set var="consumptionStatus">
					<c:choose>
						<c:when test="${consumptions.status == 'O'}"><insta:ltext key="storemgmt.stockconsumptionlist.list.open"/></c:when>
						<c:when test="${consumptions.status == 'F'}"><insta:ltext key="storemgmt.stockconsumptionlist.list.finalized"/></c:when>
						<c:when test="${consumptions.status == 'X'}"><insta:ltext key="storemgmt.stockconsumptionlist.list.cancelled"/></c:when>
					</c:choose>
				</c:set>
					<c:choose>
						<c:when test="${not empty urlRightsMap.store_stock_consumption_new && urlRightsMap.store_stock_consumption_new == 'A'}">
							<tr class="${status.index == 0 ? 'firstRow' : ''} ${status.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${status.index}, event, 'resultTable',
								{_consumption_id: '${consumptions.consumption_id}',
									dept_id: '${consumptions.store_id}'},'');"
								id="toolbarRow${status.index}" onmouseover="hideToolBar();">
						</c:when>
						<c:otherwise>
							<tr>
						</c:otherwise>
					</c:choose>
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + status.index + 1 }</td>
						<td>Stock Consumption(${consumptions.consumption_id})</td>
						<td>${consumptions.dept_name}</td>
						<td>${consumptionStatus}</td>
						<td><fmt:formatDate value="${consumptions.open_date}" pattern="dd-MM-yyyy HH:mm"/></td>
						<td><fmt:formatDate value="${consumptions.finalized_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>
	<c:if test="${not empty urlRightsMap.store_stock_consumption_new && urlRightsMap.store_stock_consumption_new == 'A'}">
		<div class="screenActions">
			<a href="stockconsumtionnew.do?_method=getStoreStockConsumptionSearchScreen&sortOrder=medicine_name"><insta:ltext key="storemgmt.stockconsumptionlist.list.addnewstockconsumptions"/></a>
		</div>
	</c:if>
</form>
</body>
</html>