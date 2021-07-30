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
<insta:link type="script" file="signature_pad.min.js" />
<insta:link type="js" file="genericdocuments/openEditableDoc.js"/>
<c:choose>
	<c:when test="${param.is_new_ux}">
		<insta:link type="css" file="genericdocuments/new-ux-doc-styles.css"/>
	</c:when>
</c:choose>
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

<jsp:useBean id="currentDate" class="java.util.Date"/>
<fmt:formatDate var="document_date" pattern="dd-MM-yyyy" value="${currentDate}"/>

<c:set var="docDate" value="today"/>
<c:if test="${param._method != 'add'}">
	<fmt:formatDate pattern="dd-MM-yyyy" value="${document_details.doc_date}" var="docDate"/>
</c:if>

<insta:link type="script" file="hmsvalidation.js" />

<c:set var="pdfUrl" value="${cpath}/pages/GenericDocuments/GenericDocumentsAction.do?_method=openPdfForm"/>
<c:if test="${documentType != 'tpapreauth'}">
	<c:set var="pdfUrl" value="${pdfUrl}&doc_id=${document_details.doc_id}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&template_id=${template_details.map.template_id}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&mr_no=${param.mr_no}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&patient_id=${param.patient_id}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&insurance_id=${param.insurance_id}" />
	<c:set var="pdfUrl" value="${pdfUrl}&consultation_id=${param.consultation_id}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&doc_type=${template_details.map.doc_type}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&prescription_id=${param.prescription_id}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&doc_name=${param._method == 'add'?template_details.map.template_name:document_details.doc_name}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&doc_date=${param._method == 'add'?document_date:docDate}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&format=${param.format}"/>
	<c:set var="pdfUrl" value="${pdfUrl}&display=view"/>
	<c:set var="pdfUrl" value="${pdfUrl}&is_new_ux=true"/>
