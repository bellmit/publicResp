<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Pharmacy Payment Terms - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/phtemp_list.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="tempList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty tempList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<body onload="init(); showFilterActive(document.tempListSearchForm)">

<h1>Payment Terms</h1>

<insta:feedback-panel/>

<form name="tempListSearchForm" method="GET">
	<input type="hidden" name="_method" value="getTemplateDashBoard">
	<input type="hidden" name="_searchMethod" value="getTemplateDashBoard"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}" />  
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="tempListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Template</div>
				<div class="sboFieldInput">
					<input type="text" name="template_name" value="${ifn:cleanHtmlAttribute(param.template_name)}"/>
					<input type="hidden" name="template_name@op" value="ilike"/>
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
			    <insta:sortablecolumn name="template_name" title="Template Name"/>
				<th>Status</th>
			</tr>
            <c:forEach var="temp" items="${tempList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{tempName:'${temp.template_code }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td><c:out value="${temp.template_name}"/></td>
					<td>${statusDisplay[temp.status]}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getTemplateDashBoard'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="screenActions">
		<a href="${cpath }/pages/masters/insta/stores/tempdetails.do?_method=getTemplateDetailsScreen">Add New Template</a>
	</div>

</form>
</body>
</html>