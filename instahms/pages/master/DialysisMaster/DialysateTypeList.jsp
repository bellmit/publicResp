<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.dialysatetype.list.dialysatetypelist"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<insta:js-bundle prefix="dialysismodule.dialysatetype"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.dialysatetype.toolbar");
	</script>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'master/DialysateType.do?_method=show',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>


</head>
<body onload="init();">
<c:set var="dialysatetype">
 <insta:ltext key="generalmasters.dialysatetype.list.dialysatetype"/>
</c:set>
<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1><insta:ltext key="generalmasters.dialysatetype.list.dialysatetype"/></h1>

	<insta:feedback-panel/>

	<form name="DialysateSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="DialysateSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialysatetype.list.dialysatetype"/>:</div>
						<div class="sboFieldInput">
							<input type="text" name="dialysate_type_name" value="${ifn:cleanHtmlAttribute(param.dialysate_type_name)}">
							<input type="hidden" name="dialysate_type_name@op" value="ico" />
						</div>
					</td>
					<td class="sboField" style="height:80px">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialysatetype.list.status"/>:</div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" opvalues="A,I" optexts="${status}" selValues="${paramValues.status}"/>
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
					<insta:sortablecolumn name="dialysate_type_name" title="${dialysatetype}"/>
					<th><insta:ltext key="generalmasters.dialysatetype.list.potassium"/></th>
					<th><insta:ltext key="generalmasters.dialysatetype.list.calcium"/></th>
					<th><insta:ltext key="generalmasters.dialysatetype.list.magnesium"/></th>
					<th><insta:ltext key="generalmasters.dialysatetype.list.sodium"/></th>
					<th><insta:ltext key="generalmasters.dialysatetype.list.glucose"/></th>
					<th><insta:ltext key="generalmasters.dialysatetype.list.remarks"/></th>
				</tr>
				<c:forEach var="list" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{dialysate_type_id: '${list.dialysate_type_id}', dialysate_type_name: '${list.dialysate_type_name}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1 }</td>
						<td>
							<c:if test="${list.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${list.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${list.dialysate_type_name}
						</td>
						<td>${list.potasium}</td>
						<td>${list.calcium}</td>
						<td>${list.magnesium}</td>
						<td>${list.sodium}</td>
						<td>${list.glucose}</td>
						<td>${list.remarks}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="DialysateType.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"><insta:ltext key="generalmasters.dialysatetype.list.addnewdialysatetype"/></a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
			<div class="flagText"><insta:ltext key="generalmasters.dialysatetype.list.inactive"/></div>
		</div>

	</form>

</body>
</html>
