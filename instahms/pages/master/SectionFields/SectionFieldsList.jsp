<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Section Fields List - Insta HMS</title>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/SectionFieldsMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Section details"
				}
		};
		function init() {
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init();">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
	<c:set var="title" value="${hasResults ? (pagedList.dtoList)[0].map.section_title : ''}"/>
	<h1>${title} Fields List</h1>
	<insta:feedback-panel/>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr>
				<th>Field Name</th>
				<th>Display Order</th>
				<th>Field Type</th>
				<th>Has Others</th>
				<th>Has Normal</th>
				<th>No. of lines</th>
				<th>Mandatory</th>
			</tr>
			<c:forEach items="${pagedList.dtoList}" var="field" varStatus="st">
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
					{field_id : '${field.map.field_id}', section_id : '${field.map.section_id}'}, '');" id="toolbarRow${st.index}">
					<td>
						<img src="${cpath}/images/${field.map.status == 'A' ? 'empty' : 'grey'}_flag.gif">
						${field.map.field_name}
					</td>
					<td>${field.map.display_order}</td>
					<td>${field.map.field_type}</td>
					<td>${field.map.allow_others}</td>
					<td>${field.map.allow_normal}</td>
					<td>${field.map.no_of_lines}</td>
					<td>${field.map.is_mandatory ? 'Yes' : 'No'}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${not empty pagedList.dtoList}"/>
	</div>
	<div class="screenActions">
		<c:url value="SectionFieldsMaster.do" var="addUrl">
			<c:param name="_method" value="add"/>
			<c:param name="section_id" value="${param.section_id}"/>
		</c:url>
		<a href="<c:out value='${addUrl}' />">Add New Field</a>
		| <a href="<c:out value='${cpath}/master/sections/list.htm?&sortOrder=section_title&sortReverse=false&status=A' />" title="Show Sections List">Section List</a>
	</div>
	<div class="legend" style="display: ${not empty pagedList.dtoList? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>

	</div>
</body>
</html>