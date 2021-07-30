<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Survey Question Category List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'patientfeedback/SurveyQuestionCategoryMaster.do?_method=show',
				description: "Show/Edit Category Details"
			}
		}
		function init() {
			createToolbar(toolbar);
		}

	</script>
</head>

<body onload="init()">
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<h1>Survey Question Category Master</h1>
	<insta:feedback-panel/>

	<form name="categorySearchForm" action="SurveyQuestionCategoryMaster.do">
		<input type="hidden" name="_method" value="getCategoryList">
			<insta:search-lessoptions form="categorySearchForm">
					<table class="searchBasicOpts" >
						<tr>
							<td class="sboField" style="height: 70px">
								<div class="sboField">
									<div class="sboFieldLabel">Category: </div>
										<div class="sboFieldInput">
											<input type="text" name="category" id="item" value="${ifn:cleanHtmlAttribute(param.category)}"/>
											<input type="hidden" name="category@op" value="ilike" />
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
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr onmouseover="hideToolBar();">
			    <th>#</th>
				<insta:sortablecolumn name="category" title="Question Category"/>
				<th>Status</th>
				<th>Category Description</th>
			</tr>
           	<c:forEach items="${dtoList}" var="record" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{category_id:'${record.category_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<insta:truncLabel value="${record.category}" length="100"/>
					</td>
					<td>${record.status == 'A' ? 'Active' : 'InActive'}</td>
					<td><insta:truncLabel value="${record.category_desc}" length="25"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>
	<div class="screenActions" style="float:left"><a href="SurveyQuestionCategoryMaster.do?_method=add">Add New Category</a></div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>
</body>
</html>