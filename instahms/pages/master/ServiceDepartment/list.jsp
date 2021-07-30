<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Service Departments List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/ServiceDepartmentMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of Service Department"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>

<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Service Department Master</h1>

<insta:feedback-panel/>

	<form name="DepartmentForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="DepartmentForm" >
			<div class="searchBasicOpts" >
				<div class="sboField" style="width:150px">
					<div class="sboFieldLabel"style="margin-bottom:5px">Service Department Name</div>
					<div class="sboFieldInput">
						<input type="text" name="department" value="${ifn:cleanHtmlAttribute(param.department)}" />
						<input type="hidden" name="department@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="dep.status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['dep.status']}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Department Type:</div>
					<div class="sboFieldInput">
						<insta:selectdb table="department_type_master" name="dep.dept_type_id" displaycol="dept_type_desc"
							valuecol="dept_type_id" dummyvalue="-- All --" dummyvalueid="" value="${param['dep.dept_type_id']}"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList" >
			<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="department" title="Service Department"/>
				<th>Department Type</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {serv_dept_id: '${record.serv_dept_id}'},'');">

					<td>
						${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
					</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.department}
					</td>
					<td>${record.dept_type_desc}</td>
				</tr>
			</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="Url" value="ServiceDepartmentMaster.do">
			<c:param name="_method" value="add"/>
			<c:param name="store_id" value="-2"/>
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New Department</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>
</body>
</html>

