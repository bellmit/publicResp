<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="storemgmt.stocktransferlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
	</script>
	<script>
    var toolbar = {}
		toolbar.Report= {
			title: toolbarOptions["transferprint"]["name"],
			imageSrc: "icons/Report.png",
			href: '/pages/stores/stocktransfer.do?_method=getStockTransferPrint',
			target: '_blank',
			onclick: null,
			description:toolbarOptions["transferprint"]["description"]
	};

	var theForm = document.StkTransSearchForm;
    var deptId = '${ifn:cleanJavaScript(dept_id)}';
    var gRoleId = '${ifn:cleanJavaScript(roleId)}';
	function init() {
		theForm = document.StkTransSearchForm;
		theForm.transfer_no.focus();
		createToolbar(toolbar);
	}

	function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
 		document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}

</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="trnsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty trnsList}"/>
<body onload="init(); showFilterActive(document.StkTransSearchForm); checkstoreallocation();">
<c:set var="transferno">
<insta:ltext key="storemgmt.stocktransferlist.list.transferno"/>
</c:set>
<c:set var="all">
<insta:ltext key="storemgmt.stocktransferlist.list.all"/>
</c:set>
<c:set var="transferdate">
<insta:ltext key="storemgmt.stocktransferlist.list.transferdate"/>
</c:set>
<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storemgmt.stocktransferlist.list.stocktransferlist"/></h1>

<insta:feedback-panel/>

<form name="StkTransSearchForm" method="GET">
	<input type="hidden" name="_method" value="getStkTransfer">
	<input type="hidden" name="_searchMethod" value="getStkTransfer"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="StkTransSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.stocktransferlist.list.transferno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="transfer_no" value="${ifn:cleanHtmlAttribute(param.transfer_no)}" onkeypress="return enterNumOnlyzeroToNine(event);">
					<input type="hidden" name="transfer_no@type" value="integer" />
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stocktransferlist.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="transfer_date" id="transfer_date0" value="${paramValues.transfer_date[0]}"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stocktransferlist.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="transfer_date" id="transfer_date1" value="${paramValues.transfer_date[1]}"/>
							<input type="hidden" name="transfer_date@op" value="ge,le"/>
							<input type="hidden" name="transfer_date@cast" value="y"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stocktransferlist.list.fromstore"/></div>
						<div class="sfField">
							<c:choose>
						    <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
								<insta:userstores username="${userid}" elename="from_id" id="from_id"/>
							</c:when>
							<c:otherwise>
								<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
								<input type="hidden" name="from_id" id="from_id" value="${pharmacyStoreId}">
					        </c:otherwise>
					     </c:choose>
								<input type="hidden" name="from_id@type" value="integer"/>
								<input type="hidden" name="from_id@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stocktransferlist.list.tostore"/></div>
						<div class="sfField">
							<insta:selectdb name="to_id" table="stores" valuecol="dept_id" class="dropdown"
								displaycol="dept_name" value="${param.to_id}" dummyvalue="${all}" orderby="dept_name"/>
							<input type="hidden" name="to_id@type" value="integer"/>
							<input type="hidden" name="to_id@cast" value="y"/>
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
				<insta:sortablecolumn name="transfer_no" title="${transferno}"/>
				<insta:sortablecolumn name="transfer_date" title="${transferdate}"/>
			    <th><insta:ltext key="storemgmt.stocktransferlist.list.fromstore"/></th>
			    <th><insta:ltext key="storemgmt.stocktransferlist.list.tostore"/></th>
			    <th><insta:ltext key="storemgmt.stocktransferlist.list.user"/></th>
			</tr>

			<c:forEach var="trns" items="${trnsList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{transfer_no: '${trns.transfer_no}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${trns.transfer_no}</td>
					<td><fmt:formatDate value="${trns.transfer_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${trns.from_store}</td>
					<td>${trns.to_store}</td>
					<td>${trns.username}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getStkTransfer'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>


</form>
</div>
</body>
</html>