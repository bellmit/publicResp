<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
<title>Record Patient Response - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script>
function init() {
	document.getElementById('ps_visit').focus();
}

function submitForm(obj) {
	if(!empty(obj.value)) {
		document.getElementById('_method').value = "recordSurveyForm";
		document.patientResponseForm.submit();
	}
}

</script>
<style>

</style>
</head>
<body onload="init();">
	<h1 style="float: left">Record Patient Response</h1>
	<c:url var="searchUrl" value="RecordPatientResponse.do"/>
	<insta:patientsearch searchType="visit" fieldName="visit_id" showStatusField="true" buttonLabel="Find"
		searchMethod="getAllActiveSurveyForms" searchUrl="${searchUrl}" openNewWindow="${(fn:length(allActiveSurveyFormDetails)) gt 1 ? 'false' : 'true'}"/>
	<insta:feedback-panel/>
	<c:if test="${not empty param.visit_id}">
		<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>
	</c:if>
	<form name="patientResponseForm" action="RecordPatientResponse.do" method="get" target="_blank">
		<input type="hidden" name="_method" id="_method" value=""/>
		<input type="hidden" name="visit_id" id="visit_id" value="${param.visit_id}"/>
		<input type="hidden" name="formId" id="formId" value="${formDetails.map.form_id}"/>
		<c:if test="${not empty param.visit_id}">
				<c:choose>
					<c:when test="${fn:length(allActiveSurveyFormDetails) gt 0}">
						<table border="0" width="100%" class="formTable">
							<tr>
								<td class="formlabel">Choose A Form:</td>
								<td style="width:90%;">
									<insta:selectdb id="form_id" name="form_id" value="" table="survey_form"
										valuecol="form_id"  displaycol="form_name" class="dropdown"  filtercol="form_status" filtervalue="A"
										orderby="form_id" dummyvalue="-- Select --" onchange="submitForm(this)"/>
								</td>
							</tr>
						</table>
					</c:when>
					<c:otherwise>
						<insta:noresults hasResults="false" message="there are no active survey forms."/>
					</c:otherwise>
				</c:choose>
	  		</c:if>
	</form>
</body>
