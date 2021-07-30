<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<head>
	<insta:link type="js" file="hmsvalidation.js" />
</head>
<body>
	<h1>Hand Over Package</h1>
	<c:choose>
		<c:when test="${not empty patient }">
			<c:set var="mrno" value="${patient.map.mr_no}"/>
			<insta:patientdetails  visitid="${patient.map.patient_id}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Patient Details</legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td>Patient Name:</td>
					<td class="forminfo">${incoming_patient.map.patient_name}</td>
					<td>From Lab:</td>
					<td class="forminfo">${incoming_patient.map.hospital_name}</td>
				</tr>
				<tr>
					<td>Patient Visit:</td>
					<td class="forminfo">${incoming_patient.map.incoming_visit_id}</td>
					<td>Age/Gender:</td>
					<td class="forminfo">${incoming_patient.map.patient_age}${fn:toLowerCase(incoming_patient.map.age_unit)} / ${incoming_patient.map.patient_gender}</td>
				</tr>
			</table>
			</fieldset>
		</c:otherwise>
	</c:choose>
	<form action="PatientPackages.do" method="POST" name="packagehandoverform">
		<input type="hidden" name="_method" id="_method" value="handOverPackage"/>
		<input type="hidden" name="reportId" id="reportId" value="${ifn:cleanHtmlAttribute(param.prescription_id) }"/>
		<c:set var="mrno" value=""/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Package Details</legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
				<tr>
					<td class="formlabel">Package Name:</td>
					<td class="forminfo">
						<label>${packageDetails.map.package_name }</label>
					</td>
					<td class="formlabel">Order Date:</td>
					<td class="forminfo">
						<fmt:formatDate value="${packageDetails.map.presc_date }" pattern="dd-MM-yyyy HH:mm"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Hand Over To:</td>
					<td>
						<input type="text" id="handover_to" name="handover_to"
								value="${packageDetails.map.handover_to }" class="required"/>
						<input type="hidden" name="handed_over" value="Y"/>
						<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id) }"/>
					</td>
					<td class="formlabel">Hand Over Date:</td>
					<c:set var="handoverDate">
						   <fmt:formatDate value="${packageDetails.map.handover_time}" pattern="dd-MM-yyyy"/>
				    </c:set>
					<c:set var="handoverTime">
					      <fmt:formatDate value="${currentDate}" pattern="HH:mm"/>
			        </c:set>
					<td>
						<insta:datewidget name="handover_time_dt" value="today"
								id="handover_time_dt" btnPos="right" />
						<input type="text" size="4" name="handover_time_tm"
								value="${handoverTime}" class="timefield"/>
						<input type="hidden" name="mr_no" value="${patient.mr_no}"/>
					</td>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions" >
			<input type="submit" name="save" id="save" value="Save"/>
			<insta:screenlink screenId="adv_pkg_patient_packages_list" extraParam="?_method=list&mr_no=${mrno}"
					label="Patient Packages"/>
		</div>
	</form>
</body>
</html>
