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
	<title><insta:ltext key="storemgmt.checkpointlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/chkpt.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="chkptList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty chkptList}"/>
<body onload="init(); showFilterActive(document.chkptSearchForm)">
<c:set var="checkpointname">
<insta:ltext key="storemgmt.stockconsumptionlist.list.checkpointname"/>
</c:set>
<c:set var="checkpointdate">
<insta:ltext key="storemgmt.stockconsumptionlist.list.checkpointdate"/>
</c:set>
<h1><insta:ltext key="storemgmt.checkpointlist.list.checkpointslist"/></h1>

<insta:feedback-panel/>

<form name="chkptSearchForm" method="GET">
	<input type="hidden" name="_method" value="viewCheckpoints">
	<input type="hidden" name="_searchMethod" value="viewCheckpoints"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="chkptSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.checkpointlist.list.checkpointname"/></div>
						<div class="sfField">
							<input type="text" name="checkpoint_name"  value="${ifn:cleanHtmlAttribute(param.checkpoint_name)}"/>
								<input type="hidden" name="checkpoint_name@op" value="ilike" />
					    </div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.checkpointlist.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="checkpoint_date" id="checkpoint_date0" value="${paramValues.checkpoint_date[0]}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.checkpointlist.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="checkpoint_date" id="checkpoint_date1" value="${paramValues.checkpoint_date[1]}"/>
							<input type="hidden" name="checkpoint_date@op" value="ge,le"/>
							<input type="hidden" name="checkpoint_date@cast" value="y"/>
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
			    <insta:sortablecolumn name="checkpoint_name" title="${checkpointname}"/>
				<insta:sortablecolumn name="checkpoint_date" title="${checkpointdate}"/>
				<th><insta:ltext key="storemgmt.checkpointlist.list.user"/></th>
				<th><insta:ltext key="storemgmt.checkpointlist.list.remarks"/></th>
			</tr>
            <c:forEach var="chkpt" items="${chkptList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{_chkId:'${chkpt.checkpoint_id }'},
						[true,true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${chkpt.checkpoint_name}</td>
					<td><fmt:formatDate value="${chkpt.checkpoint_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${chkpt.user_name}</td>
					<td><insta:truncLabel value="${chkpt.remarks}" length="30"/></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'viewCheckpoints'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="screenActions">
		<a href="${cpath }/pages/stores/stockcheckpoint.do?_method=getChkpointDetailsScreen"><insta:ltext key="storemgmt.checkpointlist.list.addnewcheckpoint"/> </a>
	</div>

</form>
</body>
</html>