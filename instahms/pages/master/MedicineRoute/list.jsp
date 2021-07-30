<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagepath" value="<%= URLRoute.ROUTE_OF_ADMINISTRATION_PATH %>" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Routes of Administration List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				description: "Edit Route Details"
			}
		}
		function init() {
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init()">
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty pagedList.dtoList}"/>
	<h1>Routes of Adminsitration</h1>
	<insta:feedback-panel/>
	<form name="searchForm" method="GET">
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Route Name</div>
						<div class="sboFieldInput">
							<input type="text" name="route_name" value="${ifn:cleanHtmlAttribute(param.route_name)}"/>
						</div>
					</td>
					<td class="sboField" style="height:70px">
						<div class="sboFieldLabel">Status</div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
											opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="route_name" title="Route Name"/>
				<th>Route Code</th>
				<th>Status</th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{route_id:'${bean.route_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${bean.route_name}</td>
					<td>${bean.route_code}</td>
					<td><img src="${cpath}/images/${bean.status == 'A'?'empty':'grey'}_flag.gif"/>${bean.status}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<table style="margin-top: 10px;float: left">
	<c:url var="url" value="${pagepath}/add.htm"></c:url>
		<tr>
			<td><a href="<c:out value='${url}'/>">Add</a></td>
		</tr>
	</table>
	<div style="float: right; margin-top: 10px">
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Inactive Route</div>
	</div>
</body>
</html>
