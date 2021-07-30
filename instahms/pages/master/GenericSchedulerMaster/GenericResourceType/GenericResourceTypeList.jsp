<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Generic Resource Types List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/GenericResourceType.do?_method=show',
				onclick: null,
				description: "View and/or Edit Generic Resource details"
				}
		};
		function init(){
			createToolbar(toolbar);
		}

	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedlist.dtoList}"></c:set>

	<h1>Generic Resource Types List</h1>

	<insta:feedback-panel/>

	<form name="GenericResourceTypesSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="GenericResourceTypesSearchForm">

			<div class="searchBasicOpts">
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel">Generic Resource Type</div>
					<div class="sboFieldInput">
						<input type="text" name="resource_type_desc" value="${ifn:cleanHtmlAttribute(param.resource_type_desc)}">
						<input type="hidden" name="resource_type_desc@op" value="ico" />
					</div>
				</div>

				<div class="sboField" style="height:30px">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['status']}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedlist.pageNumber}" numPages="${pagedlist.numPages}" totalRecords="${pagedlist.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="resource_type_desc" title="Generic Resource Type"/>
				</tr>

				<c:forEach var="record" items="${pagedlist.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{generic_resource_type_id: '${record.generic_resource_type_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedlist.pageNumber-1) * pagedlist.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${record.resource_type_desc}
						</td>
					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>