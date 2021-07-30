<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<html>

<head>
	<title>Supplier Category Master List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="pagePath" value="<%=URLRoute.SUPPLIER_CATEGORY_PATH %>"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: "${pagePath}/show.htm?",
				onclick: null,
				description: "View and/or Edit Country details"
				},
			Delete: {
				title: "Delete",
				imageSrc: "icons/Cancel.png",
				href: "${pagePath}/delete.htm?",
				onclick: null,
				description: "Delete Supplier Category"
				}
			};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>
</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Supplier Category Master</h1>

	<insta:feedback-panel/>

	<form  name="suppCatForm" method="GET">

		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="suppCatForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Supplier Category Name:</div>
						<div class="sboFieldInput">
							<input type="text" name="supp_category_name" value="${ifn:cleanHtmlAttribute(param.supp_category_name)}">
							<input type="hidden" name="supp_category_name@op" value="ico" />
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
					<insta:sortablecolumn name="supp_category_name" title="Supplier Category Name"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<c:set var="deleteEnabled" value="${record.count > 0}"/>
					<c:set var="editEnabled" value="${record.supp_category_id eq 0}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{supp_category_id: '${record.supp_category_id}', supp_category_name: '${record.supp_category_name}'},
						[!${editEnabled },!${deleteEnabled}]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>${record.supp_category_name}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		
		<c:url var="url" value="${pagePath}/add.htm">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="${url}">Add New Supplier Category</a></div>

	</form>

</body>

</html>
