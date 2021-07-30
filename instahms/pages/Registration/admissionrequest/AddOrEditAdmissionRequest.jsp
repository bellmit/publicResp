<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib  tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap() %>"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<title><insta:ltext key="patient.addeditadmissionrequest.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="outpatient/diagnosis_details.js"/>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="script" file="registration/admissionrequest/addoreditadmissionrequest.js"/>
	<insta:link type="script" file="wardactivities/prescription/ipprescriptions.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var doctors = ${doctors};
		var userRecord = ${userBean};
		var centerId = ${centerId};
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var validate_diagnosis_codification = '${validate_diagnosis_codification}';
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var gMax_centers_inc_default = ${genericPrefs.max_centers_inc_default};
		var consDoctorId = '${param.doctor_id}';
	</script>
	<insta:js-bundle prefix="patient.addoreditadmissiondetails"/>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
</head>

<body class="yui-skin-sam" ${not empty param.mr_no ? 'onload="init()"' : ''}>
<h1 style="float: left"><insta:ltext key="patient.addeditadmissionrequest.header"/></h1>
<insta:patientsearch fieldName="mr_no" searchUrl="admissionrequest.do" searchMethod="addNewAdmissionRequest" searchType="mrNo" />
<insta:feedback-panel />
<c:if test="${not empty param.mr_no}">
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form action="admissionrequest.do" name="addnewadmissionrequest" method="POST" autocomplete="off">
	<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

	<div>
		<input type="hidden" name="mr_no" id="mr_no" value="${param.mr_no}"/>
		<input type="hidden" name="adm_request_id" id="adm_request_id" value="${admissionRequestDetails.adm_request_id}"/>
		<input type="hidden" name="org_id" id="org_id" value="ORG0001">
		<input type="hidden" name="tpa_id" id="tpa_id" value="">
		<input type="hidden" name="_method" value="saveAdmissionRequestDetails">
	</div>
	<div>
 		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.addeditadmissionrequest.admissionrequest.details.fieldset"/></legend>
	 		<table class="formtable">
				<tr style="height:30px;">
					<c:choose>
						<c:when test="${genericPrefs.max_centers_inc_default > 1}">
							<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.admissionrequest.label.center"/>:</td>
							<td>
								<c:choose>
									<c:when test="${centerId == 0}">
										<select class="dropdown" name="d_center_id" id="d_center_id" onchange="loadDoctors(this);">
											<option value="">-- Select --</option>
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}" ${admissionRequestDetails.center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
											</c:forEach>
										</select><span class="star">*</span>
									</c:when>
									<c:otherwise>
										<label id="d_centerName">${centerName}</label>
										<input type="hidden" name="d_center_id" id="d_center_id" value="${centerId}"/>
									</c:otherwise>
								</c:choose>
							</td>
							<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.requesting.doctor"/>:</td>
			 				<td class="forminput">
			 					<select name="requesting_doc" id="requesting_doc" class="dropdown">
			 						<option value="">-- Select --</option>
			 						<c:forEach items="${requestingDoctors}" var="doctor">
			 							<option value="${doctor.map.doctor_id}"
			 								${!empty param.doctor_id && param.doctor_id == doctor.map.doctor_id ? 'selected' :
			 									admissionRequestDetails.requesting_doc == doctor.map.doctor_id ? 'selected' : ''}>
			 									${doctor.map.doctor_name}
			 							</option>
			 						</c:forEach>
			 					</select><span class="star">*</span>
		 					</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.requesting.doctor"/>:</td>
			 				<td class="forminput">
			 					<select name="requesting_doc" id="requesting_doc" class="dropdown">
			 						<option value="">-- Select --</option>
			 						<c:forEach items="${requestingDoctors}" var="doctor">
			 							<option value="${doctor.map.doctor_id}"
			 								${!empty param.doctor_id && param.doctor_id == doctor.map.doctor_id ? 'selected' :
			 									admissionRequestDetails.requesting_doc == doctor.map.doctor_id ? 'selected' : ''}>
			 									${doctor.map.doctor_name}
			 							</option>
			 						</c:forEach>
			 					</select><span class="star">*</span>
		 					</td>
		 					<td>&nbsp;</td>
		 					<td>&nbsp;</td>
						</c:otherwise>
					</c:choose>
				</tr>
	 			<tr style="height:50px;">
	 				<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.requested.admission.date"/>:</td>
	 				<td class="forminput">
	 					<c:set var="admissionDateValue">
	 						<fmt:formatDate value="${admissionRequestDetails.admission_date}" pattern="dd-MM-yyyy"/>
	 					</c:set>
	 					<insta:datewidget name="admission_date" id="admission_date" value="${admissionDateValue}"/><span class="star">*</span>
	 				</td>
	 				<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.estimated.period.of.stay"/>:</td>
	 				<td class="forminput">
	 					<input type="text" name="duration_of_stay" id="duration_of_stay"
	 					value="${admissionRequestDetails.duration_of_stay}" style="width:40px;"
	 					onkeypress="return enterNumOnly(event)" maxlength="4">
	 					&nbsp; Days
	 				</td>
	 			</tr>
	 			<tr>
	 				<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.chief.complaint"/>:</td>
	 				<td class="forminput">
	 					<input type="text" name="chief_complaint" id="chief_complaint"
	 						value="${not empty chief_complaint ? chief_complaint : admissionRequestDetails.chief_complaint}" style="width:500px;">
	 				</td>
	 				<td>&nbsp;</td>
	 				<td>&nbsp;</td>
	 			</tr>
	 			<tr>
	 				<td class="formlabel"><insta:ltext key="patient.addeditadmissionrequest.label.other.comments"/>:</td>
	 				<td class="forminput" colspan="5">
						<textarea name="remarks" id="remarks" cols="80" rows="4"><c:out value="${admissionRequestDetails.remarks}"/></textarea>
	 				</td>
	 			</tr>
	 		</table>
	 	</fieldset>
 		<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp" >
				<jsp:param name="form_name" value="addnewadmissionrequest"/>
				<jsp:param name="displayPrvsDiagnosisBtn" value="false"/>
				<jsp:param name="searchType" value="mr_no"/>
		</jsp:include>
		<div style="height:10px;">&nbsp;</div>
		<jsp:include page="/pages/WardActivities/prescription/IPPrescriptionsDetailsInclude.jsp" >
			<jsp:param name="prescriptions" value="${prescriptions}"/>
			<jsp:param name="form_name" value="addnewadmissionrequest"/>
			<jsp:param name="screen_id" value="addnewadmissionrequest"/>
		</jsp:include>
	 	<div style="height:5px;">&nbsp;</div>
	 	<div style="margin-top: 10px">
			<button type="button" name="save" accesskey="S" onclick="return validateAdmissionRequest();" ${(admissionRequestDetails.status == 'X' || admissionRequestDetails.status == 'I') ? 'disabled' : ''}>
				<b><u><insta:ltext key="insta.common.button.first.letter.text"/></u></b><insta:ltext key="insta.common.button.without.first.letter.text"/>
			</button>
			<a href="${cpath}/pages/registration/admissionrequest.do?_method=getAdmissionRequestList">
				<insta:ltext key="patient.addeditadmissionrequest.admission.request.list.link"/>
			</a>
		</div>
	</div>
</form>
</c:if>
</body>
</html>