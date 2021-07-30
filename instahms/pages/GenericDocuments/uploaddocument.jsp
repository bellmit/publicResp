<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="laboratory.editdocument.savedocument.title"/></title>

	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="op_case_form_template" value="OutPatientDocuments.do"/>
	<c:set target="${actionUrlMap}" property="lab_test_doc" value="${cpath}/Laboratory/AddrEditTestDocuments.do"/>
	<c:set target="${actionUrlMap}" property="rad_test_doc" value="${cpath}/Radiology/AddrEditTestDocuments.do"/>
	<c:set target="${actionUrlMap}" property="service" value="${cpath}/Services/ServiceReports.do"/>

	<jsp:useBean id="docTypeMap" class="java.util.HashMap"/>
	<c:set target="${docTypeMap}" property="op_case_form_template" value="4"/>
	<c:set target="${docTypeMap}" property="lab_test_doc" value="SYS_LR"/>
	<c:set target="${docTypeMap}" property="rad_test_doc" value="SYS_RR"/>

	<jsp:useBean id="urlForPrint" class="java.util.HashMap"/>
	<c:set target="${urlForPrint}" property="op_case_form_template" value="OutpatientDocumentsPrint.do"/>
	<c:set target="${urlForPrint}" property="lab_test_doc" value="${cpath}/Laboratory/TestDocumentsPrint.do"/>
	<c:set target="${urlForPrint}" property="rad_test_doc" value="${cpath}/Radiology/TestDocumentsPrint.do"/>
	<c:set target="${urlForPrint}" property="service" value="${cpath}/Service/ServiceReportsPrint.do"/>

	<c:set var="actionUrl" value="GenericDocumentsAction.do"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>

	</c:if>
	<insta:link type="script" file="hmsvalidation.js" />
	<script>
		var contextPath = '${cpath}';
		var actionUrl = '${actionUrl}';
		var mr_no = '${ifn:cleanJavaScript(param.mr_no)}';
		var format = "doc_fileupload";
		var doc_id = '${document_details.doc_id}';
		var documentType = '${documentType}';
		var insurance_id = '${ifn:cleanJavaScript(param.insurance_id)}';
		var consultation_id = '${ifn:cleanJavaScript(param.consultation_id)}';
		var fun = '${ifn:cleanJavaScript(param._method)}';


		function saveDetails(action) {
			var url = action + '?_method='+document.uploadForm._method.value;
			// we append the transaction token to the url to prevent double submit of the form
			// variables tokenKey and token are initialized in main.jsp
			var urlModified = appendQueryParam(url, tokenKey, token);
			document.uploadForm.action = urlModified;
			document.uploadForm.submit();
			// document.uploadForm.next.disabled = true;
			return true;
		}
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body>

	<div class="pageHeader"><insta:ltext key="laboratory.editdocument.savedocument.uploaddocument"/></div>
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
	<form action=""	method="POST" enctype="multipart/form-data" name="uploadForm">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
		<input type="hidden" name="doc_id" value="${document_details.doc_id}"/>
		<input type="hidden" name="template_id" value="${template_details.map.template_id}">
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}" />
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
		<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/> <!-- test prescribed id -->
		<input type="hidden" name="isIncomingPatient" value="${ifn:cleanHtmlAttribute(param.isIncomingPatient)}"/>
		<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}">
		<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/>
		<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(param.operation_details_id)}"/>
		<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>


		<fieldset class="fieldSetBorder">
			<table style="margin-top: 10px" class="formtable" width="100%">

				<tr>
					<c:if test="${docNameRequired}">
					<td class="formlabel"><insta:ltext key="laboratory.editdocument.savedocument.documentname"/>: </td>
					<td class="forminfo"><input type="text" name="doc_name" value="${param._method == 'show'?document_details.doc_name:''}"
						class="required field" title="document name is required." size="35"/></td>
					</c:if>
					<c:if test="${docDateRequired}">
						<td class="formlabel"><insta:ltext key="laboratory.editdocument.savedocument.documentdate"/>: </td>
						<c:set var="docDate" value="today"/>
						<c:if test="${param._method != 'add'}">
							<fmt:formatDate pattern="dd-MM-yyyy" value="${document_details.doc_date}" var="docDate"/>
						</c:if>
						<td class="forminfo"><insta:datewidget name="doc_date" id="doc_date" value="${docDate}" required="true"/></td>
					</c:if>
				</tr>
				<tr>
					<c:choose>
						<c:when test="${!specialized}">
							<td class="formlabel"><insta:ltext key="laboratory.editdocument.savedocument.documenttype"/>: </td>
							<td class="forminfo">
								<select name="doc_type" id="doc_type" class="validate-not-empty dropdown" title="Document Type is mandatory.">
                   <option value="">--select--</option>
                   <c:forEach var="docDetails" items="${doc_details}">
                       <option value="${docDetails.get('doc_type_id')}" ${document_details.doc_type == docDetails.get('doc_type_id') ? 'selected' : ''}>${docDetails.get("doc_type_name")}</option>
                   </c:forEach>
                 </select>
							</td>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="doc_type" value="${docTypeMap[documentType]}"/>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${param.format == 'doc_fileupload'}">
							<td class="formlabel"><insta:ltext key="laboratory.editdocument.savedocument.uploaddocument"/> : </td>
							<td class="forminfo"><input type="file" name="doc_content_bytea" id="doc_content_bytea"
								class=" ${param._method == 'show'?'':'required'}"
								title="please upload the file" size="0" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/> <br/>
								<b><insta:ltext key="laboratory.editdocument.savedocument.uploadlimit"/></b>
								<c:choose>
									<c:when test="${param._method == 'show'}">
										<label><b><insta:ltext key="laboratory.editdocument.savedocument.currentfile"/>:</b></label>
										<c:set var="printUrl" value="GenericDocumentsPrint.do"/>
										<c:if test="${specialized}">
											<c:set var="printUrl" value="${urlForPrint[documentType]}"/>
										</c:if>
										<a href='<c:out value="${printUrl}?_method=print&doc_id=${document_details.doc_id}"/>' target="_blank"><b><insta:ltext key="laboratory.editdocument.savedocument.view"/></b></a>
									</c:when>
									<c:otherwise>

									</c:otherwise>
								</c:choose>
							</td>	
						</c:when>
						<c:otherwise>
							<td class="formlabel"><insta:ltext key="laboratory.editdocument.savedocument.documentlocation"/>: </td>
							<td><input type="text" name="doc_location" id="doc_location" value="${document_details.doc_location}"
									class="required" title="Document Location is mandatory."/></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
		</fieldset>

		<jsp:useBean id="templatesActionUrlMap" class="java.util.HashMap"/>
		<c:set target="${templatesActionUrlMap}" property="op_case_form_template" value="/outpatient/OpCaseFormAction.do"/>
		<c:set var="templatesActionUrl" value="GenericDocumentsAction.do"/>
		<c:set var="doc_type" value="" />
		<c:if test="${specialized}">
			<c:set var="templatesActionUrl" value="${templatesActionUrlMap[documentType]}"/>
		</c:if>
		<table class="screenActions">
			<tr>
				<td>
					<c:choose>
						<c:when test="${documentType == 'mlc'}">
							<c:set var="templateUrl" value="/VisitDetailsSearch.do?_method=list&amp;_searchMethod=list&amp;_status=A&amp;sortOrder=reg_date&amp;sortReverse=true"/>
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
							<c:set var="templateUrl" value="/Insurance/InsuranceDashboard.do?_method=list"/>
						</c:when>
						<c:when test="${documentType == 'ot'}">
							<c:set var="templateUrl" value="/otservices/OperationDocumentsList.do?_method=searchOperationDocuments"/>
						</c:when>
						<c:when test="${documentType == 'service'}">
							<c:set var="templateUrl" value="/Service/Services.do?_method=serviceDetails"/>
						</c:when>
						<c:when test="${documentType == 'lab_test_doc'}">
							<c:set var="templateUrl" value="/Laboratory/TestDocumentsList.do?_method=searchTestDocuments"/>
						</c:when>
						<c:when test="${documentType == 'rad_test_doc'}">
							<c:set var="templateUrl" value="/Radiology/TestDocumentsList.do?_method=searchTestDocuments"/>
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
							<c:param name="prescribed_id" value="${param.prescribed_id}"/> <%-- test prescribed id --%>
							<c:param name="prescription_id" value="${param.prescription_id}"/>
							<c:param name="addDocFor" value="${param.addDocFor}"/>
							<c:param name="prescribed_id" value="${param.prescribed_id}"/>
							<c:param name="operation_details_id" value="${param.operation_details_id}"/>
							<c:param name="visitId" value="${param.visitId}"/>
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

					<button type="button" name="next" id="next" accesskey="S" onclick="saveDetails('${actionUrl}')"><b><u><insta:ltext key="laboratory.editdocument.savedocument.s"/></u></b><insta:ltext key="laboratory.editdocument.savedocument.ave"/></button>
					<c:if test="${not param.is_patient_documents_disabled}">
						| <a href="${templateUrlWithParams}">
							<c:choose>
								<c:when test="${documentType == 'service'}"> <insta:ltext key="laboratory.editdocument.savedocument.serviceconduction"/> </c:when>
								<c:otherwise><insta:ltext key="laboratory.editdocument.savedocument.patientdocuments"/></c:otherwise>
							</c:choose>
						 </a>
					</c:if>
				</td>
			</tr>
		</table>

	</form>
</body>
</html>
