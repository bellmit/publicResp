<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Edit Document - Insta HMS</title>
	<insta:link type="js" file="genericdocuments/rtfdocument.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCDocumentsAction.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationDocuments.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceGenericDocuments.do"/>
	<c:set target="${actionUrlMap}" property="dietary" value="DietaryGenericDocuments.do"/>
	<c:set target="${actionUrlMap}" property="op_case_form_template" value="OutPatientDocuments.do"/>
	<c:set target="${actionUrlMap}" property="service" value="ServiceReports.do"/>
	<c:set target="${actionUrlMap}" property="ot" value="AddrEditOperationDocuments.do"/>

	<jsp:useBean id="docTypeMap" class="java.util.HashMap"/>
	<c:set target="${docTypeMap}" property="mlc" value="4"/>
	<c:set target="${docTypeMap}" property="service" value="SYS_ST"/>
	<c:set target="${docTypeMap}" property="reg" value="SYS_RG"/>
	<c:set target="${docTypeMap}" property="insurance" value="SYS_INS"/>
	<c:set target="${docTypeMap}" property="dietary" value="SYS_DIE"/>
	<c:set target="${docTypeMap}" property="op_case_form_template" value="SYS_OP"/>
	<c:set target="${docTypeMap}" property="ot" value="SYS_OT"/>
	<c:choose>
		<c:when test="${param.is_new_ux}">
			<insta:link type="css" file="genericdocuments/new-ux-doc-styles.css"/>
		</c:when>
	</c:choose>
	<c:set var="actionUrl" value="GenericDocumentsAction.do"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
		<c:set var="doc_type" value="${docTypeMap[documentType]}"/>
	</c:if>
	<style type="text/css">
		input[type=text].docName {
			width: 200px;
		}
	</style>
	<script>
		var contextPath = '${cpath}';
		var actionUrl = '${actionUrl}';
		var mr_no = '${ifn:cleanJavaScript(param.mr_no)}';
		var template_id = '${template_details.map.template_id}';
		var format = '${ifn:cleanJavaScript(param.format)}';
		var doc_id = '${document_details.doc_id}';
		var documentType = '${documentType}';
		var insurance_id = '${ifn:cleanJavaScript(param.insurance_id)}';
		var patient_id = '${ifn:cleanJavaScript(param.patient_id)}';
		var consultation_id = '${ifn:cleanJavaScript(param.consultation_id)}';
		var prescription_id = '${ifn:cleanJavaScript(param.prescription_id)}';

		function saveAndFinaliseForm() {
		document.mainform._action.value = "finalize";
		if ('${document_details.doc_id}') {
			document.mainform._method.value = "update";
		} else {
			document.mainform._method.value = "create";
		}
		document.mainform.submit();
		return true;
		}
	</script>
</head>

