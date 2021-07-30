<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit ${form.map.form_name} Form - Insta HMS</title>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<insta:link type="js" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="outpatient/allergies.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
	<insta:link type="script" file="outpatient/complaint.js"/>
	<insta:link type="script" file="outpatient/healthMaintenance.js"/>
	<insta:link type="script" file="outpatient/pregnancyDetails.js"/>
	<insta:link type="script" file="outpatient/antenatal.js"/>
	<insta:link type="script" file="outpatient/preAnaesthestheticCheckup.js"/>
	<insta:link type="script" file="GenericForms/GenericFormsAddrEdit.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.complaintAc {
		    padding-bottom:2em;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var collapsiblePanels = {};
		var insta_form_json = ${insta_form_json};
		var group_patient_sections = '${group_patient_sections}';
		var insta_sections_json = ${insta_sections_json};
		var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
		var complaintForm = null;
		var sys_generated_forms = ${sys_generated_forms};
		var validate_diagnosis_codification = '${genericPrefs.validate_diagnosis_codification}';
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var diagnosis_code_type = '<%=request.getAttribute("defaultDiagnosisCodeType") %>';
		var paramType = "V";
		var normalResult = '${prefColorCodes.map.normal_color_code}';
		var abnormalResult = '${prefColorCodes.map.abnormal_color_code}';
		var criticalResult = '${prefColorCodes.map.critical_color_code}';
		var improbableResult = '${prefColorCodes.map.improbable_color_code}';
		var visit_type = '${patient.visit_type}';
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};
	</script>
	<insta:link type="script" file="outpatient/complaint.js"/>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
	<insta:js-bundle prefix="outpatient.vitalform"/>
	<insta:js-bundle prefix="patient.diagnosis"/>
