<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Feedback Forms List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var toolbar = {
			Edit : {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'patientfeedback/SurveyFeedbackForms.do?_method=editForm',
				description: "View/Edit Feedback Form Details"
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
	<h1>Feedback Forms</h1>
	<insta:feedback-panel/>

	<form name="feedbackFormSearchForm" action="SurveyFeedbackForms.do">
		<input type="hidden" name="_method" value="getFeedbackFormsList">
			<insta:search-lessoptions form="feedbackFormSearchForm">
					<table class="searchBasicOpts" >
						<tr>
							<td class="sboField" style="height: 70px">
								<div class="sboField">
									<div class="sboFieldLabel">Form Name: </div>
										<div class="sboFieldInput">
											<input type="text" name="form_name" id="item" value="${ifn:cleanHtmlAttribute(param.form_name)}"/>
											<input type="hidden" name="form_name@op" value="ilike" />
										</div>
				    				</div>
				 			</td>
				 			<td></td>
							<td class="sboField" style="height: 70px">
							<div class="sboField">
								<div class="sboFieldLabel">Status: </div>
									<div class="sboFieldInput">
										<insta:checkgroup name="form_status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.form_status}"/>
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
				<insta:sortablecolumn name="form_name" title="Form Name"/>
				<th>Form Title</th>
				<th>Form Footer</th>
			</tr>
           	<c:forEach items="${dtoList}" var="record" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{form_id:'${record.form_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>
						<c:if test="${record.form_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.form_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${record.form_name}
					</td>
					<td><insta:truncLabel value="${record.form_title}" length="30"/></td>
					<td><insta:truncLabel value="${record.form_footer}" length="30"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>
	<div class="screenActions" style="float:left"><a href="SurveyFeedbackForms.do?_method=add">Add New Form</a></div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>
</body>
</html>