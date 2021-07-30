<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %> 
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="complaint_order_id" value="<%=SystemGeneratedSections.Complaint.getSectionId()%>" />

<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<html>
	<head>
		<script>
			var collapsiblePanels = {};
			var phrase_suggestions_json = ${phrase_suggestions_json};
			var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
			var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
			var comp_immunization = '${form.map.immunization}';
			YAHOO.util.Event.onContentReady("content", init);
			var complaintForm = null;
			function init() {
				complaintForm = document.triageform;
			}
			var sys_generated_forms = ${sys_generated_forms};
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
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title><insta:ltext key="patient.outpatientlist.triageform.addshow.title"/></title>
		<insta:link type="js" file="hmsvalidation.js" />
		<insta:link type="script" file="triageform/triageform.js"/>
		<insta:link type="script" file="outpatient/insta_section.js"/>
		<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
		<insta:link type="script" file="outpatient/complaint.js"/>
		<insta:link type="script" file="outpatient/allergies.js"/>
		<insta:link type="script" file="outpatient/pregnancyDetails.js"/>
		<insta:link type="script" file="outpatient/antenatal.js"/>
		<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
		<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
		<style>

			.complaintAc {
			    padding-bottom:2em;
			}

			.scrolForContainer .yui-ac-content{
				 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
			}

		</style>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="outpatient.triageform"/>
