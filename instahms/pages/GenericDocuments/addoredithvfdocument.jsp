<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta http-equiv='cache-control' content='no-cache'>
<meta http-equiv='expires' content='0'>
<meta http-equiv='pragma' content='no-cache'>
<title>Edit Document - Insta HMS</title>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:choose>
	<c:when test="${documentType == 'mlc'}">
		<c:set var="viewImageFieldUrl" value="/VisitDetailsSearch.do?_method=list&amp;_status=A&amp;sortOrder=reg_date&amp;sortReverse=true"/>
	</c:when>
	<c:when test="${documentType == 'reg'}">
		<c:set var="viewImageFieldUrl" value="/pages/RegistrationDocuments.do?_method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'dietary'}">
		<c:set var="viewImageFieldUrl" value="/pages/ipservices/dietPrescribe.do?_method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'insurance'}">
		<c:set var="viewImageFieldUrl" value="/Insurance/InsuranceGenericDocuments.do?_method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'op_case_form_template'}">
		<c:set var="viewImageFieldUrl" value="/outpatient/OpCaseFormAction.do?_method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'tpapreauth'}">
		<c:set var="viewImageFieldUrl" value="/Insurance/InsuranceDashboard.do?method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'ot'}">
		<c:set var="viewImageFieldUrl" value="/otservices/OperationDocumentsList.do?_method=viewTextImage"/>
	</c:when>
	<c:when test="${documentType == 'service'}">
		<c:set var="viewImageFieldUrl" value="/Services/ServiceReports.do?_method=viewTextImage"/>
	</c:when>
	<c:otherwise>
		<c:set var="viewImageFieldUrl" value="/pages/GenericDocuments/GenericDocumentsAction.do?_method=viewTextImage"/>
	</c:otherwise>
</c:choose>

<insta:link type="script" file="hmsvalidation.js" />
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
<script>
	var nexusToken = '<%= session.getAttribute("nexus_token")%>';
	var contextPath = '${cpath}';
	var documentType = '${documentType}';
	var urlToViewTextImage = '${viewImageFieldUrl}';

	function validateForm() {

		var finalizeChkObj = document.mainform.finalizeChk;

		if (finalizeChkObj != null && !finalizeChkObj.disabled && finalizeChkObj.checked) {
			if (!finalizeDocument())
				return false;

			document.mainform._action.value = "finalize";
			document.mainform._method.value = "update";
			document.mainform.submit();
			return true;
		}

		document.mainform._action.value = "";
		document.mainform.submit();
		return true;
	}

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
	
	function captureSignature(btnElement, index) {
		$.ajax ({
			url: "//127.0.0.1:9876/devices/signaturepads/capture",
			"headers": { 
				"x-insta-nexus-token": nexusToken,
				"x-insta-nexus-user": userid,
			},
			beforeSend: function() {
				btnElement.disabled = true;
			},
			error: function() {
				window.alert('Please Make sure the nexus app is running and is configured for singnature pad support.');
				btnElement.disabled = false;
			},
			success: function(data) {
				if(data.status === 'captured'){
					document.getElementById('fieldImgSrc'+index).value= data.image;
					document.getElementById('fieldImgText'+index).value= data.image.split(',')[1];
					if(document.getElementById("signatureFieldImg"+index)){
						document.getElementById("signatureFieldImg"+index).src = data.image;
						$("#signatureFieldImg"+index).css({"display": "block"});
					} else {
						$("#signatureFieldDiv"+index).append('<img style="max-height: 100%;"'+
								 'src=' + data.image +' name="signatureFieldImg" id="signatureFieldImg'+index+'" />');
					}
					
					btnElement.disabled = false;
					return true;
				} else if(data.status === 'capturing_cancelled') {
					btnElement.disabled = false;
				} else{
					window.alert('Please Make sure the Signature pad is connected');
					btnElement.disabled = false;
				}
			}
		});
		return false;
	}

	function finalizeDocument() {
		var ok = confirm(" Warning: Document cannot be edited after finalization. \n                "
						+" Please check the document before finalization. \n\n Do you want to proceed?");
		if (!ok) {
			document.mainform._action.value = "";
			return false;
		}
		return true;
	}
	
