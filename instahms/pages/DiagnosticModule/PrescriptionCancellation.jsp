<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="laboratory.canceltests.list.title"/></title>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="diagnostics/prescriptionCancellation.js"/>

<insta:js-bundle prefix="laboratory.radiology"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="confirmCancel">
 <insta:ltext key="laboratory.canceltests.list.confirmcancel"/>
</c:set>
<h1><insta:ltext key="laboratory.canceltests.list.canceltests"/></h1>

<insta:feedback-panel/>
<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="laboratory.canceltests.list.cancelledby"/>:&nbsp;</td>
			<td class="forminfo">${ifn:cleanHtml(requestScope.userName)}
			   <input type="hidden" name="cancelledBy" id="cancelledBy"
			   	 value="${ifn:cleanHtmlAttribute(requestScope.userName)}" readonly="readonly"/>
			 </td>
			<td class="formlabel"><insta:ltext key="laboratory.canceltests.list.cancellationdate"/>:</td>
			<td><insta:datewidget name="toDate" valid="past" value="today" /></td>
			<td class="formlabel">&nbsp;</td>
			<td></td>
		</tr>
	</table>
	<c:choose>
		<c:when test="${not empty patientvisitdetails}">
			<insta:patientdetails  visitid="${patientvisitdetails.map.patient_id}"/>
			<c:set var="patVisitId" value="${patientvisitdetails.map.patient_id}"/>
		</c:when>
		<c:otherwise>
			<c:set var="patVisitId" value="${customer.map.incoming_visit_id}"/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="laboratory.canceltests.list.patientdetails"/></legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
						<td class="forminfo">${custmer.map.patient_name}</td>
						<td class="formlabel"><insta:ltext key="laboratory.canceltests.list.fromlab"/>:</td>
						<td class="forminfo">${custmer.map.hospital_name}</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="laboratory.canceltests.list.patientvisit"/>:</td>
						<td class="forminfo">${custmer.map.incoming_visit_id}</td>
						<td class="formlabel"><insta:ltext key="laboratory.canceltests.list.age.gender"/>:</td>
						<td class="forminfo">${custmer.map.patient_age}${fn:toLowerCase(custmer.map.age_unit)} / ${custmer.map.patient_gender}</td>
					</tr>
				</table>
			</fieldset>
		</c:otherwise>
	</c:choose>

<html:form action="${category == 'DEP_LAB' ? '/Laboratory' : '/Radiology'}/canceltest.do" onsubmit="return validate();">
<input type="hidden" name="_method" value="cancellPrescriptionDetails" />
<input type="hidden" name="user" value="${ifn:cleanHtmlAttribute(requestScope.userName)}"/>
<html:hidden property="presMrno"  value="${patientvisitdetails.map.mr_no }"/>
<html:hidden property="patVisitId" value="${not empty patientvisitdetails ? patientvisitdetails.map.patient_id :custmer.map.incoming_visit_id}"/>
<input type="hidden" name="cancelledDate" id="cancelledDate" value="" />


	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}">
	<table class="dataTable" id="testNamestable" width="100%" cellspacing="0" cellpadding="0" style="margin-top: 10px">
		<tr>
			<th><insta:ltext key="laboratory.canceltests.list.ord"/></th>
			<th><insta:ltext key="laboratory.canceltests.list.testname"/></th>
			<th><insta:ltext key="laboratory.canceltests.list.cancel"/></th>
			<th><insta:ltext key="laboratory.canceltests.list.remarks"/></th>
		</tr>
		<c:forEach var="record" items="${testDetails}" varStatus="loop">
			<tr class="${loop.first ? 'firstRow' : ''}">
				<td>${record.COMMON_ORDER_ID}</td>
				<td><insta:truncLabel value="${record.TEST_NAME}" length="32"/></td>
				<td><select name="cancelType" id='${loop.index}' class="dropdown">
						<c:choose>
						<c:when test="${record.RE_CONDUCTION eq 't' or record.COND_CENTER_RE_CONDUCTION eq 't'}">
							<option value="">${select}</option>
							<option value="N"><insta:ltext key="laboratory.canceltests.list.withoutrefund"/></option>
						</c:when>
						<c:otherwise>
							<option value="">${select}</option>
							<c:choose>
								<c:when test="${(roleId == 1 || roleId == 2 || actionRightsMap.cancel_elements_in_bill == 'A') && empty record.REPORT_ID && empty record.COND_CENTER_REPORT_ID}">
									<option value="Y"><insta:ltext key="laboratory.canceltests.list.withbillrefund"/></option>
								</c:when>
							</c:choose>
							<option value="N"><insta:ltext key="laboratory.canceltests.list.withoutrefund"/></option>
						</c:otherwise>
						</c:choose>
					</select>
				</td>
				<td><input type="text" name="remarks" maxlength="150" />
					<input type="hidden" name="presId" id="presId" value="${record.PRESCRIBED_ID }">
					<input type="hidden" name="testName" value="${record.TEST_NAME}"/>
					<input type="hidden" name="sflag" value="${record.SFLAG}"/>
					<input type="hidden" name="outsourceDestPrescId" value="${record.OUTSOURCE_DEST_PRESCRIBED_ID}" />
					<input type="hidden" name="reportId" value="${record.REPORT_ID}"/>
					<input type="hidden" name="testId" value="${record.TEST_ID}" />
					<input type="hidden" name="currentLocationPrescId" value="${record.CURR_LOCATION_PRESC_ID}" />
				</td>
				</td>
			</tr>
		</c:forEach>
	</table>

	<insta:noresults hasResults="${not empty testDetails}" message='<insta:ltext key="laboratory.canceltests.list.notestsavailable"/>'/>

	<div class="screenActions">
		<html:submit property="confirmTest" value="${confirmCancel}" ></html:submit>
		<c:choose>
			<c:when test="${category == 'DEP_LAB'}">
				<c:set var="url" value="Laboratory"/>
				<c:set var="reportlistlink" value="Laboratory Reports List"/>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="Radiology"/>
				<c:set var="reportlistlink" value="Radiology Reports List"/>
			</c:otherwise>
		</c:choose>
		| <a href="<c:out value="${cpath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${patientvisitdetails.map.mr_no}&patient_id=${not empty patientvisitdetails ? '' : ifn:cleanURL(param.visitid)}"/>">${reportlistlink}</a>
	</div>

</html:form>
</body>
</html>
