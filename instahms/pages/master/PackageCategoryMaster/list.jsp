<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Package Category Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="/masters/packageCategory.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/PackageCategoryMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Package Category details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoPackageCategoryMaster();
		}
		var packageCategory = <%= request.getAttribute("packageCategoryList") %>;

	</script>

</head>

<body onload="init()">

	<h1>Package Category Master</h1>

	<insta:feedback-panel/>

	<form name="PackageCategorySearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="PackageCategorySearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel">Package Category</div>
								<div class="sboFieldInput">
									<input type="text" name="package_category" id="package_category" value="${ifn:cleanHtmlAttribute(param.package_category)}" style = "width:32em" >
									<input type="hidden" name="package_category@op" value="ico" />
									<div id="packagecategorycontainer" style = "width:32em"></div>
								</div>
						</div>
		 			</td>
		 			<td></td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel">Status: </div>
							<div class="sboFieldInput">
								<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td></td>
				</tr>
	 	 	</table>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="package_category" title="Package Category"/>
					<th>Status</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{package_category_id: '${record.map.package_category_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.map.package_category}" length="50"/></td>
						<td>${record.map.status == 'A' ? 'Active' : 'Inactive'}</td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="PackageCategoryMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}'/>">Add Package Category</a></div>
	</form>

</body>
</html>