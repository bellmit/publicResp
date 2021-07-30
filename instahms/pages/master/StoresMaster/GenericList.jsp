<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title>Pharmacy Generic List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/generic_list.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
  <insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
  <insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="genList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty genList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<body onload="init(); showFilterActive(document.genListSearchForm)">

<h1>Generics List</h1>

<insta:feedback-panel/>

<form name="genListSearchForm" method="GET">
	<input type="hidden" name="_method" value="getGenericDashBoard">
	<input type="hidden" name="_searchMethod" value="getGenericDashBoard"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="genListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Generic</div>
				<div class="sboFieldInput">
					<input type="text" name="generic_name" value="${ifn:cleanHtmlAttribute(param.generic_name)}"/>
					<input type="hidden" name="generic_name@op" value="ilike"/>
				</div>
	    	</div>
	  	</div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,I" optexts="Active,Inactive"/>
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
			    <insta:sortablecolumn name="generic_name" title="Generic Name"/>
				<th>Status</th>
			</tr>
            <c:forEach var="gen" items="${genList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{generic_id:'${gen.generic_code }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td><c:out value="${gen.generic_name}"/></td>
					<td>${statusDisplay[gen.status]}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getGenericDashBoard'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="screenActions">
		<a href="${cpath }/master/StoresMastergendetails.do?_method=getGenericDetailsScreen">Add New Generic Name</a>
	</div>
</form>

<insta:CsvDataHandler divid="upload1" action="StoresMastergendetails.do"/>

</body>
</html>
