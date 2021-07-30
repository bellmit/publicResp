<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.CENTER_PATH %>"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Centers List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				description: "Edit Center Details"
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
	<h1>Centers</h1>
	<insta:feedback-panel/>
	<form name="searchForm" action="${cpath}${pagePath}/list.htm">
		<!-- <input type="hidden" name="_method" value="list"> -->
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Center Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="center_name" value="${ifn:cleanHtmlAttribute(param.center_name)}"/>
							<input type="hidden" name="center_name@op" value="ico"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Center Code: </div>
						<div class="sboFieldInput">
							<input type="text" name="center_code" value="${ifn:cleanHtmlAttribute(param.center_code)}"/>
						</div>
					</td>
					<td class="sboField" style="height: 70px">
						<div class="sboFieldLabel">Status: </div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Health Authority: </div>
						<div class="sboFieldInput">
							<input type="text" name="health_authority" value="${ifn:cleanHtmlAttribute(param.health_authority)}"/>
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
				<insta:sortablecolumn name="center_name" title="Center Name"/>
				<th>Center Code</th>
				<th>Status</th>
				<th>Health Authority</th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{center_id:'${bean.center_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${bean.center_name}</td>
					<td>${bean.center_code}</td>
					<td>${bean.status}</td>
					<td>${bean.health_authority}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<table style="margin-top: 10px;float: left">
		<tr>
			<td><a href="${cpath}${pagePath}/add.htm">Add</a></td>
		</tr>
	</table>
</body>
</html>