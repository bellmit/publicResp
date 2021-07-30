<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Registration Documents</title>
	<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
	<inta:link type="js" file="ajax.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script>
		var contextPath = '${cpath}';
		var roleId = '${ifn:cleanJavaScript(roleId)}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init('regDoc');ajaxForPrintUrls();">
<c:set var="regDocsList" value="${pagedList.dtoList}"/>
<h1><insta:ltext key="registration.registrationdocument.title"/></h1>
<insta:feedback-panel/>
<insta:patientdetails  visitid="${param.patient_id}" />
<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
		<tr onmouseover="hideToolBar('');">
			<th><insta:ltext key="registration.registrationdocument.visitno"/></th>
			<th><insta:ltext key="registration.registrationdocument.documentname"/></th>
			<th><insta:ltext key="registration.registrationdocument.template"/></th>
			<th><insta:ltext key="registration.registrationdocument.date"/></th>
			<th><insta:ltext key="registration.registrationdocument.user"/></th>
		</tr>
		<c:forEach var="regdocs" items="${regDocsList}">
			<tr class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
						{doc_id: ${regdocs.map.doc_id}, template_id: ${regdocs.map.template_id},
						format: '${regdocs.map.doc_format}', patient_id: '${regdocs.map.patient_id}',
						printerId: '${printpreferneces.map.printer_id}',
						access_rights: '${regdocs.map.access_rights}', username: '${regdocs.map.username}'},
						[true, true]);"
					onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
				<td>${regdocs.map.patient_id}</td>
				<td>${regdocs.map.template_name}</td>
				<td>${regdocs.map.template_name}</td>
				<td><fmt:formatDate pattern="dd-MM-yyyy" value="${regdocs.map.doc_date}"/></td>
				<td>${regdocs.map.username}</td>
			</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${not empty regDocsList}" message="No documents found for the ${param.mr_no}"/>
</div>

<div class="screenActions">
	<c:url var="addNewDoc" value="/pages/RegistrationDocuments.do">
		<c:param name="_method" value="addPatientDocument"/>
		<c:param name="mr_no" value="${patient.mr_no}"/>
		<c:param name="patient_id" value="${param.patient_id}"/>
	</c:url>
	<a href="<c:out value='${addNewDoc}'/>"><insta:ltext key="registration.registrationdocument.adddocumentlink"/></a>
</div>


</body>
</html>
