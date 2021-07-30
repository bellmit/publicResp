
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO"%>
<%@page import="org.apache.commons.beanutils.BasicDynaBean"%>
<%@page import="com.insta.hms.instaforms.AbstractInstaForms"%>
<%@page import="com.insta.hms.instaforms.PatientSectionDetailsDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="ipAndWardActivities" value="${visitType == 'i' && preferences.modulesActivatedMap.mod_wardactivities == 'Y'}"/>

<c:choose >

	<c:when test="${not empty patientPlan && patientPlan.plan_id != 0}">
		<c:set var="primaryPlan" value="${patientPlan.plan_id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="primaryPlan" value="0"/>
	</c:otherwise>
</c:choose>

<%@page import="java.sql.Timestamp"%>
<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<html>
<head>
	<script>
		var collapsiblePanels = {};
		var centerId = ${centerId};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
		var displayActivityAndDueDate = ${ipAndWardActivities};
		var prescriptions_by_generics = '${prescriptions_by_generics}';
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var consultation_reopen_time_limit = ${clinicalPrefs.consultation_reopen_time_limit};
		var no_of_sec_till_date = ${no_of_sec_till_date};
		var roleId = ${roleId};
		var reopen_consultation_after_time_limit = '${actionRightsMap.reopen_consultation_after_time_limit}';
		var tooth_numbering_system = '${genericPrefs.tooth_numbering_system}';
		var consultation_status = '${consultation_bean.status}';
		var slida = '${urlRightsMap["slida_action"]}';
		window.onbeforeunload = function() {
			var callOnBeforeUnload = document.getElementById('callOnBeforeUnload').value;
			if (callOnBeforeUnload == 'false') return ;

			for (var i=0; i<document.forms.length; i++) {
				var event = YAHOO.util.Event.getEvent(null, document.forms[i]);
				for (var key in event) {
					if (key == 'type' && event[key] == 'beforeunload') {
						if (formElValueModified(document.forms[i])) {
							return "Edited changes are not effected.";
						}
					}
				}

			}
		}
		var sys_generated_forms = ${sys_generated_forms};
		var jsonConsultationIds = ${jsonConsultationIds};

		var perdiemCodesListJSON = <%= request.getAttribute("perdiemCodesListJSON") %> ;
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		var mod_eclaim_pbm = '${preferences.modulesActivatedMap.mod_eclaim_pbm}';
		var mod_eclaim_preauth = '${preferences.modulesActivatedMap.mod_eclaim_preauth}';
		var health_authority = '${patient.health_authority}';
		var departmentId = '${patient.dept_id}';
		var diagnosis_code_type = '<%=com.insta.hms.common.Encoder.cleanJavaScript((String) request.getAttribute("defaultDiagnosisCodeType")) %>';
		var planList = <%= request.getAttribute("planList") %>;
		var planId =0;
		if(planList.length != 0){
			planId = planList[0].plan_id;
		}
		var planId1="";
		if(planList.length == 2){
			planId1 = planList[1].plan_id;
		}
		var requireERxAuth = (mod_eclaim_erx =='Y' && '${ifn:cleanJavaScript(visitType)}' =='o' && planId != 0);
		var TPArequiresPreAuth = '${TPArequiresPreAuth}';
		var TPAEAuthMode = '${TPAEAuthMode}';
		var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
		var prescribe_by_favourites = '${consultation_bean.prescribe_by_favourites}';
		var paramType = "V";
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var ceedstatus = ${ceedstatus};
		var ceedresponsemap = ${ceedResponseMapJson};
		var has_right_to_view_ceed_comments = ${actionRightsMap.view_ceed_response_comments == 'A'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};
	</script>

	<title><insta:ltext key="patient.outpatientlist.consult.details.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="outpatient/historicaldialogs.js"/>
	<insta:link type="script" file="outpatient/prescribe.js"/>
	<insta:link type="script" file="outpatient/onetimeprescriptions.js"/>
	<insta:link type="script" file="outpatient/allergies.js"/>
	<insta:link type="script" file="outpatient/healthMaintenance.js"/>
	<insta:link type="script" file="outpatient/pregnancyDetails.js"/>
	<insta:link type="script" file="outpatient/antenatal.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link type="script" file="outpatient/patientPreviousPrescriptions.js" />
	<insta:link type="script" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="outpatient/preAnaesthestheticCheckup.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="js" file="copyPaste.js"/>
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.complaintAc {
		    padding-bottom:2em;
		}

		.progrees_notes_position {
			position:relative;
			bottom:-20px;
			right:0px;
		}

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.info-overlay {
			color:#000;
			border-color: #D3D9E0 #AFB4BA #AFB4BA;
			padding: 6px;
			border-width:1px;
			border-style:solid;
			background-color: #E4EBF3;
			margin:10px;
		}
		#anchorLinks a {padding: 0px 5px;}
		table.detailList tr.zero_qty td {
			background-color:#F4FFE6;
		}


	</style>

	<script>
		
		var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
		var medDosages = ${medDosages};
		var presInstructions = <%= request.getAttribute("presInstructions") %>;
		var patientId = '${consultation_bean.patient_id}';
		var consultationId = ${ifn:cleanJavaScript(param.consultation_id)};
		var cpath='${cpath}';
		var note_taker_prefs = '${ifn:cleanJavaScript(prescriptionNoteTakerPreferences)}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var insta_form_json = ${insta_form_json};
		var group_patient_sections = '${group_patient_sections}';
		var insta_sections_json = ${insta_sections_json};

		var routesListJson = ${routes_list_json};

		function getConsultationId() {
			return consultationId;
		}

		function getUrl() {
			var anchor = document.createElement("a");
			anchor.href = cpath + "/outpatient/OpPrescribeAction.do?_method=fileUpload";
			return anchor.href;
		}
		/*
		* same method is being used while displaying in the jsp screen as well in historical consultation images.
		*/
		function displayNote(noteId, prefix, pastedImage) {
			if (empty(prefix)) prefix = "";
			var noteTakerImage = prefix + 'noteTakerImage';
			var imageTd = prefix + 'imageTd';
			var imageTr = document.getElementById(noteTakerImage);
			if (imageTr == null || imageTr == undefined) {
			} else {
				imageTr.parentNode.removeChild(imageTr);
			}
			var cell = document.getElementById(imageTd);

			if(Boolean(pastedImage)) {
				var imageEl = pastedImages[noteId]["imgTag"];
				imageEl.id = noteTakerImage;
			} else {
				var imageEl = document.createElement('img');
				imageEl.id = noteTakerImage;
				imageEl.src = "${cpath}/outpatient/OpPrescribeAction.do?_method=viewImage&image_id="+noteId;
			}
			imageEl.setAttribute('style', 'max-width:100%;max-height:100%');
			cell.style.display = 'block';
			cell.appendChild(imageEl);

			enableOtherViewLinks(noteId, prefix);
		}

		function attachImages(date, identifier, pastedImage) {
			var tab = document.getElementById('attachImages');
			var row = tab.insertRow(-1);
			row.setAttribute('id', "rowId"+identifier);
			row.setAttribute('name', 'rowName');
			row.setAttribute('class', 'status_I');

			var cell = row.insertCell(-1);
			if(Boolean(pastedImage)) {
				cell.innerHTML = '<input type="checkbox" value="'+ pastedImages[identifier]['id'] +'" name="deleteImage" onclick="togglecheckBoxValue(\''+ identifier +'\')"/>';
			} else {
				cell.innerHTML = '<input type="checkbox" value="'+ identifier +'" name="deleteImage"/>';
			}
			row.appendChild(cell);

			cell = row.insertCell(-1);
			cell.innerHTML = date;
			row.appendChild(cell);

			cell = row.insertCell(-1);
			if(Boolean(pastedImage)){
				var onClickFunction = "displayNote('"+ identifier +"', '', true)"
			} else {
				var onClickFunction = "displayNote('"+ identifier +"')";
			}
			cell.innerHTML = '<a  style="display: none;" href="#" onclick="'+ onClickFunction +'" id="viewImage'+ identifier +'" name="viewImage">View</a>';
			cell.innerHTML += '<label id="withoutView'+ identifier +'" name="withoutView" style="display: black" >View</label>';

			row.appendChild(cell);

		}

		function enableOtherViewLinks(noteId, prefix) {
			var viewImage = prefix + 'viewImage';
			var withoutView = prefix + 'withoutView';
			var rowName = prefix + 'rowName';

			var viewEls = document.getElementsByName(viewImage);
			var withoutViewEls = document.getElementsByName(withoutView);
			var rowEls = document.getElementsByName(rowName);

			for (var index=0; index<viewEls.length; index++) {
				var view = viewEls[index];
				if (view.id == viewImage+noteId) {
					view.setAttribute("style", "display: none;");
					withoutViewEls[index].setAttribute("style", "display: block;");
					rowEls[index].setAttribute("class", "status_I");
				} else {
					view.setAttribute("style", "display: block;");
					withoutViewEls[index].setAttribute("style", "display: none;");
					rowEls[index].setAttribute("class", "");
				}
			}
		}

		function postSlidaMessage(mrno) {
		    if (slida == 'A' && mrno) {
		        var slidaUrl = cpath + "/SlidaAction.do?_method=register&mr_no=" + mrno;
		        var xhr = newXMLHttpRequest();
		        xhr.open("GET",slidaUrl.toString(), false);
		        xhr.send(null);
		        if (xhr.readyState == 4 && xhr.status == 200) {
				    if (xhr.responseText!=null) {
				        eval("var resp=" + xhr.responseText+";");
				        alert (resp.message);
				    }
				}
		    }
		    return false;
		}

		var validate_diagnosis_codification = '${validate_diagnosis_codification}';
		YAHOO.util.Event.onContentReady("content", init);
		var complaintForm = null;
		var normalResult = '${prefColorCodes.map.normal_color_code}';
		var abnormalResult = '${prefColorCodes.map.abnormal_color_code}';
		var criticalResult = '${prefColorCodes.map.critical_color_code}';
		var improbableResult = '${prefColorCodes.map.improbable_color_code}';

	</script>
	<!-- the below link has to be after the variable complaintForm gets the form name.
		complaintForm is initialized iniside the init method of prescribe.js-->
	<insta:link type="script" file="outpatient/complaint.js"/>

