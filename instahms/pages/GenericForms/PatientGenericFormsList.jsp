<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Generic Forms List - Insta HMS</title>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var cpath = '${cpath}';
		function init() {
			var genericFormToolbar = {
				Edit :		{	title: "Edit",
								imageSrc: "icons/Edit.png",
								href: 'GenericForms/GenericFormsAction.do?_method=addOrEditGenericForm',
						  	},
				Print : 	{	title: "Print",
								imageSrc: "icons/Print.png",
								href: 'GenericForms/GenericFormPrintAction.do?_method=print',
								target : '_blank'
						  	},
			};
			createToolbar(genericFormToolbar);
		}

	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();" >


	<c:set var="patientformslist" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty patientformslist}"/>
	<div class="pageHeader" style="float: left;">Patient Generic Forms List</div>
	<c:url var="searchUrl" value="GenericFormsAction.do" />
	<insta:patientsearch searchType="visit" fieldName="patient_id" searchUrl="${searchUrl}" buttonLabel="Find"
		searchMethod="list"/>
	<insta:feedback-panel/>

	<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>

	<c:if test="${!param.defaultScreen}">
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="resultList" align="center" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
				<tr onmouseover="hideToolBar('');">
					<th>Sl. No.</th>
					<th>Form Name</th>
					<th>Last Modified Date &amp; Time</th>
					<th>User</th>
				</tr>
				<c:forEach var="patientform" items="${patientformslist}" varStatus="st">
					<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
									{patient_id: '${patientform.map.patient_id}', insta_form_id: '${patientform.map.form_id}',
									generic_form_id: '${patientform.map.generic_form_id}'},
									[true, true]);"
								onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td>${patientform.map.form_name}</td>
						<td>
							<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${patientform.map.mod_time}"/>
						</td>
						<td>${patientform.map.user_name}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults message="No Forms Found" hasResults="${results}"/>
		</div>
	</c:if>
	<div id="actions" class="screenActions">
		<c:url var="formUrl" value="GenericFormsAction.do">
			<c:param name="_method" value="getChooseGenericFormScreen"/>
			<c:param name="patient_id" value="${param.patient_id}"/>
		</c:url>
		<c:if test="${not empty param.patient_id}">
		    <a href='<c:out value="${formUrl}"/>' title="Add Patient Generic Form">Add Generic Form</a>
        </c:if>
	</div>
</body>
</html>
