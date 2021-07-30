<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Item Forms List - Insta HMS</title>

	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="pagePath" value="<%=URLRoute.ITEM_FORM_PATH %>"/>
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
				description: "View and/or Edit Item Form details"
				}
		};
		function init() {
			createToolbar(toolbar);
		}
	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${empty pagedList.dtoList ? false : true}"></c:set>

	<h1>Item Forms List</h1>
	<insta:feedback-panel/>

	<form name="ItemForm" method="GET">
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="ItemForm" >
			<div class="searchBasicOpts" >
				<div class="sboField" style="width:150px">
					<div class="sboFieldLabel"style="margin-bottom:5px">Item Form Name</div>
					<div class="sboFieldInput">
						<input type="text" name="item_form_name" value="${ifn:cleanHtmlAttribute(param.item_form_name)}" />
					</div>
				</div>
				<div class="sboField" style="width:150px">
					<div class="sboFieldLabel"style="margin-bottom:5px">Granular Units</div>
					<div class="sboFieldInput">
						<insta:selectoptions name="granular_units" value="${param.granular_units}" opvalues="Y,N" optexts="Yes,No" dummyvalue="---Select---" dummyvalueId=""/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="item_form_name" title="Item Form Name"/>
					<insta:sortablecolumn name="granular_units" title="Granular Units"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{item_form_id: '${record.item_form_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>  ${record.item_form_name}
						</td>
						<td>${record.granular_units}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>
		<c:url var="url" value="${pagePath}/add.htm">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New Form</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</form>
</body>
</html>