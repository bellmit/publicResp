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
	<title>Surgery/Procedure Record - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="sockjs.min.js"/>
	<insta:link type="script" file="stomp.min.js"/>
	<insta:link type="script" file="jquery-2.2.4.min.js" />
	<insta:link type="script" file="OTServices/OtRecord/OtRecord.js"/>
	<insta:link type="js" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="outpatient/allergies.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<script>
		//var isSharedLogIn = '${isSharedLogIn}';
		//var roleId = '${roleId}';
		var doctor_dept='${doctor_dept}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		YAHOO.util.Event.onContentReady("content", init);
		var complaintForm = null;
		var insta_form_json = ${insta_form_json};
		var group_patient_sections = '${group_patient_sections}';
		var insta_sections_json = ${insta_sections_json};
		var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_by_dept_json};
		var sys_generated_forms = ${sys_generated_forms};
		var validate_diagnosis_codification = '${genericPrefs.validate_diagnosis_codification}';
		var diagnosis_code_type = '<%=request.getAttribute("defaultDiagnosisCodeType") %>';
		var collapsiblePanels = {};
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};
		var is_screen_locked = ${isScreenLocked};
		var loginHandle = '${loginHandle}';
		var flashMessage='';
		var screen_locked_by_user='${screenLockedByUser}';
	</script>
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
	<insta:js-bundle prefix="patient.consultation"/>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
