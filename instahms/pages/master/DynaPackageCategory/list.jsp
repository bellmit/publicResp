<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dyna Package Category List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/DynaPackageCategoryMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this Dyna Package Category"
				}
		};

		function init() {
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>Dyna Package Category Master</h1>
<insta:feedback-panel/>
<form name="dynaPkgCategoryForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="dynaPkgCategoryForm">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Category Name:</div>
				<div class="sboFieldInput">
				<input type="text" name="dyna_pkg_cat_name" value="${ifn:cleanHtmlAttribute(param.dyna_pkg_cat_name)}"/>
				<input type="hidden" name="dyna_pkg_cat_name@op" value="ico"/>
				</div>
			</div>
		</div>
	</insta:search-lessoptions>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="dyna_pkg_cat_name" title="Category Name"/>
			<insta:sortablecolumn name="limit_type" title="Limit Type"/>
			<th>Category Description</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{dyna_pkg_cat_id: '${record.dyna_pkg_cat_id}'});" >

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>${record.dyna_pkg_cat_name}</td>
				<td>${record.limit_type eq 'A' ? 'Amount' : ( record.limit_type eq 'Q' ? 'Quantity' : 'Unlimited' )}</td>
				<td>
					<insta:truncLabel value="${record.dyna_pkg_cat_desc}" length="50"/>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:if test="${empty pagedList.dtoList}"> <insta:noresults hasResults="${hasResults}"/> </c:if>

<c:url value="DynaPackageCategoryMaster.do" var="catUrl">
	<c:param name="_method" value="add" />
</c:url>
<div class="screenActions" style="float:left"><a href="${catUrl}">Add New Pkg Category</a></div>

</body>
</html>