</head>
<body onload="init();ajaxForPrintUrls();">
	<div class="pageHeader" >Add/Edit ${form.map.form_name} Form</div>
	<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>

	<form name="patient_generic_form" action="GenericFormsAction.do" method="POST">
		<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>
        
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="mr_no" value="${patient.mr_no}"/>
		<input type="hidden" name="patient_id" value="${patient.patient_id}"/>
		<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
		<input type="hidden" name="generic_form_id" value="${param.generic_form_id}">
		<input type="hidden" name="printGenericForm" id="printGenericForm" value="false"/>

		<c:forTokens delims="," items="${form.map.sections}" var="section_id">
			<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
		</c:forTokens>

		<c:set var="vitals_order_id" value="<%=SystemGeneratedSections.Vitals.getSectionId()%>" />
		<c:set var="allergies_order_id" value="<%=SystemGeneratedSections.Allergies.getSectionId()%>" />
		<c:set var="complaint_order_id" value="<%=SystemGeneratedSections.Complaint.getSectionId()%>" />
		<c:set var="triage_order_id" value="<%=SystemGeneratedSections.TriageSummary.getSectionId()%>" />
		<c:set var="consultation_notes_order_id" value="<%=SystemGeneratedSections.ConsultationNotes.getSectionId()%>" />
		<c:set var="prescription_order_id" value="<%=SystemGeneratedSections.Prescription.getSectionId()%>" />
		<c:set var="diagnosis_details_order_id" value="<%=SystemGeneratedSections.DiagnosisDetails.getSectionId()%>" />
		<c:set var="health_maintenance_order_id" value="<%=SystemGeneratedSections.HealthMaintenance.getSectionId()%>" />
		<c:set var="pre_anaesthesthetic_order_id" value="<%=SystemGeneratedSections.PreAnaesthestheticCheckup.getSectionId()%>" />
		<c:set var="pregnancy_order_id" value="<%=SystemGeneratedSections.PregnancyHistory.getSectionId()%>" />
		<c:set var="antenatal_order_id" value="<%=SystemGeneratedSections.Antenatal.getSectionId()%>" />

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

									<div class="fltR txtRT" style="width: 25px; margin: 10px 10px 0px;">
										<img src="${cpath}/images/down.png" />
									</div>
									<div class="clrboth"></div>
								</div>
							<fieldset class="fieldSetBorder" id="fieldset" style="background-color: #F8FCFF; border-style:none ">
						</c:if>
							<jsp:include page="/pages/outpatient/InstaSection.jsp">
								<jsp:param name="section_id" value="${section_id}"/>
								<jsp:param name="patient_id" value="${param.patient_id}"/>
								<jsp:param name="form_name" value="patient_generic_form"/>
								<jsp:param name="form_type" value="Form_Gen"/>
								<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
								<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
								<jsp:param name="mr_no" value="${patient.mr_no}"/>
								<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
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
			<c:choose>
				<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
					<jsp:include page="/pages/outpatient/InstaSection.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="patient_id" value="${param.patient_id}"/>
						<jsp:param name="form_name" value="patient_generic_form"/>
						<jsp:param name="form_type" value="Form_Gen"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
						<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
					</jsp:include>
				</c:when>

				<c:otherwise>
					<c:forEach items="${insta_sections}" var="patient_form_group">
						<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to != 'patient'}">
							<jsp:include page="/pages/outpatient/InstaSection.jsp">
								<jsp:param name="section_id" value="${section_id}"/>
								<jsp:param name="patient_id" value="${param.patient_id}"/>
								<jsp:param name="form_name" value="patient_generic_form"/>
								<jsp:param name="form_type" value="Form_Gen"/>
								<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
								<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
								<jsp:param name="mr_no" value="${patient.mr_no}"/>
								<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
							</jsp:include>
						</c:if>
					</c:forEach>
				</c:otherwise>
			</c:choose>
			<c:if test="${section_id == complaint_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<jsp:include page="/pages/outpatient/ComplaintInclude.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
				</jsp:include>
			</c:if>
			<c:if test="${section_id == allergies_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<jsp:include page="/pages/outpatient/Allergies.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="patient_id" value="${param.patient_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
					<jsp:param name="form_type" value="Form_Gen"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>				
				</jsp:include>
			</c:if>

			<c:if test="${section_id == health_maintenance_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include flush="true" page="/pages/outpatient/HealthMaintenance.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id == pre_anaesthesthetic_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include flush="true" page="/pages/outpatient/PreAnaesthestheticCheckup.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="patient_id" value="${param.patient_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
					<jsp:param name="form_type" value="Form_Gen"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id == vitals_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.outpatientlist.triageform.addshow.vitals"/>
						<c:forEach var="stn" items="${sys_generated_section}">
							<c:if test="${stn.section_mandatory && stn.section_id == section_id}">
								<span class="star">*</span>
							</c:if>
						</c:forEach>
					</legend>
					<jsp:include page="/pages/vitalForm/VitalReadings.jsp"/>
				</fieldset>
			</c:if>

			<c:if test="${section_id == diagnosis_details_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<input type="hidden" id="diagnosis_details_exists" value="true"/>
				<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
					<jsp:param name="displayPrvsDiagnosisBtn" value="true"/>
				</jsp:include>
			</c:if>
			
			<c:if test="${section_id == pregnancy_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<jsp:include page="/pages/outpatient/PregnancyDetailsInclude.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="patient_id" value="${param.patient_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
					<jsp:param name="form_type" value="Form_Gen"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
				</jsp:include>
			</c:if>
			
			<c:if test="${section_id == antenatal_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<jsp:include page="/pages/outpatient/AntenatalDetails.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="patient_id" value="${param.patient_id}"/>
					<jsp:param name="form_name" value="patient_generic_form"/>
					<jsp:param name="form_type" value="Form_Gen"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="generic_form_id" value="${param.generic_form_id}"/>
				</jsp:include>
			</c:if>

		</c:forTokens>
		<table style="margin-top: 5px">
			<tr>
				<td style="width:63px">Finalize All: </td>
				<td style="width:30px"><input  id="finalizeAll" type="checkbox" value="true" onClick="finalizeAllInstaSections();"/></td>
				<td></td>
			</tr>
		</table>
		<div class="screenActions">
			<input type="button" onclick="saveGenericForm(false);" value="Save"/>
			<input type="button" onclick="saveGenericForm(true);" value="Save & Print"/>
			<c:url var="listUrl" value="GenericFormsAction.do">
				<c:param name="_method" value="list"/>
				<c:param name="patient_id" value="${param.patient_id}"/>
			</c:url>
			| <a href="${listUrl}" title="Generic Form List">Generic Form List</a>
			<c:url var="formUrl" value="GenericFormsAction.do">
				<c:param name="_method" value="getChooseGenericFormScreen"/>
				<c:param name="patient_id" value="${param.patient_id}"/>
			</c:url>
			| <a href="${formUrl}" title="Add Patient Generic Form">Add(Choose) Generic Form</a>
			<c:choose>
				<c:when test="${param.generic_form_id == '' || param.generic_form_id == null}">
					<insta:screenlink screenId="gen_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=gen_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&generic_form_id=0"/>
				</c:when>
				<c:otherwise>
					<insta:screenlink screenId="gen_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=gen_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&generic_form_id=${param.generic_form_id}"/>
				</c:otherwise>
			</c:choose>
		</div>
		<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
	</form>
</body>
</html>