</head>
<body onload="ajaxForPrintUrls();handleScreenLock('ot_record','${param.visit_id}', loginHandle); ">
	<h1 >Surgery/Procedure Record (${operation_bean.map.operation_name})</h1>
	<div id="flash" style="display:none; width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
		<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
			<img src="${cpath}/images/alert.png">
		</div>
		<div id="msg" style="float: left; margin-top: 10px">
		</div>
	</div>
	<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>

	<form id="OtRecord" name="OtRecord" action="OtRecord.do" method="POST" autocomplete="off">
		<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="printOtRecord" id="printOtRecord" value="false"/>
		<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
		<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}"/>
		<input type="hidden" name="mr_no" id="mr_no" value="${patient.mr_no}"/>
		<input type="hidden" name="patient_discharged" id="patient_discharged"
			value="${patient.visit_status == 'I' && patient.discharge_flag == 'D'}"/>
		<c:set var="mod_mrd_icd" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
		<input type="hidden" name="operation_proc_id" value="${param.operation_proc_id}"/>
		<input type="hidden" name="is_finalizeAll" id = "is_finalizeAll" value=""/>

		<c:forTokens delims="," items="${form.map.sections}" var="section_id">
			<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
		</c:forTokens>

	    <c:set var="allergies_order_id" value="<%=SystemGeneratedSections.Allergies.getSectionId()%>" />
		<c:set var="complaint_order_id" value="<%=SystemGeneratedSections.Complaint.getSectionId()%>" />
		<c:set var="diagnosis_details_order_id" value="<%=SystemGeneratedSections.DiagnosisDetails.getSectionId()%>" />

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
								<jsp:param name="operation_proc_id" value="${param.operation_proc_id}"/>
								<jsp:param name="form_type" value="Form_OT"/>
								<jsp:param name="form_name" value="OtRecord"/>
								<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
								<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
								<jsp:param name="patient_id" value="${patient.patient_id}"/>
								<jsp:param name="mr_no" value="${patient.mr_no}"/>
							</jsp:include>
					</c:if>
				</c:forEach>
			</c:if>
			<c:if test="${fieldsetField == 'true' && phs_i.last}" >
						</fieldset>
						</div>
						<script type="text/javascript">
							var mrNo = '${patient.mr_no}';
							collapsiblePanels[mrNo] = new Spry.Widget.CollapsiblePanel("CollapsiblePanel", {contentIsOpen:false});
						</script>
			</c:if>
		</c:forTokens>

		<c:forTokens items="${form.map.sections}" delims="," var="section_id" varStatus="detailsForm">
			<c:if test="${section_id == complaint_order_id}">
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<jsp:include page="/pages/outpatient/ComplaintInclude.jsp">
					<jsp:param name="form_name" value="OtRecord"/>
					<jsp:param name="section_id" value="${section_id}"/>
				</jsp:include>
			</c:if>
		 	<!-- this is for diagonosis details -->
		 	<c:if test="${section_id == diagnosis_details_order_id}">
		 		<br/>
				<input type="hidden" name="sys_gen_section_id" value="${section_id}"/>
				<input type="hidden" id="diagnosis_details_exists" value="true"/>
				<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="form_name" value="OtRecord"/>
					<jsp:param name="displayPrvsDiagnosisBtn" value="true"/>
				</jsp:include>
			</c:if>

			<c:choose>
					<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
						<jsp:include page="/pages/outpatient/InstaSection.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="operation_proc_id" value="${param.operation_proc_id}"/>
							<jsp:param name="form_type" value="Form_OT"/>
							<jsp:param name="form_name" value="OtRecord"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
						</jsp:include>
					</c:when>
					<c:otherwise>
						<c:forEach items="${insta_sections}" var="patient_form_group">
							<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to != 'patient'}">
								<jsp:include page="/pages/outpatient/InstaSection.jsp">
									<jsp:param name="section_id" value="${section_id}"/>
									<jsp:param name="operation_proc_id" value="${param.operation_proc_id}"/>
									<jsp:param name="form_type" value="Form_OT"/>
									<jsp:param name="form_name" value="OtRecord"/>
									<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
									<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
									<jsp:param name="patient_id" value="${patient.patient_id}"/>
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
				<td style="width:30px"><input  id="finalizeAll" type="checkbox" value="true" onClick="finalizeAllInstaSections();setIsFinalizeAll(event);"/></td>
				<td></td>
			</tr>
		</table>
		<div class="screenActions" style="float: left">
			<button type="button" name="send" id="Save" accessKey="S" onclick="return chkFormDetailsEdited(false);" tabindex="315">
				<b><u>S</u></b>ave</button>
	       	<input type="button" name="saveAndPrint" id="saveAndPrint" value="Save & Print" onclick="chkFormDetailsEdited(true);"/>
			<insta:screenlink screenId="get_ot_management_screen" extraParam="?_method=getOtManagementScreen&visit_id=${patient.patient_id}&prescription_id=${param.prescription_id}&operation_details_id=${operation_bean.map.operation_details_id}"
						label="Surgery/Procedure Management" addPipe="true"/>
			<insta:screenlink screenId="ot_record" extraParam="?_method=getOperationsList&visit_id=${patient.patient_id}&operation_details_id=${operation_bean.map.operation_details_id}"
												label="Surgery/Procedure Forms" addPipe="true" style="margin-left: 10px"/>
			<insta:screenlink screenId="ot_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"
				extraParam="?_method=getAuditLogDetails&al_table=ot_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.operation_proc_id}"/>

		</div>
		<div style="margin-top: 10px; float: right; ">
			<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
			<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
			<c:if test="${not empty printTemplate}">
				<c:forEach var="temp" items="${printTemplate}">
					<c:set var="templateValues" value="${templateValues},${temp.template_name}"/>
					<c:set var="templateTexts" value="${templateTexts},${temp.template_name}"/>
				</c:forEach>
			</c:if>
			<insta:selectoptions name="printTemplate" id="templateList" opvalues="${templateValues}"
												optexts="${templateTexts}" value="${templateName}"/>
			<insta:selectdb name="printerId" table="printer_definition" class="dropdown"
								valuecol="printer_id"  displaycol="printer_definition_name"
								value="${printerDef}"/>
		</div>
		<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
	</form>
	<script type="text/javascript">var mrNo = '${patient.mr_no}';</script>
	<script type="text/javascript">
		var patient_id = '${patient.patient_id}';
		var obtainLockUrl = cpath + '/multiuser/actionscreen/lock.json?screen_id=ot_record&patient_id='+patient_id;
		$("#OtRecord").on("change", function() {
			obtainScreenLock(obtainLockUrl)
		});
		if(is_screen_locked && screen_locked_by_user !== userid) {
			disableForm(true);
			var flashMessage = 'Screen currently is in use and locked by user:'+screen_locked_by_user;
		 	showFlashMessage(flashMessage);
		}
		$(window).on("unload", function() {
			if(screen_locked_by_user == userid) {
				removeLock('ot_record', patient_id);
			}
		});
	</script>
</body>
</html>
