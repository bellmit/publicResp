<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>

<html>

<head>
	<title>Country Master List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.COUNTRY_MASTER_PATH %>"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
</head>

<body>

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Country Master</h1>

	<insta:feedback-panel/>

	<form name="CountrySearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="nationality" value="f"/>

		<insta:search-lessoptions form="CountrySearchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Country Name:</div>
						<div class="sboFieldInput">
							<input type="text" name="country_name" value="${ifn:cleanHtmlAttribute(param.country_name)}">
							<input type="hidden" name="country_name@op" value="ico" />
						</div>
					</td>
					<td class="sboField" >
						<div class="sboFieldLabel">Status:</div>
						<div class="sboFieldInput"  style="height:50px">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="country_name" title="Country Name"/>
					<th>Country Code</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.country_name}
						</td>
						</td>
						<td>${record.country_code}</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</c:forEach>
			</table>
				<insta:noresults hasResults="${hasResults}"/>
		</div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>

</html>
