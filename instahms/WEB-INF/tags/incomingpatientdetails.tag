<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ attribute name="incomingVisitId" required="false" %>

<%

java.util.Map  patient =  com.bob.hms.diag.ohsampleregistration.IncomingPatientDAO.getPatientVisitDetails(incomingVisitId);
request.setAttribute("incoming_patient", patient);

%>

<fieldset class="fieldSetBorder hide-patient-details" style="margin-bottom: 5px;">
	<legend class="fieldSetLabel">Patient Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">

<tr>
	<td class="formlabel">Visit No:</td>
	<td class="forminfo"><div title="${incoming_patient.patient_id}">${incoming_patient.patient_id}</div></td>
	<td class="formlabel">Name:</td>
	<td class="forminfo"><div title="${incoming_patient.full_name}">${incoming_patient.full_name}</div></td>
	<td class="formlabel">Age/Gender:</td>
	<td class="forminfo">		
		<div title="${incoming_patient.age_text} / ${incoming_patient.gender}">
			${incoming_patient.age_text}${fn:toLowerCase(incoming_patient.age_unit)} / ${incoming_patient.gender}
		</div>
	</td>
</tr>
<tr>
	<td class="formlabel">Referral: </td>
	<td class="forminfo"><div title="${incoming_patient.doctor_name}">${incoming_patient.doctor_name}</div></td>
</tr>

</table>
</fieldset>
