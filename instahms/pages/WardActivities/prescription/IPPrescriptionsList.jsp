<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>

<head>
	<title>Doctor Order - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="wardactivities/prescription/ipprescriptions.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var doctors = ${doctors};
		var userRecord = ${userBean};
		var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
		var roleId = '${roleId}';
		var centerId = ${centerId};
		var consDoctorId = '';
		var consDoctorName = '';
		var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
	</script>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
	</style>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">
	<h1>Doctor Order</h1>
	<insta:feedback-panel/>
	<c:choose >
		<c:when test="${not empty param.visit_id}">
		<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>
		<form name="prescription" action="IPPrescriptions.do" method="POST">
			<input type="hidden" name="_method" value="update"/>
			<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
			<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}"/>
			<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
			<input type="hidden" name="authUser" id="authUser" value=""/>
			<input type="hidden" name="tpa_id" id="tpa_id" value="${patient.primary_sponsor_id}"/>
			<input type="hidden" name="patient_discharged" id="patient_discharged"
				value="${patient.visit_status == 'I' && patient.discharge_flag == 'D'}"/>
			<jsp:include page="IPPrescriptionsDetailsInclude.jsp" >
				<jsp:param name="prescriptions" value="${prescriptions}"/>
				<jsp:param name="form_name" value="prescription"/>
				<jsp:param name="screen_id" value="doctororder"/>
			</jsp:include>

			<table style="margin-top: 10px;float: left">
				<tr>
					<td>
						<input type="button" name="save" value="Save" onclick="return validateForm();" />
						| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
						<insta:screenlink screenId="activities_list" extraParam="?_method=list&patient_id=${patient.patient_id}"
									label="Patient Ward Activities" addPipe="true"/>
						<insta:screenlink screenId="medication_chart" extraParam="?_method=list&visit_id=${patient.patient_id}"
									label="Medication Chart" addPipe="true" />
						<insta:screenlink screenId="doctor_order_audit_log" label="Audit Log Search" addPipe="true" target="_blank"
							extraParam="?_method=getSearchScreen"/>
					</td>
				</tr>
			</table>
			<div style="float: right; margin-top: 10px">
				<insta:selectdb name="printerId" id="printerId" table="printer_definition" class="dropdown"
								valuecol="printer_id"  displaycol="printer_definition_name"/>
				<input type="button" name="print" value="Print" onclick="printDoctorOrder()"/>
			</div>
			<div style="clear: both"></div>
			<div class="legend">
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Discontinued Items</div>
			</div>
	</c:when>
	</c:choose>
</body>
</html>