</c:if>


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

		document.mainform._action.value = "saveExtFields";
		document.mainform._method.value = "update";
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

	function initOnLoad() {
		ajaxForPrintUrls();
		if (document.getElementById("embededPdfFieldSet") != null)
			setTimeout(document.getElementById("embededPdfFieldSet").style.display = 'block', 100000);
		if (document.getElementById("embededPdfUrl") != null)
		setTimeout(document.getElementById("embededPdfUrl").style.display = 'block', 100000);
		openDocInEditView ();
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
				} else {
					window.alert('Please Make sure the Signature pad is connected');
					btnElement.disabled = false;
				}
			}
		});
		return false;
	}
	
	function captureCanvasSignature(signaturePad, index) {
		const sign = signaturePad.toDataURL()
		document.getElementById('fieldImgSrc'+index).value= sign;
		document.getElementById('fieldImgText'+index).value= sign.split(',')[1];
		document.getElementById("startCaptureBtn"+index).disabled = true;
		document.getElementById("captureModified"+index).innerText = "";
		document.getElementById("saveBtn").click();
		return false;
	}
	
	function clearCanvasSignature(signaturePad, index) {
		signaturePad.clear();
		document.getElementById("startCaptureBtn"+index).disabled = false;
		return false;
	}
	
	function getBase64Image(url, callback) {
		    var xhr = new XMLHttpRequest();
		    xhr.onload = function() {
		        var reader = new FileReader();
		        reader.onloadend = function() {
		            callback(reader.result);
		        }
		        reader.readAsDataURL(xhr.response);
		    };
		    xhr.open('GET', url);
		    xhr.responseType = 'blob';
		    xhr.send();
		  //return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
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
	
	// let isModified = [];

	function finalizeDocument() {
		var ok = confirm(" Warning: Document cannot be edited after finalization. \n                "
						+" Please check the document before finalization. \n\n Do you want to proceed?");
		if (!ok) {
			document.mainform._action.value = "";
			return false;
		}
		return true;
	}

	var signaturePads = [];
	
	//Have to trigger resize on laod so that the canvas signatures are resized according to the screen dpi.
	function triggerResize() {
		var evt = window.document.createEvent('UIEvents'); 
		evt.initUIEvent('resize', true, false, window, 0); 
		window.dispatchEvent(evt);
	}
	
</script>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body style="${param.openDocInEditView ? 'display:none' : 'display:block'}" onload="initOnLoad();triggerResize();">
	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCDocumentsAction.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationDocuments.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceGenericDocuments.do"/>
	<c:set target="${actionUrlMap}" property="dietary" value="DietaryGenericDocuments.do"/>
	<c:set target="${actionUrlMap}" property="tpapreauth" value="PreAuthorizationForms.do"/>
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


	<h1>${param._method == 'add'?'Add':'Edit'} PDF Form Document</h1>
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
		<input type="hidden" name="_method" value="openPdfForm"/>
		<input type="hidden" name="_action" value=""/>
		<input type="hidden" name="doc_id" value="${document_details.doc_id}"/>
		<input type="hidden" name="template_id" value="${template_details.map.template_id}">
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}" />
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
		<input type="hidden" name="doc_type" value="${template_details.map.doc_type}"/>
		<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
		<input type="hidden" name="isIncomingPatient" value="${ifn:cleanHtmlAttribute(param.isIncomingPatient)}"/>
		<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/>
		<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(param.operation_details_id)}"/>
		<input type="hidden" name="visit_id" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
		<input type="hidden" name="is_new_ux" value="${ifn:cleanHtmlAttribute(param.is_new_ux)}"/>
		<input type="hidden" name="finalise_doc" value="${ifn:cleanHtmlAttribute(param.finalise_doc)}"/>

		<c:if test="${param.documentType eq 'mlc'}">
			<input type="hidden" name="filterClosed" value="true">
			<input type="hidden" name="statusActive" value="on"/>
			<input type="hidden" name="typeAll" value="on"/>
			<input type="hidden" name="visitAll" value="on" />
			<input type="hidden" name="sortOrder" value="mrno"/>
			<input type="hidden" name="sortReverse" value="true" />
		</c:if>

		<fieldset class="fieldSetBorder" >
		<table  class="formtable" width="100%">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td class="forminfo">${template_details.map.template_name}</td>
				<td class="formlabel"></td>
				<td></td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
			<tr>
				<c:if test="${docNameRequired}">
					<td class="formlabel">Document Name: </td>
					<td ><input type="text" name="doc_name" value="${param._method == 'add'?template_details.map.template_name:document_details.doc_name}"
						class="required docName " title="document name is required." ${document_details.doc_status == 'F' ? 'disabled': ''}/></td>
				</c:if>
				<c:if test="${docDateRequired}">
					<td class="formlabel">Document Date: </td>
					<td ><insta:datewidget name="doc_date" id="doc_date" value="${docDate}" required="true" editValue="${document_details.doc_status == 'F' ? 'true':'false'}"/></td>
				</c:if>
			</tr>
		</table>
		</fieldset>

		<c:if test="${param._method != 'add'}">

			<fieldset class="fieldSetBorder" id="embededPdfFieldSet" style="display:none;">
				<embed id="embededPdfUrl" src="${pdfUrl}" height="450px" width="940px" style="display:none;"/>
			</fieldset>
			<c:set var="fieldCanvasIndex" value="0" />

			<c:if test="${not empty pdf_template_ext_fields}">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"> Image Capture Fields </legend>
				<table class="formtable" width="100%" id="fieldsTable">
					<c:forEach items="${pdf_template_ext_fields}" varStatus="status" var="fieldsMap">

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
						
						<tr>
							<td class="formlabel">${fieldsMap.display_name}:
							<input type="hidden" name="field_id" value="${fieldsMap.field_id}" />
							<input type="hidden" name="field_input" value="${fieldsMap.field_input}" />
							<input type="hidden" name="fieldImgText" id="fieldImgText${fieldIndex}" value=""/>
							<input type="hidden" name="device_ip" id="device_ip${fieldIndex}" value=""/>
							<input type="hidden" name="device_info" id="device_info${fieldIndex}" value=""/>
							</td>
							<td class="fieldSetBorder" style="width:420px;">
								<div id="signatureFieldDiv${fieldIndex}" 
								    style="width:100%;height:100px;${not empty document_details && document_details.doc_status != 'P' ? "pointer-events: none;" : "" }">
									<c:if test="${param._method != 'add'}">

										<c:if test="${fieldsMap.field_input == 'E'}">
										
											<c:url var="imageFieldUrl" value="${imageFieldUrl}">
												<c:param name="format" value="${param.format}"/>
												<c:param name="doc_image_id" value="${fieldsMap.doc_image_id}"/>
											</c:url>
	
											<img style="display:${not empty fieldsMap.image_url ? 'block' : 'none'}; max-height: 100%;"
												 src='<c:out value="${imageFieldUrl}"/>' name="signatureFieldImg" id="signatureFieldImg${fieldIndex}" />
										</c:if>
										<c:if test="${fieldsMap.field_input == 'C'}">
						
											<c:url var="imageFieldUrl" value="${imageFieldUrl}">
												<c:param name="format" value="${param.format}"/>
												<c:param name="doc_image_id" value="${fieldsMap.doc_image_id}"/>
											</c:url>
	
											<canvas  style="width:100%; height:100%"
												name="signatureFieldImg" id="signatureFieldImg${fieldIndex}" />
											<script type="text/javascript">
												// This script should be run only after the canvas is created.
												// Handling for high DPI screen.
												function resizeCanvas() {
													var canvas = document.getElementById("signatureFieldImg${fieldIndex}");
												    var ratio =  Math.max(window.devicePixelRatio || 1, 1);
												    canvas.width = canvas.offsetWidth * ratio;
												    canvas.height = canvas.offsetHeight * ratio;
														canvas.getContext("2d").scale(ratio, ratio);
														//On every resized reload the data
											    	getBase64Image("${imageFieldUrl}",function(data){
														if(data && data.startsWith("data:image")) {
															signaturePads[${fieldCanvasIndex}].fromDataURL(data);
															document.getElementById("startCaptureBtn${fieldIndex}").disabled = true;
														}
													});
												    //signaturePad.clear(); // otherwise isEmpty() might return incorrect value
												}
	
												//Need this when you have multiple canvas signatures on a single screen
												window.addEventListener("resize", resizeCanvas);
												resizeCanvas();
												// Create a signature pad instance 
												var signaturePad = new SignaturePad(document.getElementById("signatureFieldImg${fieldIndex}"), {maxWidth: 2,
													  minDistance: 2,
													  onBegin: () => {
														  document.getElementById("captureModified${fieldIndex}").innerText="Signature Modified!"
														}});
												signaturePads.push(signaturePad);
												
											</script>
										</c:if>
									 </c:if>
								</div>
							</td>
							<td class="formlabel" style="width:100px" valign="top">
							<c:choose>
								<c:when test="${not empty document_details && document_details.doc_status == 'P'}">
									<c:if test="${fieldsMap.field_input == 'E'}">
										<button id="startCaptureBtn${fieldIndex}" name="startCaptureBtn"
											value="connectAndClear" onclick="return captureSignature(this, '${fieldIndex}');"
											class="button">Start Capture</button>
									</c:if>
									<c:if test="${fieldsMap.field_input == 'C'}">
										<button id="startCaptureBtn${fieldIndex}" name="startCaptureBtn" onclick="captureCanvasSignature( signaturePads[${fieldCanvasIndex}], '${fieldIndex}');"
												class="button">Save</button>
										<button id="clearCaptureBtn${fieldIndex}" name="clearCaptureBtn" onclick="return clearCanvasSignature( signaturePads[${fieldCanvasIndex}], '${fieldIndex}');"
												class="button">Clear</button>
										<div id="captureModified${fieldIndex}" />
		                                <c:set var="fieldCanvasIndex" value="${fieldCanvasIndex + 1}"/>
										
									</c:if>
								</c:when>
								<c:otherwise>
									<c:if test="${fieldsMap.field_input == 'C'}">
										<c:set var="fieldCanvasIndex" value="${fieldCanvasIndex + 1}"/>
									</c:if>
								</c:otherwise>
							</c:choose>
							</td>
							<td class="formlabel" style="width:100px" valign="top">
								<input type="hidden" name="fieldImgSrc" id="fieldImgSrc${fieldIndex}" value="" />
							</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			</c:if>
		</c:if>

		<table class="screenActions">
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
					<c:set var="templateUrl" value="/Insurance/InsuranceDashboard.do?_method=list"/>
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
			<tr>
				<td>
					<c:choose>
						<c:when test="${empty document_details}">
							<input type="button" name="next" id="next" value="${param._method == 'add'?'Add':'Edit'} PDF" onclick="document.mainform.submit()"/>
						</c:when>
						<c:when test="${not empty document_details && document_details.doc_status == 'P'}">
							<input type="checkbox" name="finalizeChk" id="finalizeChk" value=""> Mark Finalized
							<button type="button" style="display:none" name="save_finalise" id="save_finalise" onclick="return saveAndFinaliseForm();" ></button>
							<button type="button" name="saveBtn" id="saveBtn" accesskey="A"
								onclick="return validateForm();">
							S<b><u>A</u></b>ve</button>
							<input type="button" name="next" id="next" value="${param._method == 'add'?'Add':'Edit'} PDF" onclick="document.mainform.submit()"/>
						</c:when>
						<c:otherwise>
							<input type="checkbox" name="finalizeChk" id="finalizeChk" value=""
							disabled="disabled" checked="checked"> Mark Finalized
						</c:otherwise>
					</c:choose>
					<c:if test="${param._method == 'add'}">
						| <a href="${templateUrlWithParams}">${documentType eq 'tpapreauth' ? 'Case List' : 'Choose Template'}</a>
					</c:if>
				</td>
				<td>

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
						<c:choose>
							<c:when test="${specialized}">
								<c:param name="format" value="${param.format}"/>
							</c:when>
							<c:otherwise>
								<c:param name="format" value="All"/>
							</c:otherwise>
						</c:choose>
					</c:url>

					| <a href='<c:out value="${searchUrlWithParams}"/>'>
						<c:choose>
							<c:when test="${documentType == 'service'}"> Service Conduction </c:when>
							<c:otherwise>Patient Documents</c:otherwise>
						</c:choose>
					</a>

				</td>
				<td>
					<insta:screenlink target="_blank" screenId="patient_docs_audit_log"
					extraParam="?_method=getAuditLogDetails&al_table=patient_general_docs_audit_view&mr_no=${param.mr_no}"
						label="Audit Log" addPipe="true"/>
				</td>
			</tr>
		</table>

		<div style="float: right">
			<table>
				<tr>
					<c:if test="${not empty pdf_template_ext_fields}">
					<td> Server Status </td> <td>&nbsp;</td>
					<td> <img src="" id="serverStatus"/> </td> <td>&nbsp;</td>
					</c:if>
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
	<input type="hidden" name="deleteDocument" value="${document_details.doc_id},doc_pdf_form_templates"/>
	<input type="submit" id="delete-doc" accesskey="D"  value="Delete" style="display:none" />
<form>
</body>
</html>
