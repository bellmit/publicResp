<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="id" value="1" />
<html>
<head>
<c:set var="vitalText">
<insta:ltext key="registration.patient.vitalmeasurements.vital"/>
</c:set>
<c:set var="intakeoutputText">
<insta:ltext key="registration.patient.vitalmeasurements.intake.or.output"/>
</c:set>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title>${paramType eq 'V' ? vitalText:intakeoutputText} <insta:ltext key="registration.patient.vitalmeasurements.measurements.hms"/></title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
<insta:link type="js" file="shareLoginDialogCommon.js" />
<insta:link type="js" file="/triageform/triageform.js" />

<script type="text/javascript">

	function saveVitals(printVitals) {
		if (!validateVitals()) {
			return false;
		}
		document.getElementById('printVitals').value = printVitals;
		document.vitalForm.vifromDate.value = document.SearchForm.vifromDate.value;
		document.vitalForm.vitoDate.value = document.SearchForm.vitoDate.value;

		if (isSharedLogIn == 'Y') {
			loginDialog.show();
			document.getElementById("login_user").focus();
		}
		else {
			document.vitalForm.submit();
		}
		return true;
	}

	function submitHandler() {
		document.getElementById('authUser').value = document.getElementById('login_user').value;
		document.vitalForm.submit();
		return false;
	}

	function captureSubmitEvent() {
		var form = document.vitalForm;
		form.validateFormSubmit = form.submit;

		form.submit = function validatedSubmit() {
			if (!blockSubmit()) {
				var e = xGetElementById(document.vitalForm);
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			form.validateFormSubmit();
			return true;
		};
	}

	function blockSubmit() {
		if (document.getElementById('patient_discharged').value == 'true') {
			alert(getString("js.outpatient.vitalform.patientinactive.discharged")+'${paramType eq 'V' ? " Vitals " : " Intake/Output "}'+getString("js.outpatient.vitalform.notallowed"));
			return false;
		}
		return true;
	}

	function init() {
		captureSubmitEvent();
		initLoginDialog();
	}
	var visitId = '${ifn:cleanJavaScript(param.patient_id)}';
	var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
	var roleId = '${roleId}';
	var normalResult = '${prefColorCodes.map.normal_color_code}';
	var abnormalResult = '${prefColorCodes.map.abnormal_color_code}';
	var criticalResult = '${prefColorCodes.map.critical_color_code}';
	var improbableResult = '${prefColorCodes.map.improbable_color_code}';
	var paramType = '${paramType}';
	var comp_vitals = 'Y';
</script>
<insta:js-bundle prefix="outpatient.triageform"/>
<insta:js-bundle prefix="outpatient.vitalform"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="ajaxForPrintUrls(); init();">
	<h1>${paramType eq 'V' ? vitalText:intakeoutputText} <insta:ltext key="registration.patient.vitalmeasurements.measurements"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>
	<c:if test="${patient.visit_type == 'o'}">
		<c:set var="consultation_edit_across_doctors" value="${consultationEditAcrossDoctorsOp}"/>
	</c:if>

	<form name="SearchForm" method="GET">
		<input type="hidden" name="method" value="list"/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}" />
		<insta:search-lessoptions form="SearchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="registration.patient.vitalmeasurements.from"/>: </div>
						<div class="sboFieldInput">
							<insta:datewidget id="vifromDate" name="vifromDate" value="${param.vifromDate}"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="registration.patient.vitalmeasurements.to"/>: </div>
						<div class="sboFieldInput" style="height:50px">
							<insta:datewidget id="vitoDate" name="vitoDate" value="${param.vitoDate}"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
<c:choose>
	<c:when test="${paramType eq 'V'}">
		<form action="${cpath}/vitalForm/genericVitalForm.do?method=update"	method="POST" name="vitalForm" autocomplete="off">
	</c:when>
	<c:otherwise>
		<form action="${cpath}/IntakeOutput/genericIntakeOutputForm.do?method=update" method="POST" name="vitalForm" autocomplete="off">
	</c:otherwise>
</c:choose>
	<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
	<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}" />
	<input type="hidden" name="printVitals" id="printVitals" value="false"/>
	<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
	<input type="hidden" name="paramType" value="${paramType}"/>
	<input type="hidden" name="authUser" id="authUser" value=""/>
	<input type="hidden" name="patient_discharged" id="patient_discharged"
				value="${patient.visit_status == 'I' && (patient.discharge_flag == 'D' || patient.discharge_flag == '')}"/>
	<input type="hidden" name="visit_type" id="visit_type" value="${patient.visit_type}"/>
	<input type="hidden" name="vifromDate" value=""/>
	<input type="hidden" name="vitoDate" value=""/>

	<jsp:include page="VitalReadings.jsp"/>
	<table id="screenActions" style="margin-top: 10px">
		<tr>
			<td>
				<input type="button" name="save" value="Save" onclick="saveVitals(false);"/>
				<input type="button" name="saveAndPrint" value="Save & Print" onclick="saveVitals(true);"/>
				<c:choose>
					<c:when test="${paramType eq 'V'}">
						<c:if test="${patient.visit_type eq 'i'}">
							| <a href="${cpath}/pages/ipservices/IpservicesList.do?_method=getIPDashBoard" title='<insta:ltext key="registration.patient.vitalmeasurements.ipdashboard"/>'><insta:ltext key="registration.patient.vitalmeasurements.inpatientlist"/></a>
							| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
						</c:if>
						<c:set var="editNotAllowed" value="${roleId != 1 && roleId != 2 && (consultation_edit_across_doctors == 'N' && doctor_logged_in != consultation_bean.doctor_name)}"/>
						<c:if test = "${patient.visit_type eq 'o' && !editNotAllowed && not empty(param.consultation_id)}">
							| <a href="${cpath}/outpatient/OpPrescribeAction.do?_method=list&consultation_id=${ifn:cleanURL(param.consultation_id)}"
								title='<insta:ltext key="registration.patient.vitalmeasurements.consultationandmanagement"/>'><insta:ltext key="registration.patient.vitalmeasurements.consultationandmanagement"/></a>
						</c:if>
						<c:if test="${preferences.modulesActivatedMap.mod_trend == 'Y'}">
							| <a href="${cpath}/pages/Vitals/VitalTrendReport.do?method=getScreen&mrno=${patient.mr_no}" target="_blank"><insta:ltext key="registration.patient.vitalmeasurements.vitalstrendreport"/></a>
						</c:if>
						<insta:screenlink screenId="vital_form_audit_log" label="Vitals Audit Log" addPipe="true" target="_blank"
											  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&mr_no=${patient.mr_no}&visit_id=${patient.patient_id}"/>
					</c:when>
					<c:otherwise>
						<c:if test="${patient.visit_type eq 'i'}">
							| <a href="${cpath}/pages/ipservices/IpservicesList.do?_method=getIPDashBoard" title='<insta:ltext key="registration.patient.vitalmeasurements.ipdashboard"/>'><insta:ltext key="registration.patient.vitalmeasurements.inpatientlist"/></a>
							| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
						</c:if>
						<insta:screenlink screenId="intake_output_audit_log" label="Audit Log Search" addPipe="true" target="_blank"
							extraParam="?_method=getSearchScreen"/>
					</c:otherwise>
				</c:choose>
		</tr>
	</table>
	<div id="loginDiv" style="display: none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="registration.patient.vitalmeasurements.logindetails"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.vitalmeasurements.userid"/>: </td>
						<td><input type="text" name="login_user" id="login_user"/></td>
						<td class="formlabel">&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.vitalmeasurements.password"/>: </td>
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
</form>
</body>
</html>