<insta:js-bundle prefix="outpatient.vitalform"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="ajaxForPrintUrls();">
<c:set var="ipdashboard">
<insta:ltext key="patient.outpatientlist.triageform.addshow.ipdashboard"/>
</c:set>
<c:set var="consultationandmanagement">
<insta:ltext key="patient.outpatientlist.triageform.addshow.consultationandmanagement"/>
</c:set>
<c:set var="printclinicalinformationsheet">
<insta:ltext key="patient.outpatientlist.triageform.addshow.printclinicalinformationsheet"/>
</c:set>
<c:set var="addpatientgenericdocument">
<insta:ltext key="patient.outpatientlist.triageform.addshow.addpatientgenericdocument"/>
</c:set>
<c:set var="addpatientgenericform">
<insta:ltext key="patient.outpatientlist.triageform.addshow.addpatientgenericform"/>
</c:set>
<c:set var="saveBtn">
<insta:ltext key="patient.outpatientlist.triageform.addshow.save"/>
</c:set>
<c:set var="saveprintBtn">
<insta:ltext key="patient.outpatientlist.triageform.addshow.save.print"/>
</c:set>
	<h1><insta:ltext key="patient.outpatientlist.triageform.addshow.triage.or.nurseassessment"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${consultation_bean.patient_id}" showClinicalInfo="true"/>
	<c:set var="consultation_edit_across_doctors" value="${clinicalPrefs.op_consultation_edit_across_doctors}"/>
	<form action="${cpath}/TriageForm/TriageFormAction.do?method=update" method="POST" name="triageform" autocomplete="off">

	<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

	<input type="hidden" name="age" value="${patient_details.age}"/>
	<input type="hidden" name="age_in" value="${patient_details.agein}"/> <%-- Immunization status made mandatory for below 15yrs patient. --%>
	<input type="hidden" name="patient_id" value="${consultation_bean.patient_id}" />
	<input type="hidden" name="dept" id="dept" value="${consultation_bean.dept_id}"/>
	<input type="hidden" name="mr_no" value="${patient.mr_no}"/>
	<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
	<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}" />
	<input type="hidden" name="printTriage" id="printTriage" value="false"/>
	<%-- inserting/updating/deleting complaint goes here --%>

	<c:forTokens delims="," items="${form.map.sections}" var="section_id">
		<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
	</c:forTokens>

	<div class="resultList">
		<fieldset class="fieldSetBorder">
			<table style="margin-top: 10px" class="formtable">
				<c:if test="${form.map.immunization == 'Y'}">
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.triageform.addshow.immunizationuptodate"/></td>
						<td style="padding-bottom: 5px"><input type="radio" name="immunization_status_upto_date" value="Y" ${consultation_bean.immunization_status_upto_date == 'Y' ? 'checked' : ''}> <insta:ltext key="patient.outpatientlist.triageform.addshow.yes"/>
							<input type="radio" name="immunization_status_upto_date" value="N" ${consultation_bean.immunization_status_upto_date == 'N' ? 'checked' : ''}> <insta:ltext key="patient.outpatientlist.triageform.addshow.no"/>
						</td>
						<td class="formlabel">&nbsp;</td>
						<td></td>
					</tr>
				</c:if>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.triageform.addshow.priority"/>: </td>
					<td colspan="4"><input type="radio" name="emergency_category" value="E" ${consultation_bean.emergency_category == 'E' ? 'checked' : ''}> <insta:ltext key="patient.outpatientlist.triageform.addshow.emergent"/>
						<input type="radio" name="emergency_category" value="U" ${consultation_bean.emergency_category == 'U' ? 'checked' : ''}> <insta:ltext key="patient.outpatientlist.triageform.addshow.urgent"/>
						<input type="radio" name="emergency_category" value="N" ${consultation_bean.emergency_category == 'N' ? 'checked' : ''}> <insta:ltext key="patient.outpatientlist.triageform.addshow.nonurgent"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<c:set var="vital_order_id" value="<%=SystemGeneratedSections.Vitals.getSectionId()%>" />
		<c:set var="allergies_order_id" value="<%=SystemGeneratedSections.Allergies.getSectionId()%>" />
		<c:set var="pregnancy_history_order_id" value="<%=SystemGeneratedSections.PregnancyHistory.getSectionId()%>" />
		<c:set var="antenatal_order_id" value="<%=SystemGeneratedSections.Antenatal.getSectionId()%>" />

		<c:set var="fieldsetField" value="false" />
		<c:set var="pfIndex" value="0"/>
		<c:forTokens items="${form.map.sections}" delims="," var="section_id" varStatus="s_i" >
			<c:if test="${section_id > 0 && group_patient_sections == 'Y'}">
				<c:set var="secId" value="${section_id+0}"/>
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
					<jsp:include page="/pages/outpatient/InstaSection.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="consultation_id" value="${param.consultation_id}"/>
						<jsp:param name="form_name" value="triageform"/>
						<jsp:param name="form_type" value="Form_TRI"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
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

		<c:forTokens delims="," items="${form.map.sections}" var="section_id">
			<c:set var="secId" value="${section_id + 0}"/>
			<c:if test="${section_id == complaint_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/ComplaintInclude.jsp">
					<jsp:param name="section_id" value="-1"/>
					<jsp:param name="form_name" value="triageform"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id  == vital_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
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

			<c:if test="${section_id == allergies_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/Allergies.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="triageform"/>
					<jsp:param name="form_type" value="Form_TRI"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id == pregnancy_history_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/PregnancyDetailsInclude.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="triageform"/>
					<jsp:param name="form_type" value="Form_TRI"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:if>

			<c:if test="${section_id == antenatal_order_id}">
			<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
				<jsp:include page="/pages/outpatient/AntenatalDetails.jsp" >
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="consultation_id" value="${param.consultation_id}"/>
					<jsp:param name="form_name" value="triageform"/>
					<jsp:param name="form_type" value="Form_TRI"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:if>

			<c:choose>
				<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
					<jsp:include page="/pages/outpatient/InstaSection.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="consultation_id" value="${param.consultation_id}"/>
						<jsp:param name="form_name" value="triageform"/>
						<jsp:param name="form_type" value="Form_TRI"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
					</jsp:include>
				</c:when>
				<c:otherwise>
					<c:if test="${section_id > 0 && sectionsDefMap.get(secId.intValue()).map.linked_to != 'patient'}">
						<jsp:include page="/pages/outpatient/InstaSection.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="consultation_id" value="${param.consultation_id}"/>
							<jsp:param name="form_name" value="triageform"/>
							<jsp:param name="form_type" value="Form_TRI"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
						</jsp:include>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:forTokens>
	</div>
	<table style="margin-top: 5px">
		<tr>
			<td style="width:63px">Finalize All: </td>
			<td style="width:30px"><input  id="finalizeAll" type="checkbox" value="true" onClick="finalizeAllInstaSections();"/></td>
			<td style="width:99px"><insta:ltext key="patient.outpatientlist.triageform.addshow.triagecompleted"/>: </td>
			<td style="width:30px"><input type="checkbox" name="triage_done" id="triage_done" value="Y" ${consultation_bean.triage_done == 'Y' ? 'checked' : ''}></td>
			<td></td>
		</tr>
	</table>
	<table id="screenActions" style="margin-top: 10px">
		<tr>
			<td>
				<c:set var="disableBtn" value="${consultation_bean.status == 'C' or consultation_bean.cancel_status == 'C'}"/>
				<input type="button" name="save" value="${saveBtn}" onclick="validateForm(false)" ${disableBtn ? 'disabled' : ''}/> |
				<input type="button" name="saveAndprint" value="${saveprintBtn}" onclick="validateForm(true)" ${disableBtn ? 'disabled' : ''}/>
				<c:if test="${patient.visit_type eq 'i'}">
					| <a href="${cpath}/pages/ipservices/IpservicesList.do?_method=getIPDashBoard" title="${ipdashboard}"><insta:ltext key="patient.outpatientlist.triageform.addshow.inpatientlist"/></a>
				</c:if>
				<c:set var="editNotAllowed" value="${roleId != 1 && roleId != 2 && (consultation_edit_across_doctors == 'N' && doctor_logged_in != consultation_bean.doctor_name)}"/>
				<c:if test = "${patient.visit_type eq 'o' && !editNotAllowed}">
					| <a href="${cpath}/outpatient/OpPrescribeAction.do?_method=list&consultation_id=${ifn:cleanURL(param.consultation_id)}"
						title="${consultationandmanagement}"><insta:ltext key="patient.outpatientlist.triageform.addshow.consultationandmanagement"/></a>
				</c:if>
				 | <a href="${cpath}/emr/print.do?method=printClinicalInfo&consultation_id=${ifn:cleanURL(param.consultation_id)}"
					title="${printclinicalinformationsheet}" target="_blank"><insta:ltext key="patient.outpatientlist.triageform.addshow.clinicalinfo"/></a>

				 | <a href="${cpath}/pages/GenericDocuments/GenericDocumentsAction.do?_method=addPatientDocument&addDocFor=visit&mr_no=${ifn:cleanURL(patient.mr_no)}"
					title="${addpatientgenericdocument}" target="_blank"><insta:ltext key="patient.outpatientlist.triageform.addshow.addgenericdocument"/></a>
				<insta:screenlink screenId="patient_generic_form_list" label="${addpatientgenericform}" addPipe="true"
					extraParam="?_method=getChooseGenericFormScreen&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}" />
				<insta:screenlink screenId="triage_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=triage_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/>
				<insta:screenlink screenId="triage_summary_audit_log" label="Triage Summary Audit Log" addPipe="true" target="_blank" extraParam="
				?_method=getAuditLogDetails&al_table=doctor_consultation_triage_audit_log&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&consultation_id=${param.consultation_id}"/>
			</td>
		</tr>
	</table>
	<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
	</form>
	</body>
</html>
