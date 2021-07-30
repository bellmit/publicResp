<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Accounting Heads-Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="pagePath" value="<%=URLRoute.ACCOUNTING_HEAD_PATH %>"/>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Accounting details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Accounting Heads</h1>

	<insta:feedback-panel/>

	<form name="SearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="SearchForm">
			<div class="searchBasicOpts">
				<table class="searchFormTable">
					<tr>
						<td class="last">
							<div class="sboField">
								<div class="sboFieldLabel">Account Head:</div>
								<div class="sboFieldInput">
									<input type="text" name="account_head_name"
										value="${ifn:cleanHtmlAttribute(param.account_head_name)}">
									<input type="hidden" name="account_head_name@op" value="ico" />
								</div>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I"
									optexts="Active,Inactive" selValues="${paramValues.status}" />
								<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="account_head_name" title="Account Head"/>
					<insta:sortablecolumn name="display_order" title="Display Order"/>
					<th>Status</th>				
				</tr>
				<c:forEach var="accounthead" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{account_head_id: '${accounthead.account_head_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
						<td>${accounthead.account_head_name}</td>
						<td>${accounthead.display_order}</td>
						<td>
							<c:if test="${accounthead.status eq 'I'}">Inactive</c:if>
							<c:if test="${accounthead.status eq 'A'}">Active</c:if>
						</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>
		<c:url var="url" value="${pagePath}/add.htm"></c:url>
		
		<div class="screenActions" style="padding-bottom:10px"><a href="<c:out value='${url}'/>">Add New Account Head</a></div>
	</form>

</body>
</html>