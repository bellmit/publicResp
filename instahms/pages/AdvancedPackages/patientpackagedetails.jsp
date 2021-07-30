<html>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<head>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:js-bundle prefix="registration.patient"/>
		<title >Patient Package Details - Insta HMS</title>
	</head>
	<body>
	<h1 style="float: left">Patient Package Details</h1>

	<div style="clear: both"></div>
	<c:set var="mr_no" value=""/>

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
		<form name="patientpackagedetailsform" method="POST" action="PatientPackages.do">
			<input type="hidden" name="_method" value="getPatientPackageDetails">
			<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
			<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id) }"/>

			<div class="resultList">
				<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
					<tr onmouseover="hideToolBar();">
						<th>Item</th>
						<th>Order Time</th>
					</tr>

					<c:forEach var="detail" items="${package_conduction_details}" varStatus="st">
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${detail.map.activity_status == 'X'}">red</c:when>
							<c:when test="${ (detail.map.activity_type == 'Laboratory' || detail.map.activity_type == 'Radiology') ?  (detail.map.results_entry_applicable ? detail.map.activity_status == 'S' : detail.map.activity_status == 'CRN') : detail.map.activity_status == 'C' }">green</c:when>
							<c:when test="${detail.map.test_report_handed_over == 'Y'}">yellow</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<tr>
						<td>
							<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
							${detail.map.activity_description }
						</td>
						<td><fmt:formatDate value="${detail.map.presc_date }" pattern="dd-MM-yyyy HH:mm"/></td>
					</tr>
					</c:forEach>
					</table>
			</div>

			<div class="legend" >
				<div class="flag"><img src="${cpath}/images/red_flag.gif"></div>
				<div class="flagText">Cancelled</div>
				<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
				<div class="flagText">Done</div>


			</div>
		</form>

		<table style="float: left" class="screenActions">
			<tr>
				<td>
					<insta:screenlink screenId="adv_pkg_patient_packages_list" extraParam="?_method=list&mr_no=${mrno}"
					label="Patient Packages"/>
				</td>
			</tr>
		</table>
	</body>
</html>