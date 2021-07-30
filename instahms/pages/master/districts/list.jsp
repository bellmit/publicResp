<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>District Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>
	<c:set var="pagePath" value="<%=URLRoute.DISTRICT_MASTER_PATH %>"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit City details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.CitySearchForm);
		}
	</script>
</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>District Master</h1>

	<insta:feedback-panel/>

	<form name="CitySearchForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:find form="CitySearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">District Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="district_name" value="${ifn:cleanHtmlAttribute(param.district_name)}">
						<input type="hidden" name="district_name@op" value="ico" />
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">State Name:</div>
							<div class="sfField">
								<input type="text" name="state_name" value="${ifn:cleanHtmlAttribute(param.state_name)}">
								<input type="hidden" name="state_name@op" value="ico" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="districtstatus" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.districtstatus}"/>
								<input type="hidden" name="districtstatus@op" value="in" />
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:find>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="district_name" title="District Name"/>
					<insta:sortablecolumn name="state_name" title="State Name"/>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{state_id: '${record.state_id}', district_id: '${record.district_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.districtstatus eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.districtstatus eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.district_name}
						</td>
						<td>${record.state_name}</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</c:forEach>
			</table>
				<insta:noresults hasResults="${hasResults}"/>
		</div>

		<c:url var="url" value="${pagePath}/add.htm">
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New District</a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>
</body>
</html>
