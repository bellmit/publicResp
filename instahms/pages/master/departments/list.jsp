<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Departments Master List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.DEPARTMENT_PATH %>"/>

	<script type="text/javascript">
	
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Department"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
		function autoCompleteForDepartmentNames() {
			var departmentList = ${ifn:convertListToJson(departments)};
			console.log(departmentList);
			var datasource = new YAHOO.util.LocalDataSource({result: departmentList});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "dept_name"},{key : "dept_id"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('dept_name','dept_name_container', datasource);
			rAutoComp.minQueryLength = 0;
		 	rAutoComp.maxResultsDisplayed = 20;
		 	rAutoComp.forceSelection = false;
		 	rAutoComp.animVert = false;
		 	rAutoComp.resultTypeList = false;
		 	rAutoComp.typeAhead = false;
		 	rAutoComp.allowBroserAutocomplete = false;
		 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			rAutoComp.autoHighlight = true;
			rAutoComp.useShadow = false;
		 	if (rAutoComp._elTextbox.value != '') {
					rAutoComp._bItemSelected = true;
					rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
			}
		}

	</script>
</head>
<body onload="init();autoCompleteForDepartmentNames();">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Department Master</h1>

<insta:feedback-panel/>

<form name="DepartmentForm" method="GET">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="DepartmentForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Department Name:</div>
					<div class="sboFieldInput">
						<input type="text" id="dept_name" name="dept_name" value="${ifn:cleanHtmlAttribute(param.dept_name)}" />
						<input type="hidden" name="dept_name@op" value="ico"/>
						<div id="dept_name_container" style="right: 0px; width: 200px"></div>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['status']}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Department Type:</div>
					<div class="sboFieldInput">
						<select name="dept_type_id" class="dropdown">
 				<option value="">-- All --</option>
				<c:forEach items="${departmenttypes}" var="departmentTypeList">
					<option value="${departmentTypeList.dept_type_id}" ${bean.dept_type_id == departmentTypeList.dept_type_id ? 'selected' : ''} >${departmentTypeList.dept_type_desc }</option>
				</c:forEach>
				</select>
					</div>
				</div>

			</div>
		</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="dept_name" title="Department"/>
			<th>Department Type</th>
			<th>Allowed Gender</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {dept_id: '${record.dept_id}'},'');">

				<td>
					${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
				</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.dept_name}
				</td>
				<td>${record.dept_type_desc}</td>
				<td>
					<c:choose>
						<c:when test="${record.allowed_gender == 'F'}">	Female </c:when>
						<c:when test="${record.allowed_gender == 'M'}"> Male </c:when>
						<c:when test="${record.allowed_gender == 'ALL'}"> All </c:when>
						<c:otherwise></c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="${pagePath}/add.htm">
			
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
