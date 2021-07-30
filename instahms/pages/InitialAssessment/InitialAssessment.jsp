<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<html>
<head>
	<title><insta:ltext key="patient.outpatientlist.assessmentform.details.title"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
	<insta:link type="script" file="outpatient/pregnancyDetails.js"/>
	<insta:link type="script" file="outpatient/antenatal.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var collapsiblePanels = {};
		function saveForm(doPrint) {
			if (!validateSysGenForms()) return false;
			// validate mandatory fields in physician forms.
			if (!validateMandatoryFields()) return false;

			document.getElementById('printAssessment').value = doPrint;
			document.assessmentform.submit();
			return true;
		}
		var sys_generated_forms = ${sys_generated_forms};
		var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
		var normalResult = '${prefColorCodes.map.normal_color_code}';
		var abnormalResult = '${prefColorCodes.map.abnormal_color_code}';
		var criticalResult = '${prefColorCodes.map.critical_color_code}';
		var improbableResult = '${prefColorCodes.map.improbable_color_code}';
		var paramType = "V";
		var insta_sections_json = ${insta_sections_json};
		var group_patient_sections = '${group_patient_sections}';
		var insta_form_json = ${insta_form_json};
		var visit_type = '${patient.visit_type}';
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};
	</script>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="outpatient.triageform"/>