<insta:js-bundle prefix="patient.consultation"/>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="patient.diagnosis"/>
<insta:js-bundle prefix="outpatient.vitalform"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body class="yui-skin-sam" onload="ajaxForPrintUrls(); " >
	<c:choose>
		<c:when test="${visitType == 'o'}">
			<h1><insta:ltext key="patient.outpatientlist.consult.details.consultationandmanagement"/></h1>
		</c:when>
		<c:when test="${visitType == 'i'}">
			<h1><insta:ltext key="patient.outpatientlist.consult.details.management"/></h1>
		</c:when>
	</c:choose>
	<insta:feedback-panel/>

	<c:if test="${fn:length(vaccinationsInfo) ne 0 && preferences.modulesActivatedMap.mod_vaccination == 'Y' && (patient.agein eq 'Y' ? (patient.age le 18) : (patient.agein eq 'M' || patient.agein eq 'D'))}">
		<div class="helpPanel">
			<table id="infoTable">
			<c:if test="${fn:length(vaccinationsInfo) ne 0}">
				<tr>
					<td valign="top""><img src="${cpath}/images/information.png"/><label id="labelId" style="white-space: nowrap;">${vaccinationsInfo}</label></td>
				</tr>
			</c:if>
			</table>
		</div>
	</c:if>
	<%-- when consultation is not verified following information gets displayed on the page and disables the save buttons --%>
	<div style="display:none; margin-bottom:10px; padding:10px 0 10px 10px; height: 15px; background-color:#FFC;" class="brB brT brL brR" id="infoMsgDiv">
		<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;" id="infoImg"> <img src="${cpath}/images/information.png" /></div>
		<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;" id="infoDiv"></div>
	</div>
	<insta:patientdetails patient="${patient}" showClinicalInfo="true"/>

	<form method="POST" action="${cpath}/outpatient/OpPrescribeAction.do" name="prescribeForm" autocomplete="off">

	<jsp:include flush="true" page="CommonInclude.jsp"/>

	<c:set var="mod_mrd_icd" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
	<input type="hidden" name="_method" value="update"/>
	<input type="hidden" name="dept" id="dept" value="${consultation_bean.dept_id}"/>
	<input type="hidden" name="sendErxRequest" value="N"/>
	<input type="hidden" id="consultation_id" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
	<input type="hidden" name="hiddenDescription" value="<c:out value="${consultation_bean.description}"/>">
	<input type="hidden" name="visitType" id="visitType" value="${ifn:cleanHtmlAttribute(visitType)}">
	<input type="hidden" name="isPrint" id="isPrint" value="false"/>
	<input type="hidden" name="printType" id="printType" value="pres"/>
	<input type="hidden" name="ceedcheck" id="ceedcheck" value="N"/>
	<input type="hidden" name="consult_doctor_name" id="consult_doctor_name" value="${consultation_bean.doctor_full_name}">
	<input type="hidden" name="consult_doctor_id" id="consult_doctor_id" value="${consultation_bean.doctor_name}">
	<input type="hidden" name="doc_id" id="doc_id" value="${consultation_bean.doc_id}"/>
	<input type="hidden" name="patient_id" id="patient_id" value="${consultation_bean.patient_id}"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${patient.mr_no}"/>
	<input type="hidden" name="insta_form_id" id="insta_form_id" value="${form.map.form_id}"/>
	<input type="hidden" id="dialogId" value=""/>
	<input type="hidden" id="callOnBeforeUnload" name="callOnBeforeUnload" value="true"/>
	<input type="hidden" id="pharmacy_discount_percentage" value="${orgDetails.map.pharmacy_discount_percentage}"/>
	<input type="hidden" id="pharmacy_discount_type" value="${orgDetails.map.pharmacy_discount_type}"/>	
	<fmt:formatDate var="admitted_date" value="${consultation_bean.admitted_datetime}" pattern="dd-MM-yyyy"/>
	<fmt:formatDate var="admitted_time" value="${consultation_bean.admitted_datetime}" pattern="HH:mm"/>
	<input type="hidden" id="admitted_date" value="${admitted_date}"/>
	<input type="hidden" id="admitted_time" value="${admitted_time}"/>
	<input type="hidden" name="consultationStatus" id="consultationStatus" value="${consultation_bean.status == 'A' ? 'I' : 'U'}"/>
	<%-- hiddenDescription used in script to check for not empty of
	(description/tests/services/medicines) when taking print prescription --%>

	<c:set var="requireERxAuthorization" value="${mod_eclaim_erx && visitType =='o' && patient.health_authority == 'DHA'}"/>

	<c:choose >
		<c:when test="${consultation_bean.status == 'C' or consultation_bean.cancel_status == 'C'}">
			<c:set var="status" value="C"/>
		</c:when>
		<c:otherwise>
			<c:set var="status" value="O"/>
		</c:otherwise>
	</c:choose>
	<c:choose>
		<c:when test="${consultation_bean.bill_type == 'P'}">
			<c:set var="billType" value="${consultation_bean.is_tpa == true ? 'BN-I' : 'BN'}"/>
		</c:when>
		<c:otherwise>
			<c:set var="billType" value="${BL}"/>
		</c:otherwise>
	</c:choose>
	<input type="hidden" name="tpaBill" id="tpaBill"
		value="${consultation_bean.is_tpa == true 
			&& (consultation_bean.restriction_type == 'N' || consultation_bean.restriction_type == 'P')}">
	<input type="hidden" name="billType" id="billType" value="${billType}"/>
	<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
	<input type="hidden" name="op_type" id="op_type" value="${patient.op_type}"/>
	<input type="hidden" id="mr_no" name="mr_no" value="${patient.mr_no}"/>
	<input type="hidden" name="bed_type" id="bed_type" value="${empty patient.alloc_bed_type ? patient.bill_bed_type : patient.alloc_bed_type}"/>
	<input type="hidden" name="tpa_id" id="tpa_id" value="${patient.primary_sponsor_id}"/>
	<input type="hidden" name="require_pbm_authorization" id="require_pbm_authorization" value="${patient.require_pbm_authorization}"/>
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.consult.startdateortime"/></td>
			<td>
				<fmt:formatDate value="${consultation_bean.start_datetime}" pattern="dd-MM-yyyy" var="start_date"/>
				<fmt:formatDate value="${consultation_bean.start_datetime}" pattern="HH:mm" var="start_time"/>
				<insta:datewidget name="consultation_start_date" value="${start_date}"/>
				<input type="text" name="consultation_start_time" value="${ifn:cleanHtmlAttribute(start_time)}" class="timefield"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.consult.enddateortime"/></td>
			<td>
				<fmt:formatDate value="${consultation_bean.end_datetime}" pattern="dd-MM-yyyy" var="end_date"/>
				<fmt:formatDate value="${consultation_bean.end_datetime}" pattern="HH:mm" var="end_time"/>
				<insta:datewidget name="consultation_end_date" value="${end_date}"/>
				<input type="text" name="consultation_end_time" value="${end_time}" class="timefield"/>
				<input type="hidden" name="db_consultation_end_date" value="${end_date}" />
				<input type="hidden" name="db_consultation_end_time" value="${end_time}" />
			</td>
		</tr>
	</table>
	<c:if test="${requireERxAuthorization}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.erxrequestinformation"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.pbmpresc.id"/>:</td>
					<td class="forminfo">${erxBean.map.pbm_presc_id}</td>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.erxpresc.id"/>:</td>
					<td class="forminfo">${erxBean.map.erx_presc_id}</td>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.erxreferenceno"/>.:</td>
					<td class="forminfo">
						${erxBean.map.erx_reference_no}
						<input type="hidden" id=erx_presc_id name="erx_presc_id" value="${erxBean.map.erx_presc_id}"/>
						<input type="hidden" id="erx_reference_no" name="erx_reference_no" value="${erxBean.map.erx_reference_no}"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</c:if>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.patientinformation"/></legend>
		<table id="patientInfoTable" class="formtable">
			<tr>
				<c:if test="${not empty regPref.custom_field11_label}">
					<td class="formlabel">${ifn:cleanHtml(regPref.custom_field11_label)}:</td>
					<td class="forminfo"><input type="text" name="custom_field11" value="${patient.custom_field11}"/></td>
				</c:if>
				<c:if test="${not empty regPref.custom_field12}">
					<td class="formlabel">${regPref.custom_field12}:</td>
					<td class="forminfo"><input type="text" name="custom_field12" value="${patient.custom_field12}"/></td>
				</c:if>
				<c:if test="${not empty regPref.custom_field13_label}">
					<td class="formlabel">${regPref.custom_field13}:</td>
					<td class="forminfo"><input type="text" name="custom_field13" value="${patient.custom_field13}"/></td>
				</c:if>
			</tr>
			<tr>
				<td class="formlabel">${visitType == 'o'?'Consulting':'Visiting'} <insta:ltext key="patient.outpatientlist.consult.details.doctor"/>:</td>
				<td class="forminfo">${consultation_bean.doctor_full_name}</td>
				<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.viewhistory"/>: </td>
				<td ><select name="viewHistory" id="viewHistory" onchange="return onChangeViewHistory();" class="dropdown">
						<option value="">-- Select --</option>
						<option value="previous_visit_summary"><insta:ltext key="patient.outpatientlist.consult.details.previousvisitsummary"/></option>
						<option value="historical_diagnosis"><insta:ltext key="patient.outpatientlist.consult.details.historicaldiagnosis"/></option>
						<c:if test="${visitType == 'o'}">
							<option value="historical_vitals"><insta:ltext key="patient.outpatientlist.consult.details.historicalvitals"/></option>
						</c:if>
						<option value="historical_notes"><insta:ltext key="patient.outpatientlist.consult.details.historicalnotes"/></option>
						<option value="historical_images"><insta:ltext key="patient.outpatientlist.consult.details.historicalconsulationimages"/></option>
						<option value="historical_prescriptions"><insta:ltext key="patient.outpatientlist.consult.details.historicalmanagement"/></option>
					</select>
				</td>
				<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.viewreports"/>: </td>
				<td >
					<select name="viewReports" id="viewReports" class="dropdown" onchange="return onChangeViewReports();" >
						<option value="">-- Select --</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.orderremarks"/>:</td>
				<td class="forminfo" colspan="3">
					<insta:truncLabel value="${consultation_bean.remarks}" length="15"/>
					<c:if test="${not empty consultation_bean.remarks}">
						<a href="javascript:showInfoDialog('imgOrderRemarks', '${consultation_bean.remarks}', 'topright')" >
							<img class="imgHelpText" id="imgOrderRemarks" title="Order Remarks" src="${cpath}/images/information.png"/>
						</a>
					</c:if>
				</td>
				<td class="formlabel"><insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}"
				target="_blank" label="EMR View" /></td>
				<td class="formlabel" style="text-align:left;padding-left:30px;"><insta:screenlink
				screenId="slida_action" extraParam="?_method=register&mr_no=${patient.mr_no}"
				onClickValidation="return postSlidaMessage('${patient.mr_no}');"
				target="_blank" label="SIDEXIS X-Ray" /></td>
			</tr>
		</table>
	</fieldset>


	<c:forTokens delims="," items="${form.map.sections}" var="section_id">
		<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
	</c:forTokens>

	<c:set var="vital_order_id" value="<%=SystemGeneratedSections.Vitals.getSectionId()%>" />
	<c:set var="allergies_order_id" value="<%=SystemGeneratedSections.Allergies.getSectionId()%>" />
	<c:set var="complaint_order_id" value="<%=SystemGeneratedSections.Complaint.getSectionId()%>" />
	<c:set var="triage_order_id" value="<%=SystemGeneratedSections.TriageSummary.getSectionId()%>" />
	<c:set var="consultation_notes_order_id" value="<%=SystemGeneratedSections.ConsultationNotes.getSectionId()%>" />
	<c:set var="prescription_order_id" value="<%=SystemGeneratedSections.Prescription.getSectionId()%>" />
	<c:set var="diagnosis_details_order_id" value="<%=SystemGeneratedSections.DiagnosisDetails.getSectionId()%>" />
	<c:set var="health_maintenance_order_id" value="<%=SystemGeneratedSections.HealthMaintenance.getSectionId()%>" />
	<c:set var="pregnancy_history_order_id" value="<%=SystemGeneratedSections.PregnancyHistory.getSectionId()%>" />
	<c:set var="antenatal_order_id" value="<%=SystemGeneratedSections.Antenatal.getSectionId()%>" />
	<c:set var="pre_anaesthesthetic_order_id" value="<%=SystemGeneratedSections.PreAnaesthestheticCheckup.getSectionId()%>" />

	<c:set var="fieldsetField" value="false" />
	<c:set var="pfIndex" value="0"/>
	<c:forTokens items="${form.map.sections}" delims="," var="section_id" varStatus="s_i" >
		<c:set var="secId" value="${section_id + 0}"/>
		<c:if test="${section_id > 0 && group_patient_sections == 'Y'}">
			<c:if test="${sectionsDefMap.get(secId.intValue()).map.linked_to == 'patient'}">
				<c:if test="${pfIndex == 0}">
					<c:set var="pfIndex" value="${pfIndex+1}"/>
					<c:set var="fieldsetField" value="true" />
					<div id="CollapsiblePanel" class="CollapsiblePanel">
						<div class=" title CollapsiblePanelTab"  style=" border-left:none;">
							<div id="patient_history_label" class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i><insta:ltext key="patient.outpatientlist.consult.details.patienthistory"/></i></b></div>
							<div class="fltR txtRT" style="width: 25px; margin: 10px 10px 0px">
								<img src="${cpath}/images/down.png" />
							</div>
							<div class="clrboth"></div>
						</div>
					<fieldset class="fieldSetBorder" id="fieldset" style="background-color: #F8FCFF; border-style:none ">
				</c:if>
					<jsp:include page="InstaSection.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="consultation_id" value="${param.consultation_id}"/>
						<jsp:param name="form_name" value="prescribeForm"/>
						<jsp:param name="form_type" value="Form_CONS"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
					</jsp:include>
			</c:if>
		</c:if>
		<c:if test="${fieldsetField == 'true' && s_i.last}" >
					</fieldset>
					</div>
				<script type="text/javascript">
					var mrNo = '${patient.mr_no}';
					collapsiblePanels[mrNo] = new Spry.Widget.CollapsiblePanel("CollapsiblePanel", {contentIsOpen:false});
				</script>
		</c:if>
	</c:forTokens>

	<c:forTokens items="${form.map.sections}" delims="," var="section_id">
		<c:set var="secId" value="${section_id + 0}"/>
		<c:if test="${section_id == complaint_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<jsp:include page="ComplaintInclude.jsp">
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
			</jsp:include>
		</c:if>
		<c:if test="${section_id == allergies_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<jsp:include page="Allergies.jsp" >
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="consultation_id" value="${param.consultation_id}"/>
				<jsp:param name="patient_id" value="${patient.patient_id}"/>
				<jsp:param name="mr_no" value="${patient.mr_no}"/>
				<jsp:param name="form_type" value="Form_CONS"/>
				<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == health_maintenance_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
			<jsp:include flush="true" page="HealthMaintenance.jsp">
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="consultation_id" value="${param.consultation_id}"/>
				<jsp:param name="patient_id" value="${patient.patient_id}"/>
				<jsp:param name="mr_no" value="${patient.mr_no}"/>
				<jsp:param name="form_type" value="Form_CONS"/>
				<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == pregnancy_history_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
			<jsp:include flush="true" page="PregnancyDetailsInclude.jsp">
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="consultation_id" value="${param.consultation_id}"/>
				<jsp:param name="patient_id" value="${patient.patient_id}"/>
				<jsp:param name="mr_no" value="${patient.mr_no}"/>
				<jsp:param name="form_type" value="Form_CONS"/>
				<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == antenatal_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
			<jsp:include flush="true" page="AntenatalDetails.jsp">
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="consultation_id" value="${param.consultation_id}"/>
				<jsp:param name="patient_id" value="${patient.patient_id}"/>
				<jsp:param name="mr_no" value="${patient.mr_no}"/>
				<jsp:param name="form_type" value="Form_CONS"/>
				<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == pre_anaesthesthetic_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
			<jsp:include flush="true" page="PreAnaesthestheticCheckup.jsp">
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="consultation_id" value="${param.consultation_id}"/>
				<jsp:param name="patient_id" value="${patient.patient_id}"/>
				<jsp:param name="mr_no" value="${patient.mr_no}"/>
				<jsp:param name="form_type" value="Form_CONS"/>
				<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == triage_order_id}">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.triagesummary"/></legend>
				<div id="triage_summary" style="height: 100%; margin-top: -4px;margin-left: 5px">${triage_summary}<br/></div>
			</fieldset>
		</c:if>

		<c:if test="${section_id == consultation_notes_order_id}">
			<c:set var="patientMrNumber" value="${patient.mr_no}" />
			<c:set var="patientId" value="${patient.patient_id}"/>
			<c:set var="instaFormId" value="${form.map.form_id}"/>
			<!-- scriptlet code to get consultaton notes finalized state -->
			<%
				int sectionId = Integer.parseInt((String)pageContext.getAttribute("section_id"));
			    int formId = (Integer) pageContext.getAttribute("instaFormId");
			    
			    String formType = "Form_CONS";
				
				AbstractInstaForms formDAO = AbstractInstaForms.getInstance(formType);
				
				int sectionItemId = formDAO.getSectionItemId(request.getParameterMap());
				String mrNo = (String) pageContext.getAttribute("patientMrNumber");
				String patientId = (String) pageContext.getAttribute("patientId"); 
				String itemType = (String) formDAO.getKeys().get("item_type");
				
				int genericFormId = 0;
				String genericFormIdStr = request.getParameter("generic_form_id");
				if (genericFormIdStr != null && !genericFormIdStr.equals(""))
					genericFormId = Integer.parseInt(genericFormIdStr);
	
				BasicDynaBean patientSectionDetailsBean = PatientSectionDetailsDAO.getRecord(mrNo, patientId, sectionItemId, genericFormId, sectionId, formId, itemType);
				String finalized = "N";
				if (patientSectionDetailsBean != null) {
				    finalized = (String)patientSectionDetailsBean.get("finalized");
				}
				request.setAttribute("finalized", finalized);
			%>
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<c:choose>
				<c:when test="${prescriptionNoteTakerPreferences != 'Y'}">
					<c:set var="fieldsMap" value="${consultation_bean.doc_id == 0 ? consultFields : consultFieldValues}"/>
					<c:if test="${not empty fieldsMap}">
						<fieldset id="valuesFieldset" class="fieldSetBorder" style="margin-top: 10px">
							<c:set var="stn_right_access"
									value="${((roleId == 1 || roleId == 2 || fn:contains(section_rights, section_id)) && finalized != 'Y')}"/>
							<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.consultationnotes"/>:
								<c:forEach var="stn" items="${sys_generated_section}">
									<c:if test="${stn.section_mandatory && stn.section_id == section_id && stn_right_access}">
										<span class="star">*</span>
									</c:if>
								</c:forEach>
								<c:if test="${!(roleId == 1 || roleId == 2 || fn:contains(section_rights, section_id))}">
									<i>[Read-Only]</i>
								</c:if>
								<c:if test="${finalized == 'Y'}">
									<i>[Finalized]</i>
								</c:if>
							</legend>
							<table id="templateFieldsTable" width="100%">
								<c:forEach items="${fieldsMap}" var="field" varStatus="st">
									<c:if test="${st.index%3 == 0}">
										<tr>
									</c:if>
											<td>
												<div style="valign: top; padding: 3px 0px 0px 0px">${field.map.field_name}:	</div>
												<div >
													<textarea name="field_value" id="field_value" rows="${field.map.num_lines}" style="width: 300px;"
													${status == 'C' || !stn_right_access ? 'readOnly' : ''}>${field.map.field_value}</textarea>
													<input type="hidden" name="field_id" value="${field.map.field_id}"/>
													<c:if test="${st.first}">
														<input type="hidden" name="template_id" value="${field.map.template_id}"/>
													</c:if>
												</div>
											</td>
									<c:if test="${st.index%3 == 2}">
										</tr>
									</c:if>
								</c:forEach>
							</table>
							<table style="width:100%;white-space:nowrap;">
								<tr>
									<td style="width:100%"></td>
									<td style="padding-right:5px;">Finalize</td>
									<td> <span><input class="finalize" id="${section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || fn:contains(section_rights, section_id)) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${section_id}_finalized" ></span> </td>
								<tr>
							</table>
						</fieldset>
					</c:if>
				</c:when>
				<c:otherwise>
					<fieldset class="fieldSetBorder" style="margin-top: 10px">
						<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.pasteimages"/></legend>
						<table id="noteTakerTable" >
							<tr>
								<c:if test="${status != 'C'}">
								<div style="border:1px dashed grey;height: 45px;width: 65px;font-size: 11px;" id="pastedPhoto">
									<div style="padding:1px;text-align:center"><insta:ltext key="js.common.copy.and.press.paste"/></div>
								</div>
								</c:if>
								<td >
									<table class="dashboard" id="attachImages">
									   <tr>
									   	<th><insta:ltext key="patient.outpatientlist.consult.details.delete"/></th>
									   	<th><insta:ltext key="patient.outpatientlist.consult.details.date"/></th>
									   	<th><insta:ltext key="patient.outpatientlist.consult.details.action"/></th>
									   </tr>
									   <c:forEach var="image" items="${imageList}">
									   	 <tr name="rowName" id="row${image.map.image_id}">
									   	 	<td><input type="checkbox" name="deleteImage" value="${image.map.image_id}"></td>
									   	 	<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${image.map.datetime}"/></td>
									   	 	<td>
									   	 		<a name="viewImage" href="#" id="viewImage${image.map.image_id}" style="display: block;" onclick="displayNote('${image.map.image_id}', '')"><insta:ltext key="patient.outpatientlist.consult.details.view"/></a>
									   	 		<label name="withoutView" id="withoutView${image.map.image_id}" style="display: none;"><insta:ltext key="patient.outpatientlist.consult.details.view"/></label>
									   	 	</td>
									   	 </tr>
									   </c:forEach>
									 </table>
								</td>
							</tr>
							<tr>
								<td style="display: none;" id="imageTd"></td>
							</tr>
						</table>
					</fieldset>
				</c:otherwise>
			</c:choose>
		</c:if>

		<c:if test="${section_id == vital_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.triageform.addshow.vitals"/>
				<c:forEach var="stn" items="${sys_generated_section}">
					<c:if test="${stn.section_mandatory && stn.section_id == section_id}">
						<span class="star">*</span>
					</c:if>
				</c:forEach>
				</legend>
				<jsp:include page="/pages/vitalForm/VitalReadings.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
				</jsp:include>
			</fieldset>
		</c:if>
		<c:choose>
			<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
				<jsp:include page="InstaSection.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="prescribeForm"/>
					<jsp:param name="form_type" value="Form_CONS"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
				</jsp:include>
			</c:when>
			<c:otherwise>
				<c:if test="${section_id > 0 && sectionsDefMap.get(secId.intValue()).map.linked_to != 'patient'}">
					<jsp:include page="InstaSection.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="consultation_id" value="${param.consultation_id}"/>
						<jsp:param name="form_name" value="prescribeForm"/>
						<jsp:param name="form_type" value="Form_CONS"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
					</jsp:include>
				</c:if>
			</c:otherwise>
		</c:choose>
		<c:if test="${section_id == diagnosis_details_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<input type="hidden" id="diagnosis_details_exists" value="true"/>
			<jsp:include page="DiagnosisDetailsInclude.jsp" >
				<jsp:param name="section_id" value="${section_id}"/>
				<jsp:param name="form_name" value="prescribeForm"/>
				<jsp:param name="displayPrvsDiagnosisBtn" value="true"/>
			</jsp:include>
		</c:if>

		<c:if test="${section_id == prescription_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
			<input type="hidden" name="one_time_prescriptions" value="true"/>
			<jsp:include page="OneTimePrescriptions.jsp" >
				<jsp:param name="section_id" value="${section_id}"/>
			</jsp:include>
		</c:if>

	</c:forTokens>

	<table id="followUp" style="margin-top: 10px" >
		<c:if test="${requireERxAuthorization}">
			<tr>
				<td colspan="8" style="padding-left: 0px; padding-top: 10px; padding-bottom: 10px;">&nbsp;
				<button type="button" accessKey="R" onclick="return chkFormDetailsEdited(false, true)"
					${(empty consErxBean || consErxBean.map.erx_request_type == 'eRxCancellation') ? '' : 'disabled'}
				><insta:ltext key="patient.outpatientlist.consult.details.saveorsenderx"/> <b><u><insta:ltext key="patient.outpatientlist.consult.details.r"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.equest"/></button>&nbsp;

				<button type="button" accessKey="C" onclick="return cancelERxRequest();"
					${(not empty consErxBean && consErxBean.map.erx_request_type == 'eRxRequest') ? '' : 'disabled'}
				><b><u><insta:ltext key="patient.outpatientlist.consult.details.c"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.ancelerxrequest"/></button>&nbsp;
				<a href="${cpath}/ERxPrescription/ERxRequest.do?_method=viewERxRequest&consultation_id=${ifn:cleanJavaScript(param.consultation_id)}"
				><insta:ltext key="patient.outpatientlist.consult.details.viewerxrequestxml"/></a>
				</td>
			</tr>
		</c:if>
		<tr>
			<c:if test="${status != 'C'}">
				<td>Finalize All: </td>
				<td><input  id="finalizeAll" type="checkbox" value="true" onClick="finalizeAllInstaSections();"/></td>
				<td style="padding-left: 10px; padding-top: 2px;"><insta:ltext key="patient.outpatientlist.consult.details.close"/>: </td>
				<td><input type="checkbox" name="closeConsultation" id="closeConsultation" onclick="consultationEndDateTime();" /></td>
			</c:if>
			<c:if test="${visitType == 'o'}">
				<td style="padding: 5px 5px 3px 10px;"><insta:ltext key="patient.outpatientlist.consult.details.followupdate"/>: </td>
				<td>
					<c:if test="${not empty followup_bean}">
						<fmt:formatDate pattern="dd-MM-yyyy" var="followupdate" value="${followup_bean.map.followup_date}"/>
					</c:if>
					<c:choose>
						<c:when test="${status != 'C'}">
							<insta:datewidget id="followup_date" name="followup_date" pos="topleft" valid="future" value="${followupdate}"/>
						</c:when>
						<c:otherwise>
							<input type="text" name="followup_date" id="followup_date" class="datefield" value="${followupdate}" readOnly size="8"/>
						</c:otherwise>
					</c:choose>
				</td>
			</c:if>
			<c:if test="${not empty patientPlan && patientPlan.plan_id != 0 && not empty perdiemCodesList && regPrefs.allow_drg_perdiem eq 'Y'}">
				<td style="padding: 5px 5px 3px 10px;">
					<label for="perdiem_check"> <insta:ltext key="patient.outpatientlist.consult.details.useperdiem"/>: </label>
				</td>
				<td>
					<input type="checkbox" name="perdiem_check" id="perdiem_check" onclick="checkUsePerdiem()"
					 ${patient.use_perdiem == 'Y' ? 'checked' : ''}
					 ${patient.use_perdiem == 'Y' ? 'disabled' : ''}/>
					<input type="hidden" name="use_perdiem" id="use_perdiem" value="${patient.use_perdiem}">
				</td>
				<td style="padding: 5px 5px 3px 10px;"> <insta:ltext key="patient.outpatientlist.consult.details.perdiemcode"/>: </td>
				<td>
					<select name="per_diem_code" id="per_diem_code" class="dropdown" disabled>
						<option value="">-- Select --</option>
							<c:forEach items="${perdiemCodesList}" var="perdiemcode">
								<option value="${perdiemcode.per_diem_code}"
								 ${patient.per_diem_code == perdiemcode.per_diem_code ? 'selected' : ''}
								 style="width:300px;"
								 onmouseover='this.title = "${fn:escapeXml(perdiemcode.per_diem_description)}"'>
								 ${perdiemcode.per_diem_description}</option>
							</c:forEach>
					</select>
				</td>
			</c:if>
		</tr>
	</table>

	<div class="screenActions" style="float: left">
		<c:choose>
			<c:when test="${status == 'C'}">
				<button type="button" name="send" accessKey="R" onclick="return reopen();" tabindex="315">
					<label><b><u><insta:ltext key="patient.outpatientlist.consult.details.r"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.eopen"/></label></button>&nbsp;
			</c:when>
			<c:otherwise>
				<button type="button" name="send" id="Save" accessKey="S" onclick="return chkFormDetailsEdited(false);" tabindex="315">
					<b><u><insta:ltext key="patient.outpatientlist.consult.details.s"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.ave"/></button>&nbsp;
				<button type="button" name="send" id="SaveAndPrint" accessKey="P" onclick="return chkFormDetailsEdited(true);" tabindex="320">
					<insta:ltext key="patient.outpatientlist.consult.details.save.and"/> <b><u><insta:ltext key="patient.outpatientlist.consult.details.p"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.rint"/></button>&nbsp;
				<c:choose>
		            <c:when test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
		                  <button type="button" name="send" id="SaveAndCodeCheck" accessKey="R" onclick="return chkFormDetailsEdited(false, false, true);" tabindex="325">
		                  <insta:ltext key="patient.outpatientlist.consult.details.save.and"/> <b><u><insta:ltext key="patient.outpatientlist.consult.details.r"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.runcodecheck"/></button>&nbsp;
		            </c:when>
        		</c:choose> 
				<c:if test="${urlRightsMap.doc_week_scheduler == 'A' &&  consultation_bean.schedule}">
					<button type="button" name="docWeekViewBtn" accessKey="D" onclick="return openDoctorWeekView();">
						<b><u><insta:ltext key="patient.outpatientlist.consult.details.d"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.octorweekview"/>
					</button>
				</c:if>
			</c:otherwise>
		</c:choose>
		<c:if test="${visitType == 'o' && urlRightsMap.triage_form == 'A'}">
			<button type="button" name="TriagePrint" accessKey="T" onclick="triageSummaryPrint()" tabindex="335">
				<b><u><insta:ltext key="patient.outpatientlist.consult.details.t"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.riageprint"/>
			</button>
		</c:if>

		<c:choose>
			<c:when test="${visitType == 'o'}">
					<c:url var="dashboardUrl" value="/outpatient/OpListAction.do">
						<c:param name="_method" value="list"/>
						<c:param name="status" value="A"/>
						<c:param name="status" value="P"/>
						<c:param name="visit_status" value="A"/>
						<c:param name="sortReverse" value="true"/>
					</c:url>
					| <a href="${dashboardUrl}"><insta:ltext key="patient.outpatientlist.consult.details.patientlist"/></a>

					<c:choose>
						<c:when test="${mod_eclaim_erx && mod_eclaim_pbm}">
							<insta:screenlink screenId="pbm_presc_list" extraParam="?_method=getList&pbm_finalized=N"
								target="_blank" label="PBM Presc. List" addPipe="true"/>
						</c:when>
						<c:when test="${mod_eclaim_erx}">
							<insta:screenlink screenId="erx_presc_list" extraParam="?_method=getList"
								target="_blank" label="ERx Presc. List" addPipe="true"/>
						</c:when>
					</c:choose>
			</c:when>
			<c:when test="${visitType == 'i'}">
				<c:url value="/pages/ipservices/IpservicesList.do" var="patientListUrl">
					<c:param name="_method" value="getIPDashBoard"/>
					<c:param name="filterClosed" value="${param.filterClosed}"/>
				</c:url>
				| <a href="<c:out value='${patientListUrl}' />" title="IP Dashboard"><insta:ltext key="patient.outpatientlist.consult.details.inpatientlist"/></a>
				<insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}"
					target="_blank" label="EMR View" addPipe="true"/>

			    | <insta:screenlink screenId="visit_emr_screen" extraParam="?_method=list&visit_id=${patient.visit_id}"
				target="_blank" label="View Visit EMR" />
			</c:when>
		</c:choose>
	</div>
	<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
	<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
		<c:if test="${not empty printTemplate}">
			<c:forEach var="temp" items="${printTemplate}">
				<c:set var="templateValues" value="${templateValues},${temp.template_name}"/>
				<c:set var="templateTexts" value="${templateTexts},${temp.template_name}"/>
			</c:forEach>
		</c:if>
	<div style="float: right; margin-top: 10px; display: ${status != 'C' ? 'block' : 'none'}">
		<insta:selectoptions name="printTemplate" id="templateList" opvalues="${templateValues}"
											optexts="${templateTexts}" value="${printPresc}"/>
		<insta:selectdb name="printerId" table="printer_definition" class="dropdown"
							valuecol="printer_id"  displaycol="printer_definition_name"
							value="${showPrinter}"/>
	</div>
	<div class="clrboth"></div>

	<div style="margin-top: 10px;float: left" >
		<c:if test="${visitType == 'o'}">

			<insta:screenlink screenId="triage_form" label="Triage/Nurse Assessment" addPipe="true"
				extraParam="?method=show&consultation_id=${param.consultation_id}&patient_id=${consultation_bean.patient_id}" />

			<c:if test="${consultation_bean.status != 'A'}">
				<insta:screenlink screenId="op_case_form" label="Case Forms"
					addPipe="true"
					extraParam="?_method=show&consultation_id=${param.consultation_id}" />
			</c:if>

			<insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}"
				target="_blank" label="EMR View" addPipe="true"/>
			<c:if test="${consultation_bean.dept_type_id == 'DENT'}">
				<insta:screenlink screenId="dental_consultations" extraParam="?_method=show&mr_no=${patient.mr_no}"
					label="Dental Consultation" addPipe="true"/>
			</c:if>
		</c:if>
		| <a href="${cpath}/master/ConsultationFavourites.do?_method=list&doctor_id=${consultation_bean.doctor_name}"
				title="Manage Consultation Favourites"><insta:ltext key="patient.outpatientlist.consult.details.manageconsultationfavourites"/></a>
		<c:url var="vitalsUrl" value="/vitalForm/genericVitalForm.do">
			<c:param name="method" value="list"/>
			<c:param name="patient_id" value="${consultation_bean.patient_id}"/>
			<c:if test="${visitType == 'o'}">
				<c:param name="consultation_id" value="${param.consultation_id}"/>
			</c:if>
		</c:url>
		| <a href="${vitalsUrl}"><insta:ltext key="patient.outpatientlist.consult.details.vitals"/></a>
		<c:if test="${preferences.modulesActivatedMap.mod_emcalc == 'Y'}">
			<c:url var="eandmCalcUrl" value="/eandmcalculator.do">
				<c:param name="consultationId" value="${param.consultation_id}"/>
				<c:param name="_method" value="getScreen"/>
			</c:url>
			| <a href="<c:out value='${eandmCalcUrl}' />" ><insta:ltext key="patient.outpatientlist.consult.details.e.and.mcalculator"/></a>
		</c:if>
		<c:if test="${preferences.modulesActivatedMap.mod_vaccination == 'Y' && (patient.agein eq 'Y' ? (patient.age le 18) : (patient.agein eq 'M' || patient.agein eq 'D'))}">
			<c:url var="vaccinationInfo" value="/VaccinationInfo.do">
				<c:param name="_method" value="vaccinationsList" />
				<c:param name="mr_no" value="${param.mr_no}" />
			</c:url>
			| <a href="<c:out value='${vaccinationInfo}' />" target="_blank" ><insta:ltext key="patient.outpatientlist.consult.details.vaccinationinfo"/></a>
		</c:if>
 		<insta:screenlink screenId="generic_documents_list" label="Add Generic Documents" addPipe="true"
				extraParam="?_method=addPatientDocument&mr_no=${patient.mr_no}" />
		<insta:screenlink screenId="generic_documents_list" label="Upload Document" addPipe="true"
				extraParam="?_method=add&mr_no=${patient.mr_no}&insurance_id=&format=doc_fileupload&patient_id=${consultation_bean.patient_id}&prescription_id=&template_id=&selectTemplate=on&addDocFor=visit&visit_dropdown=${consultation_bean.patient_id}" />
		<insta:screenlink screenId="patient_generic_form_list" label="Add Patient Generic Form" addPipe="true"
				extraParam="?_method=getChooseGenericFormScreen&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}" />
		<insta:screenlink screenId="reg_admission_request" label="Add New Admission Request" addPipe="true"
				extraParam="?_method=addNewAdmissionRequest&mr_no=${patient.mr_no}&patient_id=${consultation_bean.patient_id}&screen_id=opconsultation&doctor_id=${consultation_bean.doctor_name}"/>
		<c:if test="${preferences.modulesActivatedMap.mod_growth_charts == 'Y' && (patient.agein eq 'Y' ? (patient.age le 20) : (patient.agein eq 'M' || patient.agein eq 'D'))}">
			<insta:screenlink screenId="growth_chart" label="Growth Charts" addPipe="true"
				extraParam="?method=list&consultation_id=${param.consultation_id}&patient_id=${consultation_bean.patient_id}&doctor_id=${consultation_bean.doctor_name}&patient_id=${patient.patient_id}" />
		</c:if>
		<insta:screenlink screenId="cons_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=cons_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/>
	</div>
	<div id="genericNameDisplayDialog"  style="visibility:hidden">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.consult.details.genericnamedetails"/></legend>
				<table border="0" class="formtable">
					<tr height="10px"></tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.generic"/>&nbsp;<insta:ltext key="patient.outpatientlist.consult.details.name"/>: </td>
						<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.classification"/>: </td>
						<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.sub_classification"/>:</td>
							<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.standardadultdose"/>:</td>
							<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.consult.details.criticality"/>:</td>
							<td class="forminfo" style="width:25em"><b><label id="criticality"></label>	</td>
					</tr>

					<tr height="10px"></tr>
				</table>
				<table>
					<tr>
				    	<td><input type="button" id="genericNameCloseBtn" value="Close"></td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	<div id="infoDialog" class="info-overlay">
		<div class="bd">
			<table width="100%">
				<tr>
					<td>
						<label id="infoDialogText" style="float: left"></label>
					</td>
					<td style="width: 12px;" valign="top">
						<a id="cancelInfoImg" href="javascript:void(0);">
							<img src="${cpath}/icons/std_cancel.png" style="width: 12px;" />
						</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="div-shadow" id="shadowDiv"></div>
	</div>
	<jsp:include page="PreviousVisitSummary.jsp" />
	<jsp:include page="HistoricalDialogs.jsp" />
	<jsp:include page="DoctorFavouritesInclude.jsp"/>
	<jsp:include page="PatientPreviousPrescriptions.jsp"/>
	<jsp:include page="InstaSectionDialogsInclude.jsp"/>
	<div class="legend" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.consult.details.issued.or.discontinueditems"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.consult.details.cancelleditems"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.consult.details.priorauthrequired"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.consult.details.priorauthrequiredsometimes"/></div>
	</div>
</form>

</body>
</html>

