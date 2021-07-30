<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Access Rule List - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function focus(){
			document.forms[0].doc_type_name.focus();
		}
	</script>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit Rule",
				imageSrc: "icons/Edit.png",
				href: 'master/EMRAccessRight.do?_method=show',
				onclick: null,
				description: "View and/or Edit Rule details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			//showFilterActive(document.DocSearchForm);
		}
	</script>

</head>
<body onload="init()">
	<h1>Access Rule List</h1>
	<insta:feedback-panel/>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<form action="EMRAccessRight.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="doc_type_id" value="${bean.map.doc_type_id}"/>
	</c:if>
	<fieldset class="fieldsetborder">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.documenttypedetails"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">DocumentType:</td>
				<td>&nbsp;${bean.map.doc_type_name}</td>
				<td class="formlabel">Status:</td>
				<td>&nbsp;${bean.map.status}</td>
				<td class="formlabel">EMR Symbol:</td>
				<td>&nbsp;${bean.map.prefix}</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldsetborder">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright"/></legend>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="rule_id" title="Rule Id"/>
					<insta:sortablecolumn name="doc_type_id" title="Document Type Id"/>
					<th>Rule Type</th>
					<th>Document Sub Type</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{rule_id: '${record.rule_id}', doc_type_id: '${record.doc_type_id}', doc_sub_type: '${record.doc_sub_type}', rule_type: '${record.rule_type}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>${record.rule_id}</td>
						<td>${record.doc_type_id}</td>
						<td>${record.rule_type}</td>
						<td>${record.doc_sub_type}</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

	</fieldset>
	<table class="screenActions">
		<tr>
			<td>
			<c:url var="urlAdd" value="EMRAccessRight.do">
				<c:param name="_method" value="add"/>
				<c:param name="doc_type_id" value="${bean.map.doc_type_id}"/>
				<c:param name="rule_type" value="DOC"/>
			</c:url>
			<a href="${urlAdd}">Add New Rule
			</td>
			<td>&nbsp;|&nbsp;</td>
		<td>
			<c:url var="docUrl" value="/master/documenttypes/list.htm">
				<c:param name="status" value="A"/><c:param name="sortReverse" value="false"/>
				<c:param name="sortOrder" value="system_type"/>
			</c:url>
			<a href="<c:out value='${docUrl}' />">Document Type List</a>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="${cpath}/pages/usermanager/UserDashBoard.do?_method=list&filterClosed=true&sortOrder=rolename&sortReverse=true&hospital=on&active=on">User/Role List</a></td>
		</tr>
	</table>

</form>

</body>
</html>
