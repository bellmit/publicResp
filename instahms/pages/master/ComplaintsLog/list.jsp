<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Complaints Log - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="hmsvalidation.js" />

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/ComplaintsLog.do?_method=show',
				onclick: null,
				description: "View and/or Edit Complaintlog details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.SearchForm);
		}
	</script>
</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Complaints Log</h1>

	<insta:feedback-panel/>

	<form name="SearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="SearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Logged By:</div>
					<div class="sboFieldInput">
						<input type="text" name="logged_by" value="${ifn:cleanHtmlAttribute(param.logged_by)}">
						<input type="hidden" name="logged_by@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Module:</div>
							<div class="sfField">
								<input type="text" name="complaint_module" value="${ifn:cleanHtmlAttribute(param.complaint_module)}">
								<input type="hidden" name="complaint_module@op" value="ico" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="complaint_status" opvalues="Open,Clarify,Pending,Fixed,NotInScope,ProdEnh" optexts="Open,Clarify,Pending,Fixed,Not In Scope,Prod Enh" selValues="${paramValues.complaint_status}"/>
								<input type="hidden" name="complaint_status@op" value="in" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Complaint Id:</div>
							<div class="sfField">
								<input type="text" name="complaint_id" value="${ifn:cleanHtmlAttribute(param.complaint_id)}" onkeypress="return enterNumOnlyzeroToNine(event);" />
								<input type="hidden" name="complaint_id@type" value="integer"/>

							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="complaint_id" title="Complaint Id"/>
					<insta:sortablecolumn name="logged_Date" title="Logged Date"/>
					<insta:sortablecolumn name="logged_by" title="Logged By"/>
					<insta:sortablecolumn name="complaint_module" title="Module"/>
					<insta:sortablecolumn name="complaint_summary" title="Complaint"/>
					<insta:sortablecolumn name="complaint_status" title="Status"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{complaint_id: '${record.complaint_id}'},'');" id="toolbarRow${st.index}">
						<td>${record.complaint_id}</td>
						<td><fmt:formatDate value="${record.logged_date}" pattern="dd-MM-yyyy HH:MM"/></td>
						<td>${record.logged_by}</td>
						<td>${record.complaint_module}</td>
						<td>${record.complaint_summary}</td>
						<td>
							<c:if test="${record.complaint_status == 'Open'}">Open</c:if>
							<c:if test="${record.complaint_status == 'Clarify'}">Clarify</c:if>
							<c:if test="${record.complaint_status == 'Pending'}">Pending</c:if>
							<c:if test="${record.complaint_status == 'Fixed'}">Fixed</c:if>
							<c:if test="${record.complaint_status == 'NotInScope'}">Not In Scope</c:if>
							<c:if test="${record.complaint_status == 'ProdEnh'}">Prod Enh</c:if>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="ComplaintsLog.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<table class="screenActions">
			<tr>
				<td><a href="<c:out value='${url}' />">Add New Complaint</a></td>
			</tr>
		</table>

	</form>

</body>
</html>
