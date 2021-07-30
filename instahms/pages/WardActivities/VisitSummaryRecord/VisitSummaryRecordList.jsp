<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<html>

<head>
	<title>IP Record - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="wardactivities/visitsummaryrecord/visitsummaryrecord.js"/>
	<insta:link type="js" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="outpatient/allergies.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link type="script" file="outpatient/preAnaesthestheticCheckup.js"/>
	<insta:link type="script" file="outpatient/healthMaintenance.js"/>
	<insta:link type="script" file="outpatient/pregnancyDetails.js" />
	<insta:link type="script" file="outpatient/antenatal.js" />
	<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<script>
		var isSharedLogIn = '${isSharedLogIn}';
		var roleId = '${roleId}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		YAHOO.util.Event.onContentReady("content", init);
		var complaintForm = null;

		var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
		var validate_diagnosis_codification = '${genericPrefs.validate_diagnosis_codification}';
		var sys_generated_forms = ${sys_generated_forms};
		var diagnosis_code_type = '<%=request.getAttribute("defaultDiagnosisCodeType") %>';
		var insta_form_json = ${insta_form_json};
		var group_patient_sections = '${group_patient_sections}';
		var insta_sections_json = ${insta_sections_json};
		var paramType = "V";
		var normalResult = '${prefColorCodes.map.normal_color_code}';
		var abnormalResult = '${prefColorCodes.map.abnormal_color_code}';
		var criticalResult = '${prefColorCodes.map.critical_color_code}';
		var improbableResult = '${prefColorCodes.map.improbable_color_code}';
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};
	</script>
	<insta:link type="script" file="outpatient/complaint.js"/>
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
		table.marker_with_label {
			width: 800px;
			border: 1px #e6e6e6 solid;
			empty-cells: show;
		}

		table.marker_with_label td {
			padding: 3px 0px 3px 5px;
			color:#666666;
			border-top: 1px #e0e0e0 solid;
			border-bottom: none;
		}

		table.marker_with_label td.img {
			width: 100px;
			text-align: right;
		}

		table.marker_with_label td.label {
			font-weight: bold;
			color:#444;
			width: 700px;
		}

		table.marker_with_label td img, td label {
			cursor: pointer;
		}

		.mrkr_selected {
			background-color: #F0E68C;
		}

		table.marker_without_label {
			width: 800px;
			border: 1px #e6e6e6 solid;
			empty-cells: show;
		}

		table.marker_without_label td {
			width: 80px;
			border-top: 1px #e0e0e0 solid;
			border-bottom: none;

		}

		table.marker_without_label td img {
			cursor: pointer;
		}

	</style>
	<insta:link type="script" file="outpatient/complaint.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:js-bundle prefix="patient.consultation"/>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
	<insta:js-bundle prefix="patient.diagnosis"/>
	<insta:js-bundle prefix="outpatient.vitalform"/>
