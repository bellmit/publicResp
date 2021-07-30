<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title> ${custom_type eq 'V' ? 'Visit' : 'Patient'} Custom List ${custom_list} - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<c:if test="${not empty custom_list && not empty custom_type}">
		<c:choose>
			<c:when test="${custom_type eq 'P'}">
				<c:set var="urlpath"> CustomMaster${custom_list}.do</c:set>
			</c:when>
			<c:otherwise>
				<c:set var="urlpath"> VisitCustomMaster${custom_list}.do</c:set>
			</c:otherwise>
		</c:choose>
	</c:if>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var customNo = '${custom_list}';
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/${urlpath}?_method=show',
				onclick: null,
				description: "View and/or Edit Custom List "+customNo+" details"
				}
		};
		function init() {
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

<h1>${custom_type eq 'V' ? 'Visit' : 'Patient'} Custom Master ${custom_list}</h1>

<insta:feedback-panel/>

	<form name="CustomSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="CustomSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Custom Value:</div>
						<div class="sboFieldInput">
							<input type="text" name="custom_value" value="${ifn:cleanHtmlAttribute(param.custom_value)}">
							<input type="hidden" name="custom_value@op" value="ico" />
						</div>
					</td>
					<td class="sboField" style="height:80px">
						<div class="sboFieldLabel">Status:</div>
						<div class="sboFieldInput">
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
					<insta:sortablecolumn name="custom_value" title="Custom Value"/>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<script>
							var customValue${st.index};
							customValue${st.index} = <insta:jsString value="${record.custom_value}"/>;
							
					</script>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{custom_value: customValue${st.index}},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.custom_value}
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>

					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<c:url var="url" value="${urlpath}">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}'/>">Add New Custom Value</a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</form>
</body>
</html>