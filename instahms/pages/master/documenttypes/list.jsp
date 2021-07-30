<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Document Types List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.DOCUMENT_TYPE_PATH %>"/>

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
				description: "View and/or Edit Document Type details"
				},
			AccessRuleList: {
				title: "EMR Access Rule",
				imageSrc: "icons/Edit.png",
				href: 'master/EMRAccessRight.do?_method=add&rule_type=DOC',
				//href: '${pagePath}/add.htm?rule_type=DOC',
				onclick: null,
				description: "EMR Access Rule"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.DocSearchForm);
		}
	</script>

</head>

<body onload="init()">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Document Type</h1>

	<insta:feedback-panel/>

	<form name="DocSearchForm" method="GET">

		
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:find form="DocSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Document Type:</div>
					<div class="sboFieldInput">
						<input type="text" name="doc_type_name" value="${ifn:cleanHtmlAttribute(param.doc_type_name)}">
						<input type="hidden" name="doc_type_name@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="system_type" opvalues="Y,N" optexts="System,User" selValues="${paramValues.system_type}"/>
								<input type="hidden" name="system_type@op" value="in" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
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
					<insta:sortablecolumn name="doc_type_name" title="Document Type"/>
					<insta:sortablecolumn name="system_type" title="Type"/>
					<th>EMR Symbol</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{doc_type_id: '${record.doc_type_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.doc_type_name}</td>
						<td>${record.system_type eq 'Y' ? 'System' : 'User'}</td>
						<td>${record.prefix}</td>
					</tr>
				</c:forEach>
			</table>
			
				<insta:noresults hasResults="${hasResults}"/>
			
		</div>

		<c:url var="url" value="${pagePath}/add.htm?">
			
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New DocumentType</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
		</div>
	</form>
</body>
</html>