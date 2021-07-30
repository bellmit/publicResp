<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Diagnostic Department List - Insta HMS</title>
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
				href : "/master/DiagnosticDepartmentMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Diagnostic Department"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Diagnostic Departments</h1>

<insta:feedback-panel/>

<form name="DiagDepartmentForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="DiagDepartmentForm" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Department Name:</div>
				<div class="sboFieldInput">
					<input type="text" name="ddept_name" value="${ifn:cleanHtmlAttribute(param.ddept_name)}">
					<input type="hidden" name="ddept_name@op" value="ico"/>
				</div>
			</div>
			<div class="sboField" style="height:69">
				<div class="sboFieldLabel">Status:</div>
				<div class="sboFieldInput">
					<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in"/>
						<input type="hidden" name="status@cast" value="Y"/>
				</div>
			</div>
		</div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<insta:sortablecolumn name="ddept_name" title="Department"/>
			<th>DESIGNATION</th>
			<th>Display Order</th>
		</tr>

		<tr style="background-color:#E4EBE0;cursor:pointer;color:#333;">
			<td style="border-bottom:1px #e0e0e0 solid"><b>LABS</b></td>
			<td style="border-bottom:1px #e0e0e0 solid">&nbsp;</td>
			<td style="border-bottom:1px #e0e0e0 solid">&nbsp;</td>
		</tr>

		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<c:if test="${record.category eq 'DEP_LAB'}">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {ddept_id: '${record.ddept_id}'},'');">
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.ddept_name}
					</td>
					<td><c:out value="${record.designation}" escapeXml="true"/></td>
					<td><c:out value="${record.display_order}" escapeXml="true"/></td>
				</tr>
			</c:if>
		</c:forEach>

		<tr style="background-color:#E4EBE0;cursor:pointer;color:#333;">
			<td><b>RADIOLOGY</b></td>
			<td style="border-bottom:1px #e0e0e0 solid">&nbsp;</td>
			<td style="border-bottom:1px #e0e0e0 solid">&nbsp;</td>
		</tr>

		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<c:if test="${record.category eq 'DEP_RAD'}">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {ddept_id: '${record.ddept_id}'},'');">
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.ddept_name}
					</td>
					<td><c:out value="${record.designation}" escapeXml="true"/></td>
					<td><c:out value="${record.display_order}" escapeXml="true"/></td>
				</tr>
			</c:if>
		</c:forEach>

		</table>
	</div>
	<c:if test="${empty pagedList.dtoList}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>
	<c:url var="Url" value="DiagnosticDepartmentMaster.do">
		<c:param name="_method" value="add"/>
	</c:url>

	<div class="screenActions" style="float: left">
		<a href="${Url}">Add New Department</a>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>

</form>
</body>
</html>
