<%@ page import = "java.util.HashMap" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>OP Case Form - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
	<inta:link type="js" file="ajax.js"/>
	<style>
		tr.deleted {background-color: #F2DCDC; color: gray; }
	</style>
	<script>
		var contextPath = '${cpath}';
		var roleId = '${roleId}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init('opdoc');ajaxForPrintUrls();">

<h1>OP Case Form</h1>
<insta:feedback-panel/>
<insta:patientdetails visitid="${consultation.patient_id}" showClinicalInfo="true"/>

<form action="${cpath}/Outpatient/OutPatientDocuments.do" method="POST" name="opcaseform">
	<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
	<input type="hidden" name="_method" value=""/>
	<c:set var="templateslist" value="${pagedList.dtoList}"/>
	<c:set var="doclist" value="${patientDocs.dtoList}"/>

	<h2>Case Forms: </h2>
	<insta:paginate curPage="${patientDocs.pageNumber}" numPages="${patientDocs.numPages}" totalRecords="${patientDocs.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>Select</th>
				<th>Visit No</th>
				<th>Form Name</th>
				<th>Template</th>
				<th>Date</th>
				<th>User</th>
			</tr>
			<c:set var="mr_no" value=""/>
			<c:forEach var="patientdoc" items="${doclist}" varStatus="st">
				<c:set var="mr_no" value="${patientdoc.map.mr_no}"/>
				<c:choose>
					<c:when test="${patientdoc.map.visit_status == 'A'}">
						<c:set var="flagColor" value="empty"/>
					</c:when>
					<c:when test="${patientdoc.map.visit_status == 'I'}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
				</c:choose>
				<tr class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${patientdoc.map.mr_no}', doc_id: '${patientdoc.map.doc_id}', template_id: '${patientdoc.map.template_id}',
							format: '${patientdoc.map.doc_format}', patient_id: '${patientdoc.map.patient_id}',
							printerId: '${printpreferences.map.printer_id}', access_rights: '${patientdoc.map.access_rights}',
							username: '${patientdoc.map.username}', consultation_id: '${ifn:cleanJavaScript(param.consultation_id)}'},
							[true, true, true]);"
						onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
					<td><input type="checkbox" name="deleteDocument" id="deleteDocument" value="${patientdoc.map.doc_id},${patientdoc.map.doc_format}"></td>
					<td><img src="${cpath}/images/${flagColor}_flag.gif"/> ${patientdoc.map.patient_id}</td>
					<td>${patientdoc.map.doc_name}</td>
					<td><font class="${patientdoc.map.doc_format}">${patientdoc.map.template_name}</font></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${patientdoc.map.doc_date}"/></td>
					<td>${patientdoc.map.username}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${not empty doclist}" message="No documents found for the ${mr_no}"/>
		<div style="display: ${not empty doclist ? 'block' : 'none'};" class="screenActions">
			<button type="button" name="deleteDocuments" onclick="return deleteSelected(event, document.forms['opcaseform']);" accessKey="D">
				<b><u>D</u></b>elete</button>
		</div>
		<div class="legend" style="display: ${not empty doclist ? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
			<div class="flagText">Active visit Case Forms</div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive visit Case Forms</div>
		</div>
	</div>
</form>

<div class="resultList" style="margin-top: 5px">
	<h2 style="margin-top: 10px">Select a Template: </h2>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"
		pageNumParam="templatePageNum"/>
	<table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<th>Template Name</th>
			<th>Format</th>
			<th>Select</th>
		</tr>

		<c:forEach var="template" items="${templateslist}" varStatus="st">

			<c:url var="addDocUrl" value="/Outpatient/OutPatientDocuments.do">
				<c:param name="_method" value="add"/>
				<c:param name="mr_no" value="${patient.mr_no}"/>
				<c:param name="template_id" value="${template.map.template_id}"/>
				<c:param name="consultation_id" value="${param.consultation_id}"/>
				<c:param name="format" value="${template.map.format}"/>
				<c:param name="documentType" value="${documentType}"/>
				<c:param name="specialized" value="${specialized}"/>
				<c:param name="patient_id" value="${patient.patient_id}"/>
			</c:url>

			<c:set var="templateFormat" value=""/>
			<c:set var="templateColor" value=""/>

			<c:choose>
				<c:when test="${template.map.format == 'doc_hvf_templates'}">
					<c:set var="templateFormat" value="HVF Template"/>
					<c:set var="templateColor" value="hvf"/>
				</c:when>
				<c:when test="${template.map.format == 'doc_rich_templates'}">
					<c:set var="templateFormat" value="Rich Text Template"/>
					<c:set var="templateColor" value="richtext"/>
				</c:when>
				<c:when test="${template.map.format == 'doc_pdf_form_templates'}">
					<c:set var="templateFormat" value="PDF Form Template"/>
					<c:set var="templateColor" value="pdfform"/>
				</c:when>
				<c:otherwise >
					<c:set var="templateFormat" value="RTF Template"/>
					<c:set var="templateColor" value="rtf"/>
				</c:otherwise>
			</c:choose>
			<tr class="${st.first ? 'firstRow' : ''}">
				<td>${template.map.template_name}</td>
				<td><font class="${templateColor}">${templateFormat}</font></td>
				<td><a href="<c:out value='${addDocUrl}' />" >Select</a></td>
			</tr>
		</c:forEach>
		<insta:noresults hasResults="${not empty templateslist}" message="No templates found."/>
		<c:if test="${uploadFile}">
			<tr>
				<c:url var="uploadUrl" value="/Outpatient/OutPatientDocuments.do">
					<c:param name="_method" value="add"/>
					<c:param name="format" value="doc_fileupload"/>
					<c:param name="mr_no" value="${patient.mr_no}"/>
					<c:param name="patient_id" value="${patient.patient_id}"/>
					<c:param name="consultation_id" value="${param.consultation_id}"/>
				</c:url>
				<td colspan="3" align="center">
					<a href="<c:out value='${uploadUrl}' />" >Uploadfile</a>
				</td>
			</tr>
		</c:if>
	</table>
</div>
<table class="screenActions">
	<tr>
		<td>
			<c:url var="dashboardUrl" value="/outpatient/OpListAction.do">
				<c:param name="_method" value="list"/>
				<c:param name="status" value="A"/>
				<c:param name="sortReverse" value="true"/>
			</c:url>
			<a href="${dashboardUrl}">Patient List</a>
		</td>
		<td>
			| <a href="${cpath}/outpatient/OpPrescribeAction.do?_method=list&consultation_id=${ifn:cleanJavaScript(param.consultation_id)}">Consultation and Management</a>
		</td>
		<td>
			| <a href="${cpath}/vitalForm/genericVitalForm.do?method=list&patient_id=${consultation.patient_id}&consultation_id=${ifn:cleanJavaScript(param.consultation_id)}">Vital Form</a>
		</td>
		<td><insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${consultation.mr_no}"
			label="EMR View" addPipe="true"/>
		</td>
	</tr>
</table>

</body>
</html>
