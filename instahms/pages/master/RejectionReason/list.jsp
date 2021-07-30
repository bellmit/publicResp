<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Rejection Reason Category List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/rejectionreason/show.htm?',
				description: "View and/or Edit Rejection Reason"
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
	<h1>Rejection Reason Categories</h1>
	<insta:feedback-panel/>
	<form name="searchForm" >
		<input type="hidden" name="_method" value="list">
		<insta:search-lessoptions form="searchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td style="width: 300px" class="sboField">
						<div class="sboFieldLabel">Rejection Reason Category: </div>
						<div class="sboFieldInput">
							<input type="text" name="rejection_reason_category_name" value="${ifn:cleanHtmlAttribute(param.rejection_reason_category_name)}"/>
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

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table width="100%" class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="rejection_reason_category_name" title="Rejection Reason Category"/>
				<th>Status</th>
			</tr>
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{rejection_reason_category_id:'${bean.rejection_reason_category_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>
						<c:if test="${bean.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${bean.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					<insta:truncLabel value="${bean.rejection_reason_category_name}" length="60"/></td>
					<td>${bean.status}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<table style="margin-top: 10px;float: left">
		<tr>
			<c:url var="url" value="/master/rejectionreason/add.htm">
				<%-- <c:param name="_method" value="add"/> --%>
			</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add</a></div>
		</tr>
	</table>
	</form>

	<div style="display: block" class="legend">
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'> </div>
		<div class="flagText">Active</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'> </div>
		<div class="flagText">Inactive</div>
	</div>
</body>
</html>