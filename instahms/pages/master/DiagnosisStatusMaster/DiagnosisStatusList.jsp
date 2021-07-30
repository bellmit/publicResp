<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Diagnosis Status List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/DiagnosisStatusAction.do?_method=show',
				description: "Edit Diagnosis Status Details"
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
	<h1>Diagnosis Status List</h1>
	<insta:feedback-panel/>
	<form name="searchForm" action="DiagnosisStatusAction.do">
		<input type="hidden" name="_method" value="list">
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Diagnosis Status Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="diagnosis_status_name" value="${ifn:cleanHtmlAttribute(param.diagnosis_status_name)}"/>
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
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="diagnosis_status_name" title="Diagnosis Status Name"/>
				<th>Status</th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{diagnosis_status_id:'${bean.diagnosis_status_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${bean.diagnosis_status_name}</td>
					<td>${bean.status == 'A' ? 'Active' : 'Inactive'}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<table style="margin-top: 10px;float: left">
		<tr>
			<td><a href="DiagnosisStatusAction.do?_method=add">Add</a></td>
		</tr>
	</table>
</body>
</html>