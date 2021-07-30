<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Govt ID Type - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "master/govtidentifiers/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Govt ID Type"
				}
		};

		function init() {

			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Govt Id Type</h1>

<insta:feedback-panel/>

<form name="GovtIDForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="GovtIDForm" >
	<div class="searchBasicOpts">
		<table  class="searchFormTable">
			<tr>
				<td class="last">
					<div class="sboField">
						<div class="sboFieldLabel">Default Value:</div>
						<div class="sboFieldInput">
							<input type="text" name="identifier_type" value="${ifn:cleanHtmlAttribute(param.identifier_type)}" />
							<input type="hidden" name="identifier_type@op" value="ico"/>
						</div>
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
			</tr>
		</table>
	</div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<th>Identifier Type</th>
				<th>Description</th>
				<th>Status</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {identifier_id :'${record.identifier_id}'},'');">
					<td>
						${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
					</td>
					<td>${record.identifier_type}</td>
					<td>${record.remarks}</td>
					<td>${record.status}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:if test="${empty pagedList.dtoList}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<div class="screenActions" style="float: left">
		<a href="${cpath}/master/govtidentifiers/add.htm?">Add New Govt ID Type</a>
	</div>
</form>
</body>
</html>
