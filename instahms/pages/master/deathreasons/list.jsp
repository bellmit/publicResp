<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Death Reasons List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.DEATH_REASON_PATH %>"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				description: "Edit Death Reason"
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
	<h1>Death Reasons</h1>
	<insta:feedback-panel/>
	<form name="searchForm" method="GET">
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Reason: </div>
						<div class="sboFieldInput">
							<input type="text" name="reason" value="${ifn:cleanHtmlAttribute(param.reason)}"/>
						</div>
					</td>
					<td class="sboField" style="height: 70px">
						<div class="sboFieldLabel">Status: </div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table width="100%" class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="reason" title="Reason"/>
				<th>Status</th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{reason_id:'${bean.reason_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td><insta:truncLabel value="${bean.reason}" length="60"/></td>
					<td>${bean.status}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<table style="margin-top: 10px;float: left">
		<tr>
			<td><a href="${cpath}/${pagePath}/add.htm?">Add</a></td>
		</tr>
	</table>
</body>
</html>