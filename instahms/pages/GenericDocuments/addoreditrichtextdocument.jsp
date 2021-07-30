<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Document - Insta HMS</title>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<script>
	var contextPath = '${cpath}';
	var mrNo = '${ifn:cleanJavaScript(param.mr_no)}';
	var documentType = '${documentType}';

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
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<insta:link type="js" file="genericdocuments/richtextdocument.js"/>
<c:choose>
	<c:when test="${param.is_new_ux}">
		<insta:link type="css" file="genericdocuments/new-ux-doc-styles.css"/>
	</c:when>
</c:choose>
	<style type="text/css">
		input[type=text].docName {
			width: 200px;
		}
	</style>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="ajaxForPrintUrls();">

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


	<c:set var="actionUrl" value="GenericDocumentsAction.do"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
		<c:set var="doc_type" value="${docTypeMap[documentType]}"/>
	</c:if>

	<div class="pageHeader">${param._method == 'add'?'Add':'Edit'} Rich Text Document</div>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${param.isIncomingPatient}">
			<insta:incomingpatientdetails incomingVisitId="${param.patient_id}" />
		</c:when>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" />
		</c:otherwise>
	</c:choose>

		<form name="mainform" action="${actionUrl}" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
		<input type="hidden" name="_action" value=""/>
		<c:set var="templateDetailsMap" value="${param._method == 'add'?template_details.map:template_details}"/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">
		<input type="hidden" name="template_id" value="${templateDetailsMap.template_id}">
		<input type="hidden" name="doc_id" value="${document_details.doc_id}"/>
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id) }"/>
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}" />
		<input type="hidden" name="doc_type" value="${templateDetailsMap.doc_type}"/>
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
				<td class="forminfo">${templateDetailsMap.template_name}</td>
				<td class="formlabel">Title: </td>
				<td class="forminfo">${templateDetailsMap.title}</td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
			<tr>
				<c:if test="${docNameRequired}">
					<td class="formlabel">Document Name: </td>
					<td ><input type="text" name="doc_name" value="${param._method == 'add'?templateDetailsMap.template_name:document_details.doc_name}"
						class="required docName field" title="document name is required." ${document_details.doc_status == 'F' ? 'disabled':''}/></td>
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
		</table>
		</fieldset>
		<table style="margin-top: 10px" width="100%">
			<tr>
				<c:set var="imageURLAction" value="/pages/GenericDocuments/PatientGeneralImageAction.do" />
				<c:if test="${documentType eq 'insurance'}">
					<c:set var="imageURLAction" value="/Insurance/InsuranceGenericDocuments/PatientGeneralImageAction.do" />
				</c:if>

				<c:url value="${imageURLAction}" var="imageUrl">
					<c:param name="_method" value="getPatientImages"/>
					<c:param name="mr_no" value="${empty param.mr_no?patient.mr_no:param.mr_no}"/>
					<c:param name="insurance_id" value="${param.insurance_id}"/>
				</c:url>
				<td ><a href='<c:out value="${imageUrl}"/>' target="_blank"><b>View/Edit Images</b></a></td>
			</tr>
			<tr>
				<td >
					<textarea id="doc_content_text" name="doc_content_text" style="width: 480pt; height: 650;">
					<c:choose>
						<c:when test="${not empty templateDetailsMap.template_content}">
							<c:out value="${templateDetailsMap.template_content}"/>
						</c:when>
						<c:otherwise>
							<c:out value="${templateDetailsMap.doc_content_text}"/>
						</c:otherwise>
					</c:choose>
					</textarea>
				</td>
			</tr>
		</table>
		<div class="screenActions" style="float: left">
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
						<c:param name="addDocFor" value="${param.addDocFor}"/>
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
						<c:param name="addDocFor" value="${param.addDocFor}"/>
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

				<button type="button" name="save" id="saveBtn" accesskey="P" onclick="document.mainform.submit()">
					Save & <b><u>P</u></b>rint</button>
				<button type="button" style="display:none" name="save_finalise" id="save_finalise" onclick="return saveAndFinaliseForm();"></button> 
				<c:if test="${param._method == 'add'}"> | <a href="${templateUrlWithParams}" >Choose Template</a></c:if>
				| <a href='<c:out value="${searchUrlWithParams}"/>'>
					<c:choose>
						<c:when test="${documentType == 'service'}"> Service Conduction </c:when>
						<c:otherwise>Patient Documents</c:otherwise>
					</c:choose>
				</a>
		</div>
		<div style="float: right">
			<insta:selectdb name="printerDef" id="printerDefId" table="printer_definition" displaycol="printer_definition_name"
					valuecol="printer_id" value="${defaultPrintDefId}"/>
		</div>
	</form>
	<form  class='new-ux hide' action="GenericDocumentsAction.do" method="GET" name="generalDocForm">
		<input type="hidden" name="_method" value="deleteDocuments"/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="_searchMethod" value="searchPatientGeneralDocuments"/>
		<input type="hidden" name="deleteDocument" value="${document_details.doc_id},doc_rich_templates"/>
		<input type="submit" id="delete-doc" accesskey="D"  value="Delete" style="display:none" />
	<form>
</body>
</html>

