<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Insurance Patients List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="Insurance/insurance.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="datetest.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<script>
		var cpath = '${cpath}';
		var roleId = '${roleId}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';

	</script>
</head>

<body onload="init('InsuranceDocs');ajaxForPrintUrls();" >
<c:set var="InsDocsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty InsDocsList}"/>
<div class="pageHeader">Insurance Documents</div>
<insta:feedback-panel/>
<div>
	<c:choose>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" />
		</c:otherwise>
	</c:choose>
</div>
<form name="addNewCase" method="POST" action="/Insurance/AddOrEditCase.do" onsubmit="getInsDetails(); return false;">
<input type="hidden" name="_method" id="_method" value="addshow">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}">
<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>

<div class="resultList">
	<table class="dashboard" align="center" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
		<tr onmouseover="hideToolBar('');">
			<th>Select</th>
			<th>Actions</th>
			<th>Visit No</th>
			<th>Document Name</th>
			<th>Template</th>
			<th>Date</th>
			<th>User</th>
		</tr>
		<c:if test="${not empty InsDocsList}">
			<c:forEach var="insDoc" items="${InsDocsList}"  varStatus="st">
				<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', doc_id: '${insDoc.map.doc_id}', template_id: '${insDoc.map.template_id}',
							format: '${insDoc.map.doc_format}',patient_id: '${insDoc.map.visit_id}',
							printerId: '${printpreferences.map.printer_id}',insurance_id: '${insDoc.map.insurance_id}',
							access_rights: '${insDoc.map.access_rights}', username: '${insDoc.map.username}'},
							[true, true]);"
						onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">

					<td><input type="checkbox" name="deleteDocument" id="deleteDocument" value="${insDoc.map.doc_id},${insDoc.map.doc_format}"></td>
					<td>${insDoc.map.visit_id}</td>
					<td>${insDoc.map.doc_name}</td>
					<td><font class="${insDoc.map.doc_format}">${insDoc.map.template_name}</font></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${insDoc.map.doc_date}"/></td>
					<td>${insDoc.map.username}</td>
				</tr>
			</c:forEach>
			</c:if>
	</table>
</div>
<insta:noresults hasResults="${hasResults}"/>
<div id="actions" class="screenActions">
	<input type="hidden" name="validate" value=""/>
	<input type="button" name="deleteDocuments" value="Delete" onclick="return deleteSelected(event, document.forms['addNewCase']);"/> |
	<a href="InsuranceGenericDocuments.do?_method=addPatientDocument&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}&insurance_id=${ifn:cleanURL(param.insurance_id)}">Add Document</a>
	| <a href="javascript:void(0)" onclick="funClose();">Case List</a>
</div>
</form>
</body>
</html>
