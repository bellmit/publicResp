<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Strength Unit Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="/masters/strengthUnit.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagepath" value="<%= URLRoute.STRENGTH_UNIT_MASTER %>" />
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Strength Unit details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoStrengthUnitMaster();
		}
		var strengthUnits = ${ifn:convertListToJson(strengthUnitsList)};

	</script>

</head>

<body onload="init()">

	<h1>Strength Unit Master</h1>

	<insta:feedback-panel/>

	<form name="StrengthUnitSearchForm" method="GET">

		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="StrengthUnitSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel">Unit Name</div>
								<div class="sboFieldInput">
									<input type="text" name="unit_name" id="unit_name" value="${ifn:cleanHtmlAttribute(param.unit_name)}" style = "width:15em" >
									<input type="hidden" name="unit_name@op" value="ico" />
									<div id="unitnamecontainer" style = "width:15em"></div>
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
					<insta:sortablecolumn name="unit_name" title="Unit Name"/>
					<th>Status</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{unit_id: '${record.unit_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.unit_name}" length="30"/></td>
						<td>${record.status == 'A' ? 'Active' : 'Inactive'}</td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="${pagepath}/add.htm"/>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add Strength Unit</a></div>
	</form>

</body>
</html>