<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html" isELIgnored="false" %>
<%@taglib  tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
	<head><title><insta:ltext key="registration.readmit.details.readmit.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html ;charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="registration.patient"/>
	</head>

<body class="yui-skin-sam">
<h1 style="float: left"><insta:ltext key="registration.readmit.details.readmitpatient"/></h1>

<insta:patientsearch searchType="visit" fieldName="patient_id" searchUrl="readmit.do"
		searchMethod="getReadmitScreen" buttonLabel="Find" showStatusField="true"/>

<insta:feedback-panel />
<insta:patientdetails visitid="${selectedVisit.patient_id}" />
<form action="readmit.do" name="readmit" method="POST">
	<input type="hidden" name="_method" value="getReadmitScreen"/>

	<table align="left" class="formtable">
		<!--  List all active visits for the selected patient -->
		<c:if test="${not empty activePatientList}">
			<tr><td><insta:ltext key="registration.readmit.details.patienthasfollowingactivevisits"/>:</td></tr>
			<c:forEach var="patient" items="${activePatientList}">
				<tr><td>${patient.map.patient_id}</td></tr>
			</c:forEach>
		</c:if>

		<!--  If the selected patient is inactive and the latest inactive visit is not the same, show both visits to activate.
				or else show only the selcted patient to activate -->
		<c:if test="${selectedVisit.status == 'I'}">
			<c:choose>
				<c:when test="${selectedVisit.patient_id ne latestInactiveVisitId}">
					<tr>
						<td>
							<insta:ltext key="registration.readmit.details.selectedvisitisinactive"/>: <b>${selectedVisit.patient_id} </b> <insta:ltext key="registration.readmit.details.notlatestactivevisitid"/>?
							<a href="${cpath}/pages/registration/readmit.do?_method=patientReadmit&visitid=${selectedVisit.patient_id}">${selectedVisit.patient_id}</a>
						</td>
					</tr>
					<c:choose>
						<c:when test="${not empty latestInactiveVisitId}">
						<tr>
							<td>
								<b>${ifn:cleanHtml(latestInactiveVisitId)} </b> <insta:ltext key="registration.readmit.details.latestactivevisitid"/>?
								<a href="${cpath}/pages/registration/readmit.do?_method=patientReadmit&visitid=${ifn:cleanURL(latestInactiveVisitId)}">${ifn:cleanHtml(latestInactiveVisitId)}</a>
							</td>
						</tr>
						</c:when>
						<c:otherwise>
							<td>
								<insta:ltext key="registration.readmit.details.thereisnoinactivevisitid"/>.
							</td>
						</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<tr>
						<td>
							<insta:ltext key="registration.readmit.details.thisvisitisinactive"/>: <b>${selectedVisit.patient_id} </b> <insta:ltext key="registration.readmit.details.doyouwanttoactivatethisvisit"/>?
							<a href="${cpath}/pages/registration/readmit.do?_method=patientReadmit&visitid=${selectedVisit.patient_id}">${selectedVisit.patient_id}</a>
						</td>
					</tr>
				</c:otherwise>
			</c:choose>
		</c:if>
		<!-- If the selected patient is active, show the latest inactive visit to activate -->
		<c:if test="${selectedVisit.status == 'A'}">
			<tr><td>T<insta:ltext key="registration.readmit.details.thisvisitidisactive"/>. <b>${selectedVisit.patient_id}</td></tr>
			<c:choose>
				<c:when test="${not empty latestInactiveVisitId}">
				<tr>
					<td>
						<b>${ifn:cleanHtml(latestInactiveVisitId)} </b> <insta:ltext key="registration.readmit.details.latestactivevisitid"/>?
						<a href="${cpath}/pages/registration/readmit.do?_method=patientReadmit&visitid=${ifn:cleanURL(latestInactiveVisitId)}">${ifn:cleanHtml(latestInactiveVisitId)}</a>
					</td>
				</tr>
				</c:when>
				<c:otherwise>
					<td>
						<insta:ltext key="registration.readmit.details.thereisnoinactivevisitid"/>.
					</td>
				</c:otherwise>
			</c:choose>
		</c:if>
	</table>
</form>
</body>
</html>
