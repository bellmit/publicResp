<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Sections List - Insta HMS</title>
	<c:set var="pagePath" value="<%=URLRoute.SECTIONS_PATH %>"/>
	<script type="text/javascript">
			var toolbar = {
				Edit: {
					title: "View/Edit",
					imageSrc: "icons/Edit.png",
					href: "${pagePath}/show.htm?",
					onclick: null,
					description: "View/Edit Section details"
				},
				FieldsList : {
					title: "Fields List",
					imageSrc: "icons/Edit.png",
					href: 'master/SectionFieldsMaster.do?_method=list',
					onclick: null,
					description: "View/Edit Section Fields details"
				},
				RoleRights : {
					title: "View/Edit Role Rights",
					imageSrc: "icons/Edit.png",
					href: 'master/sectionrolerights.do?_method=edit',
					onclick: null,
					description: "View/Edit Section Role Rights",
					show : ${urlRightsMap.mas_section_role_rights == 'A'}
				}
			};
			function init() {
				createToolbar(toolbar);
			}
	</script>

</head>
<body onload="init();">
	<h1>Sections List</h1>
	<insta:feedback-panel/>
	<form name="sectionForm" method="GET">
		
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="sectionForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Section Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="section_title" value="${ifn:cleanHtmlAttribute(param.section_title)}" />
						<input type="hidden" name="section_title@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" >
					<div class="sboFieldLabel">Linked To:</div>
					<div class="sboFieldInput" style="height: 100%">
						<insta:checkgroup name="linked_to" opvalues="patient,visit,order item,form" optexts="Patient,Visit,Order Item,Form"
							selValues="${paramValues['linked_to']}"/>
						<input type="hidden" name="linked_to@op" value="in"/>
					</div>
				</div>

			</div>
		</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="section_title" title="Section Name"/>
				<th>Allow All Normal</th>
				<insta:sortablecolumn name="linked_to" title="Linked To"/>
				<th>Status</th>
				<th>Section Mandatory</th>
			</tr>
			<c:forEach items="${pagedList.dtoList}" var="form" varStatus="st">
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
					{section_id : '${form.section_id}'}, '');" id="toolbarRow${st.index}">
					<td>${form.section_title}</td>
					<td>${form.allow_all_normal}</td>
					<td>${form.linked_to}</td>
					<td>${form.status}</td>
					<td>${form.section_mandatory ? 'Yes' : 'No'}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${not empty pagedList.dtoList}"/>
	</div>
	<div style="margin-top: 10px">
		<a href="${cpath}/${pagePath}/add.htm">Add New Section</a>
	</div>
</body>
</html>