<body onload="getRtfDocument(),ajaxForPrintUrls();">

	<div class="pageHeader">${param._method == 'add'?'Add':'Edit'} RTF Document</div>
	<insta:feedback-panel/>
	
	<form name="mainform" action="${actionUrl}?_method=${param._method == 'add'?'create':'update'}"
			method="POST" enctype="multipart/form-data">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
		<input type="hidden" name="_action" value=""/>
		<input type="hidden" name="doc_id" value="${document_details.doc_id}"/>
		<input type="hidden" name="template_id" value="${template_details.map.template_id}">
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}" />
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}">
		<input type="hidden" name="doc_type" value="${template_details.map.doc_type}"/>
		<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
		<input type="hidden" name="isIncomingPatient" value="${ifn:cleanHtmlAttribute(param.isIncomingPatient)}"/>
		<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/>
		<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(param.operation_details_id)}"/>
		<input type="hidden" name="visit_id" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
		<input type="hidden" name="is_new_ux" value="${ifn:cleanHtmlAttribute(param.is_new_ux)}"/>

		<c:if test="${param.documentType eq 'mlc'}">
			<input type="hidden" name="filterClosed" value="true">
			<input type="hidden" name="statusActive" value="on"/>
			<input type="hidden" name="typeAll" value="on"/>
			<input type="hidden" name="visitAll" value="on" />
			<input type="hidden" name="sortOrder" value="mrno"/>
			<input type="hidden" name="sortReverse" value="true" />
		</c:if>

		<fieldset class="fieldSetBorder">
			<table style="margin-top: 10px" class="formtable" width="100%">
				<tr>
					<td class="formlabel">Template Name: </td>
					<td class="forminfo"><b>${template_details.map.template_name}</b></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<c:if test="${docNameRequired}">
					<td class="formlabel">Document Name: </td>
					<td ><input type="text" name="doc_name" value="${param._method == 'add'?template_details.map.template_name:document_details.doc_name}"
						class="required docName" title="document name is required." size="35" ${document_details.doc_status == 'F' ? 'disabled': ''}/></td>
					</c:if>
					<c:if test="${docDateRequired}">
						<td class="formlabel">Document Date: </td>
						<c:set var="docDate" value="today"/>
						<c:if test="${param._method != 'add'}">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${document_details.doc_date}" var="docDate"/>
						</c:if>
						<td ><insta:datewidget name="doc_date" id="doc_date" value="${docDate}" required="true" editValue="${document_details.doc_status == 'F' ? 'true':'false'}"/></td>
					</c:if>
				</tr>
				<tr>
					<td class="formlabel">Upload Document : </td>
					<td ><input id="upload_rtf" ${document_details.doc_status == 'F' ? 'disabled': ''} type="file" name="doc_content_bytea" id="doc_content_bytea" ${param._method == 'show'?'':'class="required"'}
						title="please upload the file" size="25" accept="<insta:ltext key="upload.accept.rtf"/>"/> <b>(Upload limit: 10MB)</b></td>
				</tr>
			</table>
		</fieldset>

		<table style="margin-top: 10px;width=50em">
			<tr>
				<td align="left" >
					To edit or add an RTF document, do the following steps:
					<ul align="left" class="numbers">
						<li>You will be prompted for opening/saving a file that is being downloaded.
								Save this file to any directory on your computer.</li>
						<li>Open and edit the document using an editor that supports RTF
								(eg, Wordpad, MS Word, OpenOffice)</li>
						<li>Save the document in the editor</li>
						<li>Upload the saved file in this screen, by selecting the file in the Upload Document field.</li>
						<li>Click on save to actually upload the document. Now, the copy on the local disk can be deleted.</li>
					</ul>
				</td>
			</tr>
		</table>

		<table id="actions" class="screenActions">
			<c:choose>
				<c:when test="${documentType == 'mlc'}">
					<c:set var="templateUrl" value="/VisitDetailsSearch.do?_method=list&amp;_status=A&amp;sortOrder=reg_date&amp;sortReverse=true"/>
				</c:when>
				<c:when test="${documentType == 'reg'}">
					<c:set var="templateUrl" value="/pages/RegistrationDocuments.do?_method=addPatientDocument"/>
				</c:when>
				<c:when test="${documentType == 'dietary'}">
					<c:set var="templateUrl" value="/pages/ipservices/dietPrescribe.do?_method=getPrescriptionScreen"/>
				</c:when>
				<c:when test="${documentType == 'insurance'}">
					<c:set var="templateUrl" value="/Insurance/InsuranceGenericDocuments.do?_method=addPatientDocument"/>
				</c:when>
				<c:when test="${documentType == 'op_case_form_template'}">
					<c:set var="templateUrl" value="/outpatient/OpCaseFormAction.do?_method=show"/>
				</c:when>
				<c:when test="${documentType == 'tpapreauth'}">
					<c:set var="templateUrl" value="/Insurance/InsuranceDashboard.do?method=list"/>
				</c:when>
				<c:when test="${documentType == 'ot'}">
					<c:set var="templateUrl" value="/otservices/OperationDocumentsList.do?_method=searchOperationDocuments"/>
				</c:when>
				<c:when test="${documentType == 'service'}">
					<c:set var="templateUrl" value="/Services/ServiceReports.do?_method=addPatientDocument"/>
				</c:when>
				<c:otherwise>
					<c:set var="templateUrl" value="GenericDocumentsAction.do?_method=addPatientDocument"/>
				</c:otherwise>
			</c:choose>
			<c:url var="templateUrlWithParams" value="${templateUrl}">
				<c:if test="${documentType != 'tpapreauth'}">
					<c:param name="doc_id" value="${document_details.doc_id}"/>
					<c:param name="template_id" value="${template_details.map.template_id}"/>
					<c:param name="mr_no" value="${param.mr_no}"/>
					<c:param name="patient_id" value="${param.patient_id}"/>
					<c:param name="insurance_id" value="${param.insurance_id}" />
					<c:param name="consultation_id" value="${param.consultation_id}"/>
					<c:param name="doc_type" value="${template_details.map.doc_type}"/>
					<c:param name="prescription_id" value="${param.prescription_id}"/>
					<c:param name="prescribed_id" value="${param.prescribed_id}"/>
					<c:param name="operation_details_id" value="${param.operation_details_id}"/>
					<c:param name="visit_id" value="${param.visitId}"/>
				</c:if>
				<c:choose>
					<c:when test="${specialized}">
						<c:param name="format" value="${param.format}"/>
					</c:when>
					<c:otherwise>
						<c:param name="format" value="All"/>
					</c:otherwise>
				</c:choose>
			</c:url>

			<c:choose>
				<c:when test="${documentType == 'mlc'}">
					<c:set var="searchUrl" value="/VisitDetailsSearch.do?_method=list&amp;_searchMethod=list&amp;_status=A&amp;sortOrder=reg_date&amp;sortReverse=true"/>
				</c:when>
				<c:when test="${documentType == 'reg'}">
					<c:set var="searchUrl" value="/pages/RegistrationDocuments.do?_method=searchPatientGeneralDocuments"/>
				</c:when>
				<c:when test="${documentType == 'dietary'}">
					<c:set var="searchUrl" value="/pages/ipservices/dietPrescribe.do?_method=getPrescriptionScreen"/>
				</c:when>
				<c:when test="${documentType == 'insurance'}">
					<c:set var="searchUrl" value="/Insurance/InsuranceGenericDocuments.do?_method=searchPatientGeneralDocuments"/>
				</c:when>
				<c:when test="${documentType == 'op_case_form_template'}">
					<c:set var="searchUrl" value="/outpatient/OpCaseFormAction.do?_method=show"/>
				</c:when>
				<c:when test="${documentType == 'tpapreauth'}">
					<c:set var="searchUrl" value="/Insurance/PreAuthorizationForms.do?_method=searchPatientGeneralDocuments"/>
				</c:when>
				<c:when test="${documentType == 'ot'}">
					<c:set var="searchUrl" value="/otservices/OperationDocumentsList.do?_method=searchOperationDocuments"/>
				</c:when>
				<c:when test="${documentType == 'service'}">
					<c:set var="searchUrl" value="/Service/Services.do?_method=serviceDetails"/>
				</c:when>
				<c:when test="${documentType == 'lab_test_doc'}">
					<c:set var="searchUrl" value="/Laboratory/TestDocumentsList.do?_method=searchTestDocuments"/>
				</c:when>
				<c:when test="${documentType == 'rad_test_doc'}">
					<c:set var="searchUrl" value="/Radiology/TestDocumentsList.do?_method=searchTestDocuments"/>
				</c:when>
				<c:otherwise>
					<c:set var="searchUrl" value="GenericDocumentsAction.do?_method=searchPatientGeneralDocuments"/>
				</c:otherwise>
			</c:choose>
			<c:url var="searchUrlWithParams" value="${searchUrl}">
				<c:if test="${documentType != 'tpapreauth'}">
					<c:param name="doc_id" value="${document_details.doc_id}"/>
					<c:param name="template_id" value="${template_details.map.template_id}"/>
					<c:param name="mr_no" value="${param.mr_no}"/>
					<c:param name="patient_id" value="${param.patient_id}"/>
					<c:param name="insurance_id" value="${param.insurance_id}" />
					<c:param name="consultation_id" value="${param.consultation_id}"/>
					<c:param name="doc_type" value="${template_details.map.doc_type}"/>
					<c:param name="prescription_id" value="${param.prescription_id}"/>
					<c:param name="prescribed_id" value="${param.prescribed_id}"/>
					<c:param name="operation_details_id" value="${param.operation_details_id}"/>
					<c:param name="visit_id" value="${param.visitId}"/>
				</c:if>
				<c:choose>
					<c:when test="${specialized}">
						<c:param name="format" value="${param.format}"/>
					</c:when>
					<c:otherwise>
						<c:param name="format" value="All"/>
					</c:otherwise>
				</c:choose>
			</c:url>

			<tr>
				<td>
					<input type="button" name="next" id="saveBtn" value="Save" onclick="document.mainform.submit()"/>
					<button type="button" style="display:none" name="save_finalise" id="save_finalise" onclick="return saveAndFinaliseForm();" ></button>
					<c:if test="${param._method == 'add'}">| <a href="${templateUrlWithParams}">Choose Template</a></c:if>
					| <a href='<c:out value="${searchUrlWithParams}"/>'>
						<c:choose>
							<c:when test="${documentType == 'service'}"> Service Conduction </c:when>
							<c:otherwise>Patient Documents</c:otherwise>
						</c:choose>
					</a>
				</td>
			</tr>
		</table>

	</form>
	<form class='new-ux hide' action="GenericDocumentsAction.do" method="GET" name="generalDocForm">
		<input type="hidden" name="_method" value="deleteDocuments"/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="_searchMethod" value="searchPatientGeneralDocuments"/>
		<input type="hidden" name="deleteDocument" value="${document_details.doc_id},doc_rtf_templates"/>
		<input type="submit" id="delete-doc" accesskey="D"  value="Delete" style="display:none" />
	<form>
</body>
</html>