<insta:js-bundle prefix="outpatient.vitalform"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="ajaxForPrintUrls();">
	<h1><insta:ltext key="patient.outpatientlist.assessmentform.details.initialassessmentform"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${consultation_bean.patient_id}" showClinicalInfo="true"/>
	<c:set var="consultation_edit_across_doctors" value="${opConsultationEditAcrossDoctors}"/>

	<form action="${cpath}/InitialAssessment/InitialAssessmentAction.do?_method=update" method="POST" name="assessmentform" autocomplete="off">
		<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

		<input type="hidden" name="patient_id" value="${consultation_bean.patient_id}" />
		<input type="hidden" name="mr_no" value="${patient.mr_no}"/>
		<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}" />
		<input type="hidden" name="printAssessment" id="printAssessment" value="false"/>

		<c:set var="vital_order_id" value="<%=SystemGeneratedSections.Vitals.getSectionId()%>" />
		<c:set var="pregnancy_order_id" value="<%=SystemGeneratedSections.PregnancyHistory.getSectionId()%>" />
		<c:set var="antenatal_order_id" value="<%=SystemGeneratedSections.Antenatal.getSectionId()%>" />

		<c:forTokens delims="," items="${form.map.sections}" var="section_id">
			<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
		</c:forTokens>

		<c:set var="fieldsetField" value="false" />
		<c:set var="pfIndex" value="0"/>
		<c:forTokens items="${form.map.sections}" delims="," var="section_id" varStatus="s_i" >
			<c:if test="${section_id > 0 && group_patient_sections == 'Y'}">
				<c:forEach items="${insta_sections}" var="patient_form_group" varStatus="j">
					<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to == 'patient'}">
						<c:if test="${pfIndex == 0}">
							<c:set var="pfIndex" value="${pfIndex+1}"/>
							<c:set var="fieldsetField" value="true" />
							<div id="CollapsiblePanel" class="CollapsiblePanel">
								<div class=" title CollapsiblePanelTab"  style=" border-left:none;">
									<div id="patient_history_label" class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i><insta:ltext key="patient.outpatientlist.consult.details.patienthistory"/></i></b></div>
									<div class="fltR txtRT" style="width: 25px; margin: 10px 10px 0px ">
										<img src="${cpath}/images/down.png" />
									</div>
									<div class="clrboth"></div>
								</div>
							<fieldset class="fieldSetBorder" id="fieldset" style="background-color: #F8FCFF; border-style:none ">
						</c:if>
							<jsp:include page="/pages/outpatient/InstaSection.jsp">
								<jsp:param name="section_id" value="${fn:trim(section_id)}"/>
								<jsp:param name="consultation_id" value="${param.consultation_id}"/>
								<jsp:param name="form_name" value="assessmentform"/>
								<jsp:param name="form_type" value="Form_IA"/>
								<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
								<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
								<jsp:param name="mr_no" value="${patient.mr_no}"/>
								<jsp:param name="patient_id" value="${patient.patient_id}"/>
							</jsp:include>
					</c:if>
				</c:forEach>
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

		<c:forTokens delims="," items="${form.map.sections}" var="section_id">
			<c:if test="${section_id == vital_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.assessmentform.details.vitals"/>
						<c:forEach var="stn" items="${sys_generated_section}">
							<c:if test="${stn.section_mandatory && stn.section_id == section_id}">
								<span class="star">*</span>
							</c:if>
						</c:forEach>
					</legend>
					<jsp:include page="/pages/vitalForm/VitalReadings.jsp"/>
				</fieldset>
			</c:if>

			<c:if test="${section_id == pregnancy_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/PregnancyDetailsInclude.jsp" >
					<jsp:param name="section_id" value="${fn:trim(section_id)}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="assessmentform"/>
					<jsp:param name="form_type" value="Form_IA"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id == antenatal_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/AntenatalDetails.jsp" >
					<jsp:param name="section_id" value="${fn:trim(section_id)}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="assessmentform"/>
					<jsp:param name="form_type" value="Form_IA"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:if>

			<c:choose>
				<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
					<jsp:include page="/pages/outpatient/InstaSection.jsp">
						<jsp:param name="section_id" value="${fn:trim(section_id)}"/>
						<jsp:param name="consultation_id" value="${param.consultation_id}"/>
						<jsp:param name="form_name" value="assessmentform"/>
						<jsp:param name="form_type" value="Form_IA"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
					</jsp:include>
				</c:when>
				<c:otherwise>
					<c:forEach items="${insta_sections}" var="patient_form_group">
						<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to != 'patient'}">
							<jsp:include page="/pages/outpatient/InstaSection.jsp">
								<jsp:param name="section_id" value="${fn:trim(section_id)}"/>
								<jsp:param name="consultation_id" value="${param.consultation_id}"/>
								<jsp:param name="form_name" value="assessmentform"/>
								<jsp:param name="form_type" value="Form_IA"/>
								<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
								<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
								<jsp:param name="mr_no" value="${patient.mr_no}"/>
								<jsp:param name="patient_id" value="${patient.patient_id}"/>
							</jsp:include>
						</c:if>
					</c:forEach>
				</c:otherwise>
			</c:choose>
		</c:forTokens>
		<table style="margin-top: 5px">
			<tr>
				<td style="width:63px">Finalize All: </td>
				<td style="width:30px"><input  id="finalizeAll" type="checkbox" value="true" onClick="finalizeAllInstaSections();"/></td>
				<td></td>
			</tr>
		</table>
		<table id="screenActions" style="margin-top: 10px">
			<tr>
				<td>
					<c:set var="disableBtn" value="${consultation_bean.status == 'C' or consultation_bean.cancel_status == 'C'}"/>
					<input type="button" name="save" value="Save" onclick="saveForm(false)" ${disableBtn ? 'disabled' : ''}/>
					<input type="button" name="saveAndprint" value="Save & Print" onclick="saveForm(true)" ${disableBtn ? 'disabled' : ''}/>
					<c:set var="editNotAllowed" value="${roleId != 1 && roleId != 2 && (consultation_edit_across_doctors == 'N' && doctor_logged_in != consultation_bean.doctor_name)}"/>
					<c:if test = "${!editNotAllowed}">
						| <a href="${cpath}/outpatient/OpPrescribeAction.do?_method=list&consultation_id=${ifn:cleanURL(param.consultation_id)}"
							title="Consultation and Management"><insta:ltext key="patient.outpatientlist.assessmentform.details.consultationandmanagement"/></a>
					</c:if>
					<c:url var="dashboardUrl" value="/outpatient/OpListAction.do">
						<c:param name="_method" value="list"/>
						<c:param name="status" value="A"/>
						<c:param name="status" value="P"/>
						<c:param name="visit_status" value="A"/>
						<c:param name="sortReverse" value="false"/>
					</c:url>
					| <a href="${dashboardUrl}"><insta:ltext key="patient.outpatientlist.assessmentform.details.patientlist"/></a>
					<insta:screenlink screenId="ia_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=ia_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/>
				</td>
			</tr>
		</table>
		<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
	</form>

</body>
</html>