</head>
<body onload="ajaxForPrintUrls();">
	<h1>IP Record</h1>
	<insta:feedback-panel/>
	<c:choose >
		<c:when test="${not empty param.patient_id}">
		<insta:patientdetails visitid="${param.patient_id}" showClinicalInfo="true"/>

		<form name="VisitSummaryRecord" action="VisitSummaryRecord.do" method="POST" autocomplete="off">
			<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

			<input type="hidden" name="_method" value="update"/>
			<input type="hidden" name="printVisitSummary" id="printVisitSummary" value="false"/>
			<input type="hidden" name="isSharedLogIn" value="${isSharedLogIn}"/>
			<input type="hidden" name="authUser" id="authUser" value=""/>
			<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}"/>
			<input type="hidden" name="mr_no" id="mr_no" value="${patient.mr_no}"/>
			<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
			<input type="hidden" name="patient_discharged" id="patient_discharged"
				value="${patient.visit_status == 'I' && patient.discharge_flag == 'D'}"/>
			<c:set var="mod_mrd_icd" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
			<input type="hidden" name="_method" value="update"/>

			<c:forTokens delims="," items="${form.map.sections}" var="section_id">
				<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
			</c:forTokens>

			<c:set var="allergies_order_id" value="<%=SystemGeneratedSections.Allergies.getSectionId()%>" />
			<c:set var="complaint_order_id" value="<%=SystemGeneratedSections.Complaint.getSectionId()%>" />
			<c:set var="diagnosis_details_order_id" value="<%=SystemGeneratedSections.DiagnosisDetails.getSectionId()%>" />
			<c:set var="pre_anaesthesthetic_order_id" value="<%=SystemGeneratedSections.PreAnaesthestheticCheckup.getSectionId()%>" />
			<c:set var="vital_order_id" value="<%=SystemGeneratedSections.Vitals.getSectionId()%>" />
			<c:set var="pregnancyhistory_order_id" value="<%=SystemGeneratedSections.PregnancyHistory.getSectionId()%>" />
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
										<div id="patient_history_label" class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i>Patient History</i></b></div>
										<div class="fltR txtRT" style="width: 25px; margin: 10px 10px 0px">
											<img src="${cpath}/images/down.png" />
										</div>
										<div class="clrboth"></div>
									</div>
								<fieldset class="fieldSetBorder" id="fieldset" style="background-color: #F8FCFF; border-style:none ">
							</c:if>
								<jsp:include page="/pages/outpatient/InstaSection.jsp">
									<jsp:param name="section_id" value="${section_id}"/>
									<jsp:param name="patient_id" value="${patient.patient_id}"/>
									<jsp:param name="form_name" value="VisitSummaryRecord"/>
									<jsp:param name="form_type" value="Form_IP"/>
									<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
									<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
									<jsp:param name="mr_no" value="${patient.mr_no}"/>
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

			<c:forTokens items="${form.map.sections}" delims="," var="section_id">
				<c:if test="${section_id == complaint_order_id}">
					<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
					<jsp:include page="/pages/outpatient/ComplaintInclude.jsp">
						<jsp:param name="form_name" value="VisitSummaryRecord"/>
						<jsp:param name="section_id" value="${section_id}"/>
					</jsp:include>
				</c:if>

				<c:if test="${section_id == pre_anaesthesthetic_order_id}">
					<input type="hidden" name="sys_gen_section_id" value="${section_id}" />
					<jsp:include flush="true" page="/pages/outpatient/PreAnaesthestheticCheckup.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
						<jsp:param name="form_name" value="VisitSummaryRecord"/>
						<jsp:param name="form_type" value="Form_IP"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
					</jsp:include>
				</c:if>
				
				<c:if test="${section_id == pregnancyhistory_order_id}">
						<input type="hidden" name="sys_gen_section_id"
							value="${section_id}" />
						<jsp:include flush="true"
							page="/pages/outpatient/PregnancyDetailsInclude.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
							<jsp:param name="form_name" value="VisitSummaryRecord"/>
							<jsp:param name="form_type" value="Form_IP"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
						</jsp:include>
					</c:if>

					<c:if test="${section_id == antenatal_order_id}">
						<input type="hidden" name="sys_gen_section_id"
							value="${section_id}" />
						<jsp:include flush="true"
							page="/pages/outpatient/AntenatalDetails.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
							<jsp:param name="form_name" value="VisitSummaryRecord"/>
							<jsp:param name="form_type" value="Form_IP"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
						</jsp:include>
					</c:if>

				<!-- this is for diagonosis details -->
				<c:if test="${section_id == diagnosis_details_order_id}">
					<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
					<input type="hidden" id="diagnosis_details_exists" value="true"/>
					<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp">
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="form_name" value="VisitSummaryRecord"/>
						<jsp:param name="displayPrvsDiagnosisBtn" value="true"/>
					</jsp:include>
				</c:if>
					<!-- this is for allergies -->
				<c:if test="${section_id == allergies_order_id}">
					<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
					<jsp:include page="/pages/outpatient/Allergies.jsp" >
						<jsp:param name="section_id" value="${section_id}"/>
						<jsp:param name="patient_id" value="${patient.patient_id}"/>
						<jsp:param name="form_name" value="VisitSummaryRecord"/>
						<jsp:param name="form_type" value="Form_IP"/>
						<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
						<jsp:param name="mr_no" value="${patient.mr_no}"/>
					</jsp:include>
				</c:if>

				<c:choose>
					<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
						<jsp:include page="/pages/outpatient/InstaSection.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
							<jsp:param name="form_name" value="VisitSummaryRecord"/>
							<jsp:param name="form_type" value="Form_IP"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
						</jsp:include>
					</c:when>
					<c:otherwise>
						<c:forEach items="${insta_sections}" var="patient_form_group">
							<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to != 'patient'}">
								<jsp:include page="/pages/outpatient/InstaSection.jsp">
									<jsp:param name="section_id" value="${section_id}"/>
									<jsp:param name="patient_id" value="${patient.patient_id}"/>
									<jsp:param name="form_name" value="VisitSummaryRecord"/>
									<jsp:param name="form_type" value="Form_IP"/>
									<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
									<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
									<jsp:param name="mr_no" value="${patient.mr_no}"/>
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
			<div class="screenActions" style="float: left">
				<button type="button" name="send" id="Save" accessKey="S" onclick="return chkFormDetailsEdited(false);" tabindex="315">
					<b><u>S</u></b>ave</button>
				<input type="button" name="saveAndPrint" value="Save & Print" onclick="chkFormDetailsEdited(true);"/>
				| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
				<insta:screenlink screenId="ip_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=ip_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}"/>
			</div>
		<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
		</form>
		<div id="loginDiv" style="display: none">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Login Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">User ID: </td>
							<td><input type="text" name="login_user" id="login_user"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel">Password: </td>
							<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
					</table>
					<table style="margin-top: 10px">
						<tr>
							<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
								<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		</c:when>
	</c:choose>

</body>