</script>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="ajaxForPrintUrls()">
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

	<div class="pageHeader">${param._method == 'add'?'Add':'Edit'} HVF Document</div>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${not empty param.isIncomingPatient}">
			<insta:incomingpatientdetails incomingVisitId="${param.patient_id}" />
		</c:when>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" />
		</c:otherwise>
	</c:choose>
		<form name="mainform" action="${actionUrl}" method="POST" accept-charset="UTF-8">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
		<input type="hidden" name="_action" value=""/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">
		<input type="hidden" name="template_id" value="${template_details.map.template_id}">
		<input type="hidden" name="doc_id" value="${document_details.doc_id}"/>
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}" />
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
		<input type="hidden" name="doc_type" value="${template_details.map.doc_type}"/>
		<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
		<input type="hidden" name="doc_seq_pattern_id" id="doc_seq_pattern_id" value="${template_details.map.doc_seq_pattern_id}"/>
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

		<fieldset class="fieldSetBorder" style="margin-top: 5px">
		<table style="margin-top: 10px" class="formtable" width="100%">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td class="forminfo"><b>${template_details.map.template_name}</b></td>
				<td class="formlabel">Title: </td>
				<td class="forminfo">${template_details.map.title}</td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
			<tr>
				<c:if test="${docNameRequired}">
					<td class="formlabel">Document Name: </td>
					<td ><input type="text" name="doc_name" value="${param._method == 'add'?template_details.map.template_name:document_details.doc_name}"
						class="required docName " ${document_details.doc_status == 'F' ? 'disabled':''} title="document name is required."/></td>
				</c:if>
				<c:if test="${docDateRequired}">
					<td class="formlabel">Document Date: </td>
					<c:set var="docDate" value="today"/>
					<c:if test="${param._method != 'add'}">
						<fmt:formatDate pattern="dd-MM-yyyy" value="${document_details.doc_date}" var="docDate"/>
					</c:if>
					<td ><insta:datewidget name="doc_date" id="doc_date" value="${docDate}" editValue="${document_details.doc_status == 'F' ? 'true':'false'}" required="true"/></td>
				</c:if>
			</tr>
		</table>
		</fieldset>

		<fieldset class="fieldSetBorder" style="margin-top: 5px;" ${document_details.doc_status == 'F' ? 'disabled':''}>
			<table class="formtable" width="100%" id="fieldsTable">
				<c:set var="hasImagecaptureFields" value="N"/>
				<c:forEach items="${hvf_template_fields}" varStatus="status" var="fields">

					<c:choose>
						<c:when test="${documentType == 'mlc'}">
							<c:set var="imageFieldUrl" value="/VisitDetailsSearch.do?_method=list&amp;_status=A&amp;sortOrder=reg_date&amp;sortReverse=true"/>
						</c:when>
						<c:when test="${documentType == 'reg'}">
							<c:set var="imageFieldUrl" value="/pages/RegistrationDocuments.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'dietary'}">
							<c:set var="imageFieldUrl" value="/pages/ipservices/dietPrescribe.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'insurance'}">
							<c:set var="imageFieldUrl" value="/Insurance/InsuranceGenericDocuments.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'op_case_form_template'}">
							<c:set var="imageFieldUrl" value="/outpatient/OpCaseFormAction.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'tpapreauth'}">
							<c:set var="imageFieldUrl" value="/Insurance/InsuranceDashboard.do?method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'ot'}">
							<c:set var="imageFieldUrl" value="/otservices/OperationDocumentsList.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:when test="${documentType == 'service'}">
							<c:set var="imageFieldUrl" value="/Services/ServiceReports.do?_method=viewDocumentFieldImage"/>
						</c:when>
						<c:otherwise>
							<c:set var="imageFieldUrl" value="/pages/GenericDocuments/GenericDocumentsAction.do?_method=viewDocumentFieldImage"/>
						</c:otherwise>
					</c:choose>

					<c:set var="fieldIndex" value="${status.index}"/>
					<c:set var="fieldsMap" value="${param._method == 'add'?fields.map:fields}"/>
					<tr style="display:${fieldsMap.field_status eq 'I' ? 'none' : ''}">
						<td class="formlabel" style="width:30px"><font style="margin-left: 10px">${fieldsMap.field_name}:</font>
						<input type="hidden" id="field_name${fieldIndex}" value="${fieldsMap.field_name}" />
						<input type="hidden" name="field_id" id="field_id${fieldIndex}" value="${fieldsMap.field_id}" />
						<input type="hidden" name="value_id" value="${param._method == 'show'?fieldsMap.value_id:''}" />
						<input type="hidden" name="field_input" value="${fieldsMap.field_input}" />
						<input type="hidden" name="fieldImgText" id="fieldImgText${fieldIndex}" value=""/>
						<input type="hidden" name="device_ip" id="device_ip${fieldIndex}" value=""/>
						<input type="hidden" name="device_info" id="device_info${fieldIndex}" value=""/>
						</td>
						<td style="width:200px" valign="top">
							<c:choose>
								<c:when test="${fieldsMap.field_input eq 'E'}">
									<table>
										<tr>
											<td class="fieldSetBorder" style="width:520px;">

											<textarea style="display:none;" name="field_value" id="field_value" rows="${fieldsMap.num_lines}">${fieldsMap.default_value}</textarea>
												<div id="signatureFieldDiv${fieldIndex}" style="width:500px;height:100px;">
													<c:if test="${param._method != 'add'}">

														<c:url var="imageFieldUrl" value="${imageFieldUrl}">
															<c:param name="format" value="${param.format}"/>
															<c:param name="doc_image_id" value="${fieldsMap.doc_image_id}"/>
														</c:url>

														<img style="display:${not empty fieldsMap.image_url ? 'block' : 'none'}; max-height: 100%;"
															 src='<c:out value="${imageFieldUrl}"/>' name="signatureFieldImg" id="signatureFieldImg${fieldIndex}" />
													 </c:if>
												</div>
											</td>
											<td valign="top">
												<table style="width:250px">
													<tr>
														<td style="width:80px" valign="top">
															<c:set var="hasImagecaptureFields" value="Y"/>
															<button id="startCaptureBtn${fieldIndex}" name="startCaptureBtn"
																value="connectAndClear" onclick=" return captureSignature(this, '${fieldIndex}');"
																class="button">Start Capture</button>
														</td>
														<td style="width:80px" valign="top">
															<input type="hidden" name="fieldImgSrc" id="fieldImgSrc${fieldIndex}" value="" />
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</c:when>
								<c:otherwise>
									<textarea name="field_value" id="field_value" cols="85" rows="${fieldsMap.num_lines}">${fieldsMap.default_value}</textarea>
							</c:otherwise>
						</c:choose>
						</td>
					</tr>
				</c:forEach>
			</table>
		</fieldset>
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
					<c:set var="templateUrl" value="/pages/GenericDocuments/GenericDocumentsAction.do?_method=addPatientDocument"/>
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
				<c:when test="${empty document_details}">
					<input type="button" name="save" id="saveBtn" value="Save & Print" accesskey="P" onclick="return validateForm();"/>
					<button type="button" style="display:none" name="save_finalise" id="save_finalise" onclick="return saveAndFinaliseForm();" ></button>
				</c:when>
				<c:when test="${not empty document_details && document_details.doc_status == 'P'}">
					<input type="checkbox" name="finalizeChk" id="finalizeChk" value=""> Mark Finalized
					<input type="button" name="save" id="saveBtn" value="Save & Print" accesskey="P" onclick="return validateForm();"/>
					<button type="button" style="display:none" name="save_finalise" id="save_finalise" onclick="return saveAndFinaliseForm();" ></button>
				</c:when>
				<c:otherwise>
					<input type="checkbox" name="finalizeChk" id="finalizeChk" value=""
							disabled="disabled" checked="checked"> Mark Finalized
				</c:otherwise>
			</c:choose>


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
					<c:set var="searchUrl" value="/pages/GenericDocuments/GenericDocumentsAction.do?_method=searchPatientGeneralDocuments"/>
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

			<c:if test="${param._method == 'add'}">| <a href="${templateUrlWithParams}" >Choose Template</a></c:if>
			| <a href='<c:out value="${searchUrlWithParams}"/>'>
				<c:choose>
						<c:when test="${documentType == 'service'}"> Service Conduction </c:when>
						<c:otherwise>Patient Documents</c:otherwise>
				</c:choose>
			</a>
		</div>

		<div style="float: right">
			<table>
				<tr>
					<c:if test="${not empty hasImagecaptureFields && hasImagecaptureFields eq 'Y'}">
					<td> Server Status </td> <td>&nbsp;</td>
					<td> <img src="" id="serverStatus"/> </td> <td>&nbsp;</td>
					</c:if>
					<td>
					<insta:selectdb table="printer_definition" name="printerDef" id="printerDefId"
						displaycol="printer_definition_name" valuecol="printer_id" value="${defaultPrintDefId}"/>
					</td>
				</tr>
			</table>
		</div>
</form>

<form name="signForm">
<input type="hidden" id="signFormFieldId" value=""/>

</form>
<form class='new-ux hide' action="GenericDocumentsAction.do" method="GET" name="generalDocForm">
	<input type="hidden" name="_method" value="deleteDocuments"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
	<input type="hidden" name="_searchMethod" value="searchPatientGeneralDocuments"/>
	<input type="hidden" name="deleteDocument" value="${document_details.doc_id},doc_hvf_templates"/>
	<input type="submit" id="delete-doc" accesskey="D"  value="Delete" style="display:none" />
<form>
</body>
</html>
