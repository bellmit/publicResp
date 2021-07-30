<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="hmsvalidation.js" />
</head>
<c:set var="signOff">
 <insta:ltext key="laboratory.signedoffreportslist.report.signedoff.reportslist"/>
</c:set>
<body>
	<c:set var="URL" value="SignedOffReportList.do"/>
	<c:if test="${ param.category eq 'DEP_RAD'}">
		<c:set var="URL" value="RadSignedOffReportList.do"/>
	</c:if>
	<h1><insta:ltext key="laboratory.signedoffreportslist.report.handoverreport"/></h1>
	<c:choose>
		<c:when test="${ patient != null }">
			<insta:patientdetails patient="${patient}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.signedoffreportslist.report.patientdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td><insta:ltext key="ui.label.patient.name"/>:</td>
					<td class="forminfo">${inpatient.patient_name}</td>
					<td><insta:ltext key="laboratory.signedoffreportslist.report.fromlab"/>:</td>
					<td class="forminfo">${inpatient.hospital_name}</td>
				</tr>
				<tr>
					<td><insta:ltext key="laboratory.signedoffreportslist.report.patientvisit"/>:</td>
					<td class="forminfo">${inpatient.incoming_visit_id}</td>
					<td><insta:ltext key="laboratory.signedoffreportslist.report.age.gender"/>:</td>
					<td class="forminfo">${inpatient.patient_age}${fn:toLowerCase(inpatient.age_unit)} / ${inpatient.patient_gender}</td>
				</tr>
			</table>
			</fieldset>
		</c:otherwise>
	</c:choose>
	<form action="<c:out value='${URL}'/>" method="POST" name="signedOffForm">
		<input type="hidden" name="_method" id="_method" value="handOverReport"/>
		<input type="hidden" name="reportId" id="reportId" value="${ifn:cleanHtmlAttribute(param.reportId)}"/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="laboratory.signedoffreportslist.report.reportdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.signedoffreportslist.report.reportname"/>:</td>
					<td class="forminfo">
						<label>${reportDetails.map.report_name }</label>
					</td>
					<td class="formlabel"><insta:ltext key="laboratory.signedoffreportslist.report.reportdate"/>:</td>
					<td class="forminfo">
						<fmt:formatDate value="${reportDetails.map.report_date }" pattern="dd-MM-yyyy HH:mm"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.signedoffreportslist.report.handoverto"/>:</td>
					<td>
						<input type="text" id="handed_over_to" name="handed_over_to"
								value="${reportDetails.map.handed_over_to }" class="required"/>
					</td>
					<td class="formlabel"><insta:ltext key="laboratory.signedoffreportslist.report.handoverdate"/>:</td>
					<c:set var="handoverDate">
						   <fmt:formatDate value="${reportDetails.map.handed_over == 'N' ? currentDate : reportDetails.map.hand_over_time}" pattern="dd-MM-yyyy"/>
				    </c:set>
					<c:set var="handoverTime">
					      <fmt:formatDate value="${reportDetails.map.handed_over == 'N' ? currentDate : reportDetails.map.hand_over_time }" pattern="HH:mm"/>
			        </c:set>
					<td>
						<insta:datewidget name="hand_over_time_dt" value="${handoverDate}"
								id="hand_over_time_dt" btnPos="right" />
						<input type="text" size="4" name="hand_over_time_tm"
								value="${handoverTime}" class="timefield"/>
					</td>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions" >
			<input type="submit" name="save" id="save" value="Save"/>
			<insta:screenlink screenId="${param.category == 'DEP_LAB' ? 'lab_signedoff_report_list' : 'rad_signedoff_report_list'}"
			addPipe="true" label="${signOff}" extraParam="?_method=getReportListScreen&handed_over=N"/>
		</div>
	</form>
</body>
</html>
